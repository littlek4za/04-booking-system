package com.littlek4za.booking_system.validators;

import java.time.Instant;

import org.springframework.stereotype.Component;

import com.littlek4za.booking_system.entities.Invitation;
import com.littlek4za.booking_system.entities.InvitationUsage;
import com.littlek4za.booking_system.exception.model.ErrorCode;
import com.littlek4za.booking_system.models.ValidationResult;
import com.littlek4za.booking_system.repos.InvitationUsageRepository;

@Component
public class InvitationValidator {

    InvitationUsageRepository invitationUsageRepository;
    

    public InvitationValidator(InvitationUsageRepository invitationUsageRepository) {
        this.invitationUsageRepository = invitationUsageRepository;
    }


    public ValidationResult validateAccess(Invitation invitation, Long userId, InvitationUsage invitationUsage){
         Instant now = Instant.now();

        if (now.isAfter(invitation.getExpiresAt())) {
            return ValidationResult.fail("Invitation expired", ErrorCode.INVITATION_EXPIRED);
        }

        if (invitation.getMaxUsage() != null && invitation.getUsedCount() >= invitation.getMaxUsage()) {
            return ValidationResult.fail("This invitation has reached its maximum usage", ErrorCode.INVITATION_MAX_USAGE_REACHED);
        }

        if (invitation.getMaxUsagePerIdentity() != null && userId != null) {

            int usageCount = (invitationUsage != null)
                ? invitationUsage.getUsageCount()
                : 0;

            if (usageCount >= invitation.getMaxUsagePerIdentity()) {
                return ValidationResult.fail("User has reached maximum usage of this invitation", ErrorCode.INVITATION_USER_USAGE_LIMIT_REACHED);
            }

        }

        return ValidationResult.ok();
    }
}
