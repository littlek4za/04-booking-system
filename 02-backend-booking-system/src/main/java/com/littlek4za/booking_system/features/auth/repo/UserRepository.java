package com.littlek4za.booking_system.features.auth.repo;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.littlek4za.booking_system.features.auth.entity.User;

import java.util.Optional;


public interface UserRepository extends JpaRepository<User,Long> {

    @EntityGraph(attributePaths = "roleSet")
    Optional<User> findByUsername(String username);

    Boolean existsByUsername (String username);

    Boolean existsByEmail (String email);

    Optional<User> findByEmail(String email);
}
