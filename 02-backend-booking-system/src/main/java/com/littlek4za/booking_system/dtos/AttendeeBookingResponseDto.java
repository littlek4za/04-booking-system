package com.littlek4za.booking_system.dtos;

import java.time.Instant;

import com.littlek4za.booking_system.models.BookingStatus;

public record AttendeeBookingResponseDto(
    Long id,
    String username,
    String lastName,
    String firstName,
    String guestLastName,
    String guestFirstName,
    boolean isGuest,
    String email,
    Instant bookedStartTime,
    Instant bookedEndTime,
    String bookingToken,
    Instant bookedAt,
    BookingStatus bookingStatus,
    String eventName,
    String slotName,
    String organizerEmail,
    String attendeeEmail,
    String eventLocationAddress,
    Double latitude,
    Double longitude) {

}
