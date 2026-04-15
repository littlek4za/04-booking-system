package com.littlek4za.booking_system.dtos;

import java.time.Instant;

import com.littlek4za.booking_system.models.TokenType;

public record UserAccessTokenDto(
    String accessToken,
    Instant expiresAt,
    TokenType tokenType) {

}
