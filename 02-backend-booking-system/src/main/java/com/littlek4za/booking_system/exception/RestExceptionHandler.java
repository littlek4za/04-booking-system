package com.littlek4za.booking_system.exception;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.littlek4za.booking_system.exception.dto.ErrorResponseDto;
import com.littlek4za.booking_system.exception.dto.FieldErrorDto;
import com.littlek4za.booking_system.exception.model.ErrorCode;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class RestExceptionHandler {

        @ExceptionHandler(value = { AppException.class })
        public ResponseEntity<ErrorResponseDto> handleAppException(
                        AppException ex,
                        HttpServletRequest request) {
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
                                                ex.getErrorCode(),
                                                request.getRequestURI(),
                                                null));
        }

        @ExceptionHandler(value = { MethodArgumentNotValidException.class })
        public ResponseEntity<ErrorResponseDto> handleValidationErrors(
                        MethodArgumentNotValidException ex,
                        HttpServletRequest request) {

                // Field Error
                List<FieldErrorDto> fieldErrorList = ex.getBindingResult()
                                .getFieldErrors()
                                .stream()
                                .map(error -> new FieldErrorDto(error.getField(), error.getDefaultMessage()))
                                .collect(Collectors.toList());

                // Class Level Error
                List<FieldErrorDto> globalErrorList = ex.getBindingResult()
                                .getGlobalErrors()
                                .stream()
                                .map(error -> new FieldErrorDto(error.getObjectName(), error.getDefaultMessage()))
                                .collect(Collectors.toList());

                // Combine both
                fieldErrorList.addAll(globalErrorList);

                return ResponseEntity.badRequest()
                                .body(ErrorResponseDto.create(
                                                HttpStatus.BAD_REQUEST,
                                                "Validation Error",
                                                ErrorCode.FIELD_VALIDATION_FAILED,
                                                request.getRequestURI(),
                                                fieldErrorList));
        }

        @ExceptionHandler(IllegalStateException.class)
        public ResponseEntity<ErrorResponseDto> handleIllegalStateException(
                        IllegalStateException ex,
                        HttpServletRequest request) {
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
                                                ErrorCode.INVALID_STATE,
                                                request.getRequestURI(),
                                                null));
        }

        @ExceptionHandler(AccessDeniedException.class)
        public ResponseEntity<ErrorResponseDto> handleAccessDeniedException(
                        AccessDeniedException ex,
                        HttpServletRequest request) {

                log.warn(
                                "ACCESS DENIED: status={}, error={}, message={}, path={}",
                                HttpStatus.FORBIDDEN,
                                HttpStatus.FORBIDDEN.value(),
                                ex.getMessage(),
                                request.getRequestURI());

                return ResponseEntity
                                .status(HttpStatus.FORBIDDEN)
                                .body(ErrorResponseDto.create(
                                                HttpStatus.FORBIDDEN,
                                                "Access denied",
                                                ErrorCode.FORBIDDEN,
                                                request.getRequestURI(),
                                                null));
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ErrorResponseDto> handleGenericException(
                        Exception ex,
                        HttpServletRequest request) {
                log.error(
                                "UNEXPECTED ERROR: path={}",
                                request.getRequestURI(),
                                ex);

                return ResponseEntity
                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(ErrorResponseDto.create(
                                                HttpStatus.INTERNAL_SERVER_ERROR,
                                                "Internal System Error",
                                                ErrorCode.INTERNAL_ERROR,
                                                request.getRequestURI(),
                                                null));
        }

}
