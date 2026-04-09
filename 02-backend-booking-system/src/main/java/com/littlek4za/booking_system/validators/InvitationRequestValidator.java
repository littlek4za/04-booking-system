package com.littlek4za.booking_system.validators;

import java.time.Instant;

import com.littlek4za.booking_system.dtos.InvitationRequestDto;
import com.littlek4za.booking_system.models.SlotIncludeMode;
import com.littlek4za.booking_system.validators.annotations.ValidInvitationRequest;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class InvitationRequestValidator implements ConstraintValidator<ValidInvitationRequest, InvitationRequestDto> {

    @Override
    public boolean isValid(InvitationRequestDto dto, ConstraintValidatorContext context) {
        context.disableDefaultConstraintViolation();
        boolean valid = true;

        // expiresAt
        if (dto.expiresAt() != null && dto.expiresAt().isBefore(Instant.now())) {
            valid = false;
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("expiresAt cannot be in the past")
                    .addPropertyNode("expiresAt").addConstraintViolation();
        }

        // maxUsage & maxUsagePerIdentity
        if (dto.maxUsage() != null && dto.maxUsagePerIdentity() != null){
            if(dto.maxUsagePerIdentity() > dto.maxUsage()) {
                valid = false;
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("maxUsagePerIdentity cannot exceed maxUsage")
                        .addPropertyNode("maxUsagePerIdentity").addConstraintViolation();
            }
        }

        // slotIncludeMode & slotIdList
        if(dto.slotIncludeMode() == null) {
            valid = false;
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("slotIncludeMode must not be null")
                    .addPropertyNode("slotIncludeMode").addConstraintViolation();
        } else if (dto.slotIncludeMode().equals(SlotIncludeMode.SELECTED.toString())){
            if(dto.slotIdList() == null || dto.slotIdList().isEmpty()){
                valid = false;
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("slotIdList is required when slotIncludeMode is SELECTED")
                    .addPropertyNode("slotIdList").addConstraintViolation();
            }
        }

        return valid;
    }

}
