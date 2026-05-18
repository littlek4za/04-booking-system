package com.littlek4za.booking_system.services;

import java.util.List;

import com.littlek4za.booking_system.dtos.DeleteValidationResponseDto;
import com.littlek4za.booking_system.dtos.SlotRequestDto;
import com.littlek4za.booking_system.dtos.SlotResponseDto;

public interface SlotService {

    List<SlotResponseDto> getSlotsByEventId(Long eventId);
    SlotResponseDto createSlotByEvent(Long eventId, SlotRequestDto slotRequestDto);
    Long deleteSlotByEventAndSlot(Long eventId, Long slotId);
    SlotResponseDto getSlotByIdAndEventId(Long eventId, Long slotId);
    SlotResponseDto putSlotByIdAndEventId(Long slotId, Long eventId, SlotRequestDto slotRequestDto);
    DeleteValidationResponseDto slotDeleteValidation(Long eventId, Long slotId);
    
}
