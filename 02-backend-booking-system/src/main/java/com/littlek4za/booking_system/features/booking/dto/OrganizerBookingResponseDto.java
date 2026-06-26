package com.littlek4za.booking_system.features.booking.dto;

import java.time.Instant;

import com.littlek4za.booking_system.features.booking.model.BookingStatus;
import com.littlek4za.booking_system.features.event.dto.EventResponseDto;
import com.littlek4za.booking_system.features.slot.dto.SlotResponseDto;

public record OrganizerBookingResponseDto(
    Long bookingId,
    String attendeeLastName,
    String attendeeFirstName,
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
