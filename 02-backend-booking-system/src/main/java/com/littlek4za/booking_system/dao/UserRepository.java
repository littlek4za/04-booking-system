package com.littlek4za.booking_system.dao;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.littlek4za.booking_system.entities.User;
import java.util.Optional;


public interface UserRepository extends JpaRepository<User,Long> {

    @EntityGraph(attributePaths = "roleSet")
    Optional<User> findByUsernameWithRoles(String username);

    Boolean existsByUsername (String username);
}
