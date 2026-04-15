package com.littlek4za.booking_system.exception.dto;

import java.time.Instant;
import java.util.List;

import org.springframework.http.HttpStatus;

import com.littlek4za.booking_system.exception.model.ErrorCode;

public record ErrorResponseDto(
        int status,
        String error,
        String message,
        ErrorCode code,
        Instant timestamp,
        String path,
        List<FieldErrorDto> fieldErrorList) {

    public static ErrorResponseDto create(HttpStatus status, String message, ErrorCode code, String path, List<FieldErrorDto> fieldErrorList ) {
        return new ErrorResponseDto(
                status.value(),
                status.getReasonPhrase(),
                message,
                code,
                Instant.now(),
                path,
                fieldErrorList);
    }
}
