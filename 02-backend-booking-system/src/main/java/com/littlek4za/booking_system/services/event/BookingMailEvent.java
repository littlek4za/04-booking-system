package com.littlek4za.booking_system.services.event;

import com.littlek4za.booking_system.dtos.AttendeeBookingResponseDto;

public record BookingMailEvent(
    AttendeeBookingResponseDto dto,
    Long bookingId,
    MailType type
) {
    public enum MailType {
        CONFIRMATION,
        CANCELLATION
    }

    public static BookingMailEvent forConfirmation(AttendeeBookingResponseDto dto) {
        return new BookingMailEvent(dto, null, MailType.CONFIRMATION);
    }

    public static BookingMailEvent forCancellation(Long bookingId) {
        return new BookingMailEvent(null, bookingId, MailType.CANCELLATION);
    }

}
