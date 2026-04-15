package com.littlek4za.booking_system.security;

import java.io.IOException;

import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final UserAuthProvider userAuthProvider;

    public JwtAuthFilter(UserAuthProvider userAuthProvider) {
        this.userAuthProvider = userAuthProvider;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String path = request.getServletPath();
        log.debug("JwtAuthFilter triggered for path: {}", path);

        String authHeader = request.getHeader((HttpHeaders.AUTHORIZATION));

        // if header exists, start with Bearer -> try authenticate
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.debug("No Bearer token found. Continuing without authentication.");
            filterChain.doFilter(request, response);
            return;
        }
        try {
            String token = authHeader.substring(7);
            log.debug("Bearer token found, validating...");

            SecurityContextHolder.getContext()
                    .setAuthentication(userAuthProvider.validateTokenStrongly(token));
        } catch (Exception e) {
            log.debug("Token validation failed: {}", e.getMessage());

            // Clear context if something goes wrong
            SecurityContextHolder.clearContext();

            throw e;
        }

        filterChain.doFilter(request, response);
    }

}
