package com.littlek4za.booking_system.validators;

import com.littlek4za.booking_system.dtos.BookingRequestDto;
import com.littlek4za.booking_system.validators.annotations.ValidBookingRequest;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class BookingRequestValidator implements ConstraintValidator<ValidBookingRequest, BookingRequestDto> {

    @Override
    public boolean isValid(BookingRequestDto dto, ConstraintValidatorContext context) {
        boolean startTimeNull = dto.bookedStartTime() == null;
        boolean endTimeNull = dto.bookedEndTime() == null;
        if (startTimeNull != endTimeNull) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("bookedStartTime and bookedEndTime must both be provided")
                    .addPropertyNode("bookedStartTime").addConstraintViolation();
            return false;
        }
        if (!startTimeNull && !endTimeNull && dto.bookedStartTime().isAfter(dto.bookedEndTime())) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("bookedStartTime cannot later than bookedEndTime")
                    .addPropertyNode("bookedStartTime").addConstraintViolation();
            return false;
        }
        if (!startTimeNull && !endTimeNull && dto.bookedStartTime().equals(dto.bookedEndTime())) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("bookedStartTime cannot equal to bookedEndTime")
                    .addPropertyNode("bookedStartTime").addConstraintViolation();
            return false;
        }
        return true;
    }

}
