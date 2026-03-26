package com.littlek4za.booking_system.repos;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.littlek4za.booking_system.entities.Booking;
import com.littlek4za.booking_system.entities.Slot;

public interface BookingRepository extends JpaRepository<Booking,Long> {

    boolean existsByBookingToken(String token);

    List<Booking> getBySlot(Slot slot);

     @Query("""
            SELECT COUNT(b)
            FROM Booking b
            WHERE b.slot = :slot
            """)
    int getBookingsCountBySlot(@Param("slot") Slot slot);

}
