package com.littlek4za.booking_system.entities;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import com.littlek4za.booking_system.models.DeletedBy;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "slot_id", nullable = false)
    private Slot slot;

    @CreationTimestamp
    @Column(name = "booked_at", updatable = false)
    private Instant bookedAt;

    @Column(name = "booked_start_time", nullable = false)
    private Instant bookedStartTime;

    @Column(name = "booked_end_time", nullable = false)
    private Instant bookedEndTime;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "deleted_by")
    private DeletedBy deletedBy;

    protected Booking() {
    }

    public Booking(User user, Slot slot, Instant bookedStartTime,
            Instant bookedEndTime) {
        this.user = user;
        this.slot = slot;
        this.bookedStartTime = bookedStartTime;
        this.bookedEndTime = bookedEndTime;
    }

}
