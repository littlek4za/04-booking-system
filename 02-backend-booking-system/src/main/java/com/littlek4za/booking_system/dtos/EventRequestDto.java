package com.littlek4za.booking_system.dtos;

import com.littlek4za.booking_system.customValidator.ValidSlotType;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EventRequestDto(

        @NotBlank(message = "is required")
        @Size(min = 1, max = 350, message = "Event Name must be between 1 and 350 characters")
        String eventName,

        @Size(max = 2500, message = "Event Description must not exceed 2500 characters")
        String eventDescription,

        @NotBlank
        @Size(min = 1, max =1000, message = "Event Location Address must be between 1 and 1000 characters")
        String eventLocationAddress,

        @NotNull(message = "is required")
        Boolean includePosition,

        @DecimalMin(value = "-90.0", message = "Latitude must be >= -90")
        @DecimalMax(value = "90.0", message = "Latitude must be <= 90")
        Double latitude,

        @DecimalMin(value = "-180.0", message = "Longitude must be >= -180")
        @DecimalMax(value = "180.0", message = "Longitude must be <= 180")
        Double longitude,

        @ValidSlotType
        String slotType) {

    @AssertTrue(message = "Latitude and longitude must be provided when includePosition is true")
    private boolean isPositionValid() {
        if (!Boolean.TRUE.equals(includePosition)) {
            return true; // position not required
        }
        return latitude != null && longitude != null;
    }

}
