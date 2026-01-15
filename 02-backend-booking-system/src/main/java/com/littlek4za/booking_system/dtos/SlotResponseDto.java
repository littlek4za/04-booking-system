package com.littlek4za.booking_system.dtos;

import java.time.Instant;
import java.util.Map;

import com.littlek4za.booking_system.models.TimeRange;

public record SlotResponseDto(
    Long eventId,
    Long id,
    String slotName,
    String slotDescription,
    Instant slotStartTime,
    Instant slotEndTime,
    Integer maxBook,
    Integer slotIntervalMinutes,
    Map<Integer, TimeRange> workingDaysHours,
    Instant createdAt,
    Instant updatedAt
) {} 
