package com.littlek4za.booking_system.validators.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.littlek4za.booking_system.validators.SlotIncludeModeValidator;

import java.lang.annotation.ElementType;

@Documented
@Constraint(validatedBy = SlotIncludeModeValidator.class)
@Target({ ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidSlotIncludeMode {
    String message() default "Invalid Slot Include Mode Type";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default{};
}
