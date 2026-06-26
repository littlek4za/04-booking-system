package com.littlek4za.booking_system.features.event;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.littlek4za.booking_system.common.dto.DeleteValidationResponseDto;
import com.littlek4za.booking_system.features.event.dto.EventRequestDto;
import com.littlek4za.booking_system.features.event.dto.EventResponseDto;
import com.littlek4za.booking_system.features.event.dto.EventWithSlotCountReponseDto;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api")
public class EventController {

    private final EventService eventService;


    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping(path= "{version}/events", version ="1")
    public ResponseEntity<EventResponseDto> createEventV1(@Valid @RequestBody EventRequestDto eventRequestDto){
        EventResponseDto eResponseDto = eventService.createEvent(eventRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(eResponseDto);
    }

    @GetMapping(path = "{version}/events", version ="1")
    public ResponseEntity<List<EventWithSlotCountReponseDto>> getEventsV1() {
        List<EventWithSlotCountReponseDto> eventList =eventService.getEvents();
        return ResponseEntity.status(HttpStatus.OK).body(eventList);
    }

    @GetMapping(path = "{version}/events/{id}", version="1")
    public ResponseEntity<EventWithSlotCountReponseDto> getEventByIdV1(@PathVariable("id")Long eventId){
        EventWithSlotCountReponseDto event = eventService.getEventById(eventId);
        return ResponseEntity.status(HttpStatus.OK).body(event);
    }

    @PutMapping(path = "{version}/events/{id}", version="1")
    public ResponseEntity<EventResponseDto> putEventByIdV1(@PathVariable("id") Long eventId, @RequestBody @Valid EventRequestDto eventRequestDto){
        EventResponseDto eResponseDto = eventService.putEventById(eventId,eventRequestDto);
        return ResponseEntity.status(HttpStatus.OK).body(eResponseDto);
    }

    @DeleteMapping(path = "{version}/events/{id}", version="1")
    public ResponseEntity<String> deleteEventByIdV1(@PathVariable("id") Long eventId){
        eventService.deleteEventById(eventId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping(path = "{version}/events/{id}/delete-validation", version="1")
    public ResponseEntity<DeleteValidationResponseDto> eventDeleteValidationV1(@PathVariable("id") Long eventId){
        DeleteValidationResponseDto deleteValidationResponse = eventService.eventDeleteValidation(eventId);
        return ResponseEntity.status(HttpStatus.OK).body(deleteValidationResponse);
    }
    

}
