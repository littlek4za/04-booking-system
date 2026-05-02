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
import com.littlek4za.booking_system.entities.Invitation;
import com.littlek4za.booking_system.entities.User;
import com.littlek4za.booking_system.exception.AppException;
import com.littlek4za.booking_system.exception.model.ErrorCode;
import com.littlek4za.booking_system.models.EventType;
import com.littlek4za.booking_system.repos.EventRepository;
import com.littlek4za.booking_system.repos.InvitationRepository;
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
        private final SecurityUtil securityUtil;
        private final InvitationRepository invitationRepository;

        public EventServiceImpl(EventRepository eventRepository, UserRepository userRepository, DtoMapper dtoMapper,
                        SlotRepository slotRepository, SecurityUtil securityUtil, InvitationRepository invitationRepository) {
                this.eventRepository = eventRepository;
                this.userRepository = userRepository;
                this.dtoMapper = dtoMapper;
                this.slotRepository = slotRepository;
                this.securityUtil = securityUtil;
                this.invitationRepository = invitationRepository;
        }

        @Override
        @Transactional
        public EventResponseDto createEvent(EventRequestDto eRequestDto) {
                User user = userRepository.findById(this.securityUtil.requireUserId())
                                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND, ErrorCode.USER_NOT_FOUND));
                EventType eventTypeEnum = stringtoEventType(eRequestDto.eventType());

                Event newEvent = new Event(
                                user,
                                eRequestDto.eventName(),
                                eRequestDto.eventDescription(),
                                eRequestDto.eventLocationAddress(),
                                eRequestDto.includePosition(),
                                eRequestDto.maxBookingsPerIdentity(),
                                eventTypeEnum);

                if (Boolean.TRUE.equals(newEvent.getIncludePosition())) {
                        newEvent.setPosition(eRequestDto.latitude(), eRequestDto.longitude());
                }

                Event savedEvent = eventRepository.save(newEvent);

                return dtoMapper.toEventResponseDto(savedEvent);
        }

        @Override
        public List<EventWithSlotCountReponseDto> getEvents() {

                User user = userRepository.findById(this.securityUtil.requireUserId())
                                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND, ErrorCode.USER_NOT_FOUND));

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
                User user = userRepository.findById(this.securityUtil.requireUserId())
                                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND, ErrorCode.USER_NOT_FOUND));
                Event event = eventRepository.findByIdAndUser(eventId, user)
                                .orElseThrow(() -> new AppException("Event not found with eventId and User", HttpStatus.NOT_FOUND, ErrorCode.EVENT_NOT_FOUND));
                long slotCount = slotRepository.countSlotByEventId(eventId);

                return dtoMapper.toEventWithSlotCountResponseDto(event, slotCount);
        }

        @Override
        @Transactional
        public EventResponseDto putEventById(Long eventId,
                        EventRequestDto eRequestDto) {
                User user = userRepository.findById(this.securityUtil.requireUserId())
                                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND, ErrorCode.USER_NOT_FOUND));
                Event event = eventRepository.findByIdAndUser(eventId, user)
                                .orElseThrow(() -> new AppException("Event not found with eventId and User", HttpStatus.NOT_FOUND, ErrorCode.EVENT_NOT_FOUND));
                long slotCount = slotRepository.countSlotByEventId(eventId);
                EventType eventTypeEnum = stringtoEventType(eRequestDto.eventType());
                if (slotCount > 0 && !event.getEventType().equals(eventTypeEnum)) {
                        throw new AppException("Event type cannot be changed once slots exist",
                                        HttpStatus.BAD_REQUEST, ErrorCode.EVENT_TYPE_CHANGE_NOT_ALLOWED);
                }

                event.setEventName(eRequestDto.eventName());
                event.setEventDescription(eRequestDto.eventDescription());
                event.setEventLocationAddress(eRequestDto.eventLocationAddress());
                event.setIncludePosition(eRequestDto.includePosition());
                event.setLatitude(eRequestDto.latitude());
                event.setLongitude(eRequestDto.longitude());
                event.setMaxBookingsPerIdentity(eRequestDto.maxBookingsPerIdentity());
                event.setEventType(eventTypeEnum);

                Event updatedEvent = eventRepository.save(event);

                return dtoMapper.toEventResponseDto(updatedEvent);

        }

        @Override
        @Transactional
        public Long deleteEventById(Long eventId) {
                User user = userRepository.findById(this.securityUtil.requireUserId())
                                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND, ErrorCode.USER_NOT_FOUND));

                List<Invitation> invitationList = invitationRepository.findByEventIdWithSlotSet(eventId);

                invitationList.forEach(invitation -> {
                        invitation.getSlotSet().clear();
                        invitationRepository.save(invitation);
                        invitationRepository.delete(invitation);
                });

                int deleted = eventRepository.deleteByIdAndUserId(eventId, user.getId());

                if(deleted == 0) {
                        throw new AppException("Event not found with eventId and user", HttpStatus.NOT_FOUND, ErrorCode.EVENT_NOT_FOUND);
                }

                return eventId;
        }

        private EventType stringtoEventType(String slotName) {
                EventType eventTypeEnum;
                try {
                        eventTypeEnum = EventType.valueOf(slotName);
                } catch (IllegalArgumentException | NullPointerException ex) {
                        throw new AppException("Event type invalid", HttpStatus.BAD_REQUEST, ErrorCode.EVENT_TYPE_INVALID);
                }
                return eventTypeEnum;
        }
}
