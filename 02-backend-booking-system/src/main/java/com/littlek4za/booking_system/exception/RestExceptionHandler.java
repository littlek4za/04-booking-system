package com.littlek4za.booking_system.exception;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.littlek4za.booking_system.dtos.ErrorResponseDto;
import com.littlek4za.booking_system.dtos.FieldErrorDto;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class RestExceptionHandler {

        @ExceptionHandler(value = { AppException.class })
        @ResponseBody
        public ResponseEntity<ErrorResponseDto> handleAppException(AppException ex, HttpServletRequest request) {
                log.warn(
                                "REST EXCEPTION HANDLER error: status={}, error={}, message={}, path={}",
                                ex.getHttpStatus(),
                                ex.getHttpStatus().value(),
                                ex.getMessage(),
                                request.getRequestURI());
                return ResponseEntity
                                .status(ex.getHttpStatus())
                                .body(ErrorResponseDto.create(
                                        ex.getHttpStatus(),
                                        ex.getMessage(),
                                        request.getRequestURI(),
                                        null
                                        ));
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
                                .body(ErrorResponseDto.create(
                                                HttpStatus.BAD_REQUEST,
                                                "Validation Error",
                                                request.getRequestURI(),
                                                fieldErrorList));
        }

        @ExceptionHandler(IllegalStateException.class)
        public ResponseEntity<ErrorResponseDto> handleIllegalState(IllegalStateException ex, HttpServletRequest request) {
                log.warn(
                                "REST EXCEPTION HANDLER error: status={}, error={}, message={}, path={}",
                                HttpStatus.BAD_REQUEST,
                                HttpStatus.BAD_REQUEST.value(),
                                ex.getMessage(),
                                request.getRequestURI());
                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(ErrorResponseDto.create(
                                        HttpStatus.BAD_REQUEST,
                                        ex.getMessage(),
                                        request.getRequestURI(),
                                        null
                                        ));
        }
       
}
