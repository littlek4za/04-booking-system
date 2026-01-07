package com.littlek4za.booking_system.services;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.littlek4za.booking_system.dao.EventRepository;
import com.littlek4za.booking_system.dao.UserRepository;
import com.littlek4za.booking_system.dto.EventSaveRequestDto;
import com.littlek4za.booking_system.dto.EventResponseDto;
import com.littlek4za.booking_system.entities.Event;
import com.littlek4za.booking_system.entities.User;
import com.littlek4za.booking_system.exception.AppException;
import com.littlek4za.booking_system.models.SlotType;

import jakarta.transaction.Transactional;

@Service
public class EventServiceImpl implements EventService {

    private EventRepository eventRepository;
    private UserRepository userRepository;

    public EventServiceImpl(EventRepository eventRepository, UserRepository userRepository) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public EventResponseDto createEvent(EventSaveRequestDto eSaveRequestDto, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException("Unknow User", HttpStatus.NOT_FOUND));
        SlotType slotTypeEnum;
        try {
            slotTypeEnum = SlotType.valueOf(eSaveRequestDto.slotType());
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new AppException("Invalid slot type", HttpStatus.BAD_REQUEST);
        }

        Event newEvent = new Event(
                user,
                eSaveRequestDto.eventName(),
                eSaveRequestDto.eventDescription(),
                eSaveRequestDto.eventLocationName(),
                eSaveRequestDto.includePosition(),
                slotTypeEnum);

        if (Boolean.TRUE.equals(newEvent.getIncludePosition())) {
            newEvent.setPosition(eSaveRequestDto.latitude(), eSaveRequestDto.longitude());
        }

        Event savedEvent = eventRepository.save(newEvent);

        return new EventResponseDto(
                savedEvent.getId(),
                savedEvent.getUser().getUsername(),
                savedEvent.getEventName(),
                savedEvent.getEventDescription(),
                savedEvent.getEventLocationName(),
                savedEvent.getIncludePosition(),
                savedEvent.getLatitude(),
                savedEvent.getLongitude(),
                savedEvent.getSlotType().toString(),
                savedEvent.getCreatedAt());
    }

}
