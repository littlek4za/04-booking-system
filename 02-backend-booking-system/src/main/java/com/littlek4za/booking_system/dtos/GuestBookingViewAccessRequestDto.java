package com.littlek4za.booking_system.dtos;

public record GuestBookingViewAccessRequestDto(
    String captchaToken,
    String email,
    String bookingToken
) {

}
