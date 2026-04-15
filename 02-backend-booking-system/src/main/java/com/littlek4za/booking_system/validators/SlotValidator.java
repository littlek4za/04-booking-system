package com.littlek4za.booking_system.validators;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.littlek4za.booking_system.dtos.SlotRequestDto;
import com.littlek4za.booking_system.entities.Slot;
import com.littlek4za.booking_system.exception.AppException;
import com.littlek4za.booking_system.exception.model.ErrorCode;
import com.littlek4za.booking_system.models.EventType;

@Component
public class SlotValidator {

    private final BusinessDaysHoursValidator businessDaysHoursValidator;
    private final FlexiblaDaysHoursValidator flexiblaDaysHoursValidator;

    public SlotValidator(BusinessDaysHoursValidator businessDaysHoursValidator,
            FlexiblaDaysHoursValidator flexiblaDaysHoursValidator) {
        this.businessDaysHoursValidator = businessDaysHoursValidator;
        this.flexiblaDaysHoursValidator = flexiblaDaysHoursValidator;
    }

    public void validate(EventType eventType, SlotRequestDto dto) {
        if (EventType.FIXED.equals(eventType)) {
            validateFixedSlot(dto);
        } else if (EventType.FLEXIBLE.equals(eventType)) {
            validateFlexibleSlot(dto);
            this.flexiblaDaysHoursValidator.validate(eventType, dto.flexibleDaysHours());
        } else if (EventType.BUSINESS.equals(eventType)) {
            validateBusinessSlot(dto);
            this.businessDaysHoursValidator.validate(eventType, dto.businessDaysHours());

        } else {
            throw new AppException(
                    "Event type invalid",
                    HttpStatus.BAD_REQUEST,
                    ErrorCode.EVENT_TYPE_INVALID);
        }

    }

    private void validateFixedSlot(SlotRequestDto dto) {

        if (dto.slotStartTime() == null || dto.slotEndTime() == null) {
            throw new AppException("Fixed Type requires input of slot start time and slot end time",
                    HttpStatus.BAD_REQUEST, ErrorCode.SLOT_CONFIGURATION_INVALID);
        }

        if (!dto.slotEndTime().isAfter(dto.slotStartTime())) {
            throw new AppException("Slot end time must be after slot start time", HttpStatus.BAD_REQUEST, ErrorCode.SLOT_CONFIGURATION_INVALID);
        }

        if (dto.maxBookPerInterval() == null || dto.maxBookPerInterval() <= 0) {
            throw new AppException("Fixed Type requires maxBookPerInterval input to be larger than 0",
                    HttpStatus.BAD_REQUEST, ErrorCode.SLOT_CONFIGURATION_INVALID);
        }

        if (dto.slotIntervalMinutes() != null) {
            throw new AppException("Fixed Type requires slotIntervalMinutes input to be NULL", HttpStatus.BAD_REQUEST, ErrorCode.SLOT_CONFIGURATION_INVALID);
        }

        if (dto.businessDaysHours() != null) {
            throw new AppException("Fixed Type requires businessDaysHours input to be NULL", HttpStatus.BAD_REQUEST, ErrorCode.SLOT_CONFIGURATION_INVALID);
        }

        if (dto.flexibleDaysHours() != null) {
            throw new AppException("Fixed Type requires flexibleDaysHours input to be NULL", HttpStatus.BAD_REQUEST, ErrorCode.SLOT_CONFIGURATION_INVALID);
        }

        if (dto.slotFrequencyIntervalMinutes() != null) {
            throw new AppException("Fixed Type requires slotFrequencyIntervalMinutes input to be NULL",
                    HttpStatus.BAD_REQUEST, ErrorCode.SLOT_CONFIGURATION_INVALID);
        }

        if (dto.businessAllowOt() != null) {
            throw new AppException("Fixed Type requires businessAllowOt to be NULL",
                    HttpStatus.BAD_REQUEST, ErrorCode.SLOT_CONFIGURATION_INVALID);
        }

        if (dto.businessTimeZone() != null) {
            throw new AppException("Fixed Type requires businessTimeZone to be NULL",
                    HttpStatus.BAD_REQUEST, ErrorCode.SLOT_CONFIGURATION_INVALID);
        }
    }

