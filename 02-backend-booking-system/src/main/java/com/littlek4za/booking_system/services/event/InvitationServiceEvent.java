package com.littlek4za.booking_system.services.event;

import java.util.List;

import lombok.Getter;

@Getter
public final class InvitationServiceEvent {

    private final Long userId;
    private final Long eventId;
    private final List<Long> slotIds;
    private final String invitationToken;

    public InvitationServiceEvent(Long userId, Long eventId, List<Long> slotIds, String invitationToken) {
        this.userId = userId;
        this.eventId = eventId;
        this.slotIds = slotIds;
        this.invitationToken = invitationToken;
    }

    public static InvitationServiceEvent invitationCreated(Long userId,Long eventId){
        return new InvitationServiceEvent(userId, eventId, null,null);
    }

    public static InvitationServiceEvent invitationUpdated(Long userId,Long eventId, List<Long> slotIds, String invitationToken){
        return new InvitationServiceEvent(userId, eventId, slotIds, invitationToken);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((userId == null) ? 0 : userId.hashCode());
        result = prime * result + ((eventId == null) ? 0 : eventId.hashCode());
        result = prime * result + ((slotIds == null) ? 0 : slotIds.hashCode());
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
        InvitationServiceEvent other = (InvitationServiceEvent) obj;
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
        if (slotIds == null) {
            if (other.slotIds != null)
                return false;
        } else if (!slotIds.equals(other.slotIds))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "InvitationServiceEvent [userId=" + userId + ", eventId=" + eventId + ", slotIds=" + slotIds + "]";
    }

    
}
