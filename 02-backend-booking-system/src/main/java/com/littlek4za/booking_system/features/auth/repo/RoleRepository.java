package com.littlek4za.booking_system.features.auth.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.littlek4za.booking_system.features.auth.entity.Role;
import com.littlek4za.booking_system.features.auth.model.RoleType;


public interface RoleRepository extends JpaRepository<Role,Long>{

    Role findByRoleName(RoleType roleName);

}
