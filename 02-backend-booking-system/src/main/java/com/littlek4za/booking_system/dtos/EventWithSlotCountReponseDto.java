package com.littlek4za.booking_system.dtos;

import java.time.Instant;

public record EventWithSlotCountReponseDto(
    Long id,
    String eventName,
    String eventDescription,
    String eventLocationAddress,
    Boolean includePosition,
    Double latitude,
    Double longitude,
    String eventType,
    Instant createdAt,
    Instant updatedAt,
    Long slotCount
) {
}
