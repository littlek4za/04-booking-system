package com.littlek4za.booking_system.services;

import com.littlek4za.booking_system.dto.EventSaveRequestDto;
import com.littlek4za.booking_system.dto.EventResponseDto;

public interface EventService {

    EventResponseDto createEvent(EventSaveRequestDto eSaveRequestDto, Long userId);

}
