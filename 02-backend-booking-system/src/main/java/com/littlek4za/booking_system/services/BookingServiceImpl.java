package com.littlek4za.booking_system.services;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

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
import com.littlek4za.booking_system.models.EventType;
import com.littlek4za.booking_system.repos.BookingRepository;
import com.littlek4za.booking_system.repos.EventRepository;
import com.littlek4za.booking_system.repos.InvitationRepository;
import com.littlek4za.booking_system.repos.InvitationUsageRepository;
import com.littlek4za.booking_system.repos.SlotRepository;
import com.littlek4za.booking_system.repos.UserRepository;
import com.littlek4za.booking_system.security.SecurityUtil;
import com.littlek4za.booking_system.utils.DtoMapper;
import com.littlek4za.booking_system.validators.BookingRequestValidator;

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

    public BookingServiceImpl(SecurityUtil securityUtil, UserRepository userRepository, SlotRepository slotRepository,
            InvitationRepository invitationRepository, BookingRepository bookingRepository,
            EventRepository eventRepository,
            InvitationUsageRepository invitationUsageRepository, DtoMapper dtoMapper,
            BookingRequestValidator bookingRequestValidator) {
        this.securityUtil = securityUtil;
        this.userRepository = userRepository;
        this.slotRepository = slotRepository;
        this.invitationRepository = invitationRepository;
        this.bookingRepository = bookingRepository;
        this.eventRepository = eventRepository;
        this.invitationUsageRepository = invitationUsageRepository;
        this.dtoMapper = dtoMapper;
        this.bookingRequestValidator = bookingRequestValidator;
    }

    @Transactional
    @Override
    public BookingResponseDto createBooking(BookingRequestDto dto, Long slotId) {

        Slot slot = slotRepository.findByIdWithEvent(slotId)
                .orElseThrow(() -> new AppException("Unknow Slot Id", HttpStatus.NOT_FOUND));
        Invitation invitation = invitationRepository.findByIdWithEventAndSlotSets(dto.invitationId())
                .orElseThrow(() -> new AppException("Unknown Invitation Id", HttpStatus.NOT_FOUND));
        User user = getOrCreateUser(dto);

        bookingRequestValidator.validateSlotBelongsToInvitation(slot, invitation);
        bookingRequestValidator.validateGuestOrUserFields(dto);
        bookingRequestValidator.validateBookingInfo(dto, invitation, slot);

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
            invitationUsageRepository.save(invitationUsage);
        }

        if (securityUtil.isAuthenticated()) {
            Integer maxUsagePerUser = invitation.getMaxUsagePerUser();
            invitationUsage.incrementUsage(maxUsagePerUser);
        }

        Booking newBooking = new Booking(user, slot, requestedStartTime, requestedEndTime);
        if (user.getGuest()) {
            newBooking.setGuestFirstName(dto.firstName());
            newBooking.setGuestLastName(dto.lastName());
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

}
