package com.littlek4za.booking_system.entities;

import java.io.Serializable;

public class InvitationUsageId implements Serializable{
    private Long invitationId;
    private Long userId;

    public InvitationUsageId(){}

    public InvitationUsageId(Long invitationId, Long userId) {
        this.invitationId = invitationId;
        this.userId = userId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((invitationId == null) ? 0 : invitationId.hashCode());
        result = prime * result + ((userId == null) ? 0 : userId.hashCode());
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
        InvitationUsageId other = (InvitationUsageId) obj;
        if (invitationId == null) {
            if (other.invitationId != null)
                return false;
        } else if (!invitationId.equals(other.invitationId))
            return false;
        if (userId == null) {
            if (other.userId != null)
                return false;
        } else if (!userId.equals(other.userId))
            return false;
        return true;
    }


}
