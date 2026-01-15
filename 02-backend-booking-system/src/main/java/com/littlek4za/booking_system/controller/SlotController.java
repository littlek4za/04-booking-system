package com.littlek4za.booking_system.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.littlek4za.booking_system.dtos.SlotResponseDto;
import com.littlek4za.booking_system.services.SlotService;

import lombok.extern.slf4j.Slf4j;

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



}
