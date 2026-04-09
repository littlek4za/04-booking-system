package com.littlek4za.booking_system.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
@Table(name = "invitation_usages")
@IdClass(InvitationUsageId.class)
public class InvitationUsage {

    @Id
    @Column(name = "invitation_id")
    private Long invitationId;

    @Id
    @Column(name = "user_id")
    private Long userId;

    @ManyToOne
    @JoinColumn(name = "invitation_id", insertable = false, updatable = false)
    private Invitation invitation;

    @ManyToOne
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @Column(name = "usage_count")
    private int usageCount;

    protected InvitationUsage() {
    }

    public InvitationUsage(Invitation invitation, User user) {
        this.invitationId = invitation.getId();
        this.userId = user.getId();

        this.invitation = invitation;
        this.user = user;
        this.usageCount = 0;

        if (user.getInvitationUsages() != null) {
            user.getInvitationUsages().add(this);
        }

        if (invitation.getInvitationUsages() != null) {
            invitation.getInvitationUsages().add(this);
        }
    }

    public void incrementUsage(Integer maxUsagePerIdentity) {
        if (maxUsagePerIdentity != null && this.usageCount >= maxUsagePerIdentity) {
            throw new IllegalStateException("User/Email has reached the max usage for this invitation");
        }
        this.usageCount++;
    }

}
