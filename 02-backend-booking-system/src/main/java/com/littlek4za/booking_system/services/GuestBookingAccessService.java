package com.littlek4za.booking_system.services;

import com.littlek4za.booking_system.dtos.GuestAccessTokenDto;
import com.littlek4za.booking_system.dtos.GuestBookingCreateAccessRequestDto;
import com.littlek4za.booking_system.dtos.GuestBookingCreateInitRequestDto;
import com.littlek4za.booking_system.dtos.GuestBookingCreateInitResponseDto;
import com.littlek4za.booking_system.dtos.GuestBookingViewAccessRequestDto;
import com.littlek4za.booking_system.dtos.GuestBookingViewInitRequestDto;
import com.littlek4za.booking_system.dtos.GuestBookingViewInitResponseDto;

public interface GuestBookingAccessService {

    GuestBookingViewInitResponseDto initGuestBookingViewAccess(GuestBookingViewInitRequestDto requestDto, String clientIp);

    GuestAccessTokenDto issueGuestBookingViewAccessToken(GuestBookingViewAccessRequestDto requestDto, String clientIp);

    GuestBookingCreateInitResponseDto initGuestBookingCreateAccess(GuestBookingCreateInitRequestDto requestDto, String clientIp);

    GuestAccessTokenDto issueGuestBookingCreateAccessToken(GuestBookingCreateAccessRequestDto requestDto, String clientIp);

    
}
