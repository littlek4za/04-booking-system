package com.littlek4za.booking_system.services;

import java.util.List;

import com.littlek4za.booking_system.dtos.DeleteValidationResponseDto;
import com.littlek4za.booking_system.dtos.EventRequestDto;
import com.littlek4za.booking_system.dtos.EventResponseDto;
import com.littlek4za.booking_system.dtos.EventWithSlotCountReponseDto;

public interface EventService {

    EventResponseDto createEvent(EventRequestDto eRequestDto);
    List<EventWithSlotCountReponseDto> getEvents();
    EventWithSlotCountReponseDto getEventById(Long eventId);
    EventResponseDto putEventById(Long eventId, EventRequestDto eRequestDto);
    Long deleteEventById(Long eventId);
    DeleteValidationResponseDto eventDeleteValidation(Long eventId);
}
