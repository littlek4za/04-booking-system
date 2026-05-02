package com.littlek4za.booking_system.security;

import java.util.Set;

import com.littlek4za.booking_system.models.RoleType;
import com.littlek4za.booking_system.models.TokenType;

public class GuestPrincipal implements AuthPrincipal {

    private final String email;
    private final Set<RoleType> roles;
    private final TokenType tokenType;

    public GuestPrincipal(String email, Set<RoleType> roles, TokenType tokenType) {
        this.email = email;
        this.roles = roles;
        this.tokenType = tokenType;
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

}
