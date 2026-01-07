package com.littlek4za.booking_system.security;

import java.util.Set;

public class AuthUserPrincipal {

    private final Long id;
    private final String username;
    private final String email;
    private final Set<String> roles;

    public AuthUserPrincipal(Long id, String username, String email, Set<String> roles) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.roles = roles;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public Set<String> getRoles() {
        return roles;
    }
    
}
