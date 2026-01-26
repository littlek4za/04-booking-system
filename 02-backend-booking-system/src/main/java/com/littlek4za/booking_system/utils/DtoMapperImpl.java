package com.littlek4za.booking_system.utils;

import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.littlek4za.booking_system.dtos.EventResponseDto;
import com.littlek4za.booking_system.dtos.EventWithSlotCountReponseDto;
import com.littlek4za.booking_system.dtos.LoginResponseDto;
import com.littlek4za.booking_system.dtos.SlotRequestDto;
import com.littlek4za.booking_system.dtos.SlotResponseDto;
import com.littlek4za.booking_system.entities.Event;
import com.littlek4za.booking_system.entities.Slot;
import com.littlek4za.booking_system.entities.User;

@Component
public class DtoMapperImpl implements DtoMapper {

    @Override
    public LoginResponseDto toLoginResponseDto(User user) {
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
    public EventWithSlotCountReponseDto toEventWithSlotCountResponseDto(Event event, Long slotCount) {
        return new EventWithSlotCountReponseDto(
                    event.getId(),
                    event.getEventName(),
                    event.getEventDescription(),
                    event.getEventLocationAddress(),
                    event.getIncludePosition(),
                    event.getLatitude(),
                    event.getLongitude(),
                    event.getEventType().toString(),
                    event.getCreatedAt(),
                    event.getUpdatedAt(),
                    slotCount
                );
    }

    @Override
    public SlotResponseDto toSlotResponseDto(Slot slot) {
        return new SlotResponseDto(
                slot.getEvent().getId(),
                slot.getId(),
                slot.getSlotName(),
                slot.getSlotDescription(),
                slot.getSlotStartTime(),
                slot.getSlotEndTime(),
                slot.getMaxBook(),
                slot.getSlotIntervalMinutes(),
                slot.getSlotFrequencyIntervalMinutes(),
                slot.getWorkingDaysHours(),
                slot.getCreatedAt(),
                slot.getUpdatedAt()
        );
    }

    @Override
    public Slot toSlot(SlotRequestDto slotRequestDto, Event event) {
        return new Slot(
            event,
            slotRequestDto.slotName(),
            slotRequestDto.slotDescription(),
            slotRequestDto.slotStartTime(),
            slotRequestDto.slotEndTime(),
            slotRequestDto.maxBook(),
            slotRequestDto.slotIntervalMinutes(),
            slotRequestDto.slotFrequencyIntervalMinutes(),
            slotRequestDto.workingDaysHours()
        );
    }

    @Override
    public EventResponseDto toEventResponseDto(Event event) {
        return new EventResponseDto(
            event.getId(),
            event.getUser().getUsername(),
            event.getEventName(),
            event.getEventDescription(),
            event.getEventLocationAddress(),
            event.getIncludePosition(),
            event.getLatitude(),
            event.getLongitude(),
            event.getEventType().toString(),
            event.getCreatedAt(),
            event.getUpdatedAt()
        );
    }

}
