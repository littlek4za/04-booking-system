package com.littlek4za.booking_system.services;

import java.util.List;

import com.littlek4za.booking_system.dtos.SlotResponseDto;

public interface SlotService {

    List<SlotResponseDto> getSlotsByEvent(Long eventId);
    
}
