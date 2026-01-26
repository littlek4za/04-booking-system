package com.littlek4za.booking_system.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.littlek4za.booking_system.dtos.SlotRequestDto;
import com.littlek4za.booking_system.dtos.SlotResponseDto;
import com.littlek4za.booking_system.services.SlotService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;



@Slf4j
@RestController
@RequestMapping("/api")
public class SlotController {

    private final SlotService slotService;

    public SlotController(SlotService slotService) {
        this.slotService = slotService;
    }

    @GetMapping(path = "{version}/events/{eventId}/slots", version="1")
    public ResponseEntity<List<SlotResponseDto>> getSlotsByEventV1(@PathVariable("eventId") Long eventId){
        List<SlotResponseDto> slotList = slotService.getSlotsByEvent(eventId);
        return ResponseEntity.status(HttpStatus.OK).body(slotList);
    }

    @GetMapping(path = "{version}/events/{eventId}/slots/{slotId}", version="1")
    public ResponseEntity<SlotResponseDto> getSlotByIdV1(@PathVariable("eventId") Long eventId, @PathVariable("slotId") Long slotId){
        SlotResponseDto slot = slotService.getSlotById(eventId, slotId);
        return ResponseEntity.status(HttpStatus.OK).body(slot);
    }

    @PostMapping(path = "{version}/events/{eventId}/slots", version="1")
    public ResponseEntity<SlotResponseDto> createSlotByEventV1 (@PathVariable("eventId") Long eventId, @Valid @RequestBody SlotRequestDto slotRequestDto) {
        SlotResponseDto slotResponseDto = slotService.createSlotByEvent(eventId, slotRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(slotResponseDto);
    }
    
    @DeleteMapping(path = "{version}/events/{eventId}/slots/{slotId}", version="1")
    public ResponseEntity<SlotResponseDto> deleteSlotByEventAndSlotV1 (@PathVariable("eventId") Long eventId, @PathVariable("slotId") Long slotId){
        slotService.deleteSlotByEventAndSlot(eventId,slotId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PutMapping(path = "{version}/events/{eventId}/slots/{slotId}", version="1")
    public ResponseEntity<SlotResponseDto> putSlotByEventAdnSlotV1 (@PathVariable Long eventId, @PathVariable Long slotId, @Valid @RequestBody SlotRequestDto slotRequestDto) {
        SlotResponseDto slotResponseDto = slotService.putSlotByIdAndEventId(slotId,eventId,slotRequestDto);
        return ResponseEntity.status(HttpStatus.OK).body(slotResponseDto);
    }



}
