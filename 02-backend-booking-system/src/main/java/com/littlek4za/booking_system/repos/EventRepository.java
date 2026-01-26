package com.littlek4za.booking_system.repos;

import org.springframework.data.jpa.repository.JpaRepository;

import com.littlek4za.booking_system.entities.Event;
import com.littlek4za.booking_system.entities.User;
import java.util.List;
import java.util.Optional;


public interface EventRepository extends JpaRepository<Event,Long>{

    List<Event> findByUser(User user);
    Optional<Event> findByIdAndUser(Long id, User user);

    int deleteByIdAndUserId(Long id, Long userId);

}
