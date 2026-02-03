package com.littlek4za.booking_system.validators.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.littlek4za.booking_system.validators.InvitationRequestValidator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Documented
@Constraint(validatedBy = InvitationRequestValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidInvitationRequest {
    String message() default "Invalid invitation request";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

}
