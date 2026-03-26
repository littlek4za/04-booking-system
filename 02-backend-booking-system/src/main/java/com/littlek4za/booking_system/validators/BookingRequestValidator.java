package com.littlek4za.booking_system.validators;

import com.littlek4za.booking_system.repos.BookingRepository;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.littlek4za.booking_system.dtos.BookingRequestDto;
import com.littlek4za.booking_system.entities.Booking;
import com.littlek4za.booking_system.entities.Invitation;
import com.littlek4za.booking_system.entities.Slot;
import com.littlek4za.booking_system.exception.AppException;
import com.littlek4za.booking_system.models.EventType;
import com.littlek4za.booking_system.models.InstantRange;
import com.littlek4za.booking_system.models.SlotIncludeMode;
import com.littlek4za.booking_system.models.TimeRange;
import com.littlek4za.booking_system.security.SecurityUtil;

@Component
public class BookingRequestValidator {

    private final BookingRepository bookingRepository;
    private final SecurityUtil securityUtil;

    public BookingRequestValidator(SecurityUtil securityUtil, BookingRepository bookingRepository) {
        this.securityUtil = securityUtil;
        this.bookingRepository = bookingRepository;
    }

    public void validateSlotBelongsToInvitation(Slot slot, Invitation invitation) {
        if (!SlotIncludeMode.ALL_AND_FUTURE.equals(invitation.getSlotIncludeMode())
                && !invitation.getSlotSet().contains(slot)) {
            throw new AppException("Slot id does not belong to this invitation", HttpStatus.BAD_REQUEST);
        }

        if (SlotIncludeMode.ALL_AND_FUTURE.equals(invitation.getSlotIncludeMode())
                && !slot.getEvent().getId().equals(invitation.getEvent().getId())) {

            throw new AppException("Slot does not belong to this invitation event", HttpStatus.BAD_REQUEST);

        }
    }

    public void validateGuestOrUserFields(BookingRequestDto bookingRequestDto) {
        boolean authenticated = securityUtil.isAuthenticated();

        if (authenticated) {
            if (bookingRequestDto.email() != null || bookingRequestDto.firstName() != null
                    || bookingRequestDto.lastName() != null) {
                throw new AppException("Authenticated user should not provide email, firstName and lastName",
                        HttpStatus.BAD_REQUEST);
            }
        } else {
            if (bookingRequestDto.email() == null || bookingRequestDto.firstName() == null
                    || bookingRequestDto.lastName() == null) {
                throw new AppException("Guest booking requires email, firtName and lastName",
                        HttpStatus.BAD_REQUEST);
            }
        }
    }

    public void validateBookingInfo(BookingRequestDto dto, Invitation invitation, Slot slot) {

        EventType eventType = invitation.getEvent().getEventType();

        switch (eventType) {
            case FIXED -> validateFixedBooking(dto, slot, invitation);
            case FLEXIBLE -> validateFlexibleBooking(dto, slot, invitation);
            case BUSINESS -> validateBusinessBooking(dto, slot, invitation);
        }
    }

    private void validateFixedBooking(BookingRequestDto dto, Slot slot, Invitation invitation) {
        if (dto.bookedStartTime() != null) {
            throw new AppException(("Fixed Type Event do not need start time"), HttpStatus.BAD_REQUEST);
        }

        int count = bookingRepository.getBookingsCountBySlot(slot);

        if(count >= slot.getMaxBookPerInterval()){
            throw new AppException(("Booking failed. Max booking limit for this slot has reached."), HttpStatus.BAD_REQUEST);
        }
    }

