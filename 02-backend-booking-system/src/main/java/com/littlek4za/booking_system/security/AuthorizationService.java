package com.littlek4za.booking_system.security;

import org.springframework.stereotype.Component;


// For preauth usage
@Component("authz")
public class AuthorizationService {

    private final SecurityUtil securityUtil;

    public AuthorizationService(SecurityUtil securityUtil) {
        this.securityUtil = securityUtil;
    }

    public boolean isUser() {
        return securityUtil.isUser();
    }

    public boolean isGuestBookingView() {
        return securityUtil.isGuestBookingView();
    }

    public boolean isGuestBookingCreate() {
        return securityUtil.isGuestBookingCreate();
    }
}
