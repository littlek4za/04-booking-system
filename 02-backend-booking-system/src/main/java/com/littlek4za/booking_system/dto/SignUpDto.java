package com.littlek4za.booking_system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SignUpDto(

    @NotBlank(message = "is required")
    @Size(min = 3, message = "must be at least 3 character")
    @Size(max = 30, message = "must not be more than 30 characters")
    @Pattern(
        regexp = "^[a-zA-Z0-9][a-zA-Z0-9!@#$%^&*]{0,28}[a-zA-Z0-9]$",
        message = "Username must be 1-30 characters, only letters, digits, and !@#$%^&*, and cannot start or end with a symbol"
    )
    String username,
    
    @NotBlank(message = "is required")
    @Size(max = 40, message = "must not be more than 40 characters")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\\\d)(?=.*[!@#$%^&*])[A-Za-z\\\\d!@#$%^&*]{8,}$",
        message = "Password must be at least 8 characters, include uppercase, lowercase, digit, and special character (!@#$%^&*)"
    )
    String password, 

    @NotBlank(message = "is required")
    @Size(max = 255, message = "must not be more than 255 characters")
    @Pattern(
        regexp = "^[_A-Za-z0-9-\\\\+]+(\\\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\\\.[A-Za-z0-9]+)*(\\\\.[A-Za-z]{2,})$",
        message = "Invalid Email Address"
    )
    String email, 

    @NotBlank(message = "is required")
    @Size(min = 1, message = "must be at least 1 characters")
    @Size(max = 100, message = "must not be more than 100 characters")
    String firstName, 

    @NotBlank(message = "is required")
    @Size(min = 1, message = "must be at least 1 characters")
    @Size(max = 100, message = "must not be more than 100 characters")
    String lastName
) {}
