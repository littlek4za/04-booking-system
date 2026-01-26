package com.littlek4za.booking_system.validators;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.littlek4za.booking_system.dtos.SlotRequestDto;
import com.littlek4za.booking_system.exception.AppException;
import com.littlek4za.booking_system.models.EventType;

@Component
public class SlotValidator {

    private final WorkingDaysHoursValidator workingDaysHoursValidator;

    public SlotValidator(WorkingDaysHoursValidator workingDaysHoursValidator) {
        this.workingDaysHoursValidator = workingDaysHoursValidator;
    }

    public void validate(EventType eventType, SlotRequestDto dto) {
        if (EventType.FIXED.equals(eventType)) {
            validateFixedSlot(dto);
        } else if (EventType.FLEXIBLE.equals(eventType)) {
            validateFlexibleSlot(dto);
        } else if (EventType.BUSINESS.equals(eventType)) {
            validateBusinessSlot(dto);
            this.workingDaysHoursValidator.validate(eventType, dto.workingDaysHours());

        } else {
            throw new AppException("Event Type not valid for validation", HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    private void validateFixedSlot(SlotRequestDto dto) {

        if (dto.slotStartTime() == null || dto.slotEndTime() == null) {
            throw new AppException("Fixed Type requires input of slot start time and slot end time",
                    HttpStatus.BAD_REQUEST);
        }

        if (!dto.slotEndTime().isAfter(dto.slotStartTime())) {
            throw new AppException("Slot end time must be after slot start time", HttpStatus.BAD_REQUEST);
        }

        if (dto.maxBook() == null || dto.maxBook() <= 0) {
            throw new AppException("Fixed Type requires maxBook input to be larger than 0", HttpStatus.BAD_REQUEST);
        }

        if (dto.slotIntervalMinutes() != null) {
            throw new AppException("Fixed Type requires slotIntervalMinutes input to be NULL", HttpStatus.BAD_REQUEST);
        }

        if (dto.workingDaysHours() != null) {
            throw new AppException("Fixed Type requires workingDaysHours input to be NULL", HttpStatus.BAD_REQUEST);
        }

        if (dto.slotFrequencyIntervalMinutes() != null) {
            throw new AppException("Fixed Type requires slotFrequencyIntervalMinutes input to be NULL", HttpStatus.BAD_REQUEST);
        }
    }

    private void validateFlexibleSlot(SlotRequestDto dto) {
        if (dto.slotStartTime() == null || dto.slotEndTime() == null) {
            throw new AppException("Flexible Type requires input of slot start time and slot end time",
                    HttpStatus.BAD_REQUEST);
        }

        if (!dto.slotEndTime().isAfter(dto.slotStartTime())) {
            throw new AppException("Slot end time must be after slot start time", HttpStatus.BAD_REQUEST);
        }

        if (dto.maxBook() != null) {
            throw new AppException("Flexible Type requires maxBook input to be NULL", HttpStatus.BAD_REQUEST);
        }

        if (dto.slotIntervalMinutes() == null || dto.slotIntervalMinutes() <= 0) {
            throw new AppException("Flexible Type requires slotIntervalMinutes input to be larger than 0",
                    HttpStatus.BAD_REQUEST);
        }

        if (dto.slotIntervalMinutes() % 5 != 0) {
            throw new AppException("Flexible Type requires slotIntervalMinutes input to be divisible by 5",
                    HttpStatus.BAD_REQUEST);
        }

        if (dto.workingDaysHours() != null) {
            throw new AppException("Flexible Type requires workingDaysHours input to be NULL", HttpStatus.BAD_REQUEST);
        }

        if (dto.slotFrequencyIntervalMinutes() != null) {
            throw new AppException("Flexible Type requires slotFrequencyIntervalMinutes input to be NULL", HttpStatus.BAD_REQUEST);
        }
    }

    private void validateBusinessSlot(SlotRequestDto dto) {
        if (dto.slotStartTime() != null || dto.slotEndTime() != null) {
            throw new AppException("Business Type requires input of slot start time and slot end time to be NULL",
                    HttpStatus.BAD_REQUEST);
        }

        if (dto.maxBook() != null) {
            throw new AppException("Business Type requires maxBook input to be NULL", HttpStatus.BAD_REQUEST);
        }

        if (dto.slotIntervalMinutes() == null || dto.slotIntervalMinutes() <= 0) {
            throw new AppException("Business Type requires slotIntervalMinutes input to be larger than 0",
                    HttpStatus.BAD_REQUEST);
        }

        if (dto.slotIntervalMinutes() % 5 != 0) {
            throw new AppException("Business Type requires slotIntervalMinutes input to be divisible by 5",
                    HttpStatus.BAD_REQUEST);
        }

        if (dto.workingDaysHours() == null) {
            throw new AppException("Business Type requires input of workingDaysHours", HttpStatus.BAD_REQUEST);
        }

        
        if (dto.slotFrequencyIntervalMinutes() == null || dto.slotFrequencyIntervalMinutes() <= 0 || dto.slotFrequencyIntervalMinutes() > 1440) {
            throw new AppException("Business Type requires slotFrequencyIntervalMinutes input to be between 0 and 1440", HttpStatus.BAD_REQUEST);
        }
    }

}
