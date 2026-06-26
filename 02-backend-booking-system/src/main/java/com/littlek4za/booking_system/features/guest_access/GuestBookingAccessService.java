package com.littlek4za.booking_system.features.guest_access;

import com.littlek4za.booking_system.features.guest_access.dto.GuestAccessTokenDto;
import com.littlek4za.booking_system.features.guest_access.dto.GuestBookingCreateAccessRequestDto;
import com.littlek4za.booking_system.features.guest_access.dto.GuestBookingCreateInitRequestDto;
import com.littlek4za.booking_system.features.guest_access.dto.GuestBookingCreateInitResponseDto;
import com.littlek4za.booking_system.features.guest_access.dto.GuestBookingViewAccessRequestDto;
import com.littlek4za.booking_system.features.guest_access.dto.GuestBookingViewInitRequestDto;
import com.littlek4za.booking_system.features.guest_access.dto.GuestBookingViewInitResponseDto;

public interface GuestBookingAccessService {

    GuestBookingViewInitResponseDto initGuestBookingViewAccess(GuestBookingViewInitRequestDto requestDto, String clientIp);

    GuestAccessTokenDto issueGuestBookingViewAccessToken(GuestBookingViewAccessRequestDto requestDto, String clientIp);

    GuestBookingCreateInitResponseDto initGuestBookingCreateAccess(GuestBookingCreateInitRequestDto requestDto, String clientIp);

    GuestAccessTokenDto issueGuestBookingCreateAccessToken(GuestBookingCreateAccessRequestDto requestDto, String clientIp);

    
}
