package com.littlek4za.booking_system.validators;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = EventTypeValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidEventType {
    String message() default "Invalid event type"; // will appear in fieldErrorList
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}