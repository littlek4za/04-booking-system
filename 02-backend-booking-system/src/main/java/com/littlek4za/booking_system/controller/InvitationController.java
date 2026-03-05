package com.littlek4za.booking_system.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.littlek4za.booking_system.dtos.InvitationRequestDto;
import com.littlek4za.booking_system.dtos.InvitationResponseDto;
import com.littlek4za.booking_system.dtos.InvitationValidationResponseDto;
import com.littlek4za.booking_system.services.InvitationService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;


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

    @GetMapping(path = "{version}/events/{eventId}/invitations", version="1")
    public ResponseEntity<List<InvitationResponseDto>> getInvitationsByEventIdV1(@PathVariable("eventId") Long eventId) {
        List<InvitationResponseDto> invitationResponseDtos = invitationService.getInvitationsByEventId(eventId);

        return ResponseEntity.ok(invitationResponseDtos);
    }
    
    @DeleteMapping(path = "{version}/events/{eventId}/invitations/{invitationId}", version="1")
    public ResponseEntity<Void> deleteInvitationByEventAndIdV1 (@PathVariable("eventId") Long eventId, @PathVariable("invitationId") Long invitationId){
        invitationService.deleteInvitationByEventAndId(eventId, invitationId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
    
    @GetMapping(path = "{version}/invitations/{token}/validate", version="1")
    public ResponseEntity<InvitationValidationResponseDto> validateToken(@PathVariable("token") String token) {
        InvitationValidationResponseDto validationResponseDto = invitationService.validateAccessToken(token);
        return ResponseEntity.ok(validationResponseDto);
    }
    

}
