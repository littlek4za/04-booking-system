package com.littlek4za.booking_system.repos;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.littlek4za.booking_system.entities.Booking;
import com.littlek4za.booking_system.entities.Event;
import com.littlek4za.booking_system.entities.Slot;
import com.littlek4za.booking_system.entities.User;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    boolean existsByBookingToken(String token);

    @Query("""
            SELECT b
            FROM Booking b
            JOIN FETCH b.user u
            JOIN FETCH b.slot s
            JOIN FETCH s.event e
            JOIN FETCH e.user
            WHERE b.slot = :slot
            AND b.isDeleted = false
            """)
    List<Booking> findBySlot(Slot slot);

    @Query("""
            SELECT COUNT(b)
            FROM Booking b
            WHERE b.slot = :slot
            AND b.isDeleted = false
            """)
    int getBookingsCountBySlot(@Param("slot") Slot slot);

    @Query("""
                SELECT b 
                FROM Booking b
                JOIN FETCH b.user
                JOIN FETCH b.slot s
                JOIN FETCH s.event e
                JOIN FETCH e.user
                WHERE e = :event
                AND b.isDeleted = false
            """)
    List<Booking> findActiveBookingsByEventId(@Param("event") Event event);

    
    @Query("""
                SELECT b 
                FROM Booking b
                JOIN FETCH b.user u
                JOIN FETCH b.slot s
                JOIN FETCH s.event e
                JOIN FETCH e.user
                WHERE e = :event
                AND b.isDeleted = false
            """)
    List<Booking> findActiveBookingsByEventIdWithDeletedFalse(@Param("event") Event event);

    @Query("""
                SELECT b 
                FROM Booking b
                JOIN FETCH b.user
                JOIN FETCH b.slot s
                JOIN FETCH s.event e
                JOIN FETCH e.user
                WHERE b.id = :bookingId
                AND e.user.id = :userId
                AND b.slot.id = :slotId
                AND b.isDeleted = false
            """)
    Optional<Booking> findOrganizerBookingByIdAndSlotIdAndUserId(@Param("bookingId") Long bookingId, @Param("slotId") Long slotId, @Param("userId") Long userId);

    @Query("""
                SELECT b 
                FROM Booking b
                JOIN FETCH b.user
                JOIN FETCH b.slot s
                JOIN FETCH s.event e
                WHERE b.id = :bookingId
                AND b.user.id = :userId
                AND b.isDeleted = false
            """)
    Optional<Booking> findAttendeeBookingByIdAndUserId(@Param("bookingId") Long bookingId, @Param("userId") Long userId);

    @Query("""
                SELECT b 
                FROM Booking b
                JOIN FETCH b.user
                JOIN FETCH b.slot s
                JOIN FETCH s.event e
                WHERE b.id = :bookingId
                AND b.user.email = :email
                AND b.bookingToken = :bookingToken
                AND b.isDeleted = false
            """)
    Optional<Booking> findAttendeeBookingByIdAndUserEmailAndBookingToken(@Param("bookingId") Long bookingId, @Param("email") String email, @Param("bookingToken") String bookingToken);


    @Query("""
                SELECT b.slot.id, COUNT(b)
                FROM Booking b
                WHERE b.slot.event.id = :eventId
                AND b.isDeleted = false
                GROUP BY b.slot.id
            """)    
    List<Object[]> countBookingsByEventGrouped(@Param("eventId") Long eventId);
      
    @Query("""
                SELECT COUNT(b)
                FROM Booking b
                WHERE b.slot.id = :slotId
                AND b.isDeleted = false
            """)  
    Long countBySlotId(Long slotId);


    @Query("""
                SELECT COUNT(b)
                FROM Booking b
                WHERE b.user = :user
                AND b.slot = :slot
                AND b.isDeleted = false
            """)  
    Long countByUserAndSlot(@Param("user") User user,@Param("slot") Slot slot);


    @Query("""
                SELECT COUNT(b)
                FROM Booking b
                WHERE b.user = :user
                AND b.slot.event = :event
                AND b.isDeleted = false
            """)  
    Long countByUserAndEvent(@Param("user") User user, @Param("event") Event event);

    @Query("""
                SELECT b
                FROM Booking b
                JOIN FETCH b.user
                WHERE b.user.id = :userId
                AND b.bookingToken = :bookingToken
                AND b.isDeleted = false
            """) 
    Optional<Booking> findByBookingTokenAndUserId(@Param("bookingToken") String bookingToken,@Param("userId") Long userId);

    @Query("""
                SELECT b
                FROM Booking b
                LEFT JOIN FETCH b.user
                WHERE b.bookingToken = :bookingToken
                AND b.isDeleted = false
            """) 
    Optional<Booking> findByBookingToken(@Param("bookingToken") String bookingToken);

    @Query("""
                SELECT b
                FROM Booking b
                LEFT JOIN FETCH b.user
                WHERE b.id = :bookingId
                AND b.isDeleted = true
            """) 
    Optional<Booking> findByIdAndIsDeletedTrueWithUser(@Param("bookingId") Long bookingId);

    @Query("""
                SELECT b
                FROM Booking b
                JOIN FETCH b.user
                WHERE b.user.id = :userId
                AND b.isDeleted = false
            """) 
    List<Booking> findByUserId(@Param("userId") Long userId);

    @Query("""
                SELECT b
                FROM Booking b
                JOIN FETCH b.user
                WHERE b.user.email = :email
                AND b.bookingToken = :bookingToken
                AND b.isDeleted = false
            """)     
    Optional<Booking> findbyBookingTokenAndEmail(@Param("bookingToken") String bookingToken,@Param("email") String email);


    List<Booking> findBySlot_Event_IdAndIsDeletedFalse(Long eventId);

    List<Booking> findBySlot_IdAndSlot_Event_IdAndIsDeletedFalse(Long slotId, Long eventId);

}
