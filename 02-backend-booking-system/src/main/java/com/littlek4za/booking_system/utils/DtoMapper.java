package com.littlek4za.booking_system.utils;

import com.littlek4za.booking_system.dtos.EventWithSlotCountReponseDto;
import com.littlek4za.booking_system.dtos.LoginResponseDto;
import com.littlek4za.booking_system.dtos.SlotResponseDto;
import com.littlek4za.booking_system.entities.Event;
import com.littlek4za.booking_system.entities.Slot;
import com.littlek4za.booking_system.entities.User;


public interface DtoMapper {

    LoginResponseDto toLoginResponseDto(User user);
    EventWithSlotCountReponseDto toEventWithSlotCountResponseDto(Event event, Long slotCount);
    SlotResponseDto toSlotResponseDto(Slot slot);

}
