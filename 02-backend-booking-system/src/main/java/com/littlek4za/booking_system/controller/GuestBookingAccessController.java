package com.littlek4za.booking_system.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.littlek4za.booking_system.dtos.GuestAccessTokenDto;
import com.littlek4za.booking_system.dtos.GuestBookingViewAccessRequestDto;
import com.littlek4za.booking_system.dtos.GuestBookingViewInitRequestDto;
import com.littlek4za.booking_system.dtos.GuestBookingViewInitResponseDto;
import com.littlek4za.booking_system.services.GuestBookingAccessService;

@RestController
@RequestMapping("/api")
public class GuestBookingAccessController {
    private final GuestBookingAccessService guestAccessService;

    public GuestBookingAccessController(GuestBookingAccessService guestAccessService) {
        this.guestAccessService = guestAccessService;
    }

    @GetMapping(path = "{version}/guest/bookings/view/init", version = "1")
    public ResponseEntity<GuestBookingViewInitResponseDto> initGuestBookingViewAccessV1(
            @RequestBody GuestBookingViewInitRequestDto requestDto) {
        GuestBookingViewInitResponseDto responseDto = guestAccessService.initGuestBookingViewAccess(requestDto);
        return ResponseEntity.ok(responseDto);
    }

    @PostMapping(path = "{version}/guest/bookings/view/access", version = "1")
    public ResponseEntity<GuestAccessTokenDto> issueGuestBookingViewAccessTokenV1(
            @RequestBody GuestBookingViewAccessRequestDto requestDto) {

        GuestAccessTokenDto responseDto = guestAccessService.issueGuestBookingViewAccessToken(requestDto);

        return ResponseEntity.ok(responseDto);
    }
}
