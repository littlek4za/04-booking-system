package com.littlek4za.booking_system.mapper;

import com.littlek4za.booking_system.dto.LoginResponseDto;
import com.littlek4za.booking_system.entities.User;

public interface DtoMapper {

    LoginResponseDto userToLoginResponseDto(User user);

}
