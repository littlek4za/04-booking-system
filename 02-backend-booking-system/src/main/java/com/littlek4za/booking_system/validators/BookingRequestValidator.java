package com.littlek4za.booking_system.validators;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.littlek4za.booking_system.dtos.BookingRequestDto;
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

    private final SecurityUtil securityUtil;

    public BookingRequestValidator(SecurityUtil securityUtil) {
        this.securityUtil = securityUtil;
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
    }

    private void validateFlexibleBooking(BookingRequestDto dto, Slot slot, Invitation invitation) {
        if (dto.bookedStartTime() == null) {
            throw new AppException(("Flexible Type Event require input for start time"), HttpStatus.BAD_REQUEST);
        }

        Instant bookedStartTimeInstant = Instant.parse(dto.bookedStartTime());
        Instant bookedEndTimeInstant = bookedStartTimeInstant.plus(slot.getSlotIntervalMinutes(), ChronoUnit.MINUTES);

        List<InstantRange> instantRangeList = slot.getFlexibleDaysHours();

        boolean fitsInInstantRange = instantRangeList.stream()
                .anyMatch(range -> !bookedStartTimeInstant.isBefore(range.open)
                        && !bookedEndTimeInstant.isAfter(range.close));

        if (!fitsInInstantRange) {
            throw new AppException(("The chosen start time + slot interval exceeds the allowed slot time ranges"),
                    HttpStatus.BAD_REQUEST);
        }
    }

    public void validateBusinessBooking(BookingRequestDto dto, Slot slot, Invitation invitation) {
        if (dto.bookedStartTime() == null) {
            throw new AppException(("Business Type Event require input for start time"), HttpStatus.BAD_REQUEST);
        }

        Instant bookedStartTimeInstant = Instant.parse(dto.bookedStartTime());
        ZoneId zone = ZoneId.of(slot.getBusinessTimeZone());
        ZonedDateTime zdt = bookedStartTimeInstant.atZone(zone);
        LocalTime requestedStart = zdt.toLocalTime();
        LocalTime reqeustedEnd = requestedStart.plusMinutes(slot.getSlotIntervalMinutes());

        int dayOfWeek = zdt.getDayOfWeek().getValue() % 7;

        Map<Integer, List<TimeRange>> businessDaysHours = slot.getBusinessDaysHours();
        List<TimeRange> instantRangeList = businessDaysHours.getOrDefault(dayOfWeek, List.of());

        boolean fitsInInstantRange = instantRangeList.stream()
                .anyMatch(range -> {
                    LocalTime open = LocalTime.parse(range.getOpen());
                    LocalTime close = LocalTime.parse(range.getClose());

                    return !requestedStart.isBefore(open) && !reqeustedEnd.isAfter(close);
                });

        if (!fitsInInstantRange) {
            throw new AppException(("The chosen start time + slot interval exceeds the allowed slot time ranges"),
                    HttpStatus.BAD_REQUEST);
        }
    }

}
