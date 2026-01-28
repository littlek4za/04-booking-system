package com.littlek4za.booking_system.validators;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.littlek4za.booking_system.exception.AppException;
import com.littlek4za.booking_system.models.EventType;
import com.littlek4za.booking_system.models.TimeRange;

@Component
public class BusinessDaysHoursValidator {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    public void validate(EventType eventType, Map<Integer, List<TimeRange>> businessDaysHours) {

        if (eventType != EventType.BUSINESS) {
            throw new AppException(
                    "Invalid event type for business type validation",
                    HttpStatus.BAD_REQUEST);
        }

        if (businessDaysHours == null || businessDaysHours.isEmpty()) {
            throw new AppException(
                    "Business days hours cannot be empty",
                    HttpStatus.BAD_REQUEST);
        }

        for (Map.Entry<Integer, List<TimeRange>> entry : businessDaysHours.entrySet()) {
            validateDay(entry.getKey());
            validateRanges(entry.getValue());
        }

    }

    private void validateDay(Integer day) {
        if (day == null || day < 0 || day > 6) {
            throw new AppException("Invalid day of week, must be within (0-6): " + day, HttpStatus.BAD_REQUEST);
        }
    }

    private void validateRanges(List<TimeRange> ranges) {
        if (ranges == null) {
            return;
        }
        List<LocalTime[]> times = new ArrayList<>();

        for (TimeRange range : ranges) {
            LocalTime open = parseTime(range.getOpen());
            LocalTime close = parseTime(range.getClose());

            if (open == null || close == null) {
                throw new AppException("Open and Close time cannot be null", HttpStatus.BAD_REQUEST);
            }

            if (!open.isBefore(close)) {
                throw new AppException("Open time must be before Close time", HttpStatus.BAD_REQUEST);
            }
            times.add(new LocalTime[] { open, close });
        }

        // sort by open time
        times.sort(Comparator.comparing(t -> t[0]));

        // overlap check
        for (int i = 1; i < times.size(); i++) {
            if (!times.get(i)[0].isAfter(times.get(i - 1)[1])) {
                throw new AppException("Overlapping business hours detected", HttpStatus.BAD_REQUEST);
            }
        }
    }

    private LocalTime parseTime(String value) {
        try {
            return LocalTime.parse(value, TIME_FORMAT);
        } catch (Exception e) {
            throw new AppException(
                    "Invalid time format: " + value, HttpStatus.BAD_REQUEST);
        }
    }
}
