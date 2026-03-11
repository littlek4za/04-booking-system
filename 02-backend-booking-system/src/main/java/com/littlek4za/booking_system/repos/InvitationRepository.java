package com.littlek4za.booking_system.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.littlek4za.booking_system.entities.Event;
import com.littlek4za.booking_system.entities.Invitation;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface InvitationRepository extends JpaRepository<Invitation, Long> {

    List<Invitation> findByEvent(Event event);

    @Query("""
            SELECT DISTINCT i
            FROM Invitation i
            LEFT JOIN FETCH i.slotSet
            WHERE i.event = :event
            """)
    Set<Invitation> findByEventWithSlotSet(Event event);

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

    @Query("""
            SELECT i
            FROM Invitation i
            LEFT JOIN FETCH i.event
            LEFT JOIN FETCH i.slotSet     
            WHERE i.id = :id
            """)
    Optional<Invitation> findByIdWithEventAndSlotSets(@Param("id") Long id);

}
 