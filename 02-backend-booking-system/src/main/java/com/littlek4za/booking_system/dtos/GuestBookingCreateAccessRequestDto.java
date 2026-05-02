package com.littlek4za.booking_system.dtos;

import jakarta.validation.constraints.NotNull;

public record GuestBookingCreateAccessRequestDto(
    String email,
    String captchaToken,

    @NotNull
    Long invitationId,

    @NotNull
    Long slotId


) {

}
