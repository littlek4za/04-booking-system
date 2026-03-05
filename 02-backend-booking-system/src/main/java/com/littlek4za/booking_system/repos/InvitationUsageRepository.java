package com.littlek4za.booking_system.repos;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.littlek4za.booking_system.entities.InvitationUsage;
import com.littlek4za.booking_system.entities.InvitationUsageId;

public interface InvitationUsageRepository extends JpaRepository <InvitationUsage, InvitationUsageId>{

    Optional<InvitationUsage> findByUserIdAndInvitationId(Long userId, Long invitationId);

}
