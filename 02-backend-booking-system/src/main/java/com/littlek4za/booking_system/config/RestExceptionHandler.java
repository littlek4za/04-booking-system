package com.littlek4za.booking_system.config;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.littlek4za.booking_system.dto.ErrorResponseDto;
import com.littlek4za.booking_system.dto.FieldErrorDto;
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
                                                request.getRequestURI(),
                                                null));
        }

        @ExceptionHandler(value = { MethodArgumentNotValidException.class })
        @ResponseBody
        public ResponseEntity<ErrorResponseDto> handleValidationErrors(MethodArgumentNotValidException ex,
                        HttpServletRequest request) {
                List<FieldErrorDto> fieldErrorList = ex.getBindingResult()
                                .getFieldErrors()
                                .stream()
                                .map(error -> new FieldErrorDto(error.getField(), error.getDefaultMessage()))
                                .collect(Collectors.toList());

                return ResponseEntity.badRequest()
                                .body(new ErrorResponseDto(
                                                HttpStatus.BAD_REQUEST.value(),
                                                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                                                "Validation Error",
                                                Instant.now(),
                                                request.getRequestURI(),
                                                fieldErrorList));
        }
       
}
