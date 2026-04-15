package com.littlek4za.booking_system.utils;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.littlek4za.booking_system.dtos.BookingResponseDto;
import com.littlek4za.booking_system.dtos.EventResponseDto;
import com.littlek4za.booking_system.dtos.EventWithSlotCountReponseDto;
import com.littlek4za.booking_system.dtos.InvitationRequestDto;
import com.littlek4za.booking_system.dtos.InvitationResponseDto;
import com.littlek4za.booking_system.dtos.SlotRequestDto;
import com.littlek4za.booking_system.dtos.SlotResponseDto;
import com.littlek4za.booking_system.dtos.UserDto;
import com.littlek4za.booking_system.entities.Booking;
import com.littlek4za.booking_system.entities.Event;
import com.littlek4za.booking_system.entities.Invitation;
import com.littlek4za.booking_system.entities.Slot;
import com.littlek4za.booking_system.entities.User;
import com.littlek4za.booking_system.models.BookingStatus;
import com.littlek4za.booking_system.models.SlotIncludeMode;

@Component
public class DtoMapperImpl implements DtoMapper {

        @Value("${security.jwt.token.secret-key:dev-secret-key}")
        private String secretKey;
        @Value("${security.jwt.issuer:booking-system}")
        private String issuerString;

        @Override
        public EventWithSlotCountReponseDto toEventWithSlotCountResponseDto(Event event, Long slotCount) {
                return new EventWithSlotCountReponseDto(
                                event.getId(),
                                event.getEventName(),
                                event.getEventDescription(),
                                event.getEventLocationAddress(),
                                event.getIncludePosition(),
                                event.getLatitude(),
                                event.getLongitude(),
                                event.getMaxBookingsPerIdentity(),
                                event.getEventType().toString(),
                                event.getCreatedAt(),
                                event.getUpdatedAt(),
                                slotCount);
        }

        @Override
        public SlotResponseDto toSlotResponseDto(Slot slot) {
                return toSlotResponseDto(slot, null);
        }

        @Override
        public SlotResponseDto toSlotResponseDto(Slot slot, Long bookingsCount) {
                return new SlotResponseDto(
                                slot.getEvent().getId(),
                                slot.getId(),
                                slot.getSlotName(),
                                slot.getSlotDescription(),
                                slot.getSlotStartTime(),
                                slot.getSlotEndTime(),
                                slot.getMaxBookingsPerIdentity(),
                                slot.getMaxBookPerInterval(),
                                slot.getSlotIntervalMinutes(),
                                slot.getSlotFrequencyIntervalMinutes(),
                                slot.getBusinessDaysHours(),
                                slot.getBusinessTimeZone(),
                                slot.getBusinessAllowOT(),
                                slot.getFlexibleDaysHours(),
                                slot.getCreatedAt(),
                                slot.getUpdatedAt(),
                                bookingsCount);
        }

        @Override
        public Slot toSlot(SlotRequestDto slotRequestDto, Event event) {
                return new Slot(
                                event,
                                slotRequestDto.slotName(),
                                slotRequestDto.slotDescription(),
                                slotRequestDto.slotStartTime(),
                                slotRequestDto.slotEndTime(),
                                slotRequestDto.maxBookingsPerIdentity(),
                                slotRequestDto.maxBookPerInterval(),
                                slotRequestDto.slotIntervalMinutes(),
                                slotRequestDto.slotFrequencyIntervalMinutes(),
                                slotRequestDto.businessDaysHours(),
                                slotRequestDto.flexibleDaysHours(),
                                slotRequestDto.businessTimeZone(),
                                slotRequestDto.businessAllowOt());
        }

        @Override
        public EventResponseDto toEventResponseDto(Event event) {
                return new EventResponseDto(
                                event.getId(),
                                event.getUser().getUsername(),
                                event.getEventName(),
                                event.getEventDescription(),
                                event.getEventLocationAddress(),
                                event.getIncludePosition(),
                                event.getLatitude(),
                                event.getLongitude(),
                                event.getMaxBookingsPerIdentity(),
                                event.getEventType().toString(),
                                event.getCreatedAt(),
                                event.getUpdatedAt());
        }

        @Override
        public Invitation toInvitation(InvitationRequestDto invitationRequestDto, Event event, User user) {
                return new Invitation(
                                event,
                                user,
                                invitationRequestDto.expiresAt(),
                                invitationRequestDto.maxUsage(),
                                SlotIncludeMode.valueOf(invitationRequestDto.slotIncludeMode()),
                                invitationRequestDto.requiredLogin(),
                                invitationRequestDto.maxUsagePerIdentity());
        }

        @Override
        public InvitationResponseDto toInvitationResponseDto(Invitation invitation, Set<Slot> slotSet) {

                List<SlotResponseDto> slotResponseDtos = slotSet.stream()
                                .map(slot -> toSlotResponseDto(slot))
                                .collect(Collectors.toList());

                return new InvitationResponseDto(
                                invitation.getId(),
                                toEventResponseDto(invitation.getEvent()),
                                invitation.getUser().getId(),
                                invitation.getExpiresAt(),
                                invitation.getMaxUsage(),
                                invitation.getUsedCount(),
                                invitation.getAccessToken(),
                                invitation.getSlotIncludeMode(),
                                invitation.isRequiredLogin(),
                                invitation.getMaxUsagePerIdentity(),
                                invitation.getCreatedAt(),
                                slotResponseDtos);
        }

        @Override
        public BookingResponseDto toBookingResponseDto(Booking savedBooking) {

                BookingStatus status = calculateStatus(savedBooking);

                String guestFirstName = null;
                String guestLastName = null;

                if (Boolean.TRUE.equals(savedBooking.getUser().getGuest())) {
                        guestFirstName = savedBooking.getGuestFirstName();
                        guestLastName = savedBooking.getGuestLastName();
                }

                return new BookingResponseDto(
                                savedBooking.getId(),
                                savedBooking.getUser().getUsername(),
                                savedBooking.getUser().getLastName(),
                                savedBooking.getUser().getFirstName(),
                                guestLastName,
                                guestFirstName,
                                savedBooking.getUser().getGuest(),
                                savedBooking.getUser().getEmail(),
                                toSlotResponseDto(savedBooking.getSlot()),
                                savedBooking.getBookedStartTime(),
                                savedBooking.getBookedEndTime(),
                                savedBooking.getBookingToken(),
                                savedBooking.getBookedAt(),
                                status);
        }

        private BookingStatus calculateStatus(Booking savedBooking) {
                Instant bookedStartTime = savedBooking.getBookedStartTime();
                Instant bookedEndTime = savedBooking.getBookedEndTime();
                Instant now = Instant.now();

                if (savedBooking.isDeleted()) {
                        return BookingStatus.DELETED;
                }
                if (now.isBefore(bookedStartTime)) {
                        return BookingStatus.UPCOMING;
                }
                if (!now.isBefore(bookedStartTime) && !now.isAfter(bookedEndTime)) {
                        return BookingStatus.ONGOING;
                }
                return BookingStatus.EXPIRED;
        }

        @Override
        public UserDto toUserDto(User user) {
                return new UserDto(
                        user.getId(), 
                        user.getUsername(), 
                        user.getEmail(), 
                        user.getFirstName(), 
                        user.getLastName(), 
                        user.getRoleSet()
                        .stream()
                        .map(role -> role.getRoleName())
                        .collect(Collectors.toSet())
                );
        }

}
