package com.littlek4za.booking_system.common.utils;

import java.util.Set;

import com.littlek4za.booking_system.features.auth.dto.UserDto;
import com.littlek4za.booking_system.features.auth.entity.User;
import com.littlek4za.booking_system.features.booking.Booking;
import com.littlek4za.booking_system.features.booking.dto.AttendeeBookingResponseDto;
import com.littlek4za.booking_system.features.booking.dto.OrganizerBookingResponseDto;
import com.littlek4za.booking_system.features.booking.dto.SlotBookedTimeResponseDto;
import com.littlek4za.booking_system.features.event.Event;
import com.littlek4za.booking_system.features.event.dto.EventResponseDto;
import com.littlek4za.booking_system.features.event.dto.EventWithSlotCountReponseDto;
import com.littlek4za.booking_system.features.invitation.dto.InvitationRequestDto;
import com.littlek4za.booking_system.features.invitation.dto.InvitationResponseDto;
import com.littlek4za.booking_system.features.invitation.enitity.Invitation;
import com.littlek4za.booking_system.features.slot.dto.SlotRequestDto;
import com.littlek4za.booking_system.features.slot.dto.SlotResponseDto;
import com.littlek4za.booking_system.features.slot.entity.Slot;

public interface DtoMapper {

    EventWithSlotCountReponseDto toEventWithSlotCountResponseDto(Event event, Long slotCount);

    SlotResponseDto toSlotResponseDto(Slot slot);

    SlotResponseDto toSlotResponseDto(Slot slot, Long bookingsCount);

    Slot toSlot(SlotRequestDto slotRequestDto, Event event);

    EventResponseDto toEventResponseDto(Event event);

    Invitation toInvitation(InvitationRequestDto invitationRequestDto, Event event, User user);

    InvitationResponseDto toInvitationResponseDto(Invitation invitation, Set<Slot> slotList);

    OrganizerBookingResponseDto toOrganizerBookingResponseDto(Booking savedBooking);

    AttendeeBookingResponseDto toAttendeeBookingResponseDto(Booking booking);

    UserDto toUserDto(User user);

    SlotBookedTimeResponseDto toSlotBookedTimeResponseDto(Booking booking);

}
