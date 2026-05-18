package com.littlek4za.booking_system.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.littlek4za.booking_system.dtos.BookingRequestDto;
import com.littlek4za.booking_system.dtos.OrganizerBookingResponseDto;
import com.littlek4za.booking_system.dtos.SlotBookedTimeResponseDto;
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

    private final BookingService bookingService;
    private final IpResolver ipResolver;

    public BookingController(BookingService bookingService, IpResolver ipResolver) {
        this.bookingService = bookingService;
        this.ipResolver = ipResolver;
    }

    @PostMapping(path = "{version}/slots/{slotId}/bookings", version = "1")
    public ResponseEntity<OrganizerBookingResponseDto> createBookingV1(@Valid @RequestBody BookingRequestDto bookingRequestDto,
            @PathVariable("slotId") Long slotId, HttpServletRequest request) {

        String clientIp = ipResolver.getClientIp(request);
        OrganizerBookingResponseDto organizerBookingResponseDto = bookingService.createBooking(bookingRequestDto, slotId, clientIp);
        return ResponseEntity.status(HttpStatus.CREATED).body(organizerBookingResponseDto);
    }

    // full calendar using specifically by organizer
    @GetMapping(path = "{version}/slots/{slotId}/bookings", version = "1")
    public ResponseEntity<List<OrganizerBookingResponseDto>> getOrganizerBookingsBySlotIdV1(@PathVariable("slotId") Long slotId) {
        List<OrganizerBookingResponseDto> organizerBookingResponseDto = bookingService.getOrganizerBookingsBySlotId(slotId);
        return ResponseEntity.status(HttpStatus.OK).body(organizerBookingResponseDto);
    }

    // for guest and user
    @GetMapping(path = "{version}/slots/{slotId}/booked-times", version = "1")
    public ResponseEntity<List<SlotBookedTimeResponseDto>> getSlotBookedTimesBySlotIdV1(@PathVariable("slotId") Long slotId) {
        List<SlotBookedTimeResponseDto> slotBookedTimeResponseDtos = bookingService.getSlotBookedTimesBySlotId(slotId);
        return ResponseEntity.status(HttpStatus.OK).body(slotBookedTimeResponseDtos);
    }

    @GetMapping(path = "{version}/events/{eventId}/bookings", version = "1")
    public ResponseEntity<List<OrganizerBookingResponseDto>> getOrganizerBookingsByEventIdV1(@PathVariable("eventId") Long eventId) {
        List<OrganizerBookingResponseDto> organizerBookingResponseDto = bookingService.getOrganizerBookingsByEventId(eventId);
        return ResponseEntity.status(HttpStatus.OK).body(organizerBookingResponseDto);
    }

    @PatchMapping(path = "{version}/slots/{slotId}/bookings/{bookingId}/delete", version = "1")
    public ResponseEntity<OrganizerBookingResponseDto> softDeleteBookingForOrganizerV1(@PathVariable("slotId") Long slotId,
            @PathVariable("bookingId") Long bookingId) {
        OrganizerBookingResponseDto organizerBookingResponseDto = bookingService.softDeleteBookingAsOrganizer(slotId, bookingId);
        return ResponseEntity.status(HttpStatus.OK).body(organizerBookingResponseDto);
    }

    @PatchMapping(path = "{version}/bookings/{bookingId}/delete", version = "1")
    public ResponseEntity<Long> softDeleteBookingAsAttendeeV1(@PathVariable("bookingId") Long bookingId) {
        bookingService.softDeleteBookingAsAttendee(bookingId);
        return ResponseEntity.status(HttpStatus.OK).body(bookingId);
    }

    @GetMapping(path = "{version}/bookings/{bookingToken}", version = "1")
    public ResponseEntity<AttendeeBookingResponseDto> getBookingByBookingTokenV1(
            @PathVariable("bookingToken") String bookingToken) {

        AttendeeBookingResponseDto bookingResponseDto;

        bookingResponseDto = bookingService.getBookingByTokenAsAttendee(bookingToken);

        return ResponseEntity.ok(bookingResponseDto);
    }

    @GetMapping(path = "{version}/bookings", version = "1")
    public ResponseEntity<List<AttendeeBookingResponseDto>> getAttendeeBookingsV1() {
        List<AttendeeBookingResponseDto> bookingUserResponseDtos = bookingService.getAttendeeBookings();
        return ResponseEntity.ok(bookingUserResponseDtos);
    }

}
