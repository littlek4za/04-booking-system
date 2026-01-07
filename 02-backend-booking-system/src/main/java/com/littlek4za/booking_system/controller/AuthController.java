package com.littlek4za.booking_system.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.littlek4za.booking_system.dto.LoginRequestDto;
import com.littlek4za.booking_system.dto.LoginResponseDto;
import com.littlek4za.booking_system.dto.SignUpRequestDto;
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
        LoginResponseDto loginResponseDto = userService.login(loginRequestDto);
        loginResponseDto.setToken(userAuthProvider.createToken(loginResponseDto));
        return ResponseEntity.ok(loginResponseDto);
    }

    @PostMapping(path = "{version}/register", version ="1")
    public ResponseEntity<LoginResponseDto> register(@Valid @RequestBody SignUpRequestDto signUpRequestDto){
        LoginResponseDto loginResponseDto = userService.register(signUpRequestDto);
        return ResponseEntity.ok(loginResponseDto);
        
    }

}
