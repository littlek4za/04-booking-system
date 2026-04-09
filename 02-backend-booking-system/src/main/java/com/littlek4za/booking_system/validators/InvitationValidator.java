package com.littlek4za.booking_system.validators;

import java.time.Instant;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.littlek4za.booking_system.entities.Invitation;
import com.littlek4za.booking_system.entities.InvitationUsage;
import com.littlek4za.booking_system.models.ValidationResult;
import com.littlek4za.booking_system.repos.InvitationUsageRepository;

@Component
public class InvitationValidator {

    InvitationUsageRepository invitationUsageRepository;
    

    public InvitationValidator(InvitationUsageRepository invitationUsageRepository) {
        this.invitationUsageRepository = invitationUsageRepository;
    }


    public ValidationResult validateAccess(Invitation invitation, Long userId){
         Instant now = Instant.now();

        if (now.isAfter(invitation.getExpiresAt())) {
            return ValidationResult.fail("TOKEN EXPIRED");
        }

        if (invitation.getMaxUsage() != null && invitation.getUsedCount() >= invitation.getMaxUsage()) {
            return ValidationResult.fail("REACHED MAXIMUM USAGE");
        }

        if (invitation.getMaxUsagePerIdentity() != null) {

            if (userId != null) {
                Optional<InvitationUsage> invitationUsage = invitationUsageRepository
                        .findByUserIdAndInvitationId(userId, invitation.getId());

                int usageCount = invitationUsage.map(u -> u.getUsageCount()).orElse(0);

                if (usageCount >= invitation.getMaxUsagePerIdentity()) {
                    return ValidationResult.fail("REACHED MAXIMUM USAGE PER IDENTITY");
                }
            }
        }

        return ValidationResult.ok();
    }
}
