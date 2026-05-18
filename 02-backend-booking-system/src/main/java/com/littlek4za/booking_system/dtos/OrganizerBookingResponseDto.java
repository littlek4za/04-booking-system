package com.littlek4za.booking_system.dtos;

import java.time.Instant;

import com.littlek4za.booking_system.models.BookingStatus;

public record OrganizerBookingResponseDto(
    Long bookingId,
    String attendeeUsername,
    String attendeeLastName,
    String attendeeFirstName,
    String guestAttendeeLastName,
    String guestAttendeeFirstName,
    boolean isGuest,
    String attendeeEmail,
    SlotResponseDto slot,
    Instant bookedStartTime,
    Instant bookedEndTime,
    String bookingToken,
    Instant bookedAt,
    BookingStatus bookingStatus,
    EventResponseDto eventResponseDto

) { 

}
