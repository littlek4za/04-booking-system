package com.littlek4za.booking_system.services;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.littlek4za.booking_system.dtos.BookingRequestDto;
import com.littlek4za.booking_system.dtos.BookingResponseDto;
import com.littlek4za.booking_system.dtos.AttendeeBookingResponseDto;
import com.littlek4za.booking_system.entities.Booking;
import com.littlek4za.booking_system.entities.Event;
import com.littlek4za.booking_system.entities.Invitation;
import com.littlek4za.booking_system.entities.InvitationUsage;
import com.littlek4za.booking_system.entities.Slot;
import com.littlek4za.booking_system.entities.User;
import com.littlek4za.booking_system.exception.AppException;
import com.littlek4za.booking_system.exception.model.ErrorCode;
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
    private final EmailService emailService;
    private final RiskService riskService;

    public BookingServiceImpl(SecurityUtil securityUtil, UserRepository userRepository, SlotRepository slotRepository,
            InvitationRepository invitationRepository, BookingRepository bookingRepository,
            EventRepository eventRepository,
            InvitationUsageRepository invitationUsageRepository, DtoMapper dtoMapper,
            BookingRequestValidator bookingRequestValidator, InvitationValidator invitationValidator,
            EmailService emailService, RiskService riskService) {
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
        this.emailService = emailService;
        this.riskService = riskService;
    }

    @PreAuthorize("@authz.isGuestBookingCreate() or (@authz.isUser() and hasAnyAuthority('ROLE_ADMIN','ROLE_ATTENDEE'))")
    @Transactional
    @Override
    public BookingResponseDto createBooking(BookingRequestDto dto, Long slotId, String clientIp) {

        boolean isGuest = securityUtil.isGuest();

        Slot slot = slotRepository.findByIdWithEventForUpdate(slotId)
                .orElseThrow(() -> {
                    if (isGuest) {
                        riskService.recordAttemptForCreate(dto.email(), clientIp);
                    }
                    return new AppException("Unknown Slot Id", HttpStatus.NOT_FOUND, ErrorCode.SLOT_NOT_FOUND);
                });

        Event event = slot.getEvent();

        Invitation invitation = invitationRepository.findByIdWithEventAndSlotSetsAndUsersForUpdate(dto.invitationId())
                .orElseThrow(() -> {
                    if (isGuest) {
                        riskService.recordAttemptForCreate(dto.email(), clientIp);
                    }
                    return new AppException("Unknown Invitation Id", HttpStatus.NOT_FOUND,
                            ErrorCode.INVITATION_NOT_FOUND);
                });

        User user = getOrCreateUser(dto);


        try {
            bookingRequestValidator.validateSlotBelongsToInvitation(slot, invitation);
        } catch (AppException e) {

            if (isGuest && e.getErrorCode() == ErrorCode.SLOT_INVITATION_MISMATCH) {
                riskService.recordAttemptForCreate(dto.email(), clientIp);
            }

            throw e;
        }

        bookingRequestValidator.validateGuestOrUserFields(dto);

        ValidationResult validationResult = invitationValidator.validateAccess(invitation, user.getId());
        if (!validationResult.isValid()) {
            if (isGuest) {
                riskService.recordAttemptForCreate(dto.email(), clientIp);
            }
            throw new AppException(validationResult.getMessage(), HttpStatus.BAD_REQUEST,
                    validationResult.getErrorCode());
        }

        bookingRequestValidator.validateBookingInfo(dto, invitation, slot, event, user);

        BookingResponseDto bookingResponseDto = saveBookingByEventType(dto, invitation, slot, user);

        emailService.sendBookingConfirmationDetails(bookingResponseDto);

        if (isGuest) {
            riskService.resetEmailIpForCreate(dto.email(), clientIp);
            riskService.reduceIpPenaltyForCreate(clientIp);
            riskService.recordCreateSuccess(clientIp);
        }

        return bookingResponseDto;
    }

    private User getOrCreateUser(BookingRequestDto dto) {
        Long userId = securityUtil.getUserIdOrNull();
        if (userId != null) {
            return userRepository.findById(userId)
                    .orElseThrow(
                            () -> new AppException("User not found", HttpStatus.NOT_FOUND, ErrorCode.USER_NOT_FOUND));
        }
        Optional<User> existingUser = userRepository.findByEmail(dto.email());

        if (existingUser.isEmpty()) {
            return userRepository.save(User.createGuest(dto.email()));

        }

        User user = existingUser.get();

        if (!user.getGuest()) {
            throw new AppException("Email already registered.",
                    HttpStatus.BAD_REQUEST, ErrorCode.EMAIL_ALREADY_REGISTERED);
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
            throw new AppException("Event type invalid", HttpStatus.NOT_FOUND, ErrorCode.EVENT_TYPE_INVALID);
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
                  // and this slower instance under go catch will try to get the invitationusage
                  // one more time, if cant, then throw the error
                invitationUsageRepository.save(invitationUsage);
            } catch (DataIntegrityViolationException e) {
                invitationUsage = invitationUsageRepository
                        .findByUserIdAndInvitationId(user.getId(), invitation.getId())
                        .orElseThrow();
            }

        }

        Integer maxUsagePerIdentity = invitation.getMaxUsagePerIdentity();
        invitationUsage.incrementUsage(maxUsagePerIdentity);

        Booking newBooking = new Booking(user, slot, requestedStartTime, requestedEndTime,
                slot.getEvent().getEventName(), slot.getSlotName(), invitation.getUser().getEmail(), user.getEmail(),
                invitation.getEvent().getEventLocationAddress());
        if (user.getGuest()) {
            newBooking.setGuestFirstName(dto.firstName());
            newBooking.setGuestLastName(dto.lastName());
        }

        if (invitation.getEvent().getLatitude() != null && invitation.getEvent().getLongitude() != null) {
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
                .orElseThrow(() -> new AppException("Slot not found", HttpStatus.NOT_FOUND, ErrorCode.SLOT_NOT_FOUND));

        List<Booking> bookingList = bookingRepository.findBySlot(slot);

        List<BookingResponseDto> bookingResponseDtoList = bookingList.stream()
                .map(booking -> dtoMapper.toBookingResponseDto(booking)).toList();

        return bookingResponseDtoList;
    }

    @Override
    public Integer getCountBySlotId(Long slotId) {

        Slot slot = slotRepository.findByIdWithEvent(slotId)
                .orElseThrow(() -> new AppException("Slot not found", HttpStatus.NOT_FOUND, ErrorCode.SLOT_NOT_FOUND));

        return bookingRepository.getBookingsCountBySlot(slot);
    }

    @PreAuthorize("@authz.isUser()")
    @Override
    public List<BookingResponseDto> getBookingsByEventId(Long eventId) {

        User user = userRepository.findById(this.securityUtil.requireUserId())
                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND, ErrorCode.USER_NOT_FOUND));
        Event event = eventRepository.findByIdAndUser(eventId, user)
                .orElseThrow(() -> new AppException("Event not found with eventId and user",
                        HttpStatus.NOT_FOUND, ErrorCode.EVENT_NOT_FOUND));

        List<Booking> bookingList = bookingRepository.findActiveBookingsByEventId(event);

        List<BookingResponseDto> bookingResponseDtoList = bookingList.stream()
                .map(booking -> dtoMapper.toBookingResponseDto(booking)).toList();

        return bookingResponseDtoList;
    }

    @PreAuthorize("@authz.isUser()")
    @Override
    @Transactional
    public BookingResponseDto softDeleteBookingAsOrganizer(Long slotId, Long bookingId) {

        userRepository.findById(this.securityUtil.requireUserId())
                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND, ErrorCode.USER_NOT_FOUND));

        Booking booking = bookingRepository
                .findOrganizerBookingByIdAndSlotIdAndUserId(bookingId, slotId, this.securityUtil.requireUserId())
                .orElseThrow(() -> new AppException("Booking not found with bookingId, slotId and user",
                        HttpStatus.NOT_FOUND, ErrorCode.BOOKING_NOT_FOUND));

        booking.setDeletedBy(DeletedBy.ORGANIZER);
        booking.setDeletedAt(Instant.now());
        booking.setDeleted(true);

        bookingRepository.save(booking);

        return dtoMapper.toBookingResponseDto(booking);
    }

    @PreAuthorize("@authz.isUser()")
    @Override
    @Transactional
    public BookingResponseDto softDeleteBookingAsUserAttendee(Long bookingId) {

        Booking booking = bookingRepository
                .findAttendeeBookingByIdAndUserId(bookingId, this.securityUtil.requireUserId())
                .orElseThrow(() -> new AppException("Booking not found with bookingId and user",
                        HttpStatus.NOT_FOUND, ErrorCode.BOOKING_NOT_FOUND));

        booking.setDeletedBy(DeletedBy.ATTENDEE);
        booking.setDeletedAt(Instant.now());
        booking.setDeleted(true);

        bookingRepository.save(booking);

        return dtoMapper.toBookingResponseDto(booking);
    }

    @PreAuthorize("@authz.isGuestBookingView()")
    @Override
    @Transactional
    public BookingResponseDto softDeleteBookingAsGuestAttendee(Long bookingId) {
        throw new AppException("METHOD NOT IMPLMENTED", null, null);

    }

    @PreAuthorize("@authz.isUser()")
    @Override
    public AttendeeBookingResponseDto getBookingByTokenAsUserAttendee(String bookingToken) {

        Long userId = securityUtil.requireUserId();
        Booking booking = bookingRepository.findByBookingTokenAndUserId(bookingToken, userId)
                .orElseThrow(() -> new AppException("Booking not found with booking token and user",
                        HttpStatus.NOT_FOUND, ErrorCode.BOOKING_NOT_FOUND));

        AttendeeBookingResponseDto bookingResponseDto = dtoMapper.toAttendeeBookingResponseDto(booking);

        return bookingResponseDto;
    }

    @PreAuthorize("@authz.isGuestBookingView()")
    @Override
    public AttendeeBookingResponseDto getBookingByTokenAsGuestAttendee(String bookingToken) {

        String email = securityUtil.requireEmail();
        Booking booking = bookingRepository.findbyBookingTokenAndEmail(bookingToken, email)
                .orElseThrow(() -> new AppException("Booking not found with booking token and email",
                        HttpStatus.NOT_FOUND, ErrorCode.BOOKING_NOT_FOUND));

        AttendeeBookingResponseDto bookingResponseDto = dtoMapper.toAttendeeBookingResponseDto(booking);

        return bookingResponseDto;
    }

    @PreAuthorize("@authz.isUser()")
    @Override
    public List<AttendeeBookingResponseDto> getUserBookings() {
        Long userId = securityUtil.requireUserId();
        List<Booking> bookings = bookingRepository.findByUserId(userId);

        List<AttendeeBookingResponseDto> bookingUserResponseDtos = bookings.stream()
                .map(booking -> {
                    return dtoMapper.toAttendeeBookingResponseDto(booking);
                }).toList();

        return bookingUserResponseDtos;

    }

}
