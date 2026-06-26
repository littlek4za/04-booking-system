package com.littlek4za.booking_system.features.event;

import java.util.List;

import com.littlek4za.booking_system.common.dto.DeleteValidationResponseDto;
import com.littlek4za.booking_system.features.event.dto.EventRequestDto;
import com.littlek4za.booking_system.features.event.dto.EventResponseDto;
import com.littlek4za.booking_system.features.event.dto.EventWithSlotCountReponseDto;

public interface EventService {

    EventResponseDto createEvent(EventRequestDto eRequestDto);
    List<EventWithSlotCountReponseDto> getEvents();
    EventWithSlotCountReponseDto getEventById(Long eventId);
    EventResponseDto putEventById(Long eventId, EventRequestDto eRequestDto);
    Long deleteEventById(Long eventId);
    DeleteValidationResponseDto eventDeleteValidation(Long eventId);
}
