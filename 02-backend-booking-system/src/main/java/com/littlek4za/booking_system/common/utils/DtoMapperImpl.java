package com.littlek4za.booking_system.common.utils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.littlek4za.booking_system.features.auth.dto.UserDto;
import com.littlek4za.booking_system.features.auth.entity.User;
import com.littlek4za.booking_system.features.booking.Booking;
import com.littlek4za.booking_system.features.booking.dto.AttendeeBookingResponseDto;
import com.littlek4za.booking_system.features.booking.dto.OrganizerBookingResponseDto;
import com.littlek4za.booking_system.features.booking.dto.SlotBookedTimeResponseDto;
import com.littlek4za.booking_system.features.booking.model.BookingStatus;
import com.littlek4za.booking_system.features.event.Event;
import com.littlek4za.booking_system.features.event.dto.EventResponseDto;
import com.littlek4za.booking_system.features.event.dto.EventWithSlotCountReponseDto;
import com.littlek4za.booking_system.features.invitation.dto.InvitationRequestDto;
import com.littlek4za.booking_system.features.invitation.dto.InvitationResponseDto;
import com.littlek4za.booking_system.features.invitation.enitity.Invitation;
import com.littlek4za.booking_system.features.invitation.model.SlotIncludeMode;
import com.littlek4za.booking_system.features.slot.dto.SlotRequestDto;
import com.littlek4za.booking_system.features.slot.dto.SlotResponseDto;
import com.littlek4za.booking_system.features.slot.entity.Slot;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class DtoMapperImpl implements DtoMapper {

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
        public OrganizerBookingResponseDto toOrganizerBookingResponseDto(Booking savedBooking) {

                BookingStatus status = BookingStatus.from(savedBooking.getBookedStartTime(),
                                savedBooking.getBookedEndTime(), savedBooking.isDeleted());

                return new OrganizerBookingResponseDto(
                                savedBooking.getId(),
                                savedBooking.getAttendeeLastName(),
                                savedBooking.getAttendeeFirstName(),
                                savedBooking.getIsGuest(),
                                savedBooking.getAttendeeEmail(),
                                toSlotResponseDto(savedBooking.getSlot()),
                                savedBooking.getBookedStartTime(),
                                savedBooking.getBookedEndTime(),
                                savedBooking.getBookingToken(),
                                savedBooking.getBookedAt(),
                                status,
                                toEventResponseDto(savedBooking.getSlot().getEvent()));
        }

        @Override
        public AttendeeBookingResponseDto toAttendeeBookingResponseDto(Booking savedBooking) {

                BookingStatus status = BookingStatus.from(savedBooking.getBookedStartTime(),
                                savedBooking.getBookedEndTime(), savedBooking.isDeleted());

                return new AttendeeBookingResponseDto(
                                savedBooking.getId(),
                                savedBooking.getAttendeeLastName(),
                                savedBooking.getAttendeeFirstName(),
                                savedBooking.getIsGuest(),
                                savedBooking.getBookedStartTime(),
                                savedBooking.getBookedEndTime(),
                                savedBooking.getBookingToken(),
                                savedBooking.getBookedAt(),
                                status,
                                savedBooking.getEventName(),
                                savedBooking.getEventDescription(),
                                savedBooking.getSlotName(),
                                savedBooking.getSlotDescription(),
                                savedBooking.getOrganizerEmail(),
                                savedBooking.getAttendeeEmail(),
                                savedBooking.getEventLocationAddress(),
                                savedBooking.getLatitude(),
                                savedBooking.getLongitude());
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
                                                .collect(Collectors.toSet()));
        }

        @Override
        public SlotBookedTimeResponseDto toSlotBookedTimeResponseDto(Booking booking) {
                return new SlotBookedTimeResponseDto(
                                booking.getBookedStartTime(),
                                booking.getBookedEndTime());
        }

}
