package com.littlek4za.booking_system.security;

import java.io.IOException;

import org.springframework.http.HttpStatus;

import com.littlek4za.booking_system.exception.filter.SecurityBouncerFilterException;
import com.littlek4za.booking_system.exception.model.ErrorCode;
import com.littlek4za.booking_system.services.RedisRiskService;
import com.littlek4za.booking_system.utils.IpResolver;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

public class SecurityBouncerFilter implements Filter {

    private final RedisRiskService riskService;
    private final IpResolver ipResolver;

    public SecurityBouncerFilter(RedisRiskService riskService, IpResolver ipResolver) {
        this.riskService = riskService;
        this.ipResolver = ipResolver;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpServletRequest = (HttpServletRequest) request;

        // URL check
        if("/api/v1/login".equals(httpServletRequest.getRequestURI())) {
            String ip = ipResolver.getClientIp(httpServletRequest);

            if(riskService.isIpBannedForLogin(ip)) {
                throw new SecurityBouncerFilterException("Too many login attempts. Please try again later", HttpStatus.TOO_MANY_REQUESTS, ErrorCode.LOGIN_ATTEMPT_LIMIT_EXCEEDED);
            }
        }

        chain.doFilter(request, response);
    }

}
