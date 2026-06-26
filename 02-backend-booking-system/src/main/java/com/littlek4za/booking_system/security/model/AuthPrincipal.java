package com.littlek4za.booking_system.security.model;

import java.util.Set;

import com.littlek4za.booking_system.features.auth.model.RoleType;
import com.littlek4za.booking_system.features.auth.model.TokenType;

public interface AuthPrincipal {

    String getEmail();
    Set<RoleType> getRoles();
    TokenType getTokenType();

}
