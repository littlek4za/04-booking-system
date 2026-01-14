package com.littlek4za.booking_system.mapper;

import com.littlek4za.booking_system.dtos.EventWithSlotCountReponseDto;
import com.littlek4za.booking_system.dtos.LoginResponseDto;
import com.littlek4za.booking_system.entities.Event;
import com.littlek4za.booking_system.entities.User;


public interface DtoMapper {

    LoginResponseDto userToLoginResponseDto(User user);
    EventWithSlotCountReponseDto eventToEListResponseDto(Event event, Long slotCount);

}
