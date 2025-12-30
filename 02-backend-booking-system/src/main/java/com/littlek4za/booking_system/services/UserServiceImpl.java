package com.littlek4za.booking_system.services;

import java.nio.CharBuffer;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.littlek4za.booking_system.dao.RoleRepository;
import com.littlek4za.booking_system.dao.UserRepository;
import com.littlek4za.booking_system.dto.CredentialsDto;
import com.littlek4za.booking_system.dto.JwtUserDto;
import com.littlek4za.booking_system.dto.SignUpDto;
import com.littlek4za.booking_system.entities.User;
import com.littlek4za.booking_system.exception.AppException;
import com.littlek4za.booking_system.mapper.DtoMapper;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final DtoMapper dtoMapper;

    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, DtoMapper dtoMapper) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.dtoMapper = dtoMapper;
    }

    @Override
    public JwtUserDto login(CredentialsDto credentialsDto) {
        
        User user = userRepository.findByUsername(credentialsDto.username())
                    .orElseThrow(()-> new AppException("Unknown user", HttpStatus.NOT_FOUND));

        if(passwordEncoder.matches(CharBuffer.wrap(credentialsDto.password()),user.getPassword())) {
            return dtoMapper.userToJwtUserDto(user);
        }
        throw new AppException("Invalid password", HttpStatus.BAD_REQUEST);
    }

    @Override
    public JwtUserDto register(SignUpDto signUpDto) {

        if(userRepository.existsByUsername(signUpDto.username())) {
            throw new AppException("Username already exists", HttpStatus.BAD_REQUEST);
        }

        if(userRepository.existsByEmail(signUpDto.email())) {
            throw new AppException("Email already exists", HttpStatus.BAD_REQUEST);
        }

        User user = dtoMapper.signUpToUser(signUpDto);
        user.setPassword(passwordEncoder.encode(signUpDto.password()));
        
        user.addRole(roleRepository.findByRoleName("ROLE_ATTENDEE"));
        user.addRole(roleRepository.findByRoleName("ROLE_ORGANIZER"));

        User savedUser = userRepository.save(user);

        return dtoMapper.userToJwtUserDto(savedUser);
    }

}
