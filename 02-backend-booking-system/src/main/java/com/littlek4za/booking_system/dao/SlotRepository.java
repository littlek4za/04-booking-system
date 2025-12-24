package com.littlek4za.booking_system.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.littlek4za.booking_system.entities.Slot;

public interface SlotRepository extends JpaRepository<Slot,Long>{

}
