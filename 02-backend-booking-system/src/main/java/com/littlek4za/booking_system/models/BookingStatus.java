package com.littlek4za.booking_system.models;

import java.time.Instant;

public enum BookingStatus {
    UPCOMING,
    ONGOING,
    EXPIRED,
    DELETED;

    public static BookingStatus from(
            Instant bookedStartTime,
            Instant bookedEndTime,
            boolean deleted) {
        Instant now = Instant.now();

        if (deleted) {
            return DELETED;
        }

        if (now.isBefore(bookedStartTime)) {
            return UPCOMING;
        }

        if (!now.isAfter(bookedEndTime)) {
            return ONGOING;
        }

        return EXPIRED;
    }
}
