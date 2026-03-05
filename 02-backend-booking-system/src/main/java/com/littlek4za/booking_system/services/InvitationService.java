package com.littlek4za.booking_system.services;

import java.util.List;

import com.littlek4za.booking_system.dtos.InvitationRequestDto;
import com.littlek4za.booking_system.dtos.InvitationResponseDto;
import com.littlek4za.booking_system.dtos.InvitationValidationResponseDto;

public interface InvitationService {

    InvitationResponseDto createInvitation(InvitationRequestDto invitationRequestDto, Long eventId);

    List<InvitationResponseDto> getInvitationsByEventId(Long eventId);

    Long deleteInvitationByEventAndId(Long eventId, Long invitationId);

    InvitationValidationResponseDto validateAccessToken(String token);
    
}
