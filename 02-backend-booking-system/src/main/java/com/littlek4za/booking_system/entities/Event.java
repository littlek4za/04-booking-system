package com.littlek4za.booking_system.entities;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.littlek4za.booking_system.models.EventType;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Setter
@Getter
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "event_name", nullable = false)
    private String eventName;

    @Column(name = "event_description")
    private String eventDescription;

    @Column(name = "event_location_address", nullable = false)
    private String eventLocationAddress;

    @Column(name = "include_position", nullable = false)
    private Boolean includePosition = false;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "max_bookings_per_identity")
    private Integer maxBookingsPerIdentity; // null means unlimited usage for user

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private EventType eventType;

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Slot> slotList = new ArrayList<>();

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
    private Set<Invitation> invitationSet;

    protected Event() {
    }

    public Event(User user, String eventName, String eventDescription, String eventLocationAddress,
            Boolean includePosition, Integer maxBookingsPerIdentity, EventType eventType) {
        this.user = user;
        this.eventName = eventName;
        this.eventDescription = eventDescription;
        this.eventLocationAddress = eventLocationAddress;
        this.includePosition = includePosition;
        this.maxBookingsPerIdentity = maxBookingsPerIdentity;
        this.eventType = eventType;
    }

    public void setPosition(Double lat, Double lon) {
        if (!Boolean.TRUE.equals(includePosition)) {
            throw new IllegalStateException("Position not enabled");
        }
        this.latitude = lat;
        this.longitude = lon;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Event))
            return false;
        Event other = (Event) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @PrePersist
    @PreUpdate
    void validateInvariant(){
        if (Boolean.TRUE.equals(includePosition) && (latitude == null || longitude== null)){
            throw new IllegalStateException("latitude and longitude is requred when includePosition is true");
        }
    }

}
