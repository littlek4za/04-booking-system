package com.littlek4za.booking_system.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
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
    @MapsId("invitationId")
    @JoinColumn(name = "invitation_id")
    private Invitation invitation;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "usage_count")
    private int usageCount;

    protected InvitationUsage() {
    }

    public InvitationUsage(Invitation invitation, User user) {
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

    public void incrementUsage(Integer maxUsagePerUser) {
        if (maxUsagePerUser == null) {
            throw new IllegalStateException("Unlimited usage for this invitation");
        }
        if (maxUsagePerUser != null && this.usageCount >= maxUsagePerUser) {
            throw new IllegalStateException("User has reached the max usage for this invitation");
        }
        this.usageCount++;
    }

}
