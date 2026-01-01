package com.littlek4za.booking_system.mapper;

import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.littlek4za.booking_system.dto.LoginResponseDto;
import com.littlek4za.booking_system.entities.User;

@Component
public class DtoMapperImpl implements DtoMapper {

    @Override
    public LoginResponseDto userToLoginResponseDto(User user) {
        return LoginResponseDto.builder()
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .roleSet(user.getRoleSet()
                        .stream()
                        .map(role -> role.getRoleName())
                        .collect(Collectors.toSet()))
                .username(user.getUsername())
                .build();
    }

}
