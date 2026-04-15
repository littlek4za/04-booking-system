package com.littlek4za.booking_system.dtos;

public record GuestBookingViewInitRequestDto(
    String bookingToken,
    String email
) {

}
