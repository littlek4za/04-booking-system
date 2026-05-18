package com.littlek4za.booking_system.dtos;

import java.time.Instant;

import com.littlek4za.booking_system.models.BookingStatus;

public record AttendeeBookingResponseDto(
    Long bookingId,
    String attendeeUsername,
    String attendeeLastName,
    String attendeeFirstName,
    String guestAttendeeLastName,
    String guestAttendeeFirstName,
    boolean isGuest,
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
