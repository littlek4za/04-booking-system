package com.littlek4za.booking_system.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.littlek4za.booking_system.config.UserAuthProvider;
import com.littlek4za.booking_system.dto.CredentialsDto;
import com.littlek4za.booking_system.dto.JwtUserDto;
import com.littlek4za.booking_system.services.UserService;

@RestController
public class AuthController {

    private UserService userService;
    private UserAuthProvider userAuthProvider;

    public AuthController(UserService userService, UserAuthProvider userAuthProvider) {
        this.userService = userService;
        this.userAuthProvider = userAuthProvider;
    }

    @PostMapping("/login")
    public ResponseEntity<JwtUserDto> login(@RequestBody CredentialsDto credentialsDto){
        JwtUserDto jwtUserDto = userService.login(credentialsDto);
        jwtUserDto.setToken(userAuthProvider.createToken(jwtUserDto));
        return ResponseEntity.ok(jwtUserDto);
    }

    @PostMapping("/register")
    public ReponseEntity<JwTUserDto> register(@RequestBody SignUpDto signUpDto)



}
