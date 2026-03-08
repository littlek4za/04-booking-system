package com.littlek4za.booking_system.utils;

import java.util.Set;

import com.littlek4za.booking_system.dtos.EventResponseDto;
import com.littlek4za.booking_system.dtos.EventWithSlotCountReponseDto;
import com.littlek4za.booking_system.dtos.InvitationRequestDto;
import com.littlek4za.booking_system.dtos.InvitationResponseDto;
import com.littlek4za.booking_system.dtos.LoginResponseDto;
import com.littlek4za.booking_system.dtos.SlotRequestDto;
import com.littlek4za.booking_system.dtos.SlotResponseDto;
import com.littlek4za.booking_system.entities.Event;
import com.littlek4za.booking_system.entities.Invitation;
import com.littlek4za.booking_system.entities.Slot;
import com.littlek4za.booking_system.entities.User;

public interface DtoMapper {

    LoginResponseDto toLoginResponseDto(User user);

    EventWithSlotCountReponseDto toEventWithSlotCountResponseDto(Event event, Long slotCount);

    SlotResponseDto toSlotResponseDto(Slot slot);

    Slot toSlot(SlotRequestDto slotRequestDto, Event event);

    EventResponseDto toEventResponseDto(Event event);

    Invitation toInvitation(InvitationRequestDto invitationRequestDto, Event event, User user);

    InvitationResponseDto toInvitationResponseDto(Invitation invitation, Set<Slot> slotSet );

}
