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
import com.littlek4za.booking_system.models.EventType;
import com.littlek4za.booking_system.repos.EventRepository;
import com.littlek4za.booking_system.repos.SlotRepository;
import com.littlek4za.booking_system.repos.UserRepository;
import com.littlek4za.booking_system.repos.projections.EventSlotCount;
import com.littlek4za.booking_system.security.SecurityUtil;
import com.littlek4za.booking_system.utils.DtoMapper;

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
                EventType eventTypeEnum = stringtoEventType(eRequestDto.eventType());

                Event newEvent = new Event(
                                user,
                                eRequestDto.eventName(),
                                eRequestDto.eventDescription(),
                                eRequestDto.eventLocationAddress(),
                                eRequestDto.includePosition(),
                                eventTypeEnum);

                if (Boolean.TRUE.equals(newEvent.getIncludePosition())) {
                        newEvent.setPosition(eRequestDto.latitude(), eRequestDto.longitude());
                }

                Event savedEvent = eventRepository.save(newEvent);

                return dtoMapper.toEventResponseDto(savedEvent);
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
                                .map(event -> dtoMapper.toEventWithSlotCountResponseDto(event,
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
                long slotCount = slotRepository.countSlotByEventId(eventId);

                return dtoMapper.toEventWithSlotCountResponseDto(event, slotCount);
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
                long slotCount = slotRepository.countSlotByEventId(eventId);
                EventType eventTypeEnum = stringtoEventType(eRequestDto.eventType());
                if (slotCount > 0 && !event.getEventType().equals(eventTypeEnum)) {
                        throw new AppException("Event type cannot be changed once slots exist",
                                        HttpStatus.BAD_REQUEST);
                }

                event.setEventName(eRequestDto.eventName());
                event.setEventDescription(eRequestDto.eventDescription());
                event.setEventLocationAddress(eRequestDto.eventLocationAddress());
                event.setIncludePosition(eRequestDto.includePosition());
                event.setLatitude(eRequestDto.latitude());
                event.setLongitude(eRequestDto.longitude());
                event.setEventType(eventTypeEnum);

                Event updatedEvent = eventRepository.save(event);

                return dtoMapper.toEventResponseDto(updatedEvent);

        }

        @Override
        @Transactional
        public Long deleteEventById(Long eventId) {
                User user = userRepository.findById(this.securityUtil.getCurrentAuthUserId())
                                .orElseThrow(() -> new AppException("Unknown User", HttpStatus.NOT_FOUND));

                int deleted = eventRepository.deleteByIdAndUserId(eventId, user.getId());

                if(deleted == 0) {
                        throw new AppException("No event found with this Id and User",
                                                HttpStatus.NOT_FOUND);
                }

                return eventId;
        }

        private EventType stringtoEventType(String slotName) {
                EventType eventTypeEnum;
                try {
                        eventTypeEnum = EventType.valueOf(slotName);
                } catch (IllegalArgumentException | NullPointerException ex) {
                        throw new AppException("Invalid event type", HttpStatus.BAD_REQUEST);
                }
                return eventTypeEnum;
        }
}
