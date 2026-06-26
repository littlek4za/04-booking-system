package com.littlek4za.booking_system.features.guest_access.dto;

import java.time.Instant;

import com.littlek4za.booking_system.features.auth.model.TokenType;

public record GuestAccessTokenDto(
    String accessToken,
    Instant expiresAt,
    TokenType tokenType
) {
    
}
