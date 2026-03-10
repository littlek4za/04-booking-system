package com.littlek4za.booking_system.validators.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.auth0.jwt.interfaces.Payload;
import com.littlek4za.booking_system.validators.BookingRequestValidator;

import jakarta.validation.Constraint;

@Documented
@Constraint(validatedBy = BookingRequestValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidBookingRequest {
    String message() default "Invalid booking request";
    Class<?>[] groups() default{};
    Class<? extends Payload>[] payload() default{};
}
