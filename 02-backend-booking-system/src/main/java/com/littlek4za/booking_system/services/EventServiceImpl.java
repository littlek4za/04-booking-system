package com.littlek4za.booking_system.services;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.littlek4za.booking_system.dtos.EventRequestDto;
import com.littlek4za.booking_system.dtos.EventResponseDto;
import com.littlek4za.booking_system.dtos.EventWithSlotCountReponseDto;
import com.littlek4za.booking_system.entities.Event;
import com.littlek4za.booking_system.entities.User;
import com.littlek4za.booking_system.exception.AppException;
import com.littlek4za.booking_system.mapper.DtoMapper;
import com.littlek4za.booking_system.models.SlotType;
import com.littlek4za.booking_system.repos.EventRepository;
import com.littlek4za.booking_system.repos.SlotRepository;
import com.littlek4za.booking_system.repos.UserRepository;
import com.littlek4za.booking_system.repos.projections.EventSlotCount;
import com.littlek4za.booking_system.security.SecurityUtil;

import jakarta.transaction.Transactional;

@Service
public class EventServiceImpl implements EventService {

        private final EventRepository eventRepository;
        private final UserRepository userRepository;
        private final DtoMapper dtoMapper;
        private final SlotRepository slotRepository;
        public final SecurityUtil securityUtil;

        public EventServiceImpl(EventRepository eventRepository, UserRepository userRepository, DtoMapper dtoMapper,
                        SlotRepository slotRepository, SecurityUtil securityUtil) {
                this.eventRepository = eventRepository;
                this.userRepository = userRepository;
                this.dtoMapper = dtoMapper;
                this.slotRepository = slotRepository;
                this.securityUtil = securityUtil;
        }

        @Override
        @Transactional
        public EventResponseDto createEvent(EventRequestDto eRequestDto) {
                User user = userRepository.findById(this.securityUtil.getCurrentAuthUserId())
                                .orElseThrow(() -> new AppException("Unknown User", HttpStatus.NOT_FOUND));
                SlotType slotTypeEnum = stringtoSlotType(eRequestDto.slotType());

                Event newEvent = new Event(
                                user,
                                eRequestDto.eventName(),
                                eRequestDto.eventDescription(),
                                eRequestDto.eventLocationAddress(),
                                eRequestDto.includePosition(),
                                slotTypeEnum);

                if (Boolean.TRUE.equals(newEvent.getIncludePosition())) {
                        newEvent.setPosition(eRequestDto.latitude(), eRequestDto.longitude());
                }

                Event savedEvent = eventRepository.save(newEvent);

                return new EventResponseDto(
                                savedEvent.getId(),
                                savedEvent.getUser().getUsername(),
                                savedEvent.getEventName(),
                                savedEvent.getEventDescription(),
                                savedEvent.getEventLocationAddress(),
                                savedEvent.getIncludePosition(),
                                savedEvent.getLatitude(),
                                savedEvent.getLongitude(),
                                savedEvent.getSlotType().toString(),
                                savedEvent.getCreatedAt(),
                                savedEvent.getUpdatedAt());
        }

        @Override
        public List<EventWithSlotCountReponseDto> getEvents() {

                User user = userRepository.findById(this.securityUtil.getCurrentAuthUserId())
                                .orElseThrow(() -> new AppException("Unknown User", HttpStatus.NOT_FOUND));

                List<Event> eventList = eventRepository.findByUser(user);
                List<EventSlotCount> slotCountList = slotRepository.countSlotForEvents(eventList);
                Map<Long, Long> slotCountMap = slotCountList.stream()
                                .collect(Collectors.toMap(EventSlotCount::getEventId, EventSlotCount::getSlotCount));
                List<EventWithSlotCountReponseDto> eResponseDtoList = eventList.stream()
                                .map(event -> dtoMapper.eventToEListResponseDto(event,
                                                slotCountMap.getOrDefault(event.getId(), 0L)))
                                .toList();
                return eResponseDtoList;
        }

        @Override
        public EventWithSlotCountReponseDto getEventById(Long eventId) {
                User user = userRepository.findById(this.securityUtil.getCurrentAuthUserId())
                                .orElseThrow(() -> new AppException("Unknown User", HttpStatus.NOT_FOUND));
                Event event = eventRepository.findByIdAndUser(eventId, user)
                                .orElseThrow(() -> new AppException("No event found with this Id and User",
                                                HttpStatus.NOT_FOUND));
                Long slotCount = slotRepository.countSlotByEventId(eventId);
                EventWithSlotCountReponseDto eResponseDtoList = dtoMapper.eventToEListResponseDto(event, slotCount);
                return eResponseDtoList;
        }

        @Override
        @Transactional
        public EventResponseDto putEventById(Long eventId,
                        EventRequestDto eRequestDto) {
                User user = userRepository.findById(this.securityUtil.getCurrentAuthUserId())
                                .orElseThrow(() -> new AppException("Unknown User", HttpStatus.NOT_FOUND));
                Event event = eventRepository.findByIdAndUser(eventId, user)
                                .orElseThrow(() -> new AppException("No event found with this Id and User",
                                                HttpStatus.NOT_FOUND));
                SlotType slotTypeEnum = stringtoSlotType(eRequestDto.slotType());

                event.setEventName(eRequestDto.eventName());
                event.setEventDescription(eRequestDto.eventDescription());
                event.setEventLocationAddress(eRequestDto.eventLocationAddress());
                event.setIncludePosition(eRequestDto.includePosition());
                event.setLatitude(eRequestDto.latitude());
                event.setLongitude(eRequestDto.longitude());
                event.setSlotType(slotTypeEnum);

                Event updatedEvent = eventRepository.save(event);

                return new EventResponseDto(
                                updatedEvent.getId(),
                                updatedEvent.getUser().getUsername(),
                                updatedEvent.getEventName(),
                                updatedEvent.getEventDescription(),
                                updatedEvent.getEventLocationAddress(),
                                updatedEvent.getIncludePosition(),
                                updatedEvent.getLatitude(),
                                updatedEvent.getLongitude(),
                                updatedEvent.getSlotType().toString(),
                                updatedEvent.getCreatedAt(),
                                updatedEvent.getUpdatedAt());

        }

        @Override
        @Transactional
        public Long deleteEventById(Long eventId) {
                User user = userRepository.findById(this.securityUtil.getCurrentAuthUserId())
                                .orElseThrow(() -> new AppException("Unknown User", HttpStatus.NOT_FOUND));
                Event event = eventRepository.findByIdAndUser(eventId, user)
                                .orElseThrow(() -> new AppException("No event found with this Id and User",
                                                HttpStatus.NOT_FOUND));
                this.eventRepository.delete(event);
                return eventId;
        }

        private SlotType stringtoSlotType(String slotName) {
                SlotType slotTypeEnum;
                try {
                        slotTypeEnum = SlotType.valueOf(slotName);
                } catch (IllegalArgumentException | NullPointerException ex) {
                        throw new AppException("Invalid slot type", HttpStatus.BAD_REQUEST);
                }
                return slotTypeEnum;
        }
}
