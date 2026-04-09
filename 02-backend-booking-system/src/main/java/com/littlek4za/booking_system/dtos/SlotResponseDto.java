package com.littlek4za.booking_system.dtos;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import com.littlek4za.booking_system.models.InstantRange;
import com.littlek4za.booking_system.models.TimeRange;

public record SlotResponseDto(
    Long eventId,
    Long id,
    String slotName,
    String slotDescription,
    Instant slotStartTime,
    Instant slotEndTime,
    Integer maxBookingsPerIdentity,
    Integer maxBookPerInterval,
    Integer slotIntervalMinutes,
    Integer slotFrequencyIntervalMinutes,
    Map<Integer, List<TimeRange>> businessDaysHours,
    String businessTimeZone,
    Boolean businessAllowOt,
    List<InstantRange> flexibleDaysHours,
    Instant createdAt,
    Instant updatedAt,
    Long bookingsCount
) {} 
