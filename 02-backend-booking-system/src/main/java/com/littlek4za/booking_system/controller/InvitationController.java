package com.littlek4za.booking_system.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.littlek4za.booking_system.dtos.InvitationRequestDto;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@Slf4j
@RestController
@RequestMapping("/api")
public class InvitationController {

    @PostMapping(path = "{version}/invitations/", version="1")
    public ResponseEntity<InvitationReponseDto> postMethodName(@Valid @RequestBody InvitationRequestDto invitationRequestDto) {
        //TODO: process POST request
        
        return ResponseEntity
    }
    

}
