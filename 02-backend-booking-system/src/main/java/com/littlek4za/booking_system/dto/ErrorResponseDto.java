package com.littlek4za.booking_system.dto;

import java.time.Instant;

public record ErrorResponseDto(
    int status,
    String error,
    String message,
    Instant timestamp,
    String path
    ) {

}
