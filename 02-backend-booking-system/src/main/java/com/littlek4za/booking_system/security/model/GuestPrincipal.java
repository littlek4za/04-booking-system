package com.littlek4za.booking_system.security.model;

import java.util.Set;

import com.littlek4za.booking_system.features.auth.model.RoleType;
import com.littlek4za.booking_system.features.auth.model.TokenType;

public class GuestPrincipal implements AuthPrincipal {

    private final String email;
    private final Set<RoleType> roles;
    private final TokenType tokenType;
    private final String bookingToken;

    public GuestPrincipal(String email, Set<RoleType> roles, TokenType tokenType, String bookingToken) {
        this.email = email;
        this.roles = roles;
        this.tokenType = tokenType;
        this.bookingToken = bookingToken;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public Set<RoleType> getRoles() {
        return roles;
    }

    @Override
    public TokenType getTokenType() {
        return tokenType;
    }

    public String getBookingToken() {
        return bookingToken;
    }

}
