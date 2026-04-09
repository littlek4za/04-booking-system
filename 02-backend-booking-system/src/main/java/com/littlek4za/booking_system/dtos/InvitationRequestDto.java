package com.littlek4za.booking_system.dtos;

import java.time.Instant;
import java.util.List;

import com.littlek4za.booking_system.validators.annotations.ValidInvitationRequest;
import com.littlek4za.booking_system.validators.annotations.ValidSlotIncludeMode;

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
