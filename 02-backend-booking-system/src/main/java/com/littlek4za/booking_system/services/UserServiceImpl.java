package com.littlek4za.booking_system.services;

import java.nio.CharBuffer;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.littlek4za.booking_system.dao.UserRepository;
import com.littlek4za.booking_system.dto.CredentialsDto;
import com.littlek4za.booking_system.dto.JwtUserDto;
import com.littlek4za.booking_system.entities.User;
import com.littlek4za.booking_system.exception.AppException;
import com.littlek4za.booking_system.mapper.DtoMapper;

public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final DtoMapper dtoMapper;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.dtoMapper = null;
    }

    @Override
    public JwtUserDto login(CredentialsDto credentialsDto) {
        
        User user = userRepository.findByUsernameWithRoles(credentialsDto.username())
                    .orElseThrow(()-> new AppException("Unknown user", HttpStatus.NOT_FOUND));

        if(passwordEncoder.matches(CharBuffer.wrap(credentialsDto.password()),user.getPassword())) {
            return dtoMapper.toJwtUserDto(user);
        }
        throw new AppException("Invalid password", HttpStatus.BAD_REQUEST);
    }

}
