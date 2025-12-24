package com.littlek4za.booking_system.mapper;

import java.util.stream.Collectors;

import com.littlek4za.booking_system.dto.JwtUserDto;
import com.littlek4za.booking_system.dto.UserDto;
import com.littlek4za.booking_system.entities.User;

public class DtoMapperImpl implements DtoMapper {

    @Override
    public UserDto toUserDto(User user) {

        UserDto userDto = UserDto.builder()
                            .createdAt(user.getCreatedAt())
                            .id(user.getId())
                            .email(user.getEmail())
                            .firstName(user.getFirstName())
                            .lastName(user.getLastName())
                            .username(user.getUsername())
                            .roleSet(user.getRoleSet()
                                        .stream()
                                        .map(role -> role.getRoleName())
                                        .collect(Collectors.toSet()))
                            .build();

        return userDto;
                            
    }

    @Override
    public JwtUserDto toJwtUserDto(User user) {
        return JwtUserDto.builder()
                            .email(user.getEmail())
                            .firstName(user.getFirstName())
                            .lastName(user.getLastName())
                            .roleSet(user.getRoleSet()
                                            .stream()
                                            .map(role->role.getRoleName())
                                            .collect(Collectors.toSet()))
                            .username(user.getUsername())
                            .build();
    }

}
