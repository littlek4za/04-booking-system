package com.littlek4za.booking_system.features.invitation.dto;

import java.time.Instant;
import java.util.List;

import com.littlek4za.booking_system.features.event.dto.EventResponseDto;
import com.littlek4za.booking_system.features.invitation.model.SlotIncludeMode;
import com.littlek4za.booking_system.features.slot.dto.SlotResponseDto;

public record InvitationResponseDto(
    Long id,
    EventResponseDto event,
    Long userId,
    Instant expiresAt,
    Integer maxUsage,
    int usedCount,
    String accessToken,
    SlotIncludeMode slotIncludeMode,
    boolean requiredLogin,
    Integer maxUsagePerIdentity,
    Instant createdAt,
    List<SlotResponseDto> slotList

) {

}
