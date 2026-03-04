package com.littlek4za.booking_system.services;

import com.littlek4za.booking_system.dtos.InvitationRequestDto;
import com.littlek4za.booking_system.dtos.InvitationResponseDto;

public interface InvitationService {

    InvitationResponseDto createInvitation(InvitationRequestDto invitationRequestDto, Long eventId);
    
}
