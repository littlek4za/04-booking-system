package com.littlek4za.booking_system.controller;

import com.littlek4za.booking_system.security.SecurityUtil;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.littlek4za.booking_system.dtos.BookingRequestDto;
import com.littlek4za.booking_system.dtos.BookingResponseDto;
import com.littlek4za.booking_system.dtos.AttendeeBookingResponseDto;
import com.littlek4za.booking_system.services.BookingService;
import com.littlek4za.booking_system.utils.IpResolver;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api")
public class BookingController {

    private final SecurityUtil securityUtil;
    private final BookingService bookingService;
    private final IpResolver ipResolver;

    public BookingController(BookingService bookingService, SecurityUtil securityUtil, IpResolver ipResolver) {
        this.bookingService = bookingService;
        this.securityUtil = securityUtil;
        this.ipResolver = ipResolver;
    }

    @PostMapping(path = "{version}/slots/{slotId}/bookings", version = "1")
    public ResponseEntity<BookingResponseDto> createBookingV1(@Valid @RequestBody BookingRequestDto bookingRequestDto,
            @PathVariable("slotId") Long slotId, HttpServletRequest request) {

        String clientIp = ipResolver.getClientIp(request);
        BookingResponseDto bookingResponseDto = bookingService.createBooking(bookingRequestDto, slotId, clientIp);
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingResponseDto);
    }

    @GetMapping(path = "{version}/slots/{slotId}/bookings", version = "1")
    public ResponseEntity<List<BookingResponseDto>> getBookingsBySlotV1(@PathVariable("slotId") Long slotId) {
        List<BookingResponseDto> bookingResponseDto = bookingService.getBookingsBySlotId(slotId);
        return ResponseEntity.status(HttpStatus.OK).body(bookingResponseDto);
    }

    @GetMapping(path = "{version}/events/{eventId}/bookings", version = "1")
    public ResponseEntity<List<BookingResponseDto>> getBookingsByEventIdV1(@PathVariable("eventId") Long eventId) {
        List<BookingResponseDto> bookingResponseDto = bookingService.getBookingsByEventId(eventId);
        return ResponseEntity.status(HttpStatus.OK).body(bookingResponseDto);
    }

    @GetMapping(path = "{version}/slots/{slotId}/bookings/count", version = "1")
    public ResponseEntity<Integer> countBySlotIdV1(@PathVariable("slotId") Long slotId) {
        Integer count = bookingService.getCountBySlotId(slotId);
        return ResponseEntity.status(HttpStatus.OK).body(count);
    }

    @PatchMapping(path = "{version}/slots/{slotId}/bookings/{bookingId}/delete", version = "1")
    public ResponseEntity<BookingResponseDto> softDeleteBookingForOrganizerV1(@PathVariable("slotId") Long slotId,
            @PathVariable("bookingId") Long bookingId) {
        BookingResponseDto bookingResponseDto = bookingService.softDeleteBookingAsOrganizer(slotId, bookingId);
        return ResponseEntity.status(HttpStatus.OK).body(bookingResponseDto);
    }

    @PatchMapping(path = "{version}/bookings/{bookingId}/delete", version = "1")
    public ResponseEntity<Long> softDeleteBookingForAttendeeV1(@PathVariable("bookingId") Long bookingId) {
        bookingService.softDeleteBookingAsUserAttendee(bookingId);
        return ResponseEntity.status(HttpStatus.OK).body(bookingId);
    }

    @GetMapping(path = "{version}/bookings/{bookingToken}", version = "1")
    public ResponseEntity<AttendeeBookingResponseDto> getBookingByBookingTokenV1(
            @PathVariable("bookingToken") String bookingToken) {

        AttendeeBookingResponseDto bookingResponseDto;

        if (securityUtil.isGuest()) {
            bookingResponseDto = bookingService.getBookingByTokenAsGuestAttendee(bookingToken);
        } else {
            bookingResponseDto = bookingService.getBookingByTokenAsUserAttendee(bookingToken);
        }

        return ResponseEntity.ok(bookingResponseDto);
    }

    @GetMapping(path = "{version}/bookings", version = "1")
    public ResponseEntity<List<AttendeeBookingResponseDto>> getUserBookingsV1() {
        List<AttendeeBookingResponseDto> bookingUserResponseDtos = bookingService.getUserBookings();
        return ResponseEntity.ok(bookingUserResponseDtos);
    }

}
