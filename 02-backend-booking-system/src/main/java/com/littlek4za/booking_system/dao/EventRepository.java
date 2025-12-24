package com.littlek4za.booking_system.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.littlek4za.booking_system.entities.Event;

public interface EventRepository extends JpaRepository<Event,Long>{

}
