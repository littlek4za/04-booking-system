package com.littlek4za.booking_system.mapper;

import com.littlek4za.booking_system.dto.JwtUserDto;
import com.littlek4za.booking_system.dto.UserDto;
import com.littlek4za.booking_system.entities.User;

public interface DtoMapper {

    UserDto toUserDto(User user);
    JwtUserDto toJwtUserDto(User user);

}
