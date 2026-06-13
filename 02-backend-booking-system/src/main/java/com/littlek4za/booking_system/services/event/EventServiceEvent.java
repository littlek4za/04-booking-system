package com.littlek4za.booking_system.services.event;

import lombok.Getter;

@Getter
public final class EventServiceEvent {
    
    private final Long userId;
    private final Long eventId;

    private EventServiceEvent(Long userId, Long eventId) {
        this.userId = userId;
        this.eventId = eventId;
    }

    public static EventServiceEvent eventCreated(Long userId) {
        return new EventServiceEvent(userId, null);
    }

    public static EventServiceEvent eventUpdated(Long userId, Long eventId) {
        return new EventServiceEvent(userId, eventId);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((userId == null) ? 0 : userId.hashCode());
        result = prime * result + ((eventId == null) ? 0 : eventId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        EventServiceEvent other = (EventServiceEvent) obj;
        if (userId == null) {
            if (other.userId != null)
                return false;
        } else if (!userId.equals(other.userId))
            return false;
        if (eventId == null) {
            if (other.eventId != null)
                return false;
        } else if (!eventId.equals(other.eventId))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "EventServiceEvent [userId=" + userId + ", eventId=" + eventId + "]";
    }

    
}
