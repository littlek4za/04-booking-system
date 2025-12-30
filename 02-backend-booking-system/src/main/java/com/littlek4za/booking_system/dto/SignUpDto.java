package com.littlek4za.booking_system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SignUpDto(

    @NotBlank(message = "is required")
    @Size(min = 3, max = 30, message = "must be between 3 and 30 characters")
    @Pattern(
        regexp = "^[a-zA-Z0-9][a-zA-Z0-9!@#$%^&*]{0,28}[a-zA-Z0-9]$",
        message = "Username must be 3-30 characters, only letters, digits, and special character !@#$%^&*; cannot start or end with special character"
    )
    String username,
    
    @NotBlank(message = "is required")
    @Size(min = 8, max = 40, message = "must be between 8 and 40 characters")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*])[A-Za-z\\d!@#$%^&*]{8,}$",
        message = "Password must be 8-40 characters, include uppercase, lowercase, digit, and special character !@#$%^&*"
    )
    String password, 

    @NotBlank(message = "is required")
    @Size(max = 255, message = "must not be more than 255 characters")
    @Pattern(
        regexp = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$",
        message = "Invalid Email Address"
    )
    String email, 

    @NotBlank(message = "is required")
    @Size(min = 1 ,max = 100, message = "First Name must be between 1 and 100 characters")
    String firstName, 

    @NotBlank(message = "is required")
    @Size(min = 1 ,max = 100, message = "Last Name must be between 1 and 100 characters")
    String lastName
) {}
