package com.littlek4za.booking_system.security;

import java.io.IOException;
import java.time.Instant;
import java.util.Set;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.littlek4za.booking_system.dto.ErrorResponseDto;
import com.littlek4za.booking_system.exception.JwtAuthException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final UserAuthProvider userAuthProvider;
    private final ObjectMapper objectMapper;

    public JwtAuthFilter(UserAuthProvider userAuthProvider, ObjectMapper objectMapper) {
        this.userAuthProvider = userAuthProvider;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader((HttpHeaders.AUTHORIZATION));
        String path = request.getServletPath();
        Set<String> ignorePaths = Set.of(
                "/api/v1/login",
                "/api/v1/register");

        if (ignorePaths.contains(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (authHeader != null) {
            String[] authElements = authHeader.split(" ");

            if (authElements.length == 2 && "Bearer".equals(authElements[0])) {
                try {
                    SecurityContextHolder.getContext()
                            .setAuthentication(userAuthProvider.validateTokenStrongly(authElements[1]));
                } catch (JWTVerificationException e) {
                    SecurityContextHolder.clearContext();
                    log.warn("mylog JWT Verfication failed: {}", e.getMessage());
                    writeJson(response, HttpStatus.UNAUTHORIZED, e.getMessage(), request.getRequestURI());
                    return;
                } catch (JwtAuthException e) {
                    SecurityContextHolder.clearContext();
                    log.warn("mylog JWT Auth Exception: {}", e.getMessage());
                    writeJson(response, e.getHttpStatus(), e.getMessage(), request.getRequestURI());
                    return;
                }
            }
        }

        filterChain.doFilter(request, response);

    }

    private void writeJson(HttpServletResponse response, HttpStatus httpStatus, String message, String path)
            throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");

        ErrorResponseDto errorResponseDto = new ErrorResponseDto(
                httpStatus.value(),
                httpStatus.getReasonPhrase(),
                message,
                Instant.now(),
                path,
                null);

        response.getWriter().write(objectMapper.writeValueAsString(errorResponseDto));
    }

}
