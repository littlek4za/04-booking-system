package com.littlek4za.booking_system.security;

import java.util.Set;

import com.littlek4za.booking_system.models.RoleType;
import com.littlek4za.booking_system.models.TokenType;

public class UserPrincipal implements AuthPrincipal{

    private final Long id;
    private final String username;
    private final String email;
    private final Set<RoleType> roles;
    private final TokenType tokenType;

    public UserPrincipal(Long id, String username, String email, Set<RoleType> roles, TokenType tokenType) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.roles = roles;
        this.tokenType = tokenType;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
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
    public TokenType getTokenType(){
        return tokenType;
    }
    
}
