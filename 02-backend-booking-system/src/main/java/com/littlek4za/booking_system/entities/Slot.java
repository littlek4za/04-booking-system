package com.littlek4za.booking_system.entities;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.littlek4za.booking_system.models.TimeRange;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
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
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Table(name = "slots",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"event_id", "id"})
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

    @Column(name = "slot_start_time")
    private Instant slotStartTime;

    @Column(name = "slot_end_time")
    private Instant slotEndTime;

    @Column(name = "max_book", nullable = false)
    private Integer maxBook;

    @Column(name = "slot_interval_minutes")
    private Integer slotIntervalMinutes;

    @Column(name = "slot_frequency_interval_minutes")
    private Integer slotFrequencyIntervalMinutes;

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
    // @Convert(converter = WorkingDayHoursConverter.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "working_days_hours",columnDefinition = "jsonb")
    private Map<Integer, List<TimeRange>> workingDaysHours;

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Slot() {
    }

    public Slot(Event event, String slotName, String slotDescription, Instant slotStartTime, Instant slotEndTime,
            Integer maxBook, Integer slotIntervalMinutes, Integer slotFrequencyIntervalMinutes, Map<Integer, List<TimeRange>> workingDaysHours) {
        this.event = event;
        this.slotName = slotName;
        this.slotDescription = slotDescription;
        this.slotStartTime = slotStartTime;
        this.slotEndTime = slotEndTime;
        this.maxBook = maxBook;
        this.slotIntervalMinutes = slotIntervalMinutes;
        this.slotFrequencyIntervalMinutes = slotFrequencyIntervalMinutes;
        this.workingDaysHours = workingDaysHours;
    }
}
