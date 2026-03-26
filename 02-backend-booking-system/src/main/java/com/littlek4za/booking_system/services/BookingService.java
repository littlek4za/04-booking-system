package com.littlek4za.booking_system.services;

import java.util.List;

import com.littlek4za.booking_system.dtos.BookingRequestDto;
import com.littlek4za.booking_system.dtos.BookingResponseDto;

public interface BookingService {

    BookingResponseDto createBooking(BookingRequestDto bookingRequestDto, Long slotId);

    List<BookingResponseDto> getBookingsBySlotId(Long slotId);

    Integer getCountBySlotId(Long slotId);

    List<BookingResponseDto> getBookingsByEventId(Long eventId);

}