    private void validateFlexibleBooking(BookingRequestDto dto, Slot slot, Invitation invitation) {
        if (dto.bookedStartTime() == null) {
            throw new AppException(("Flexible Type Event require input for start time"), HttpStatus.BAD_REQUEST);
        }
        
        Instant requestedStartTime = Instant.parse(dto.bookedStartTime());
        Instant requestedEndTime = requestedStartTime.plus(Duration.ofMinutes(slot.getSlotIntervalMinutes()));

        // check if the time is occupied
        List<Booking> bookingList = bookingRepository.getBySlot(slot);
        boolean timeIsBookedByOthers = bookingList.stream().anyMatch((book)->{
            Instant occupiedStartTime = book.getBookedStartTime();
            Instant occupiedEndTime = book.getBookedEndTime();

            return requestedStartTime.isBefore(occupiedEndTime) && occupiedStartTime.isBefore(requestedEndTime);
        });

        if(timeIsBookedByOthers){
            throw new AppException(("The selected time is booked by others"), HttpStatus.BAD_REQUEST);
        }

        // check if selected book time is out of timerange
        List<InstantRange> instantRangeList = slot.getFlexibleDaysHours();
        boolean fitsInInstantRange = instantRangeList.stream()
                .anyMatch(range -> !requestedStartTime.isBefore(range.open)
                        && !requestedEndTime.isAfter(range.close));

        if (!fitsInInstantRange) {
            throw new AppException(("The chosen start time + slot interval exceeds the allowed slot time ranges"),
                    HttpStatus.BAD_REQUEST);
        }
    }

    public void validateBusinessBooking(BookingRequestDto dto, Slot slot, Invitation invitation) {
        if (dto.bookedStartTime() == null) {
            throw new AppException(("Business Type Event require input for start time"), HttpStatus.BAD_REQUEST);
        }

        Instant requestedStartTime = Instant.parse(dto.bookedStartTime());
        Instant requestedEndTime = requestedStartTime.plus(Duration.ofMinutes(slot.getSlotIntervalMinutes()));
        

        // check if the time is occupied
        List<Booking> bookingList = bookingRepository.getBySlot(slot);
        boolean timeIsBookedByOthers = bookingList.stream().anyMatch((book)->{
            Instant occupiedStartTime = book.getBookedStartTime();
            Instant occupiedEndTime = book.getBookedEndTime();

            return requestedStartTime.isBefore(occupiedEndTime) && occupiedStartTime.isBefore(requestedEndTime);
        });

        if(timeIsBookedByOthers){
            throw new AppException(("The selected time is booked by others"), HttpStatus.BAD_REQUEST);
        }

        // check if selected book time is out of timerange
        ZoneId zone = ZoneId.of(slot.getBusinessTimeZone());
        ZonedDateTime requestedStartTimeZdt = requestedStartTime.atZone(zone);
        ZonedDateTime reqeustedEndTimeZdt = requestedStartTimeZdt.plusMinutes(slot.getSlotIntervalMinutes());

        LocalDate requestedStartDate = requestedStartTimeZdt.toLocalDate();
        int dayOfWeekStart = requestedStartTimeZdt.getDayOfWeek().getValue() % 7;

        Map<Integer, List<TimeRange>> businessDaysHours = slot.getBusinessDaysHours();
        List<TimeRange> instantRangeList = businessDaysHours.getOrDefault(dayOfWeekStart, List.of());

        boolean fitsInInstantRange = instantRangeList.stream()
                .anyMatch(range -> {
                    LocalTime openTime = LocalTime.parse(range.getOpen());
                    LocalTime closeTime = LocalTime.parse(range.getClose());
                    ZonedDateTime openZdt = ZonedDateTime.of(requestedStartDate, openTime, zone);
                    ZonedDateTime closeZdt = ZonedDateTime.of(requestedStartDate, closeTime, zone);

                    if (closeZdt.isBefore(openZdt)) {
                        closeZdt = closeZdt.plusDays(1);
                    }

                    if (Boolean.TRUE.equals(slot.getBusinessAllowOT())) {
                        return !requestedStartTimeZdt.isBefore(openZdt) && !requestedStartTimeZdt.isAfter(closeZdt);
                    } else {
                        return !requestedStartTimeZdt.isBefore(openZdt) && !reqeustedEndTimeZdt.isAfter(closeZdt);
                    }
                });

        if (!fitsInInstantRange) {
            throw new AppException(("The chosen start time + slot interval exceeds the allowed slot time ranges"),
                    HttpStatus.BAD_REQUEST);
        }
    }

}
