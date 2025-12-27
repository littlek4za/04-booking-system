package com.littlek4za.booking_system.services;

import com.littlek4za.booking_system.dto.CredentialsDto;
import com.littlek4za.booking_system.dto.JwtUserDto;
import com.littlek4za.booking_system.dto.SignUpDto;

public interface UserService {

    JwtUserDto login(CredentialsDto credentialsDto);

    JwtUserDto register(SignUpDto signUpDto);

}
