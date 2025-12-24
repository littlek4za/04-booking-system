package com.littlek4za.booking_system.entities;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
    @JoinColumn(name = "slot_id", nullable = false)
    private Slot slot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User user;

    @Column(name = "code", unique = true, nullable = false)
    private String code;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "max_usage", nullable = false)
    private int maxUsage = 1;

    @Column(name = "used_count", nullable = false)
    private int usedCount = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    protected Invitation() {
    }

    public Invitation(Slot slot, User user, String code, Instant expiresAt, int maxUsage, int usedCount) {
        this.slot = slot;
        this.user = user;
        this.code = code;
        this.expiresAt = expiresAt;
        this.maxUsage = maxUsage;
        this.usedCount = usedCount;
    }

    public void incrementUsedCount() {
        if (usedCount < maxUsage) {
            usedCount++;
        } else {
            throw new IllegalStateException("Max usage reached");
        }
    }

}
