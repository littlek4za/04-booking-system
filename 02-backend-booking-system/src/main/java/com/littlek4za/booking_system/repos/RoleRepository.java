package com.littlek4za.booking_system.repos;

import org.springframework.data.jpa.repository.JpaRepository;

import com.littlek4za.booking_system.entities.Role;
import com.littlek4za.booking_system.models.RoleType;


public interface RoleRepository extends JpaRepository<Role,Long>{

    Role findByRoleName(RoleType roleName);

}
