package com.littlek4za.booking_system.dtos;

public record DeleteValidationResponseDto(
    boolean canDelete,
    Long upcomingBookingCount,
    Long ongoingBookingCount,
    Long expiredBookingCount
) {

}
