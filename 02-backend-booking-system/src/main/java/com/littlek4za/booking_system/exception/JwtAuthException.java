package com.littlek4za.booking_system.exception;

import org.springframework.http.HttpStatus;

public class JwtAuthException extends RuntimeException{

    private final HttpStatus httpStatus;

    public JwtAuthException(String message, HttpStatus httpStatus){
        super(message);
        this.httpStatus = httpStatus;
    }

    public HttpStatus getHttpStatus(){
        return this.httpStatus;
    }

}
