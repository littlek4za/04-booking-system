package com.littlek4za.booking_system.models;

public enum EventType {
    FIXED,
    FLEXIBLE,
    BUSINESS;

    public boolean supportMaxBookPerInterval(){
        return this == FIXED;
    }
}
