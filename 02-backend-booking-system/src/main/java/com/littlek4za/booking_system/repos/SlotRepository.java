package com.littlek4za.booking_system.repos;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.littlek4za.booking_system.entities.Event;
import com.littlek4za.booking_system.entities.Slot;
import com.littlek4za.booking_system.repos.projections.EventSlotCount;

public interface SlotRepository extends JpaRepository<Slot,Long>{

    @Query("SELECT s.event.id AS eventId, COUNT (s) AS slotCount FROM Slot s WHERE s.event IN :events GROUP BY s.event.id")
    List<EventSlotCount> countSlotForEvents(@Param("events") List<Event> events);

    @Query("SELECT COUNT (s) FROM Slot s WHERE s.event.id = :eventId")
    long countSlotByEventId(@Param("eventId") Long eventId);

    List<Slot> findByEvent(Event event);

    int deleteByIdAndEventId(Long id, Long eventId);

    Optional<Slot> findByIdAndEventId (Long slotId, Long eventId);

}
