package com.littlek4za.booking_system.services;

import java.nio.CharBuffer;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.littlek4za.booking_system.dtos.LoginRequestDto;
import com.littlek4za.booking_system.dtos.SignUpRequestDto;
import com.littlek4za.booking_system.dtos.UserDto;
import com.littlek4za.booking_system.entities.User;
import com.littlek4za.booking_system.exception.AppException;
import com.littlek4za.booking_system.exception.model.ErrorCode;
import com.littlek4za.booking_system.models.RoleType;
import com.littlek4za.booking_system.repos.RoleRepository;
import com.littlek4za.booking_system.repos.UserRepository;
import com.littlek4za.booking_system.utils.DtoMapper;

import jakarta.transaction.Transactional;

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
    public UserDto login(LoginRequestDto loginRequestDto) {

        User user = userRepository.findByUsername(loginRequestDto.username())
                .orElseThrow(() -> new AppException("Invalid username or password", HttpStatus.UNAUTHORIZED, ErrorCode.INVALID_CREDENTIALS));

        if (passwordEncoder.matches(CharBuffer.wrap(loginRequestDto.password()), user.getPassword())) {
            return dtoMapper.toUserDto(user);
        }
        throw new AppException("Invalid username or password", HttpStatus.UNAUTHORIZED, ErrorCode.INVALID_CREDENTIALS);
    }

    @Override
    @Transactional
    public void register(SignUpRequestDto signUpRequestDto) {

        if (userRepository.existsByUsername(signUpRequestDto.username())) {
            throw new AppException("Username already exists", HttpStatus.BAD_REQUEST, ErrorCode.USERNAME_ALREADY_REGISTERED);
        }

        if (userRepository.existsByEmail(signUpRequestDto.email())) {
            throw new AppException("Email already exists", HttpStatus.BAD_REQUEST, ErrorCode.EMAIL_ALREADY_REGISTERED);
        }

        User user = User.createRegistered(signUpRequestDto.username(), passwordEncoder.encode(signUpRequestDto.password()),
                signUpRequestDto.email(), signUpRequestDto.firstName(), signUpRequestDto.lastName());

        user.addRole(roleRepository.findByRoleName(RoleType.ROLE_ATTENDEE));
        user.addRole(roleRepository.findByRoleName(RoleType.ROLE_ORGANIZER));

        userRepository.save(user);
    }

}
