package com.littlek4za.booking_system.entities;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.CreationTimestamp;

import com.littlek4za.booking_system.models.SlotIncludeMode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "invitations")
public class Invitation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User user;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "max_usage")
    private Integer maxUsage;

    @Column(name = "used_count", nullable = false)
    private int usedCount = 0;

    @Column(name = "access_token", nullable = false, unique = true)
    private String accessToken;

    @Enumerated(EnumType.STRING)
    @Column(name = "include_mode", nullable = false)
    private SlotIncludeMode slotIncludeMode;

    @Column(name = "required_login", nullable = false)
    private boolean requiredLogin = true;

    @Column(name = "max_usage_per_identity")
    private Integer maxUsagePerIdentity; // null means unlimited usage for user

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @ManyToMany
    @JoinTable(name = "invitation_slots", joinColumns = @JoinColumn(name = "invitation_id"), inverseJoinColumns = @JoinColumn(name = "slot_id"))
    private Set<Slot> slotSet = new HashSet<>();

    @OneToMany(mappedBy = "invitation", fetch = FetchType.LAZY)
    private Set<InvitationUsage> invitationUsages = new HashSet<>();

    @OneToMany(mappedBy = "invitation", fetch = FetchType.LAZY)
    private Set<Booking> bookingSet = new HashSet<>();

    protected Invitation() {
    }

    public Invitation(Event event, User user, Instant expiresAt, Integer maxUsage,
            SlotIncludeMode slotIncludeMode, boolean requiredLogin, Integer maxUsagePerIdentity) {
        this.event = event;
        this.user = user;
        this.expiresAt = expiresAt;
        this.maxUsage = maxUsage;
        this.slotIncludeMode = slotIncludeMode;
        this.requiredLogin = requiredLogin;
        this.maxUsagePerIdentity = maxUsagePerIdentity;

    }

    public void incrementUsedCount() {
        if (maxUsage == null) {
            usedCount++;
            return;
        }
        if (usedCount < maxUsage) {
            usedCount++;
        } else {
            throw new IllegalStateException("Max usage reached");
        }
    }

    public void addBooking(Booking booking) {
        booking.setInvitation(this);
        this.bookingSet.add(booking);
    }

    public void removeBookin(Booking booking) {
        this.bookingSet.remove(booking);
        booking.setInvitation(null);
    }

    public void addInvitationUsage(InvitationUsage usage) {
        usage.setInvitation(this);
        this.invitationUsages.add(usage);
    }

    public void removeInvitationUsage(InvitationUsage usage) {
        this.invitationUsages.remove(usage);
        usage.setInvitation(null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Invitation))
            return false;
        Invitation that = (Invitation) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

}
