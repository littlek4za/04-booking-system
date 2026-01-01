package com.littlek4za.booking_system.config;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.littlek4za.booking_system.dto.ErrorResponseDto;
import com.littlek4za.booking_system.exception.AppException;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class RestExceptionHandler {

        @ExceptionHandler(value = { AppException.class })
        @ResponseBody
        public ResponseEntity<ErrorResponseDto> handleRestException(AppException ex, HttpServletRequest request) {
                log.warn(
                                "mylog Handled AppException: status={}, error={}, message={}, path={}",
                                ex.getHttpStatus(),
                                ex.getHttpStatus().value(),
                                ex.getMessage(),
                                request.getRequestURI());
                return ResponseEntity
                                .status(ex.getHttpStatus())
                                .body(new ErrorResponseDto(
                                                ex.getHttpStatus().value(),
                                                ex.getHttpStatus().getReasonPhrase(),
                                                ex.getMessage(),
                                                Instant.now(),
                                                request.getRequestURI()));
        }

        @ExceptionHandler(value = { MethodArgumentNotValidException.class })
        @ResponseBody
        public ResponseEntity<Map<String, List<String>>> handleValidationErrors(MethodArgumentNotValidException ex) {
                Map<String, List<String>> errors = new HashMap<>();

                ex.getBindingResult().getFieldErrors().forEach(error -> {
                        errors
                                        .computeIfAbsent(error.getField(), k -> new ArrayList<>())
                                        .add(error.getDefaultMessage());
                });

                return ResponseEntity.badRequest().body(errors);
        }

        //For Enum mismatch(from AI to study and test)
        @ExceptionHandler(value = { HttpMessageNotReadableException.class })
        @ResponseBody
        public ResponseEntity<Map<String, List<String>>> handleJsonParseErrors(HttpMessageNotReadableException ex) {
                Map<String, List<String>> errors = new HashMap<>();

                Throwable cause = ex.getCause();
                if (cause instanceof com.fasterxml.jackson.databind.exc.InvalidFormatException invalidFormatEx) {
                        // Get the field that failed
                        String fieldName = invalidFormatEx.getPath().get(0).getFieldName();
                        // Build a friendly message
                        String targetType = invalidFormatEx.getTargetType().getSimpleName();
                        String allowedValues = "";
                        if (invalidFormatEx.getTargetType().isEnum()) {
                                allowedValues = Arrays.stream(invalidFormatEx.getTargetType().getEnumConstants())
                                                .map(Object::toString)
                                                .collect(Collectors.joining(", "));
                        }

                        String message = invalidFormatEx.getTargetType().isEnum()
                                        ? "Must be one of: " + allowedValues
                                        : "Invalid value for type " + targetType;

                        errors.put(fieldName, List.of(message));
                } else {
                        // fallback: generic error
                        errors.put("request", List.of("Malformed JSON or invalid input"));
                }

                return ResponseEntity.badRequest().body(errors);
        }
}
