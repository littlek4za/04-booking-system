package com.littlek4za.booking_system.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.littlek4za.booking_system.dtos.DeleteValidationResponseDto;
import com.littlek4za.booking_system.entities.Booking;
import com.littlek4za.booking_system.models.BookingStatus;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class DeleteValidationService {

    public DeleteValidationResponseDto buildDeleteValidationResponseDto(List<Booking> bookingList) {
        if (bookingList.isEmpty()) {
            return new DeleteValidationResponseDto(true, 0L, 0L, 0L);
        }

        boolean canDelete = false;
        long upcoming = 0L;
        long ongoing = 0L;
        long expired = 0L;

        for (Booking booking : bookingList) {

            BookingStatus bookingStatus = BookingStatus.from(booking.getBookedStartTime(), booking.getBookedEndTime(),
                    booking.isDeleted());

            if (bookingStatus == BookingStatus.DELETED) {
                continue;
            } else if (bookingStatus == BookingStatus.UPCOMING) {
                upcoming++;
            } else if (bookingStatus == BookingStatus.ONGOING) {
                ongoing++;
            } else if (bookingStatus == BookingStatus.EXPIRED) {
                expired++;
            } else {
                log.warn("Unhandle booking status: {}", bookingStatus);
            }
        }

        if (upcoming == 0 && ongoing == 0) {
            canDelete = true;
        }
        return new DeleteValidationResponseDto(canDelete, upcoming, ongoing, expired);

    }

}
