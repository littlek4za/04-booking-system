package com.littlek4za.booking_system.features.booking.dto;

import java.time.Instant;

public record SlotBookedTimeResponseDto(
    Instant bookedStartTime,
    Instant bookedEndTime
) {

}
