package com.littlek4za.booking_system.dtos;

import java.time.Instant;
import java.util.List;

import org.springframework.http.HttpStatus;

public record ErrorResponseDto(
        int status,
        String error,
        String message,
        Instant timestamp,
        String path,
        List<FieldErrorDto> fieldErrorList) {

    public static ErrorResponseDto create(HttpStatus status, String message, String path, List<FieldErrorDto> fieldErrorList ) {
        return new ErrorResponseDto(
                status.value(),
                status.getReasonPhrase(),
                message,
                Instant.now(),
                path,
                fieldErrorList);
    }
}
