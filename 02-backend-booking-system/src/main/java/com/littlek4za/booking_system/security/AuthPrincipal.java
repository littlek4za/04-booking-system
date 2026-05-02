package com.littlek4za.booking_system.security;

import java.util.Set;

import com.littlek4za.booking_system.models.RoleType;
import com.littlek4za.booking_system.models.TokenType;

public interface AuthPrincipal {

    String getEmail();
    Set<RoleType> getRoles();
    TokenType getTokenType();

}
