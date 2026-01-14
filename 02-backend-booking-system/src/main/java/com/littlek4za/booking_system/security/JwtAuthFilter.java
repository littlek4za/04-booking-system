package com.littlek4za.booking_system.security;

import java.io.IOException;
import java.util.Set;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.InsufficientAuthenticationException;
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
        Set<String> ignorePaths = Set.of(
                "/api/v1/login",
                "/api/v1/register");

        if (ignorePaths.contains(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader((HttpHeaders.AUTHORIZATION));

        // to allow without header, will need to declare the path at security config, and jwtauthfilter ignorePaths
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // writeJson(response, HttpStatus.UNAUTHORIZED,
            //         "Missing or invalid Authorization header",
            //         request.getRequestURI());
            // return; 
            throw new InsufficientAuthenticationException("Missing or invalid Authorization header");
        }

        
            String token = authHeader.substring(7);
            SecurityContextHolder.getContext()
                    .setAuthentication(userAuthProvider.validateTokenStrongly(token));
        

        filterChain.doFilter(request, response);
    }

}
