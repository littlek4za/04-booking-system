package com.littlek4za.booking_system.features.slot;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.littlek4za.booking_system.features.event.Event;
import com.littlek4za.booking_system.features.slot.entity.Slot;
import com.littlek4za.booking_system.features.slot.model.EventSlotCount;

import jakarta.persistence.LockModeType;

public interface SlotRepository extends JpaRepository<Slot, Long> {

        @Query("""
                        SELECT s.event.id AS eventId,
                            COUNT (s) AS slotCount
                        FROM Slot s
                        WHERE s.event.id IN :eventIds
                        GROUP BY s.event.id
                        """)
        List<EventSlotCount> countSlotForEvents(@Param("eventIds") List<Long> eventIds);

        @Query("""
                        SELECT COUNT (s)
                        FROM Slot s
                        WHERE s.event.id = :eventId
                        """)
        long countSlotByEventId(@Param("eventId") Long eventId);

        @Query("""
                        SELECT s
                        FROM Slot s
                        JOIN FETCH s.event
                        WHERE s.event = :event
                        """)
        Set<Slot> findByEventWithEvent(@Param("event") Event event);

        int deleteByIdAndEventId(Long id, Long eventId);

        @Query("""
                        SELECT s
                        FROM Slot s
                        JOIN FETCH s.event e
                        WHERE s.id = :slotId
                        AND e.id = :eventId
                        """)
        Optional<Slot> findByIdAndEventId(@Param("slotId") Long slotId, @Param("eventId") Long eventId);

        @Query("""
                        SELECT COUNT(s)
                        FROM Slot s
                        WHERE s.event.id = :event_id
                        AND s.id IN :slotIds
                        """)
        long countByEventIdAndSlotIds(@Param("eventId") Long eventId, List<Integer> slotIds);

        @Query("""
                        SELECT s
                        FROM Slot s
                        JOIN FETCH s.event e
                        WHERE s.id IN :slotIdList
                        AND e.id = :eventId
                            """)
        Set<Slot> findByIdInAndEventIdWithEvent(List<Integer> slotIdList, Long eventId);

        @Query("""
                        SELECT s
                        FROM Slot s
                        LEFT JOIN FETCH s.event
                        WHERE s.id = :slotId
                                """)
        Optional<Slot> findByIdWithEvent(Long slotId);

        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("""
                        SELECT s
                        FROM Slot s
                        LEFT JOIN FETCH s.event
                        WHERE s.id = :slotId
                                """)
        Optional<Slot> findByIdWithEventForUpdate(Long slotId);

        @Query("""
                            SELECT s
                            FROM Slot s
                            LEFT JOIN FETCH s.invitationSet
                            WHERE s.id = :slotId AND s.event.id = :eventId
                        """)
        Optional<Slot> findByIdAndEventIdWithInvitationSet(@Param("slotId") Long slotId,
                        @Param("eventId") Long eventId);

        Optional<Slot> findByIdAndEvent_User_Id(Long slotId, Long userId);

}
