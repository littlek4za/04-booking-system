package com.littlek4za.booking_system.exception.filter;

import org.springframework.http.HttpStatus;

import com.littlek4za.booking_system.exception.model.ErrorCode;

public class SecurityBouncerFilterException extends RuntimeException {

    private final HttpStatus httpStatus;
    private final ErrorCode errorCode;

    public SecurityBouncerFilterException(String message, HttpStatus httpStatus, ErrorCode errorCode) {
        super(message);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
    }

    public HttpStatus getHttpStatus(){
        return this.httpStatus;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

}
