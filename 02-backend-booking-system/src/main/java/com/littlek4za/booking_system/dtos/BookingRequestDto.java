package com.littlek4za.booking_system.dtos;

import java.time.Instant;

import com.littlek4za.booking_system.validators.annotations.ValidBookingRequest;

import jakarta.validation.constraints.NotNull;

@ValidBookingRequest
public record BookingRequestDto(

    @NotNull
    Long slotId,

    @NotNull
    Long invitationId,
    
    Instant bookedStartTime,
    Instant bookedEndTime,
    String email,
    String firstName,
    String lastName
) {

}
