package com.littlek4za.booking_system.features.guest_access.dto;

public record GuestBookingViewInitRequestDto(
    String bookingToken,
    String email
) {

}
