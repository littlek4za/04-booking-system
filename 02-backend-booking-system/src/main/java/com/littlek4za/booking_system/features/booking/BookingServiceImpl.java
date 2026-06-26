package com.littlek4za.booking_system.features.booking;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.littlek4za.booking_system.common.service.RedisRiskService;
import com.littlek4za.booking_system.common.service.event.BookingMailEvent;
import com.littlek4za.booking_system.common.utils.DtoMapper;
import com.littlek4za.booking_system.exception.AppException;
import com.littlek4za.booking_system.exception.model.ErrorCode;
import com.littlek4za.booking_system.features.auth.entity.User;
import com.littlek4za.booking_system.features.auth.repo.UserRepository;
import com.littlek4za.booking_system.features.booking.dto.AttendeeBookingResponseDto;
import com.littlek4za.booking_system.features.booking.dto.BookingRequestDto;
import com.littlek4za.booking_system.features.booking.dto.OrganizerBookingResponseDto;
import com.littlek4za.booking_system.features.booking.dto.SlotBookedTimeResponseDto;
import com.littlek4za.booking_system.features.booking.model.BookingStatus;
import com.littlek4za.booking_system.features.booking.model.DeletedBy;
import com.littlek4za.booking_system.features.booking.validator.BookingValidator;
import com.littlek4za.booking_system.features.event.Event;
import com.littlek4za.booking_system.features.event.EventRepository;
import com.littlek4za.booking_system.features.event.model.EventType;
import com.littlek4za.booking_system.features.invitation.InvitationRepository;
import com.littlek4za.booking_system.features.invitation.InvitationUsageRepository;
import com.littlek4za.booking_system.features.invitation.enitity.Invitation;
import com.littlek4za.booking_system.features.invitation.enitity.InvitationUsage;
import com.littlek4za.booking_system.features.invitation.enitity.InvitationUsageId;
import com.littlek4za.booking_system.features.invitation.model.ValidationResult;
import com.littlek4za.booking_system.features.invitation.validator.InvitationValidator;
import com.littlek4za.booking_system.features.slot.SlotRepository;
import com.littlek4za.booking_system.features.slot.entity.Slot;
import com.littlek4za.booking_system.security.SecurityUtil;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BookingServiceImpl implements BookingService {

    private final SecurityUtil securityUtil;
    private final UserRepository userRepository;
    private final SlotRepository slotRepository;
    private final InvitationRepository invitationRepository;
    private final BookingRepository bookingRepository;
    private final EventRepository eventRepository;
    private final InvitationUsageRepository invitationUsageRepository;
    private final DtoMapper dtoMapper;
    private final BookingValidator bookingValidator;
    private final InvitationValidator invitationValidator;
    private final RedisRiskService riskService;

    private final ApplicationEventPublisher eventPublisher;

    public BookingServiceImpl(SecurityUtil securityUtil, UserRepository userRepository, SlotRepository slotRepository,
            InvitationRepository invitationRepository, BookingRepository bookingRepository,
            EventRepository eventRepository,
            InvitationUsageRepository invitationUsageRepository, DtoMapper dtoMapper,
            BookingValidator bookingValidator, InvitationValidator invitationValidator,
            RedisRiskService riskService, ApplicationEventPublisher eventPublisher) {
        this.securityUtil = securityUtil;
        this.userRepository = userRepository;
        this.slotRepository = slotRepository;
        this.invitationRepository = invitationRepository;
        this.bookingRepository = bookingRepository;
        this.eventRepository = eventRepository;
        this.invitationUsageRepository = invitationUsageRepository;
        this.dtoMapper = dtoMapper;
        this.bookingValidator = bookingValidator;
        this.invitationValidator = invitationValidator;
        this.riskService = riskService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @PreAuthorize("@authz.isGuestBookingCreate() or (@authz.isUser() and hasAnyAuthority('ROLE_ADMIN','ROLE_ATTENDEE'))")
    @Transactional
    public AttendeeBookingResponseDto createBooking(BookingRequestDto dto, Long slotId, String clientIp) {

        boolean isGuestBookingCreate = securityUtil.isGuestBookingCreate();

        Slot slot = slotRepository.findByIdWithEventForUpdate(slotId)
                .orElseThrow(() -> {
                    if (isGuestBookingCreate) {
                        riskService.recordAttemptForBookingCreate(dto.email(), clientIp);
                    }
                    return new AppException("Unknown Slot Id", HttpStatus.NOT_FOUND, ErrorCode.SLOT_NOT_FOUND);
                });

        Event event = slot.getEvent();

        Invitation invitation = invitationRepository.findByIdWithEventAndSlotSetsAndUsersForUpdate(dto.invitationId())
                .orElseThrow(() -> {
                    if (isGuestBookingCreate) {
                        riskService.recordAttemptForBookingCreate(dto.email(), clientIp);
                    }
                    return new AppException("Unknown Invitation Id", HttpStatus.NOT_FOUND,
                            ErrorCode.INVITATION_NOT_FOUND);
                });

        User user = getOrCreateUser(dto);

        InvitationUsageId invitationUsageId = new InvitationUsageId(invitation.getId(), user.getId());

        InvitationUsage invitationUsage = invitationUsageRepository.findById(invitationUsageId)
                .orElseGet(() -> {
                    try {
                        InvitationUsage iu = new InvitationUsage(invitation, user);
                        return invitationUsageRepository.save(iu);
                    } catch (DataIntegrityViolationException e) {
                        return invitationUsageRepository.findById(invitationUsageId)
                                .orElseThrow();
                    }
                });

        try {
            bookingValidator.validateSlotBelongsToInvitation(slot, invitation);
        } catch (AppException e) {

            if (isGuestBookingCreate && e.getErrorCode() == ErrorCode.SLOT_INVITATION_MISMATCH) {
                riskService.recordAttemptForBookingCreate(dto.email(), clientIp);
            }

            throw e;
        }

        bookingValidator.validateGuestOrUserFields(dto);

        ValidationResult validationResult = invitationValidator.validateAccess(invitation, user.getId(),
                invitationUsage);
        if (!validationResult.isValid()) {
            if (isGuestBookingCreate) {
                riskService.recordAttemptForBookingCreate(dto.email(), clientIp);
            }
            throw new AppException(validationResult.getMessage(), HttpStatus.BAD_REQUEST,
                    validationResult.getErrorCode());
        }

        bookingValidator.validateBookingRequestInfo(dto, invitation, slot, event, user);

        AttendeeBookingResponseDto attendeeBookingResponseDto = saveBookingByEventType(dto, invitation, slot, user,
                invitationUsage);

        eventPublisher.publishEvent(BookingMailEvent.forConfirmation(attendeeBookingResponseDto));

        if (isGuestBookingCreate) {
            riskService.resetEmailIpForBookingCreate(dto.email(), clientIp);
            riskService.reduceIpPenaltyForBookingCreate(clientIp);
            riskService.recordBookingCreateSuccess(clientIp);
        }

        return attendeeBookingResponseDto;
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
    private AttendeeBookingResponseDto saveBookingByEventType(BookingRequestDto dto, Invitation invitation, Slot slot,
            User user, InvitationUsage invitationUsage) {

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

        Integer maxUsagePerIdentity = invitation.getMaxUsagePerIdentity();
        invitationUsage.incrementUsage(maxUsagePerIdentity);

        String attendeeFirstName;
        String attendeeLastName;

        if (user.getGuest()) {
            attendeeFirstName = dto.firstName();
            attendeeLastName = dto.lastName();
        } else {
            attendeeFirstName = user.getFirstName();
            attendeeLastName = user.getLastName();
        }

        Booking newBooking = new Booking(user, slot, requestedStartTime, requestedEndTime,
                slot.getEvent().getEventName(), slot.getEvent().getEventDescription(), slot.getSlotName(),
                slot.getSlotDescription(), invitation.getUser().getEmail(), user.getEmail(),
                invitation.getEvent().getEventLocationAddress(), attendeeFirstName, attendeeLastName, user.getGuest());

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

        return dtoMapper.toAttendeeBookingResponseDto(savedBooking);

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

    @PreAuthorize("@authz.isUser() and hasAnyAuthority('ROLE_ADMIN','ROLE_ORGANIZER')")
    @Override
    public List<OrganizerBookingResponseDto> getOrganizerBookingsBySlotId(Long slotId) {

        Long userId = this.securityUtil.requireUserId();

        Slot slot = slotRepository.findByIdAndEvent_User_Id(slotId, userId)
                .orElseThrow(() -> new AppException("Slot not found", HttpStatus.NOT_FOUND, ErrorCode.SLOT_NOT_FOUND));

        List<Booking> bookingList = bookingRepository.findBySlot(slot);

        List<OrganizerBookingResponseDto> organizerBookingResponseDtoList = bookingList.stream()
                .map(booking -> dtoMapper.toOrganizerBookingResponseDto(booking)).toList();

        return organizerBookingResponseDtoList;
    }

    @PreAuthorize("@authz.isUser() and hasAnyAuthority('ROLE_ADMIN','ROLE_ORGANIZER')")
    @Override
    public List<OrganizerBookingResponseDto> getOrganizerBookingsByEventId(Long eventId) {

        User user = userRepository.findById(this.securityUtil.requireUserId())
                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND, ErrorCode.USER_NOT_FOUND));
        Event event = eventRepository.findByIdAndUser(eventId, user)
                .orElseThrow(() -> new AppException("Event not found with eventId and user",
                        HttpStatus.NOT_FOUND, ErrorCode.EVENT_NOT_FOUND));

        List<Booking> bookingList = bookingRepository.findActiveBookingsByEventId(event);

        List<OrganizerBookingResponseDto> organizerBookingResponseDtoList = bookingList.stream()
                .map(booking -> dtoMapper.toOrganizerBookingResponseDto(booking)).toList();

        return organizerBookingResponseDtoList;
    }

    @Override
    @PreAuthorize("@authz.isUser() and hasAnyAuthority('ROLE_ADMIN','ROLE_ORGANIZER')")
    @Transactional
    public OrganizerBookingResponseDto softDeleteBookingAsOrganizer(Long slotId, Long bookingId) {

        userRepository.findById(this.securityUtil.requireUserId())
                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND, ErrorCode.USER_NOT_FOUND));

        Booking booking = bookingRepository
                .findOrganizerBookingByIdAndSlotIdAndUserId(bookingId, slotId, this.securityUtil.requireUserId())
                .orElseThrow(() -> new AppException("Booking not found with bookingId, slotId and user",
                        HttpStatus.NOT_FOUND, ErrorCode.BOOKING_NOT_FOUND));

        BookingStatus bookingStatus = BookingStatus.from(booking.getBookedStartTime(), booking.getBookedEndTime(),
                booking.isDeleted());

        boolean shouldSendCancellationEmail = bookingStatus == BookingStatus.UPCOMING
                || bookingStatus == BookingStatus.ONGOING;

        booking.setDeletedBy(DeletedBy.ORGANIZER);
        booking.setDeletedAt(Instant.now());
        booking.setDeleted(true);

        bookingRepository.save(booking);

        if (shouldSendCancellationEmail) {
            eventPublisher.publishEvent(BookingMailEvent.forOrganizerCancellation(
                    booking.getBookingToken(),
                    booking.getAttendeeEmail(),
                    booking.getAttendeeFirstName(),
                    booking.getAttendeeLastName(),
                    booking.getOrganizerEmail(),
                    booking.getSlot().getEvent().getUser().getFirstName(),
                    booking.getSlot().getEvent().getUser().getLastName(),
                    booking.getBookedStartTime(),
                    booking.getBookedEndTime(),
                    booking.getEventName(),
                    booking.getSlotName(),
                    booking.getEventLocationAddress()));
        }

        return dtoMapper.toOrganizerBookingResponseDto(booking);
    }

    @Override
    @PreAuthorize("(@authz.isUser() and hasAnyAuthority('ROLE_ADMIN','ROLE_ATTENDEE')) or @authz.isGuestBookingView()")
    @Transactional
    public OrganizerBookingResponseDto softDeleteBookingAsAttendee(Long bookingId) {

        Booking booking;
        if (securityUtil.isUser()) {
            booking = bookingRepository
                    .findAttendeeBookingByIdAndUserId(bookingId, this.securityUtil.requireUserId())
                    .orElseThrow(() -> new AppException("Booking not found with bookingId and user",
                            HttpStatus.NOT_FOUND, ErrorCode.BOOKING_NOT_FOUND));
        } else if (securityUtil.isGuestBookingView()) {
            booking = bookingRepository
                    .findAttendeeBookingByIdAndUserEmailAndBookingToken(bookingId, this.securityUtil.requireEmail(),
                            this.securityUtil.requireBookingToken())
                    .orElseThrow(() -> new AppException("Booking not found with bookingId and email",
                            HttpStatus.NOT_FOUND, ErrorCode.BOOKING_NOT_FOUND));
        } else {
            throw new AppException("Access denied", HttpStatus.FORBIDDEN, ErrorCode.ACCESS_DENIED);
        }

        BookingStatus bookingStatus = BookingStatus.from(booking.getBookedStartTime(), booking.getBookedEndTime(),
                booking.isDeleted());

        boolean shouldSendCancellationEmail = bookingStatus == BookingStatus.UPCOMING
                || bookingStatus == BookingStatus.ONGOING;

        booking.setDeletedBy(DeletedBy.ATTENDEE);
        booking.setDeletedAt(Instant.now());
        booking.setDeleted(true);

        bookingRepository.save(booking);

        if (shouldSendCancellationEmail) {
            eventPublisher.publishEvent(BookingMailEvent.forAttendeeCancellation(
                    booking.getBookingToken(),
                    booking.getAttendeeEmail(),
                    booking.getAttendeeFirstName(),
                    booking.getAttendeeLastName(),
                    booking.getOrganizerEmail(),
                    booking.getSlot().getEvent().getUser().getFirstName(),
                    booking.getSlot().getEvent().getUser().getLastName(),
                    booking.getBookedStartTime(),
                    booking.getBookedEndTime(),
                    booking.getEventName(),
                    booking.getSlotName(),
                    booking.getEventLocationAddress()));
        }

        return dtoMapper.toOrganizerBookingResponseDto(booking);
    }

    @PreAuthorize("(@authz.isUser() and hasAnyAuthority('ROLE_ADMIN','ROLE_ATTENDEE')) or @authz.isGuestBookingView()")
    @Override
    public AttendeeBookingResponseDto getBookingByTokenAsAttendee(String bookingToken) {

        Booking booking;
        if (securityUtil.isUser()) {
            Long userId = securityUtil.requireUserId();
            booking = bookingRepository.findByBookingTokenAndUserId(bookingToken, userId)
                    .orElseThrow(() -> new AppException("Booking not found with booking token and user",
                            HttpStatus.NOT_FOUND, ErrorCode.BOOKING_NOT_FOUND));
        } else if (securityUtil.isGuestBookingView()) {
            String email = securityUtil.requireEmail();
            String jwtBookingToken = securityUtil.requireBookingToken();
            if (!jwtBookingToken.equals(bookingToken)) {
                log.warn("Booking Token and JWT Booking Token Mismatch detected");
            }
            booking = bookingRepository.findbyBookingTokenAndEmail(jwtBookingToken, email)
                    .orElseThrow(() -> new AppException("Booking not found with booking token and email",
                            HttpStatus.NOT_FOUND, ErrorCode.BOOKING_NOT_FOUND));
        } else {
            throw new AppException("Access denied", HttpStatus.FORBIDDEN, ErrorCode.ACCESS_DENIED);
        }

        AttendeeBookingResponseDto bookingResponseDto = dtoMapper.toAttendeeBookingResponseDto(booking);

        return bookingResponseDto;
    }

    @PreAuthorize("@authz.isUser() and hasAnyAuthority('ROLE_ADMIN','ROLE_ATTENDEE')")
    @Override
    public List<AttendeeBookingResponseDto> getAttendeeBookings() {
        Long userId = securityUtil.requireUserId();
        List<Booking> bookings = bookingRepository.findByUserId(userId);

        List<AttendeeBookingResponseDto> bookingUserResponseDtos = bookings.stream()
                .map(booking -> {
                    return dtoMapper.toAttendeeBookingResponseDto(booking);
                }).toList();

        return bookingUserResponseDtos;

    }

    @Override
    public List<SlotBookedTimeResponseDto> getSlotBookedTimesBySlotId(Long slotId) {

        Slot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new AppException("Slot not found", HttpStatus.NOT_FOUND, ErrorCode.SLOT_NOT_FOUND));

        List<Booking> bookingList = bookingRepository.findBySlot(slot);

        List<SlotBookedTimeResponseDto> slotBookedTimeResponseDtoList = bookingList.stream()
                .map(booking -> dtoMapper.toSlotBookedTimeResponseDto(booking)).toList();

        return slotBookedTimeResponseDtoList;
    }

}
