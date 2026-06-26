package com.littlek4za.booking_system.features.auth.dto;

import java.util.Set;

import com.littlek4za.booking_system.features.auth.model.RoleType;

public record UserDto(
    Long id,
    String username,
    String email,
    String firstName,
    String lastName,
    Set<RoleType> roleSet
) {}
