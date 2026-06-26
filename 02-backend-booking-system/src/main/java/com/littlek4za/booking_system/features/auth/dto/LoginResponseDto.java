package com.littlek4za.booking_system.features.auth.dto;

public record LoginResponseDto(
    UserDto userDto,
    UserAccessTokenDto userAccessTokenDto
) {

}
