package com.littlek4za.booking_system.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.littlek4za.booking_system.dtos.BookingRequestDto;
import com.littlek4za.booking_system.dtos.BookingResponseDto;
import com.littlek4za.booking_system.services.BookingService;

import jakarta.validation.Valid;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;



@RestController
@RequestMapping("/api")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping(path = "{version}/slots/{slotId}/bookings", version = "1")
    public ResponseEntity<BookingResponseDto> createBookingV1(@Valid @RequestBody BookingRequestDto bookingRequestDto, @PathVariable("slotId") Long slotId) {
        BookingResponseDto bookingResponseDto = bookingService.createBooking(bookingRequestDto, slotId);
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingResponseDto);
    }

    @GetMapping(path = "{version}/slots/{slotId}/bookings", version = "1")
    public ResponseEntity<List<BookingResponseDto>> getBookingsBySlotV1(@PathVariable ("slotId") Long slotId) {
        List<BookingResponseDto> bookingResponseDto = bookingService.getBookingsBySlot(slotId);
        return ResponseEntity.status(HttpStatus.OK).body(bookingResponseDto);
    }
    
    @GetMapping(path = "{version}/slots/{slotId}/bookings/count", version = "1") 
    public ResponseEntity<Integer> countBySlotIdV1(@PathVariable ("slotId") Long slotId) {
        Integer count = bookingService.getCountBySlotId(slotId);
        return ResponseEntity.status(HttpStatus.OK).body(count);
    }
    

}
