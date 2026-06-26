package com.littlek4za.booking_system.features.booking.validator;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.littlek4za.booking_system.exception.AppException;
import com.littlek4za.booking_system.exception.model.ErrorCode;
import com.littlek4za.booking_system.features.auth.entity.User;
import com.littlek4za.booking_system.features.booking.Booking;
import com.littlek4za.booking_system.features.booking.BookingRepository;
import com.littlek4za.booking_system.features.booking.dto.BookingRequestDto;
import com.littlek4za.booking_system.features.event.Event;
import com.littlek4za.booking_system.features.event.model.EventType;
import com.littlek4za.booking_system.features.invitation.enitity.Invitation;
import com.littlek4za.booking_system.features.invitation.model.SlotIncludeMode;
import com.littlek4za.booking_system.features.slot.entity.Slot;
import com.littlek4za.booking_system.features.slot.model.InstantRange;
import com.littlek4za.booking_system.features.slot.model.TimeRange;
import com.littlek4za.booking_system.security.SecurityUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class BookingValidator {

    private final BookingRepository bookingRepository;
    private final SecurityUtil securityUtil;

    public BookingValidator(SecurityUtil securityUtil, BookingRepository bookingRepository) {
        this.securityUtil = securityUtil;
        this.bookingRepository = bookingRepository;
    }

    public void validateSlotBelongsToInvitation(Slot slot, Invitation invitation) {
        if (!SlotIncludeMode.ALL_AND_FUTURE.equals(invitation.getSlotIncludeMode())
                && !invitation.getSlotSet().contains(slot)) {
            throw new AppException("Slot does not belong to this invitation", HttpStatus.BAD_REQUEST,
                    ErrorCode.SLOT_INVITATION_MISMATCH);
        }

        if (SlotIncludeMode.ALL_AND_FUTURE.equals(invitation.getSlotIncludeMode())
                && !slot.getEvent().getId().equals(invitation.getEvent().getId())) {
            throw new AppException("Slot does not belong to this invitation", HttpStatus.BAD_REQUEST,
                    ErrorCode.SLOT_INVITATION_MISMATCH);
        }
    }

    public void validateGuestOrUserFields(BookingRequestDto bookingRequestDto) {
        boolean authenticated = securityUtil.isUser();

        if (authenticated) {
            if (bookingRequestDto.email() != null || bookingRequestDto.firstName() != null
                    || bookingRequestDto.lastName() != null) {
                throw new AppException(
                        "Authenticated user booking request should not provide email, firstName and lastName",
                        HttpStatus.BAD_REQUEST, ErrorCode.BOOKING_REQUEST_INVALID);
            }
        } else {
            if (bookingRequestDto.email() == null || bookingRequestDto.firstName() == null
                    || bookingRequestDto.lastName() == null) {
                throw new AppException("Guest booking request requires email, firtName and lastName",
                        HttpStatus.BAD_REQUEST, ErrorCode.BOOKING_REQUEST_INVALID);
            }
        }
    }

    public void validateBookingRequestInfo(BookingRequestDto dto, Invitation invitation, Slot slot, Event event,
            User user) {

        Integer bookingsLimitForEvent = event.getMaxBookingsPerIdentity();

        if (bookingsLimitForEvent != null) {
            Long bookingsCount = bookingRepository.countByUserAndEvent(user, event);

            if (bookingsCount >= bookingsLimitForEvent) {
                throw new AppException(("Booking failed. Max booking limit per user for this event has reached"),
                        HttpStatus.BAD_REQUEST, ErrorCode.EVENT_BOOKING_LIMIT_REACHED);
            }
        }

        Integer bookingsLimitForSlot = slot.getMaxBookingsPerIdentity();

        if (bookingsLimitForSlot != null) {
            Long bookingsCount = bookingRepository.countByUserAndSlot(user, slot);

            if (bookingsCount >= bookingsLimitForSlot) {
                throw new AppException(("Booking failed. Max booking limit per user for this slot has reached"),
                        HttpStatus.BAD_REQUEST, ErrorCode.SLOT_BOOKING_LIMIT_REACHED);
            }
        }

        EventType eventType = invitation.getEvent().getEventType();

        switch (eventType) {
            case FIXED -> validateFixedBooking(dto, slot, invitation);
            case FLEXIBLE -> validateFlexibleBooking(dto, slot, invitation);
            case BUSINESS -> validateBusinessBooking(dto, slot, invitation);
        }
    }

    private void validateFixedBooking(BookingRequestDto dto, Slot slot, Invitation invitation) {
        if (dto.bookedStartTime() != null) {
            throw new AppException(("Fixed Type Event do not require start time field"), HttpStatus.BAD_REQUEST,
                    ErrorCode.BOOKING_REQUEST_INVALID);
        }

        Instant requestedStartTime = slot.getSlotStartTime();

        if (!requestedStartTime.isAfter(Instant.now())) {
            throw new AppException("The selected time has already passed", HttpStatus.BAD_REQUEST,
                ErrorCode.BOOKING_TIME_INVALID);
        }

        int count = bookingRepository.getBookingsCountBySlot(slot);

        if (count >= slot.getMaxBookPerInterval()) {
            throw new AppException(("Booking failed. Slot capacity has been reached."), HttpStatus.BAD_REQUEST,
                    ErrorCode.SLOT_FULL);
        }
    }

    private void validateFlexibleBooking(BookingRequestDto dto, Slot slot, Invitation invitation) {
        if (dto.bookedStartTime() == null) {
            throw new AppException(("Flexible type event booking request require input for start time"),
                    HttpStatus.BAD_REQUEST, ErrorCode.BOOKING_REQUEST_INVALID);
        }

        Instant requestedStartTime = Instant.parse(dto.bookedStartTime());

        if (!requestedStartTime.isAfter(Instant.now())) {
            throw new AppException("The selected time has already passed", HttpStatus.BAD_REQUEST,
                ErrorCode.BOOKING_TIME_INVALID);
        }

        Instant requestedEndTime = requestedStartTime.plus(Duration.ofMinutes(slot.getSlotIntervalMinutes()));

        // check if the time is occupied
        List<Booking> bookingList = bookingRepository.findBySlot(slot);
        boolean timeIsBookedByOthers = bookingList.stream().anyMatch((book) -> {
            Instant occupiedStartTime = book.getBookedStartTime();
            Instant occupiedEndTime = book.getBookedEndTime();

            return requestedStartTime.isBefore(occupiedEndTime) && occupiedStartTime.isBefore(requestedEndTime);
        });

        if (timeIsBookedByOthers) {
            throw new AppException(("The selected time is already booked by others"), HttpStatus.BAD_REQUEST,
                    ErrorCode.SLOT_TIME_ALREADY_BOOKED);
        }

        // check if selected book time is out of timerange
        List<InstantRange> instantRangeList = slot.getFlexibleDaysHours();
        boolean fitsInInstantRange = instantRangeList.stream()
                .anyMatch(range -> !requestedStartTime.isBefore(range.open)
                        && !requestedEndTime.isAfter(range.close));

        if (!fitsInInstantRange) {
            throw new AppException(("The chosen start time + slot interval exceeds the allowed slot time ranges"),
                    HttpStatus.BAD_REQUEST, ErrorCode.BOOKING_REQUEST_INVALID);
        }
    }

    public void validateBusinessBooking(BookingRequestDto dto, Slot slot, Invitation invitation) {
        if (dto.bookedStartTime() == null) {
            throw new AppException(("Business type event booking request require input for start time"),
                    HttpStatus.BAD_REQUEST, ErrorCode.BOOKING_REQUEST_INVALID);
        }

        Instant requestedStartTime = Instant.parse(dto.bookedStartTime());

        if (!requestedStartTime.isAfter(Instant.now())) {
            throw new AppException("The selected time has already passed", HttpStatus.BAD_REQUEST,
                ErrorCode.BOOKING_TIME_INVALID);
        }
        
        Instant requestedEndTime = requestedStartTime.plus(Duration.ofMinutes(slot.getSlotIntervalMinutes()));

        // check if the time is occupied
        List<Booking> bookingList = bookingRepository.findBySlot(slot);
        boolean timeIsBookedByOthers = bookingList.stream().anyMatch((book) -> {
            Instant occupiedStartTime = book.getBookedStartTime();
            Instant occupiedEndTime = book.getBookedEndTime();

            return requestedStartTime.isBefore(occupiedEndTime) && occupiedStartTime.isBefore(requestedEndTime);
        });

        if (timeIsBookedByOthers) {
            throw new AppException(("The selected time is already booked by others"), HttpStatus.BAD_REQUEST,
                    ErrorCode.SLOT_TIME_ALREADY_BOOKED);
        }

        // check if selected book time is out of timerange
        ZoneId zone = ZoneId.of(slot.getBusinessTimeZone());
        ZonedDateTime requestedStartTimeZdt = requestedStartTime.atZone(zone);
        ZonedDateTime reqeustedEndTimeZdt = requestedStartTimeZdt.plusMinutes(slot.getSlotIntervalMinutes());

        LocalDate requestedStartDate = requestedStartTimeZdt.toLocalDate();
        int dayOfWeekStart = requestedStartTimeZdt.getDayOfWeek().getValue() % 7;

        Map<Integer, List<TimeRange>> businessDaysHours = slot.getBusinessDaysHours();
        List<TimeRange> instantRangeList = businessDaysHours.getOrDefault(dayOfWeekStart, List.of())
                .stream()
                .sorted(Comparator.comparing(r -> parseBusinessOpenTime(r.getOpen())))
                .toList();

        if (instantRangeList.isEmpty()) {
            throw new AppException("No business hours defined for the chosen day",
                    HttpStatus.BAD_REQUEST, ErrorCode.BOOKING_REQUEST_INVALID);
        }

        TimeRange lastRange = instantRangeList.get(instantRangeList.size() - 1);

        boolean fitsInInstantRange = instantRangeList.stream()
                .anyMatch(range -> {
                    LocalTime openTime = parseBusinessOpenTime(range.getOpen());
                    ZonedDateTime openZdt = ZonedDateTime.of(requestedStartDate, openTime, zone);
                    ZonedDateTime closeZdt = toBusinessCloseDateTime(range.getClose(), requestedStartDate, zone, openZdt);

                    boolean isLastRange = range.equals(lastRange);

                    if (Boolean.TRUE.equals(slot.getBusinessAllowOT()) && isLastRange) {
                        return !requestedStartTimeZdt.isBefore(openZdt) && !requestedStartTimeZdt.isAfter(closeZdt);
                    } else {
                        return !requestedStartTimeZdt.isBefore(openZdt) && !reqeustedEndTimeZdt.isAfter(closeZdt);
                    }
                });

        if (!fitsInInstantRange) {
            throw new AppException(("The chosen start time + slot interval exceeds the allowed slot time ranges"),
                    HttpStatus.BAD_REQUEST, ErrorCode.BOOKING_REQUEST_INVALID);
        }
    }

    private LocalTime parseBusinessOpenTime(String time) {
        return LocalTime.parse(time);
    }

    private ZonedDateTime toBusinessCloseDateTime(String time, LocalDate requestedStartDate, ZoneId zone,
            ZonedDateTime openZdt) {
        ZonedDateTime closeZdt = "24:00".equals(time)
                ? ZonedDateTime.of(requestedStartDate.plusDays(1), LocalTime.MIDNIGHT, zone)
                : ZonedDateTime.of(requestedStartDate, LocalTime.parse(time), zone);

        if (closeZdt.isBefore(openZdt)) {
            closeZdt = closeZdt.plusDays(1);
        }

        return closeZdt;
    }

}
