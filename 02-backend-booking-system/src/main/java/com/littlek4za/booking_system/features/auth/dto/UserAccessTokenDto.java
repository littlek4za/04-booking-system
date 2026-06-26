package com.littlek4za.booking_system.features.auth.dto;

import java.time.Instant;

import com.littlek4za.booking_system.features.auth.model.TokenType;

public record UserAccessTokenDto(
    String accessToken,
    Instant expiresAt,
    TokenType tokenType) {

}
