package com.littlek4za.booking_system.services;

import java.util.List;

import com.littlek4za.booking_system.dtos.BookingRequestDto;
import com.littlek4za.booking_system.dtos.OrganizerBookingResponseDto;
import com.littlek4za.booking_system.dtos.SlotBookedTimeResponseDto;
import com.littlek4za.booking_system.dtos.AttendeeBookingResponseDto;

public interface BookingService {

    AttendeeBookingResponseDto createBooking(BookingRequestDto bookingRequestDto, Long slotId, String clientIp);

    List<OrganizerBookingResponseDto> getOrganizerBookingsBySlotId(Long slotId);

    List<OrganizerBookingResponseDto> getOrganizerBookingsByEventId(Long eventId);

    OrganizerBookingResponseDto softDeleteBookingAsOrganizer(Long slotId, Long bookingId);

    OrganizerBookingResponseDto softDeleteBookingAsAttendee(Long bookingId);

    OrganizerBookingResponseDto softDeleteBookingAsGuestAttendee(Long bookingId);

    AttendeeBookingResponseDto getBookingByTokenAsAttendee(String bookingToken);

    List<AttendeeBookingResponseDto> getAttendeeBookings();

    List<SlotBookedTimeResponseDto> getSlotBookedTimesBySlotId(Long slotId);

}
