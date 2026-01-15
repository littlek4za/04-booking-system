package com.littlek4za.booking_system.entities;

import java.time.Instant;
import java.util.Map;

import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.LastModifiedDate;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.littlek4za.booking_system.models.TimeRange;
import com.littlek4za.booking_system.utils.WorkingDayHoursConverter;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
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
    @JsonBackReference
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

    @Column(name = "slot_interval_minutes", nullable = false)
    private int slotIntervalMinutes = 0;

        //Working Days Hours as Map<DayOfWeek, TimeRang>
//    {
//   "1": [{"open": "09:00", "close": "12:00"}, {"open": "13:00", "close": "17:00"}],
//   "2": [{"open": "09:00", "close": "17:00"}],
//   "3": [{"open": "09:00", "close": "17:00"}],
//   "4": [{"open": "09:00", "close": "17:00"}],
//   "5": [{"open": "09:00", "close": "17:00"}],
//   "6": [], 
//   "0": []
//   }
    @Convert(converter = WorkingDayHoursConverter.class)
    @Column(name = "working_days_hours",columnDefinition = "jsonb")
    private Map<Integer, TimeRange> workingDaysHours;

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Slot() {
    }

    public Slot(Event event, String slotName, String slotDescription, Instant slotStartTime, Instant slotEndTime,
            int maxBook, int slotIntervalMinutes, Map<Integer, TimeRange> workingDaysHours) {
        this.event = event;
        this.slotName = slotName;
        this.slotDescription = slotDescription;
        this.slotStartTime = slotStartTime;
        this.slotEndTime = slotEndTime;
        this.maxBook = maxBook;
        this.slotIntervalMinutes = slotIntervalMinutes;
        this.workingDaysHours = workingDaysHours;
    }


}
