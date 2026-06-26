package com.littlek4za.booking_system.common.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.littlek4za.booking_system.common.dto.DeleteValidationResponseDto;
import com.littlek4za.booking_system.features.booking.Booking;
import com.littlek4za.booking_system.features.booking.model.BookingStatus;

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

    public boolean canDelete(List<Booking> bookingList) {
        if (bookingList.isEmpty()) {
            return true;
        }

        boolean canDelete = false;
        long upcoming = 0L;
        long ongoing = 0L;

        for (Booking booking : bookingList) {

            BookingStatus bookingStatus = BookingStatus.from(booking.getBookedStartTime(), booking.getBookedEndTime(),
                    booking.isDeleted());

            if (bookingStatus == BookingStatus.DELETED) {
                continue;
            } else if (bookingStatus == BookingStatus.UPCOMING) {
                upcoming++;
            } else if (bookingStatus == BookingStatus.ONGOING) {
                ongoing++;
            } else {
                log.warn("Unhandle booking status: {}", bookingStatus);
            }
        }

        if (upcoming == 0 && ongoing == 0) {
            canDelete = true;
        }
        
        return canDelete;
    }

}
