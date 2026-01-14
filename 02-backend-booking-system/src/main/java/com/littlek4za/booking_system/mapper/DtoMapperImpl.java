package com.littlek4za.booking_system.mapper;

import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.littlek4za.booking_system.dtos.EventWithSlotCountReponseDto;
import com.littlek4za.booking_system.dtos.LoginResponseDto;
import com.littlek4za.booking_system.entities.Event;
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

    @Override
    public EventWithSlotCountReponseDto eventToEListResponseDto(Event event, Long slotCount) {
        return new EventWithSlotCountReponseDto(
                    event.getId(),
                    event.getEventName(),
                    event.getEventDescription(),
                    event.getEventLocationAddress(),
                    event.getIncludePosition(),
                    event.getLatitude(),
                    event.getLongitude(),
                    event.getSlotType().toString(),
                    event.getCreatedAt(),
                    event.getUpdatedAt(),
                    slotCount
                );
    }

}
