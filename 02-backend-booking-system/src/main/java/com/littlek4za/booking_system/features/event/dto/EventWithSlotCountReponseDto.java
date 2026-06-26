package com.littlek4za.booking_system.features.event.dto;

import java.time.Instant;

public record EventWithSlotCountReponseDto(
    Long id,
    String eventName,
    String eventDescription,
    String eventLocationAddress,
    Boolean includePosition,
    Double latitude,
    Double longitude,
    Integer maxBookingsPerIdentity,
    String eventType,
    Instant createdAt,
    Instant updatedAt,
    Long slotCount
) {
}
