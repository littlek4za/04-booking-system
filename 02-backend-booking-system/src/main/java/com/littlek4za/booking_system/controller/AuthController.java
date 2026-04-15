package com.littlek4za.booking_system.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.littlek4za.booking_system.dtos.LoginRequestDto;
import com.littlek4za.booking_system.dtos.LoginResponseDto;
import com.littlek4za.booking_system.dtos.SignUpRequestDto;
import com.littlek4za.booking_system.dtos.UserAccessTokenDto;
import com.littlek4za.booking_system.dtos.UserDto;
import com.littlek4za.booking_system.security.UserAuthProvider;
import com.littlek4za.booking_system.services.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class AuthController {

    private UserService userService;
    private UserAuthProvider userAuthProvider;

    public AuthController(UserService userService, UserAuthProvider userAuthProvider) {
        this.userService = userService;
        this.userAuthProvider = userAuthProvider;
    }

    @PostMapping(path = "{version}/login", version = "1")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto loginRequestDto){
        UserDto userDto = userService.login(loginRequestDto);
        String token = userAuthProvider.createToken(userDto);
        UserAccessTokenDto userAccessTokenDto = userAuthProvider.toUserAccessTokenDto(token);
        LoginResponseDto loginResponseDto = new LoginResponseDto(userDto, userAccessTokenDto);
        return ResponseEntity.ok(loginResponseDto);
    }

    @PostMapping(path = "{version}/register", version ="1")
    public ResponseEntity<LoginResponseDto> register(@Valid @RequestBody SignUpRequestDto signUpRequestDto){
        userService.register(signUpRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

}
