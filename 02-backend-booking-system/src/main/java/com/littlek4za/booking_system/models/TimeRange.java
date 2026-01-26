package com.littlek4za.booking_system.models;

public class TimeRange {

    private String open;
    private String close;

    public TimeRange() {
    }

    public TimeRange(String open, String close) {
        this.open = open;
        this.close = close;
    }

    public String getOpen() {
        return open;
    }

    public void setOpen(String open) {
        this.open = open;
    }

    public String getClose() {
        return close;
    }

    public void setClose(String close) {
        this.close = close;
    }
}
