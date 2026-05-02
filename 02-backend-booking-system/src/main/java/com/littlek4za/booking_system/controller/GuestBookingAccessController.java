package com.littlek4za.booking_system.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.littlek4za.booking_system.dtos.GuestAccessTokenDto;
import com.littlek4za.booking_system.dtos.GuestBookingCreateAccessRequestDto;
import com.littlek4za.booking_system.dtos.GuestBookingCreateInitRequestDto;
import com.littlek4za.booking_system.dtos.GuestBookingCreateInitResponseDto;
import com.littlek4za.booking_system.dtos.GuestBookingViewAccessRequestDto;
import com.littlek4za.booking_system.dtos.GuestBookingViewInitRequestDto;
import com.littlek4za.booking_system.dtos.GuestBookingViewInitResponseDto;
import com.littlek4za.booking_system.services.GuestBookingAccessService;
import com.littlek4za.booking_system.utils.IpResolver;

import jakarta.servlet.http.HttpServletRequest;


@RestController
@RequestMapping("/api")
public class GuestBookingAccessController {
    private final GuestBookingAccessService guestAccessService;
    private final IpResolver ipResolver;

    public GuestBookingAccessController(GuestBookingAccessService guestAccessService, IpResolver ipResolver) {
        this.guestAccessService = guestAccessService;
        this.ipResolver = ipResolver;
    }

    @PostMapping(path = "{version}/guest/bookings/view/init", version = "1")
    public ResponseEntity<GuestBookingViewInitResponseDto> initGuestBookingViewAccessV1(
            @RequestBody GuestBookingViewInitRequestDto requestDto, HttpServletRequest request) {

        String clientIp = ipResolver.getClientIp(request);

        GuestBookingViewInitResponseDto responseDto = guestAccessService.initGuestBookingViewAccess(requestDto, clientIp);
        return ResponseEntity.ok(responseDto);
    }

    @PostMapping(path = "{version}/guest/bookings/view/access", version = "1")
    public ResponseEntity<GuestAccessTokenDto> issueGuestBookingViewAccessTokenV1(
            @RequestBody GuestBookingViewAccessRequestDto requestDto, HttpServletRequest request) {

        String clientIp = ipResolver.getClientIp(request);

        GuestAccessTokenDto responseDto = guestAccessService.issueGuestBookingViewAccessToken(requestDto, clientIp);
        return ResponseEntity.ok(responseDto);
    }

    @PostMapping(path = "{version}/guest/bookings/create/init", version = "1")
    public ResponseEntity<GuestBookingCreateInitResponseDto> initGuestBookingCreateAccessV1(
            @RequestBody GuestBookingCreateInitRequestDto requestDto, HttpServletRequest request) {

        String clientIp = ipResolver.getClientIp(request);

        GuestBookingCreateInitResponseDto responseDto = guestAccessService.initGuestBookingCreateAccess(requestDto, clientIp);
        return ResponseEntity.ok(responseDto);
    }

    @PostMapping(path = "{version}/guest/bookings/create/access", version = "1")
    public ResponseEntity<GuestAccessTokenDto> issueGuestBookingCreateAccessTokenV1(
            @RequestBody GuestBookingCreateAccessRequestDto requestDto, HttpServletRequest request) {

        String clientIp = ipResolver.getClientIp(request);

        GuestAccessTokenDto responseDto = guestAccessService.issueGuestBookingCreateAccessToken(requestDto, clientIp);
        return ResponseEntity.ok(responseDto);
    }
    
}
