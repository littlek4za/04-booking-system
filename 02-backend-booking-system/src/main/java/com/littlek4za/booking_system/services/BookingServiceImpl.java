package com.littlek4za.booking_system.services;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.littlek4za.booking_system.dtos.BookingRequestDto;
import com.littlek4za.booking_system.dtos.BookingResponseDto;
import com.littlek4za.booking_system.entities.Booking;
import com.littlek4za.booking_system.entities.Event;
import com.littlek4za.booking_system.entities.Invitation;
import com.littlek4za.booking_system.entities.InvitationUsage;
import com.littlek4za.booking_system.entities.Slot;
import com.littlek4za.booking_system.entities.User;
import com.littlek4za.booking_system.exception.AppException;
import com.littlek4za.booking_system.models.DeletedBy;
import com.littlek4za.booking_system.models.EventType;
import com.littlek4za.booking_system.models.ValidationResult;
import com.littlek4za.booking_system.repos.BookingRepository;
import com.littlek4za.booking_system.repos.EventRepository;
import com.littlek4za.booking_system.repos.InvitationRepository;
import com.littlek4za.booking_system.repos.InvitationUsageRepository;
import com.littlek4za.booking_system.repos.SlotRepository;
import com.littlek4za.booking_system.repos.UserRepository;
import com.littlek4za.booking_system.security.SecurityUtil;
import com.littlek4za.booking_system.utils.DtoMapper;
import com.littlek4za.booking_system.validators.BookingRequestValidator;
import com.littlek4za.booking_system.validators.InvitationValidator;

import jakarta.transaction.Transactional;

@Service
public class BookingServiceImpl implements BookingService {

    private final SecurityUtil securityUtil;
    private final UserRepository userRepository;
    private final SlotRepository slotRepository;
    private final InvitationRepository invitationRepository;
    private final BookingRepository bookingRepository;
    private final EventRepository eventRepository;
    private final InvitationUsageRepository invitationUsageRepository;
    private final DtoMapper dtoMapper;
    private final BookingRequestValidator bookingRequestValidator;
    private final InvitationValidator invitationValidator;

    public BookingServiceImpl(SecurityUtil securityUtil, UserRepository userRepository, SlotRepository slotRepository,
            InvitationRepository invitationRepository, BookingRepository bookingRepository,
            EventRepository eventRepository,
            InvitationUsageRepository invitationUsageRepository, DtoMapper dtoMapper,
            BookingRequestValidator bookingRequestValidator, InvitationValidator invitationValidator) {
        this.securityUtil = securityUtil;
        this.userRepository = userRepository;
        this.slotRepository = slotRepository;
        this.invitationRepository = invitationRepository;
        this.bookingRepository = bookingRepository;
        this.eventRepository = eventRepository;
        this.invitationUsageRepository = invitationUsageRepository;
        this.dtoMapper = dtoMapper;
        this.bookingRequestValidator = bookingRequestValidator;
        this.invitationValidator = invitationValidator;
    }

    @Transactional
    @Override
    public BookingResponseDto createBooking(BookingRequestDto dto, Long slotId) {

        Slot slot = slotRepository.findByIdWithEventForUpdate(slotId)
                .orElseThrow(() -> new AppException("Unknow Slot Id", HttpStatus.NOT_FOUND));
        Event event = slot.getEvent();
        Invitation invitation = invitationRepository.findByIdWithEventAndSlotSetsAndUsersForUpdate(dto.invitationId())
                .orElseThrow(() -> new AppException("Unkncalculatorown Invitation Id", HttpStatus.NOT_FOUND));
        User user = getOrCreateUser(dto);

        bookingRequestValidator.validateSlotBelongsToInvitation(slot, invitation);
        bookingRequestValidator.validateGuestOrUserFields(dto);

        ValidationResult validationResult = invitationValidator.validateAccess(invitation, user.getId());
        if (!validationResult.isValid()) {
            throw new AppException(validationResult.getMessage(), HttpStatus.BAD_REQUEST);
        }

        bookingRequestValidator.validateBookingInfo(dto, invitation, slot, event, user);

        BookingResponseDto bookingResponseDto = saveBookingByEventType(dto, invitation, slot, user);

        return bookingResponseDto;
    }

    private User getOrCreateUser(BookingRequestDto dto) {
        if (securityUtil.isAuthenticated()) {
            return userRepository.findById(this.securityUtil.getCurrentAuthUserId())
                    .orElseThrow(() -> new AppException("Unknown User", HttpStatus.NOT_FOUND));
        }
        Optional<User> existingUser = userRepository.findByEmail(dto.email());

        if (existingUser.isEmpty()) {
            return userRepository.save(User.createGuest(dto.email()));

        }

        User user = existingUser.get();

        if (!user.getGuest()) {
            throw new AppException("Email already registered. Please log in to continue.",
                    HttpStatus.BAD_REQUEST);
        }

        return user;
    }

