package com.littlek4za.booking_system.utils;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.littlek4za.booking_system.dtos.EventResponseDto;
import com.littlek4za.booking_system.dtos.EventWithSlotCountReponseDto;
import com.littlek4za.booking_system.dtos.InvitationRequestDto;
import com.littlek4za.booking_system.dtos.InvitationResponseDto;
import com.littlek4za.booking_system.dtos.LoginResponseDto;
import com.littlek4za.booking_system.dtos.SlotRequestDto;
import com.littlek4za.booking_system.dtos.SlotResponseDto;
import com.littlek4za.booking_system.entities.Event;
import com.littlek4za.booking_system.entities.Invitation;
import com.littlek4za.booking_system.entities.Slot;
import com.littlek4za.booking_system.entities.User;
import com.littlek4za.booking_system.models.SlotIncludeMode;

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
                slotCount);
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
                slot.getBusinessDaysHours(),
                slot.getFlexibleDaysHours(),
                slot.getCreatedAt(),
                slot.getUpdatedAt());
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
                slotRequestDto.businessDaysHours(),
                slotRequestDto.flexibleDaysHours());
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
                event.getUpdatedAt());
    }

    @Override
    public Invitation toInvitation(InvitationRequestDto invitationRequestDto, Event event, User user) {
        return new Invitation(
                event,
                user,
                invitationRequestDto.expiresAt(),
                invitationRequestDto.maxUsage(),
                SlotIncludeMode.valueOf(invitationRequestDto.slotIncludeMode()),
                invitationRequestDto.requiredLogin(),
                invitationRequestDto.maxUsagePerUser());
    }

    @Override
    public InvitationResponseDto toInvitationResponseDto(Invitation invitation, List<String> slotNames) {

        return new InvitationResponseDto(
                invitation.getId(),
                invitation.getEvent().getId(),
                invitation.getUser().getId(),
                invitation.getExpiresAt(),
                invitation.getMaxUsage(),
                invitation.getUsedCount(),
                invitation.getAccessToken(),
                invitation.getSlotIncludeMode(),
                invitation.isRequiredLogin(),
                invitation.getMaxUsagePerUser(),
                invitation.getCreatedAt(),
                slotNames
            );
    }

}
