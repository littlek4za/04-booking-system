package com.littlek4za.booking_system.dto;

import java.time.Instant;

public record ErrorDto(
    int status,
    String error,
    String message,
    Instant timestamp,
    String path
    ) {

}
