package com.littlek4za.booking_system.entities;

import java.time.Instant;

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
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "guest_first_name")
    private String guestFirstName;

    @Column(name = "guest_last_name")
    private String guestLastName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "slot_id")
    private Slot slot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invitation_id")
    private Invitation invitation;

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

    @Column(name = "booking_token", nullable = false, unique = true)
    private String bookingToken;

    // history info to keep
    @Column(name = "event_name", nullable = false)
    private String eventName;

    @Column(name = "slot_name", nullable = false)
    private String slotName;

    @Column(name = "organizer_email", nullable = false)
    private String organizerEmail;

    @Column(name = "attendee_email", nullable = false)
    private String attendeeEmail;

    @Column(name = "event_location_address", nullable = false)
    private String eventLocationAddress;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    protected Booking() {
    }

    public Booking(User user, Slot slot, Instant bookedStartTime,
            Instant bookedEndTime, String eventName, String slotName, String organizerEmail, String attendeeEmail,String eventLocationAddress) {
        this.user = user;
        this.slot = slot;
        this.bookedStartTime = bookedStartTime;
        this.bookedEndTime = bookedEndTime;
        this.eventName = eventName;
        this.slotName = slotName;
        this.organizerEmail = organizerEmail;
        this.attendeeEmail = attendeeEmail;
        this.eventLocationAddress = eventLocationAddress;
    }

}
