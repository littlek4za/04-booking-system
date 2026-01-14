package com.littlek4za.booking_system.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.littlek4za.booking_system.dtos.EventRequestDto;
import com.littlek4za.booking_system.dtos.EventResponseDto;
import com.littlek4za.booking_system.dtos.EventWithSlotCountReponseDto;
import com.littlek4za.booking_system.security.AuthUserPrincipal;
import com.littlek4za.booking_system.services.EventService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Slf4j
@RestController
@RequestMapping("/api")
public class EventController {

    private final EventService eventService;


    public EventController(EventService eventService) {
        this.eventService = eventService;
    }


    @PostMapping(path= "{version}/events", version ="1")
    public ResponseEntity<EventResponseDto> createEventV1(@AuthenticationPrincipal AuthUserPrincipal userAuthPrincipal, @RequestBody @Valid EventRequestDto eventRequestDto){
        EventResponseDto eResponseDto = eventService.createEvent(eventRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(eResponseDto);
    }

    @GetMapping(path = "{version}/events", version ="1")
    public ResponseEntity<List<EventWithSlotCountReponseDto>> getEventsByAuthUserV1(@AuthenticationPrincipal AuthUserPrincipal userPrincipal) {
        List<EventWithSlotCountReponseDto> eventList =eventService.getEvents();
        return ResponseEntity.status(HttpStatus.OK).body(eventList);
    }

    @GetMapping(path = "{version}/events/{id}", version="1")
    public ResponseEntity<EventWithSlotCountReponseDto> getEventByIdV1(@PathVariable("id")Long eventId, @AuthenticationPrincipal AuthUserPrincipal userAuthPrincipal){
        EventWithSlotCountReponseDto event = eventService.getEventById(eventId);
        return ResponseEntity.status(HttpStatus.OK).body(event);
    }

    @PutMapping(path = "{version}/events/{id}", version="1")
    public ResponseEntity<EventResponseDto> putEventByIdV1(@PathVariable("id") Long eventId, @AuthenticationPrincipal AuthUserPrincipal userAuthPrincipal, @RequestBody @Valid EventRequestDto eventRequestDto){
        EventResponseDto eResponseDto = eventService.putEventById(eventId,eventRequestDto);
        return ResponseEntity.status(HttpStatus.OK).body(eResponseDto);
    }

    @DeleteMapping(path = "{version}/events/{id}", version="1")
    public ResponseEntity<String> deleteEventByIdV1(@PathVariable("id") Long eventId, @AuthenticationPrincipal AuthUserPrincipal userAuthPrincipal){
        eventService.deleteEventById(eventId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
    

}
