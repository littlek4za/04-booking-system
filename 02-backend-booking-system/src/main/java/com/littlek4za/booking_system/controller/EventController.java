package com.littlek4za.booking_system.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.littlek4za.booking_system.dto.EventSaveRequestDto;
import com.littlek4za.booking_system.dto.EventResponseDto;
import com.littlek4za.booking_system.security.AuthUserPrincipal;
import com.littlek4za.booking_system.services.EventService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class EventController {

    private final EventService eventService;


    public EventController(EventService eventService) {
        this.eventService = eventService;
    }


    @PostMapping(path= "{version}/events", version ="1")
    public ResponseEntity<EventResponseDto> createEventV1(@AuthenticationPrincipal AuthUserPrincipal userAuthPrincipal, @RequestBody @Valid EventSaveRequestDto eventSaveRequestDto){
        EventResponseDto eSaveResponseDto = eventService.createEvent(eventSaveRequestDto,userAuthPrincipal.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(eSaveResponseDto);
    }

}
