package com.littlek4za.booking_system.dtos;

public record GuestBookingViewInitResponseDto(
    boolean captchaRequired,
    Boolean valid
) {

}
