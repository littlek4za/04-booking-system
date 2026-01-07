package com.littlek4za.booking_system.customValidator;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;

import com.littlek4za.booking_system.models.SlotType;

public class SlotTypeValidator implements ConstraintValidator<ValidSlotType, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) return false; // or true if optional
        return Arrays.stream(SlotType.values())
                     .anyMatch(e -> e.name().equals(value));
    }
}