package com.littlek4za.booking_system.mapper;

import com.littlek4za.booking_system.dto.JwtUserDto;
import com.littlek4za.booking_system.dto.SignUpDto;
import com.littlek4za.booking_system.dto.UserDto;
import com.littlek4za.booking_system.entities.User;

public interface DtoMapper {

    UserDto userToUserDto(User user);
    JwtUserDto userToJwtUserDto(User user);
    User signUpToUser(SignUpDto signUpDto);

}
