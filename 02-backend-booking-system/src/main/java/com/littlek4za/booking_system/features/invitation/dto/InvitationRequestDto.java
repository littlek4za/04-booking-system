package com.littlek4za.booking_system.features.invitation.dto;

import java.time.Instant;
import java.util.List;

import com.littlek4za.booking_system.features.invitation.validator.annotation.ValidInvitationRequest;
import com.littlek4za.booking_system.features.invitation.validator.annotation.ValidSlotIncludeMode;

import jakarta.validation.constraints.NotNull;

@ValidInvitationRequest
public record InvitationRequestDto(

    Instant expiresAt,
    
    Integer maxUsage,
    
    Integer maxUsagePerIdentity,

    @NotNull
    boolean requiredLogin,

    @ValidSlotIncludeMode
    String slotIncludeMode,

    List<Integer> slotIdList

) {

}
