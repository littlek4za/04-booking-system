package com.littlek4za.booking_system.customValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = SlotTypeValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidSlotType {
    String message() default "Invalid slot type"; // will appear in fieldErrorList
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}