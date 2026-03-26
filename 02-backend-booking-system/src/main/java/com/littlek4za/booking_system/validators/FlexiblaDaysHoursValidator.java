package com.littlek4za.booking_system.validators;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.littlek4za.booking_system.exception.AppException;
import com.littlek4za.booking_system.models.EventType;
import com.littlek4za.booking_system.models.InstantRange;

@Component
public class FlexiblaDaysHoursValidator {

    public void validate(EventType eventType, List<InstantRange> flexibleDaysHours) {

        if (eventType != EventType.FLEXIBLE) {
            throw new AppException(
                    "Invalid event type for flexible type validation",
                    HttpStatus.BAD_REQUEST);
        }

        if (flexibleDaysHours == null || flexibleDaysHours.isEmpty()) {
            throw new AppException(
                    "Business days hours cannot be empty",
                    HttpStatus.BAD_REQUEST);
        }

        validateRanges(flexibleDaysHours);
    }

    private void validateRanges(List<InstantRange> ranges) {
        if (ranges == null) {
            return;
        }
        List<Instant[]> times = new ArrayList<>();

        for (InstantRange range : ranges) {
            Instant open = range.getOpen();
            Instant close = range.getClose();

            if (open == null || close == null) {
                throw new AppException("Open and Close time cannot be null", HttpStatus.BAD_REQUEST);
            }

            if (!open.isBefore(close)) {
                throw new AppException("Open time must be before Close time", HttpStatus.BAD_REQUEST);
            }

            times.add(new Instant[]{open, close});
        }

        times.sort(Comparator.comparing((t->t[0])));

        // Overlap Check
        for (int i = 1; i < times.size(); i++) {
        Instant currentOpen = times.get(i)[0];
        Instant previousClose = times.get(i - 1)[1];

        if (currentOpen.isBefore(previousClose)) {
            throw new AppException(
                "Overlapping time ranges detected",
                HttpStatus.BAD_REQUEST
            );
        }
    }
    }

}
