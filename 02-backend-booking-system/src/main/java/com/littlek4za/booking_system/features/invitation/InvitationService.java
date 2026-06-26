package com.littlek4za.booking_system.features.invitation;

import java.util.List;

import com.littlek4za.booking_system.features.invitation.dto.InvitationRequestDto;
import com.littlek4za.booking_system.features.invitation.dto.InvitationResponseDto;
import com.littlek4za.booking_system.features.invitation.dto.InvitationValidationResponseDto;

public interface InvitationService {

    InvitationResponseDto createInvitation(InvitationRequestDto invitationRequestDto, Long eventId);

    List<InvitationResponseDto> getInvitationsByEventId(Long eventId);

    Long deleteInvitationByEventAndId(Long eventId, Long invitationId);

    InvitationValidationResponseDto validateInvitationAccess(String token);

    InvitationResponseDto getInvitationByToken(String token);

    List<InvitationResponseDto> getInvitationsByEventIdAndSlotId(Long eventId, Long slotId);
    
}
