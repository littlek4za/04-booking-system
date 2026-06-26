package com.littlek4za.booking_system.features.booking.dto;

import java.time.Instant;

import com.littlek4za.booking_system.features.booking.model.BookingStatus;

public record AttendeeBookingResponseDto(
    Long bookingId,
    String attendeeLastName,
    String attendeeFirstName,
    boolean isGuest,
    Instant bookedStartTime,
    Instant bookedEndTime,
    String bookingToken,
    Instant bookedAt,
    BookingStatus bookingStatus,
    String eventName,
    String eventDescription,
    String slotName,
    String slotDescription,
    String organizerEmail,
    String attendeeEmail,
    String eventLocationAddress,
    Double latitude,
    Double longitude) {

}
