package com.littlek4za.booking_system.services.event;

import com.littlek4za.booking_system.dtos.OrganizerBookingResponseDto;

public record BookingMailEvent(
    OrganizerBookingResponseDto dto,
    Long bookingId,
    MailType type
) {
    public enum MailType {
        CONFIRMATION,
        CANCELLATION
    }

    public static BookingMailEvent forConfirmation(OrganizerBookingResponseDto dto) {
        return new BookingMailEvent(dto, null, MailType.CONFIRMATION);
    }

    public static BookingMailEvent forCancellation(Long bookingId) {
        return new BookingMailEvent(null, bookingId, MailType.CANCELLATION);
    }

}
