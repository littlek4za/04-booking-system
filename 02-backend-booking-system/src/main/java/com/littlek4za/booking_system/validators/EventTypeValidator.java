package com.littlek4za.booking_system.validators;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;

import com.littlek4za.booking_system.models.EventType;
import com.littlek4za.booking_system.validators.annotations.ValidEventType;

public class EventTypeValidator implements ConstraintValidator<ValidEventType, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) return false; // or true if optional
        return Arrays.stream(EventType.values())
                     .anyMatch(e -> e.name().equals(value));
    }
}