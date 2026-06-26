package com.littlek4za.booking_system.features.auth;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.littlek4za.booking_system.common.utils.IpResolver;
import com.littlek4za.booking_system.features.auth.dto.LoginRequestDto;
import com.littlek4za.booking_system.features.auth.dto.LoginResponseDto;
import com.littlek4za.booking_system.features.auth.dto.SignUpRequestDto;
import com.littlek4za.booking_system.features.auth.dto.UserAccessTokenDto;
import com.littlek4za.booking_system.features.auth.dto.UserDto;
import com.littlek4za.booking_system.security.JwtTokenService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final UserService userService;
    private final JwtTokenService jwtTokenService;
    private final IpResolver ipResolver;

    public AuthController(UserService userService, JwtTokenService jwtTokenService, IpResolver ipResolver) {
        this.userService = userService;
        this.jwtTokenService = jwtTokenService;
        this.ipResolver = ipResolver;
    }

    @PostMapping(path = "{version}/login", version = "1")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto loginRequestDto, HttpServletRequest request){

        String clientIp = ipResolver.getClientIp(request);

        UserDto userDto = userService.login(loginRequestDto, clientIp);
        String token = jwtTokenService.createUserToken(userDto);
        UserAccessTokenDto userAccessTokenDto = jwtTokenService.toUserAccessTokenDto(token);
        LoginResponseDto loginResponseDto = new LoginResponseDto(userDto, userAccessTokenDto);
        return ResponseEntity.ok(loginResponseDto);
    }

    @PostMapping(path = "{version}/register", version ="1")
    public ResponseEntity<LoginResponseDto> register(@Valid @RequestBody SignUpRequestDto signUpRequestDto){
        userService.register(signUpRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

}