    // create booking by Event Type sub method
    private BookingResponseDto saveBookingByEventType(BookingRequestDto dto, Invitation invitation, Slot slot,
            User user) {

        Instant requestedStartTime;
        Instant requestedEndTime;

        if (EventType.FIXED.equals(invitation.getEvent().getEventType())) {
            requestedStartTime = slot.getSlotStartTime();
            requestedEndTime = slot.getSlotEndTime();

        } else if (EventType.FLEXIBLE.equals(invitation.getEvent().getEventType())
                || EventType.BUSINESS.equals(invitation.getEvent().getEventType())) {

            requestedStartTime = Instant.parse(dto.bookedStartTime());
            requestedEndTime = requestedStartTime.plus(Duration.ofMinutes(slot.getSlotIntervalMinutes()));

        } else {
            throw new AppException("EventType not found", HttpStatus.BAD_REQUEST);
        }

        Optional<InvitationUsage> existingInvitationUsage = invitationUsageRepository
                .findByUserIdAndInvitationId(user.getId(), invitation.getId());

        InvitationUsage invitationUsage;

        if (existingInvitationUsage.isPresent()) {
            invitationUsage = existingInvitationUsage.get();
        } else {
            invitationUsage = new InvitationUsage(invitation, user);
            try { // for case that same user doing two instance at same time, 
            // and two instance reaches this point at the same time, 
            // saving invitationUsage will failed for one of them 
            // because the faster instance already save it
            // and this slower instance under go catch will try to get the invitationusage one more time, if cant, then throw the error
                invitationUsageRepository.save(invitationUsage);
            } catch(DataIntegrityViolationException e) {
                invitationUsage = invitationUsageRepository.findByUserIdAndInvitationId(user.getId(), invitation.getId())
                .orElseThrow();
            }
            
        }

        Integer maxUsagePerIdentity = invitation.getMaxUsagePerIdentity();
        invitationUsage.incrementUsage(maxUsagePerIdentity);
    
        Booking newBooking = new Booking(user, slot, requestedStartTime, requestedEndTime,
                slot.getEvent().getEventName(), slot.getSlotName(), invitation.getUser().getEmail(), user.getEmail(),invitation.getEvent().getEventLocationAddress());
        if (user.getGuest()) {
            newBooking.setGuestFirstName(dto.firstName());
            newBooking.setGuestLastName(dto.lastName());
        }

        if(invitation.getEvent().getLatitude() != null && invitation.getEvent().getLongitude() != null ){
            newBooking.setLatitude(invitation.getEvent().getLatitude());
            newBooking.setLongitude(invitation.getEvent().getLongitude());
        }

        newBooking.setBookingToken(generateUniqueBookingToken());
        invitation.addBooking(newBooking);
        invitation.incrementUsedCount();

        Booking savedBooking = bookingRepository.save(newBooking);
        invitationRepository.save(invitation);
        invitationUsageRepository.save(invitationUsage);

        return dtoMapper.toBookingResponseDto(savedBooking);

    }

    // generate token
    private String generateUniqueBookingToken() {
        String token;
        do {
            token = generateBookingToken(6);
        } while (bookingRepository.existsByBookingToken(token));
        return token;
    }

    private String generateBookingToken(int length) {
        String CHAR_POOL = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder token = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            token.append(CHAR_POOL.charAt(random.nextInt(CHAR_POOL.length())));
        }
        return token.toString();
    }

    @Override
    public List<BookingResponseDto> getBookingsBySlotId(Long slotId) {

        Slot slot = slotRepository.findByIdWithEvent(slotId)
                .orElseThrow(() -> new AppException("Unknow Slot Id", HttpStatus.NOT_FOUND));

        List<Booking> bookingList = bookingRepository.findBySlot(slot);

        List<BookingResponseDto> bookingResponseDtoList = bookingList.stream()
                .map(booking -> dtoMapper.toBookingResponseDto(booking)).toList();

        return bookingResponseDtoList;
    }

    @Override
    public Integer getCountBySlotId(Long slotId) {

        Slot slot = slotRepository.findByIdWithEvent(slotId)
                .orElseThrow(() -> new AppException("Unknow Slot Id", HttpStatus.NOT_FOUND));

        return bookingRepository.getBookingsCountBySlot(slot);
    }

    @Override
    public List<BookingResponseDto> getBookingsByEventId(Long eventId) {

        User user = userRepository.findById(this.securityUtil.getCurrentAuthUserId())
                .orElseThrow(() -> new AppException("Unknown User", HttpStatus.NOT_FOUND));
        Event event = eventRepository.findByIdAndUser(eventId, user)
                .orElseThrow(() -> new AppException("No event found with this Id and User",
                        HttpStatus.NOT_FOUND));

        List<Booking> bookingList = bookingRepository.findActiveBookingsByEventId(event);

        List<BookingResponseDto> bookingResponseDtoList = bookingList.stream()
                .map(booking -> dtoMapper.toBookingResponseDto(booking)).toList();

        return bookingResponseDtoList;
    }

    @Override
    @Transactional
    public BookingResponseDto softDeleteBooking(Long slotId, Long bookingId) {

        userRepository.findById(this.securityUtil.getCurrentAuthUserId())
                .orElseThrow(() -> new AppException("Unknown User", HttpStatus.NOT_FOUND));

        Booking booking = bookingRepository
                .findByIdAndSlotIdAndUserId(bookingId, slotId, this.securityUtil.getCurrentAuthUserId())
                .orElseThrow(() -> new AppException("Booking not found or no access", HttpStatus.NOT_FOUND));

        booking.setDeletedBy(DeletedBy.ORGANIZER);
        booking.setDeletedAt(Instant.now());
        booking.setDeleted(true);

        bookingRepository.save(booking);

        return dtoMapper.toBookingResponseDto(booking);
    }

}
