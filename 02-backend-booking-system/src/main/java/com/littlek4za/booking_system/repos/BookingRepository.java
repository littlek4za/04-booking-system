package com.littlek4za.booking_system.repos;

import org.springframework.data.jpa.repository.JpaRepository;

import com.littlek4za.booking_system.entities.Booking;

public interface BookingRepository extends JpaRepository<Booking,Long> {

}
