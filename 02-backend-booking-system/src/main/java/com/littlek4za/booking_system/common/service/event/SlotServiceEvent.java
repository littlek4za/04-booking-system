package com.littlek4za.booking_system.common.service.event;

import lombok.Getter;

@Getter
public final class SlotServiceEvent {

    private final Long userId;
    private final Long eventId;
    private final Long slotId;

    private SlotServiceEvent(Long userId, Long eventId, Long slotId) {
        this.userId = userId;
        this.eventId = eventId;
        this.slotId = slotId;
    }

    public static SlotServiceEvent slotCreated(Long userId, Long eventId){
        return new SlotServiceEvent(userId, eventId, null);
    }

    public static SlotServiceEvent slotUpdated(Long userId, Long eventId, Long slotId) {
        return new SlotServiceEvent(userId, eventId, slotId);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((userId == null) ? 0 : userId.hashCode());
        result = prime * result + ((eventId == null) ? 0 : eventId.hashCode());
        result = prime * result + ((slotId == null) ? 0 : slotId.hashCode());
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
        SlotServiceEvent other = (SlotServiceEvent) obj;
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
        if (slotId == null) {
            if (other.slotId != null)
                return false;
        } else if (!slotId.equals(other.slotId))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "SlotServiceEvent [userId=" + userId + ", eventId=" + eventId + ", slotId=" + slotId + "]";
    }



}
