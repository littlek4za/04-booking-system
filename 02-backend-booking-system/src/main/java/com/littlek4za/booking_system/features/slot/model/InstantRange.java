package com.littlek4za.booking_system.features.slot.model;

import java.time.Instant;

public class InstantRange {

    public Instant open;
    public Instant close;

    public InstantRange(){
        
    }
    
    public InstantRange(Instant open, Instant close) {
        this.open = open;
        this.close = close;
    }

    public Instant getOpen() {
        return open;
    }

    public void setOpen(Instant open) {
        this.open = open;
    }

    public Instant getClose() {
        return close;
    }

    public void setClose(Instant close) {
        this.close = close;
    } 
}
