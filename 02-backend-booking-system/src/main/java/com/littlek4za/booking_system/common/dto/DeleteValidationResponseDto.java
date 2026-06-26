package com.littlek4za.booking_system.common.dto;

public record DeleteValidationResponseDto(
    boolean canDelete,
    Long upcomingBookingCount,
    Long ongoingBookingCount,
    Long expiredBookingCount
) {

}
