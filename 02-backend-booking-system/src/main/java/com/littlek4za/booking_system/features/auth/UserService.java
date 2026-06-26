package com.littlek4za.booking_system.features.auth;

import com.littlek4za.booking_system.features.auth.dto.LoginRequestDto;
import com.littlek4za.booking_system.features.auth.dto.SignUpRequestDto;
import com.littlek4za.booking_system.features.auth.dto.UserDto;

public interface UserService {

    UserDto login(LoginRequestDto loginRequestDto, String ip);

    void register(SignUpRequestDto signUpRequestDto);

}
