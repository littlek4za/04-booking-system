package com.littlek4za.booking_system.services;

import com.littlek4za.booking_system.dto.LoginRequestDto;
import com.littlek4za.booking_system.dto.LoginResponseDto;
import com.littlek4za.booking_system.dto.SignUpRequestDto;

public interface UserService {

    LoginResponseDto login(LoginRequestDto loginRequestDto);

    LoginResponseDto register(SignUpRequestDto signUpRequestDto);

}
