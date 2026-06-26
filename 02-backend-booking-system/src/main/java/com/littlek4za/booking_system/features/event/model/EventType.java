package com.littlek4za.booking_system.features.event.model;

public enum EventType {
    FIXED,
    FLEXIBLE,
    BUSINESS;

    public boolean supportMaxBookPerInterval(){
        return this == FIXED;
    }
}
