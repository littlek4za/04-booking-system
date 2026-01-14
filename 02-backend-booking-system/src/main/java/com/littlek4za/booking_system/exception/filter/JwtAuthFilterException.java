package com.littlek4za.booking_system.exception.filter;

import org.springframework.http.HttpStatus;

public class JwtAuthFilterException extends RuntimeException{

    private final HttpStatus httpStatus;

    public JwtAuthFilterException(String message, HttpStatus httpStatus){
        super(message);
        this.httpStatus = httpStatus;
    }

    public HttpStatus getHttpStatus(){
        return this.httpStatus;
    }

}
