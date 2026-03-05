package com.littlek4za.booking_system.dtos;

import java.time.Instant;

public record InvitationValidationResponseDto(
    boolean valid,
    boolean requiredLogin,
    String token,
    String eventTitle,
    Instant expiresAt,
    String reason
) {

}
