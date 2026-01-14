package com.littlek4za.booking_system.services;

import com.littlek4za.booking_system.dtos.LoginRequestDto;
import com.littlek4za.booking_system.dtos.LoginResponseDto;
import com.littlek4za.booking_system.dtos.SignUpRequestDto;

public interface UserService {

    LoginResponseDto login(LoginRequestDto loginRequestDto);

    LoginResponseDto register(SignUpRequestDto signUpRequestDto);

}
