package com.littlek4za.booking_system.exception.filter;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.web.filter.OncePerRequestFilter;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.littlek4za.booking_system.dtos.ErrorResponseDto;

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
        } catch (InsufficientAuthenticationException | JWTVerificationException e) {
            writeErrorResponse(response, HttpStatus.UNAUTHORIZED, e.getMessage(), request);
        } catch (JwtAuthFilterException e) {
            writeErrorResponse(response, e.getHttpStatus(), e.getMessage(), request);
        } catch (AccessDeniedException e) {
            writeErrorResponse(response, HttpStatus.FORBIDDEN, e.getMessage(), request);
        } catch (Exception e) {
            log.error("EXCEPTION HANDLER FILTER error: ", e);
            writeErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR, "Internal System Error", request);
        }
    }

    private void writeErrorResponse(HttpServletResponse response, HttpStatus status, String message,
            HttpServletRequest request) throws IOException {

        response.setStatus(status.value());
        response.setContentType("application/json");

        ErrorResponseDto errorResponseDto = ErrorResponseDto.create(
                status,
                message,
                request.getRequestURI(),
                null);

        response.getWriter().write(objectMapper.writeValueAsString(errorResponseDto));
    }
}