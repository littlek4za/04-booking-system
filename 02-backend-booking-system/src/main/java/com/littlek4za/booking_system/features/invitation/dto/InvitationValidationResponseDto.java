package com.littlek4za.booking_system.features.invitation.dto;

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
