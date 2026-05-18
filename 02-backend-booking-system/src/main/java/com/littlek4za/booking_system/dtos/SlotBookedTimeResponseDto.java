package com.littlek4za.booking_system.dtos;

import java.time.Instant;

public record SlotBookedTimeResponseDto(
    Instant bookedStartTime,
    Instant bookedEndTime
) {

}
