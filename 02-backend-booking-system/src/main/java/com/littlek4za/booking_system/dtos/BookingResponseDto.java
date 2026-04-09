package com.littlek4za.booking_system.dtos;

import java.time.Instant;

import com.littlek4za.booking_system.models.BookingStatus;

public record BookingResponseDto(
    Long id,
    String username,
    String lastName,
    String firstName,
    String guestLastName,
    String guestFirstName,
    boolean isGuest,
    String email,
    SlotResponseDto slot,
    Instant bookedStartTime,
    Instant bookedEndTime,
    String bookingToken,
    Instant bookedAt,
    BookingStatus bookingStatus
    
) { 

}
