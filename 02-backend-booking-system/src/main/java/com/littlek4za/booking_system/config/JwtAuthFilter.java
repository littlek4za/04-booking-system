package com.littlek4za.booking_system.config;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.littlek4za.booking_system.exception.AppException;

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

        String authHeader = request.getHeader((HttpHeaders.AUTHORIZATION));
        String path = request.getServletPath();
        Set<String> ignorePaths = new HashSet<>();
        ignorePaths.add("/login");
        ignorePaths.add("/register");

        if (ignorePaths.contains(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (authHeader != null) {
            String[] authElements = authHeader.split(" ");

            if (authElements.length == 2 && "Bearer".equals(authElements[0])) {
                try {
                    if ("GET".equals(request.getMethod())) {
                        SecurityContextHolder.getContext()
                                .setAuthentication(userAuthProvider.validateToken(authElements[1]));
                    } else {
                        SecurityContextHolder.getContext()
                                .setAuthentication(userAuthProvider.validateTokenStrongly(authElements[1]));
                    }
                } catch (JWTVerificationException e) {
                    SecurityContextHolder.clearContext();
                    log.error("mylog JWTVerificationException: {}", e.getMessage(), e);
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"message\":\"" + e.getMessage() + "\"}");
                    return;
                } catch (AppException e) {
                    SecurityContextHolder.clearContext();
                    log.error("mylog AppException: {}", e.getMessage(), e);
                    response.setStatus(e.getHttpStatus().value());
                    response.setContentType("application/json");
                    response.getWriter().write("{\"message\":\"" + e.getMessage() + "\"}");
                    return;
                } catch (Exception e) {
                    SecurityContextHolder.clearContext();
                    log.error("mylog Exception: {}", e.getMessage(), e);
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"message\":\"" + e.getMessage() + "\"}");
                    return;
                }
            }
        }

        filterChain.doFilter(request, response);

    }
}
