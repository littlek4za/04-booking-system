package com.littlek4za.booking_system.dtos;

public record LoginResponseDto(
    UserDto userDto,
    UserAccessTokenDto userAccessTokenDto
) {

}
