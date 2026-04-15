package com.littlek4za.booking_system.dtos;

import java.util.Set;

import com.littlek4za.booking_system.models.RoleName;

public record UserDto(
    Long id,
    String username,
    String email,
    String firstName,
    String lastName,
    Set<RoleName> roleSet
) {}
