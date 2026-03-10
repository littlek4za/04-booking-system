package com.littlek4za.booking_system.dtos;

import java.time.Instant;

import com.littlek4za.booking_system.entities.Slot;

public record BookingResponseDto(
    String username,
    String lastName,
    String firstName,
    String email,
    Slot slot,
    Instant bookedStartTime,
    Instant bookedEndTime,
    String bookingToken
) { 

}
