package com.littlek4za.booking_system.mapper;

import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.littlek4za.booking_system.dto.JwtUserDto;
import com.littlek4za.booking_system.dto.SignUpDto;
import com.littlek4za.booking_system.dto.UserDto;
import com.littlek4za.booking_system.entities.User;

@Component
public class DtoMapperImpl implements DtoMapper {

    @Override
    public UserDto userToUserDto(User user) {

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
    public JwtUserDto userToJwtUserDto(User user) {
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

    @Override
    public User signUpToUser(SignUpDto signUpDto) {
        return new User(
            signUpDto.username(), 
            signUpDto.email(), 
            signUpDto.firstName(), 
            signUpDto.lastName());
    }

}
