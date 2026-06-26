package com.littlek4za.booking_system.features.invitation.validator;

import java.util.Arrays;

import com.littlek4za.booking_system.features.invitation.model.SlotIncludeMode;
import com.littlek4za.booking_system.features.invitation.validator.annotation.ValidSlotIncludeMode;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class SlotIncludeModeValidator implements ConstraintValidator<ValidSlotIncludeMode, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) return false;
        return Arrays.stream(SlotIncludeMode.values())
                    .anyMatch(e -> e.name().equals(value));
    }

}
