package com.littlek4za.booking_system.features.booking;

import java.util.List;

import com.littlek4za.booking_system.features.booking.dto.AttendeeBookingResponseDto;
import com.littlek4za.booking_system.features.booking.dto.BookingRequestDto;
import com.littlek4za.booking_system.features.booking.dto.OrganizerBookingResponseDto;
import com.littlek4za.booking_system.features.booking.dto.SlotBookedTimeResponseDto;

public interface BookingService {

    AttendeeBookingResponseDto createBooking(BookingRequestDto bookingRequestDto, Long slotId, String clientIp);

    List<OrganizerBookingResponseDto> getOrganizerBookingsBySlotId(Long slotId);

    List<OrganizerBookingResponseDto> getOrganizerBookingsByEventId(Long eventId);

    OrganizerBookingResponseDto softDeleteBookingAsOrganizer(Long slotId, Long bookingId);

    OrganizerBookingResponseDto softDeleteBookingAsAttendee(Long bookingId);

    AttendeeBookingResponseDto getBookingByTokenAsAttendee(String bookingToken);

    List<AttendeeBookingResponseDto> getAttendeeBookings();

    List<SlotBookedTimeResponseDto> getSlotBookedTimesBySlotId(Long slotId);

}
