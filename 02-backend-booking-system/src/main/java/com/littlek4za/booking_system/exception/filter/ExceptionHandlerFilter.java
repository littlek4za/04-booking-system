package com.littlek4za.booking_system.exception.filter;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.littlek4za.booking_system.exception.dto.ErrorResponseDto;
import com.littlek4za.booking_system.exception.model.ErrorCode;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExceptionHandlerFilter extends OncePerRequestFilter {

    ObjectMapper objectMapper;

    public ExceptionHandlerFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        }  catch(JWTVerificationException e) {
            writeErrorResponse(response, HttpStatus.UNAUTHORIZED, e.getMessage(),ErrorCode.TOKEN_INVALID, request);
        } catch (JwtAuthFilterException e) { // custom filter
            writeErrorResponse(response, e.getHttpStatus(), e.getMessage(),ErrorCode.USER_NOT_FOUND, request);
        }  
        // catch (Exception e) {
        //     log.error("EXCEPTION HANDLER FILTER error: ", e);
        //     writeErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR, "Internal System Error",ErrorCode.INTERNAL_ERROR, request);
        // }
    }

    private void writeErrorResponse(HttpServletResponse response, HttpStatus status, String message, ErrorCode code,
            HttpServletRequest request) throws IOException {

        response.setStatus(status.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        ErrorResponseDto errorResponseDto = ErrorResponseDto.create(
                status,
                message,
                code,
                request.getRequestURI(),
                null);

        response.getWriter().write(objectMapper.writeValueAsString(errorResponseDto));
    }
}