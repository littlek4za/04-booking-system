package com.littlek4za.booking_system.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.littlek4za.booking_system.dtos.InvitationRequestDto;
import com.littlek4za.booking_system.dtos.InvitationResponseDto;
import com.littlek4za.booking_system.services.InvitationService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@Slf4j
@RestController
@RequestMapping("/api")
public class InvitationController {

    private final InvitationService invitationService;

    public InvitationController(InvitationService invitationService) {
        this.invitationService = invitationService;
    }

    @PostMapping(path = "{version}/events/{eventId}/invitations", version="1")
    public ResponseEntity<InvitationResponseDto> createInvitationV1(@Valid @RequestBody InvitationRequestDto invitationRequestDto, @PathVariable("eventId") Long eventId) {
        
        InvitationResponseDto invitationResponseDto = invitationService.createInvitation(invitationRequestDto, eventId);
        
        return ResponseEntity.status(HttpStatus.OK).body(invitationResponseDto);
    }
    

}
