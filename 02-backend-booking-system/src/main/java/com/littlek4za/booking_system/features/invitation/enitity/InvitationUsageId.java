package com.littlek4za.booking_system.features.invitation.enitity;

import java.io.Serializable;
import java.util.Objects;

public class InvitationUsageId implements Serializable{
    private Long invitationId;
    private Long userId;

    public InvitationUsageId(){}

    public InvitationUsageId(Long invitationId, Long userId) {
        this.invitationId = invitationId;
        this.userId = userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InvitationUsageId)) return false;
        InvitationUsageId that = (InvitationUsageId) o;
        return Objects.equals(invitationId, that.invitationId) &&
               Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(invitationId, userId);
    }

}
