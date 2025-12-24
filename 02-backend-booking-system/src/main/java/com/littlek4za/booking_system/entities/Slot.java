package com.littlek4za.booking_system.entities;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;

import com.littlek4za.booking_system.models.SlotType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "slots",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"event_id", "slot_name"})
    }
)
public class Slot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "slot_name", nullable = false)
    private String slotName;

    @Column(name = "slot_description")
    private String slotDescription;

    @Column(name = "slot_start_time", nullable = false)
    private Instant slotStartTime;

    @Column(name = "slot_end_time", nullable = false)
    private Instant slotEndTime;

    @Column(name = "max_book", nullable = false)
    private int maxBook = 1;

    @Enumerated(EnumType.STRING)
    @Column(name = "slot_type", nullable = false)
    private SlotType slotType;

    @Column(name = "slot_interval_minutes", nullable = false)
    private int slotIntervalMinutes = 30;

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;

    protected Slot() {
    }

    public Slot(Event event, String slotName, String slotDescription, Instant slotStartTime,
            Instant slotEndTime, int maxBook, SlotType slotType) {
        this.event = event;
        this.slotName = slotName;
        this.slotDescription = slotDescription;
        this.slotStartTime = slotStartTime;
        this.slotEndTime = slotEndTime;
        this.maxBook = maxBook;
        this.slotType = slotType;
    }

}
