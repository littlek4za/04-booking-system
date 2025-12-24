package com.littlek4za.booking_system.config;

import java.time.Instant;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.littlek4za.booking_system.dto.ErrorDto;
import com.littlek4za.booking_system.exception.AppException;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class RestExceptionHandler {

    @ExceptionHandler(value = { AppException.class })
    @ResponseBody
    public ResponseEntity<ErrorDto> handleRestException(AppException ex, HttpServletRequest request) {
        log.warn(
                "mylog Handled AppException: status={}, error={}, message={}, path={}",
                ex.getHttpStatus(),
                ex.getHttpStatus().value(),
                ex.getMessage(),
                request.getRequestURI());
        return ResponseEntity
                .status(ex.getHttpStatus())
                .body(new ErrorDto(
                        ex.getHttpStatus().value(),
                        ex.getHttpStatus().getReasonPhrase(),
                        ex.getMessage(),
                        Instant.now(),
                        request.getRequestURI()));
    }

}
