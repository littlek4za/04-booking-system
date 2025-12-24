package com.littlek4za.booking_system.services;

import com.littlek4za.booking_system.dto.CredentialsDto;
import com.littlek4za.booking_system.dto.JwtUserDto;

public interface UserService {

    JwtUserDto login(CredentialsDto credentialsDto);

}
