package com.littlek4za.booking_system.services;

import com.littlek4za.booking_system.dtos.GuestAccessTokenDto;
import com.littlek4za.booking_system.dtos.GuestBookingViewAccessRequestDto;
import com.littlek4za.booking_system.dtos.GuestBookingViewInitRequestDto;
import com.littlek4za.booking_system.dtos.GuestBookingViewInitResponseDto;

public interface GuestBookingAccessService {

    GuestBookingViewInitResponseDto initGuestBookingViewAccess(GuestBookingViewInitRequestDto requestDto);

    GuestAccessTokenDto issueGuestBookingViewAccessToken(GuestBookingViewAccessRequestDto requestDto);

    
}
