package com.littlek4za.booking_system.services;

import com.littlek4za.booking_system.dtos.LoginRequestDto;
import com.littlek4za.booking_system.dtos.SignUpRequestDto;
import com.littlek4za.booking_system.dtos.UserDto;

public interface UserService {

    UserDto login(LoginRequestDto loginRequestDto, String ip);

    void register(SignUpRequestDto signUpRequestDto);

}
