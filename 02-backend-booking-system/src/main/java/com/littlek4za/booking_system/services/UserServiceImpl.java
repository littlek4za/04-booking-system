package com.littlek4za.booking_system.services;

import java.nio.CharBuffer;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.littlek4za.booking_system.dao.RoleRepository;
import com.littlek4za.booking_system.dao.UserRepository;
import com.littlek4za.booking_system.dto.LoginRequestDto;
import com.littlek4za.booking_system.dto.LoginResponseDto;
import com.littlek4za.booking_system.dto.SignUpRequestDto;
import com.littlek4za.booking_system.entities.User;
import com.littlek4za.booking_system.exception.AppException;
import com.littlek4za.booking_system.mapper.DtoMapper;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final DtoMapper dtoMapper;

    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository,
            PasswordEncoder passwordEncoder, DtoMapper dtoMapper) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.dtoMapper = dtoMapper;
    }

    @Override
    public LoginResponseDto login(LoginRequestDto loginRequestDto) {

        User user = userRepository.findByUsername(loginRequestDto.username())
                .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));

        if (passwordEncoder.matches(CharBuffer.wrap(loginRequestDto.password()), user.getPassword())) {
            return dtoMapper.userToLoginResponseDto(user);
        }
        throw new AppException("Invalid password", HttpStatus.BAD_REQUEST);
    }

    @Override
    public LoginResponseDto register(SignUpRequestDto signUpRequestDto) {

        if (userRepository.existsByUsername(signUpRequestDto.username())) {
            throw new AppException("Username already exists", HttpStatus.BAD_REQUEST);
        }

        if (userRepository.existsByEmail(signUpRequestDto.email())) {
            throw new AppException("Email already exists", HttpStatus.BAD_REQUEST);
        }

        User user = User.createRegistered(signUpRequestDto.username(), passwordEncoder.encode(signUpRequestDto.password()),
                signUpRequestDto.email(), signUpRequestDto.firstName(), signUpRequestDto.lastName());

        user.addRole(roleRepository.findByRoleName("ROLE_ATTENDEE"));
        user.addRole(roleRepository.findByRoleName("ROLE_ORGANIZER"));

        User savedUser = userRepository.save(user);

        return dtoMapper.userToLoginResponseDto(savedUser);
    }

}
