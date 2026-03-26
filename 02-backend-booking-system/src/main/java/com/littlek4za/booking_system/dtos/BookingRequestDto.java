package com.littlek4za.booking_system.dtos;

import jakarta.validation.constraints.NotNull;

public record BookingRequestDto(

    @NotNull
    Long invitationId,
    
    String bookedStartTime,

    String email,
    String firstName,
    String lastName
) {

}
