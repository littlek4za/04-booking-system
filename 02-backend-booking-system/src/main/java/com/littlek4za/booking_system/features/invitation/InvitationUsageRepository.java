package com.littlek4za.booking_system.features.invitation;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.littlek4za.booking_system.features.invitation.enitity.InvitationUsage;
import com.littlek4za.booking_system.features.invitation.enitity.InvitationUsageId;

public interface InvitationUsageRepository extends JpaRepository <InvitationUsage, InvitationUsageId>{

    @Query("""
            SELECT iu FROM InvitationUsage iu
            WHERE iu.id.userId = :userId
            AND iu.id.invitationId = :invitationId
            """)
    Optional<InvitationUsage> findByUserIdAndInvitationId(Long userId, Long invitationId);

}
