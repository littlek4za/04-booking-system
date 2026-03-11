package com.littlek4za.booking_system.services;

import java.security.SecureRandom;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.littlek4za.booking_system.dtos.BookingRequestDto;
import com.littlek4za.booking_system.dtos.BookingResponseDto;
import com.littlek4za.booking_system.entities.Booking;
import com.littlek4za.booking_system.entities.Invitation;
import com.littlek4za.booking_system.entities.InvitationUsage;
import com.littlek4za.booking_system.entities.Slot;
import com.littlek4za.booking_system.entities.User;
import com.littlek4za.booking_system.exception.AppException;
import com.littlek4za.booking_system.models.EventType;
import com.littlek4za.booking_system.models.SlotIncludeMode;
import com.littlek4za.booking_system.repos.BookingRepository;
import com.littlek4za.booking_system.repos.InvitationRepository;
import com.littlek4za.booking_system.repos.InvitationUsageRepository;
import com.littlek4za.booking_system.repos.SlotRepository;
import com.littlek4za.booking_system.repos.UserRepository;
import com.littlek4za.booking_system.security.SecurityUtil;
import com.littlek4za.booking_system.utils.DtoMapper;

import jakarta.transaction.Transactional;

@Service
public class BookingServiceImpl implements BookingService {

    private final SecurityUtil securityUtil;
    private final UserRepository userRepository;
    private final SlotRepository slotRepository;
    private final InvitationRepository invitationRepository;
    private final BookingRepository bookingRepository;
    private final InvitationUsageRepository invitationUsageRepository;
    private final DtoMapper dtoMapper;

    public BookingServiceImpl(SecurityUtil securityUtil, UserRepository userRepository, SlotRepository slotRepository,
            InvitationRepository invitationRepository, BookingRepository bookingRepository,
            InvitationUsageRepository invitationUsageRepository, DtoMapper dtoMapper) {
        this.securityUtil = securityUtil;
        this.userRepository = userRepository;
        this.slotRepository = slotRepository;
        this.invitationRepository = invitationRepository;
        this.bookingRepository = bookingRepository;
        this.invitationUsageRepository = invitationUsageRepository;
        this.dtoMapper = dtoMapper;
    }

    @Transactional
    @Override
    public BookingResponseDto createBooking(BookingRequestDto dto) {

        Slot slot = slotRepository.findById(dto.slotId())
                .orElseThrow(() -> new AppException("Unknow Slot Id", HttpStatus.NOT_FOUND));
        Invitation invitation = invitationRepository.findByIdWithEventAndSlotSets(dto.invitationId())
                .orElseThrow(() -> new AppException("Unknown Invitation Id", HttpStatus.NOT_FOUND));

        if (!SlotIncludeMode.ALL_AND_FUTURE.equals(invitation.getSlotIncludeMode())
                && !invitation.getSlotSet().contains(slot)) {
            throw new AppException("Slot id does not belong to this invitation", HttpStatus.BAD_REQUEST);
        }

        validateGuestOrUserFields(dto);

        User user;
        if (securityUtil.isAuthenticated()) {
            user = userRepository.findById(this.securityUtil.getCurrentAuthUserId())
                    .orElseThrow(() -> new AppException("Unknown User", HttpStatus.NOT_FOUND));
        } else {
            Optional<User> existingUser = userRepository.findByEmail(dto.email());
            
            if(existingUser.isPresent()) {
                user = existingUser.get();

                if(!user.getGuest()){
                    throw new AppException("Email already registered. Please log in to continue.", HttpStatus.BAD_REQUEST);
                }
            } else {
                user = userRepository.save(User.createGuest(dto.email()));
            }
        }

        validateBookingInfo(dto, invitation, slot);

        BookingResponseDto bookingResponseDto = createBookingByEventType(dto, invitation, slot, user);

        return bookingResponseDto;
    }

    private void validateGuestOrUserFields(BookingRequestDto bookingRequestDto) {
        boolean authenticated = securityUtil.isAuthenticated();

        if (authenticated) {
            if (bookingRequestDto.email() != null || bookingRequestDto.firstName() != null
                    || bookingRequestDto.lastName() != null) {
                throw new AppException("Authenticated user should not provide email, firstName and lastName",
                        HttpStatus.BAD_REQUEST);
            }
        } else {
            if (bookingRequestDto.email() == null || bookingRequestDto.firstName() == null
                    || bookingRequestDto.lastName() == null) {
                throw new AppException("Guest booking requires email, firtName and lastName",
                        HttpStatus.BAD_REQUEST);
            }
        }
    }

    private void validateBookingInfo(BookingRequestDto dto, Invitation invitation, Slot slot) {

        EventType eventType = invitation.getEvent().getEventType();

        switch (eventType) {
            case FIXED -> validateFixedBooking(dto, slot, invitation);
            case FLEXIBLE -> validateFlexibleBooking(dto, slot, invitation);
            case BUSINESS -> validateBusinessBooking(dto, slot, invitation);
        }
    }

    private void validateFixedBooking(BookingRequestDto dto, Slot slot, Invitation invitation) {
        if (dto.bookedStartTime() != null || dto.bookedEndTime() != null) {
            throw new AppException(("Fixed Type Event do not need start and end time"), HttpStatus.BAD_REQUEST);
        }
    }

    private void validateFlexibleBooking(BookingRequestDto dto, Slot slot, Invitation invitation) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'validateFlexibleBooking'");
    }

    private void validateBusinessBooking(BookingRequestDto dto, Slot slot, Invitation invitation) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'validateFixedBooking'");
    }

    // create booking by Event Type sub method
    private BookingResponseDto createBookingByEventType(BookingRequestDto dto, Invitation invitation, Slot slot,
            User user) {
        InvitationUsage invitationUsage = invitationUsageRepository
                .findByUserIdAndInvitationId(user.getId(), invitation.getId())
                .orElseGet(() -> {
                    InvitationUsage newUsage = new InvitationUsage(invitation, user);
                    return newUsage;
                });

        if (EventType.FIXED.equals(invitation.getEvent().getEventType())) {

            if (securityUtil.isAuthenticated()) {
                Integer maxUsagePerUser = invitation.getMaxUsagePerUser();
                if (maxUsagePerUser != null && invitationUsage.getUsageCount() >= maxUsagePerUser) {
                    throw new AppException("User maximum usage reached", HttpStatus.BAD_REQUEST);
                } else {
                    invitationUsage.setUsageCount(invitationUsage.getUsageCount() + 1);
                }
            }
            Booking newBooking = new Booking(user, slot, slot.getSlotStartTime(), slot.getSlotEndTime());
            if(user.getGuest()){
                newBooking.setGuestFirstName(dto.firstName());
                newBooking.setGuestLastName(dto.lastName());
            }
            newBooking.setBookingToken(generateUniqueBookingToken());
            invitation.addBooking(newBooking);

            Booking savedBooking = bookingRepository.save(newBooking);
            invitationUsageRepository.save(invitationUsage);

            return dtoMapper.toBookingResponseDto(savedBooking);

            // } else if (EventType.FLEXIBLE.equals(invitation.getEvent().getEventType())){

            // } else if (EventType.BUSINESS.equals(invitation.getEvent().getEventType())){

        } else {
            throw new AppException("EventType not found", HttpStatus.BAD_REQUEST);
        }

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

}