    private void validateFlexibleSlot(SlotRequestDto dto) {
        if (dto.slotStartTime() != null || dto.slotEndTime() != null) {
            throw new AppException("Flexible Type requires input of slot start time and slot end time to be NULL",
                    HttpStatus.BAD_REQUEST, ErrorCode.SLOT_CONFIGURATION_INVALID);
        }

        if (dto.maxBookPerInterval() != null) {
            throw new AppException("Flexible Type requires maxBookPerInterval input to be NULL",
                    HttpStatus.BAD_REQUEST, ErrorCode.SLOT_CONFIGURATION_INVALID);
        }

        if (dto.slotIntervalMinutes() == null || dto.slotIntervalMinutes() <= 0) {
            throw new AppException("Flexible Type requires slotIntervalMinutes input to be larger than 0",
                    HttpStatus.BAD_REQUEST, ErrorCode.SLOT_CONFIGURATION_INVALID);
        }

        if (dto.slotIntervalMinutes() % 5 != 0) {
            throw new AppException("Flexible Type requires slotIntervalMinutes input to be divisible by 5",
                    HttpStatus.BAD_REQUEST, ErrorCode.SLOT_CONFIGURATION_INVALID);
        }

        if (dto.businessDaysHours() != null) {
            throw new AppException("Flexible Type requires businessDaysHours input to be NULL", HttpStatus.BAD_REQUEST, ErrorCode.SLOT_CONFIGURATION_INVALID);
        }

        if (dto.flexibleDaysHours() == null) {
            throw new AppException("Flexible Type requires input for flexibleDaysHours", HttpStatus.BAD_REQUEST, ErrorCode.SLOT_CONFIGURATION_INVALID);
        }

        if (dto.slotFrequencyIntervalMinutes() == null || dto.slotFrequencyIntervalMinutes() <= 0
                || dto.slotFrequencyIntervalMinutes() > 1440) {
            throw new AppException("Flexible Type requires slotFrequencyIntervalMinutes input to be between 0 and 1440",
                    HttpStatus.BAD_REQUEST, ErrorCode.SLOT_CONFIGURATION_INVALID);
        }

        if (dto.businessAllowOt() != null) {
            throw new AppException("Flexible Type requires businessAllowOt to be NULL",
                    HttpStatus.BAD_REQUEST, ErrorCode.SLOT_CONFIGURATION_INVALID);
        }

        if (dto.businessTimeZone() != null) {
            throw new AppException("Flexible Type requires businessTimeZone to be NULL",
                    HttpStatus.BAD_REQUEST, ErrorCode.SLOT_CONFIGURATION_INVALID);
        }
    }

    private void validateBusinessSlot(SlotRequestDto dto) {
        if (dto.slotStartTime() != null || dto.slotEndTime() != null) {
            throw new AppException("Business Type requires input of slot start time and slot end time to be NULL",
                    HttpStatus.BAD_REQUEST, ErrorCode.SLOT_CONFIGURATION_INVALID);
        }

        if (dto.maxBookPerInterval() != null) {
            throw new AppException("Business Type requires maxBookPerInterval input to be NULL",
                    HttpStatus.BAD_REQUEST, ErrorCode.SLOT_CONFIGURATION_INVALID);
        }

        if (dto.slotIntervalMinutes() == null || dto.slotIntervalMinutes() <= 0) {
            throw new AppException("Business Type requires slotIntervalMinutes input to be larger than 0",
                    HttpStatus.BAD_REQUEST, ErrorCode.SLOT_CONFIGURATION_INVALID);
        }

        if (dto.slotIntervalMinutes() % 5 != 0) {
            throw new AppException("Business Type requires slotIntervalMinutes input to be divisible by 5",
                    HttpStatus.BAD_REQUEST, ErrorCode.SLOT_CONFIGURATION_INVALID);
        }

        if (dto.businessDaysHours() == null) {
            throw new AppException("Business Type requires input for businessDaysHours", HttpStatus.BAD_REQUEST, ErrorCode.SLOT_CONFIGURATION_INVALID);
        }

        if (dto.flexibleDaysHours() != null) {
            throw new AppException("Business Type requires flexibleDaysHours input to be NULL", HttpStatus.BAD_REQUEST, ErrorCode.SLOT_CONFIGURATION_INVALID);
        }

        if (dto.slotFrequencyIntervalMinutes() == null || dto.slotFrequencyIntervalMinutes() <= 0
                || dto.slotFrequencyIntervalMinutes() > 1440) {
            throw new AppException("Business Type requires slotFrequencyIntervalMinutes input to be between 0 and 1440",
                    HttpStatus.BAD_REQUEST, ErrorCode.SLOT_CONFIGURATION_INVALID);
        }

        if (dto.businessAllowOt() == null) {
            throw new AppException("Business Type requires businessAllowOt",
                    HttpStatus.BAD_REQUEST, ErrorCode.SLOT_CONFIGURATION_INVALID);
        }

        if (dto.businessTimeZone() == null) {
            throw new AppException("Business Type requires businessTimeZone",
                    HttpStatus.BAD_REQUEST, ErrorCode.SLOT_CONFIGURATION_INVALID);
        }
    }

    public void validateForUpdate(EventType eventType, SlotRequestDto dto, Slot slot, Long bookingsCount) {

        if (EventType.FIXED.equals(eventType) && bookingsCount >= 1) {
            validateFixedSlotForUpdate(dto, slot);
        }
    }

    private void validateFixedSlotForUpdate(SlotRequestDto dto, Slot slot) {
        if (!dto.slotStartTime().equals(slot.getSlotStartTime())) {
            throw new AppException(
                    "Cannot update slotStartTime for fixed type event's if bookingsCount is larger than 0",
                    HttpStatus.BAD_REQUEST, ErrorCode.SLOT_UPDATE_NOT_ALLOWED);
        }

        if (!dto.slotEndTime().equals(slot.getSlotEndTime())) {
            throw new AppException("Cannot update slotEndTime for fixed type event's if bookingsCount is larger than 0",
                    HttpStatus.BAD_REQUEST, ErrorCode.SLOT_UPDATE_NOT_ALLOWED);
        }
    }
}
