package com.littlek4za.booking_system.common.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CaptchaResponseDto(
        boolean success,

        @JsonProperty("challenge_ts") 
        String challengeTs,

        String hostname,

        @JsonProperty("error-codes") 
        List<String> errorCodes

        // double score, // for v3 only

        // String action // for v3 only

        ) {

}
