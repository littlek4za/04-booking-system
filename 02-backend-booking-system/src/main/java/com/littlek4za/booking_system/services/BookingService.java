package com.littlek4za.booking_system.services;

import java.util.List;

import com.littlek4za.booking_system.dtos.BookingRequestDto;
import com.littlek4za.booking_system.dtos.BookingResponseDto;
import com.littlek4za.booking_system.dtos.AttendeeBookingResponseDto;

public interface BookingService {

    BookingResponseDto createBooking(BookingRequestDto bookingRequestDto, Long slotId, String clientIp);

    List<BookingResponseDto> getBookingsBySlotId(Long slotId);

    Integer getCountBySlotId(Long slotId);

    List<BookingResponseDto> getBookingsByEventId(Long eventId);

    BookingResponseDto softDeleteBookingAsOrganizer(Long slotId, Long bookingId);

    BookingResponseDto softDeleteBookingAsUserAttendee(Long bookingId);

    BookingResponseDto softDeleteBookingAsGuestAttendee(Long bookingId);

    AttendeeBookingResponseDto getBookingByTokenAsUserAttendee(String bookingToken);

    AttendeeBookingResponseDto getBookingByTokenAsGuestAttendee(String bookingToken);

    List<AttendeeBookingResponseDto> getUserBookings();

}
