package com.littlek4za.booking_system.security;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.littlek4za.booking_system.exception.AppException;
import com.littlek4za.booking_system.exception.model.ErrorCode;
import com.littlek4za.booking_system.models.TokenType;

import lombok.extern.slf4j.Slf4j;

// Read information from SecurityContext
@Slf4j
@Component
public class SecurityUtil {

    public AuthPrincipal getCurrentPrincipal() {
        Authentication auth = SecurityContextHolder
                .getContext()
                .getAuthentication();
        if (auth == null
                || !auth.isAuthenticated()
                || auth instanceof AnonymousAuthenticationToken
                || !(auth.getPrincipal() instanceof AuthPrincipal principal)) {
            return null;
        }

        return principal;
    }

    // strict getter
    public AuthPrincipal requirePrincipal() {
        AuthPrincipal principal = getCurrentPrincipal();
        if (principal == null) {
            throw new AppException("Authentication required", HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED);
        }

        return principal;
    }

    public UserPrincipal requireUserPrincipal() {
        AuthPrincipal principal = requirePrincipal();

        if (!(principal instanceof UserPrincipal userPrincipal)) {
            throw new AppException("User access required", HttpStatus.FORBIDDEN, ErrorCode.FORBIDDEN);
        }

        return userPrincipal;
    }

    public GuestPrincipal requireGuestPrincipal() {
        AuthPrincipal principal = requirePrincipal();

        if (!(principal instanceof GuestPrincipal guestPrincipal)) {
            throw new AppException("Guest access required", HttpStatus.FORBIDDEN, ErrorCode.FORBIDDEN);
        }

        return guestPrincipal;
    }

    public Long requireUserId() {
        return requireUserPrincipal().getId();
    }

    public String requireEmail() {
        return requirePrincipal().getEmail();
    }

    public String requireBookingToken() {

        GuestPrincipal guestPrincipal = requireGuestPrincipal();

        if (guestPrincipal.getTokenType() != TokenType.GUEST_BOOKING_VIEW) {
            throw new AppException(
                    "Guest booking view access required",
                    HttpStatus.FORBIDDEN,
                    ErrorCode.FORBIDDEN);
        }

        String bookingToken = guestPrincipal.getBookingToken();

        if (bookingToken == null || bookingToken.isBlank()) {
            throw new AppException("Booking token required", HttpStatus.FORBIDDEN, ErrorCode.FORBIDDEN);
        }
        return bookingToken;
    }

    // getter
    public Long getUserIdOrNull() {
        AuthPrincipal principal = getCurrentPrincipal();

        if (principal instanceof UserPrincipal user) {
            return user.getId();
        }

        return null;
    }

    // boolean check

    public boolean isAuthenticated() {
        return getCurrentPrincipal() != null;
    }

    public boolean isUser() {
        return getCurrentPrincipal() instanceof UserPrincipal;
    }

    public boolean isGuest() {
        return getCurrentPrincipal() instanceof GuestPrincipal;
    }

    public boolean isGuestBookingView() {
        AuthPrincipal principal = getCurrentPrincipal();
        return principal != null
                && principal.getTokenType() == TokenType.GUEST_BOOKING_VIEW;
    }

    public boolean isGuestBookingCreate() {
        AuthPrincipal principal = getCurrentPrincipal();
        return principal != null
                && principal.getTokenType() == TokenType.GUEST_BOOKING_CREATE;
    }

}
