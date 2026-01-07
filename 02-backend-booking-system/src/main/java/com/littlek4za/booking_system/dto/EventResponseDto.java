package com.littlek4za.booking_system.dto;

import java.time.Instant;

public record EventResponseDto(
    Long id,
    String userName,
    String eventName,
    String eventDescription,
    String eventLocationName,
    Boolean includePosition,
    Double latitude,
    Double longitude,
    String slotType,
    Instant createdAt
) {}
