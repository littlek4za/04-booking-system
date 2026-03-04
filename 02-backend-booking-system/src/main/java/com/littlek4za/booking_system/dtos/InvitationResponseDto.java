package com.littlek4za.booking_system.dtos;

import java.time.Instant;
import java.util.UUID;

import com.littlek4za.booking_system.models.SlotIncludeMode;

public record InvitationResponseDto(
    Long id,
    Long eventId,
    Long userId,
    Instant expiresAt,
    Integer maxUsage,
    int usedCount,
    UUID accessToken,
    SlotIncludeMode slotIncludeMode,
    boolean requiredLogin,
    Integer maxUsagePerUser,
    Instant createdAt

) {

}
