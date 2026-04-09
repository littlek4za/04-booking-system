package com.littlek4za.booking_system.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.littlek4za.booking_system.entities.Event;
import com.littlek4za.booking_system.entities.Invitation;
import com.littlek4za.booking_system.models.SlotIncludeMode;

import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface InvitationRepository extends JpaRepository<Invitation, Long> {

        List<Invitation> findByEvent(Event event);

        @Query("""
                        SELECT i
                        FROM Invitation i
                        LEFT JOIN FETCH i.slotSet
                        WHERE i.event.id = :eventId
                        """)
        List<Invitation> findByEventIdWithSlotSet(Long eventId);

        @Query("""
                        SELECT DISTINCT i
                        FROM Invitation i
                        LEFT JOIN FETCH i.slotSet
                        WHERE i.event = :event
                        """)
        Set<Invitation> findByEventWithSlotSet(@Param("event") Event event);

        boolean existsByAccessToken(String token);

        Optional<Invitation> findByEventAndId(Event event, Long id);

        @Query("""
                        SELECT i
                        FROM Invitation i
                        LEFT JOIN FETCH i.event
                        WHERE i.accessToken = :accessToken
                        """)
        Optional<Invitation> findByAccessTokenWithEvent(@Param("accessToken") String token);

        @Query("""
                        SELECT i
                        FROM Invitation i
                        LEFT JOIN FETCH i.event
                        LEFT JOIN FETCH i.slotSet
                        WHERE i.accessToken = :accessToken
                        """)
        Optional<Invitation> findByAccessTokenWithEventAndSlotSet(@Param("accessToken") String token);

        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("""
                        SELECT i
                        FROM Invitation i
                        LEFT JOIN FETCH i.event
                        LEFT JOIN FETCH i.slotSet
                        LEFT JOIN FETCH i.user
                        WHERE i.id = :id
                        """)
        Optional<Invitation> findByIdWithEventAndSlotSetsAndUsersForUpdate(@Param("id") Long id);

        @Query("""
                        SELECT DISTINCT i
                        FROM Invitation i
                        LEFT JOIN FETCH i.event e
                        LEFT JOIN FETCH i.slotSet s
                        WHERE e.id = :eventId
                        AND (i.slotIncludeMode = :allAndFuture
                            OR EXISTS (
                                    SELECT 1 FROM i.slotSet s2 WHERE s2.id = :slotId
                            )
                            )
                        """)
        Set<Invitation> findByEventIdAndSlotIdOrAllAndFutureWithEventAndSlotSets(@Param("eventId") Long eventId,
                        @Param("slotId") Long slotId, @Param("allAndFuture") SlotIncludeMode allAndFuture);

}
