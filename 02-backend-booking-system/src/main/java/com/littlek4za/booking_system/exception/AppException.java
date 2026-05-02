package com.littlek4za.booking_system.exception;

import org.springframework.http.HttpStatus;

import com.littlek4za.booking_system.exception.model.ErrorCode;

public class AppException extends RuntimeException {

    private final HttpStatus httpStatus;
    private final ErrorCode code;

    public AppException(String message, HttpStatus httpStatus, ErrorCode code){
        super(message);
        this.httpStatus = httpStatus;
        this.code = code;
    }

    public HttpStatus getHttpStatus(){
        return this.httpStatus;
    }

    public ErrorCode getErrorCode(){
        return this.code;
    }

}
