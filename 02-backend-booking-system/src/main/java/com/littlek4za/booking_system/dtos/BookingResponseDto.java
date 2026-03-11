package com.littlek4za.booking_system.dtos;

import java.time.Instant;

public record BookingResponseDto(
    String username,
    String lastName,
    String firstName,
    String email,
    SlotResponseDto slot,
    Instant bookedStartTime,
    Instant bookedEndTime,
    String bookingToken
) { 

}
