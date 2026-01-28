package com.littlek4za.booking_system.dtos;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import com.littlek4za.booking_system.models.InstantRange;
import com.littlek4za.booking_system.models.TimeRange;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SlotRequestDto(

    @NotNull(message = "is required")
    Long eventId,

    @NotBlank(message = "is required")
    @Size(min = 1, max = 350, message = "Slot Name must be between 1 and 350 characters")
    String slotName,

    @Size(max = 2500, message = "Slot Description must not exceed 2500 characters")
    String slotDescription,

    Instant slotStartTime,

    Instant slotEndTime,
    
    Integer maxBook,

    Integer slotIntervalMinutes,

    Integer slotFrequencyIntervalMinutes,

    Map<Integer, List<TimeRange>> businessDaysHours,

    List<InstantRange> flexibleDaysHours

) {} 
