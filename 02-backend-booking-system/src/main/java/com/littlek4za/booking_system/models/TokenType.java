package com.littlek4za.booking_system.models;

public enum TokenType {
    USER,
    GUEST_BOOKING_VIEW,
    GUEST_BOOKING_CREATE;

    public boolean isGuestToken() {
        return this == GUEST_BOOKING_VIEW
            || this == GUEST_BOOKING_CREATE;
    }

    public boolean isUserToken() {
        return this == USER;
    }
}
