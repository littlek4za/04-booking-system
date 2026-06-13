package com.littlek4za.booking_system.services.event;

import java.time.Instant;

import com.littlek4za.booking_system.dtos.AttendeeBookingResponseDto;

public record BookingMailEvent(
        AttendeeBookingResponseDto dto,
        MailType type,
        String bookingToken,
        String attendeeEmail,
        String attendeeFirstName,
        String attendeeLastName,
        String organizerEmail,
        String organizerFirstName,
        String organizerLastName,
        Instant bookedStartTime,
        Instant bookedEndTime,
        String eventName,
        String slotName,
        String eventLocationAddress
    ) {

    public enum MailType {
        CONFIRMATION,
        ORGANIZER_CANCELLATION,
        ATTENDEE_CANCELLATION
    }

    public static BookingMailEvent forConfirmation(AttendeeBookingResponseDto dto) {
        return new BookingMailEvent(dto, MailType.CONFIRMATION, null, null, null, null, null, null, null, null, null,
                null, null, null);
    }

    public static BookingMailEvent forOrganizerCancellation(
            String bookingToken,
            String attendeeEmail,
            String attendeeFirstName,
            String attendeeLastName,
            String organizerEmail,
            String organizerFirstName,
            String organizerLastName,
            Instant bookedStartTime,
            Instant bookedEndTime,
            String eventName,
            String slotName,
            String eventLocationAddress) {
        return new BookingMailEvent(
                null,
                MailType.ORGANIZER_CANCELLATION,
                bookingToken,
                attendeeEmail,
                attendeeFirstName,
                attendeeLastName,
                organizerEmail,
                organizerFirstName,
                organizerLastName,
                bookedStartTime,
                bookedEndTime,
                eventName,
                slotName,
                eventLocationAddress);
    }

    public static BookingMailEvent forAttendeeCancellation(
            String bookingToken,
            String attendeeEmail,
            String attendeeFirstName,
            String attendeeLastName,
            String organizerEmail,
            String organizerFirstName,
            String organizerLastName,
            Instant bookedStartTime,
            Instant bookedEndTime,
            String eventName,
            String slotName,
            String eventLocationAddress

    ) {
        return new BookingMailEvent(
                null,
                MailType.ATTENDEE_CANCELLATION,
                bookingToken,
                attendeeEmail,
                attendeeFirstName,
                attendeeLastName,
                organizerEmail,
                organizerFirstName,
                organizerLastName,
                bookedStartTime,
                bookedEndTime,
                eventName,
                slotName,
                eventLocationAddress);
    }

}
