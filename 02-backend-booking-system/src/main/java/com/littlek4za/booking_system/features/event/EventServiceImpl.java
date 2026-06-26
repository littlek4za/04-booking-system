package com.littlek4za.booking_system.features.event;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.littlek4za.booking_system.common.dto.DeleteValidationResponseDto;
import com.littlek4za.booking_system.common.model.CacheKeys;
import com.littlek4za.booking_system.common.service.DeleteValidationService;
import com.littlek4za.booking_system.common.service.RedisCacheService;
import com.littlek4za.booking_system.common.service.event.EventServiceEvent;
import com.littlek4za.booking_system.common.utils.DtoMapper;
import com.littlek4za.booking_system.exception.AppException;
import com.littlek4za.booking_system.exception.model.ErrorCode;
import com.littlek4za.booking_system.features.auth.entity.User;
import com.littlek4za.booking_system.features.auth.repo.UserRepository;
import com.littlek4za.booking_system.features.booking.Booking;
import com.littlek4za.booking_system.features.booking.BookingRepository;
import com.littlek4za.booking_system.features.event.dto.EventRequestDto;
import com.littlek4za.booking_system.features.event.dto.EventResponseDto;
import com.littlek4za.booking_system.features.event.dto.EventWithSlotCountReponseDto;
import com.littlek4za.booking_system.features.event.model.EventType;
import com.littlek4za.booking_system.features.invitation.InvitationRepository;
import com.littlek4za.booking_system.features.slot.SlotRepository;
import com.littlek4za.booking_system.features.slot.model.EventSlotCount;
import com.littlek4za.booking_system.security.SecurityUtil;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class EventServiceImpl implements EventService {

        private final EventRepository eventRepository;
        private final UserRepository userRepository;
        private final DtoMapper dtoMapper;
        private final SlotRepository slotRepository;
        private final SecurityUtil securityUtil;
        private final BookingRepository bookingRepository;
        private final DeleteValidationService deleteValidationService;
        private final ApplicationEventPublisher eventPublisher;
        private final RedisCacheService redisCacheService;

        public EventServiceImpl(EventRepository eventRepository, UserRepository userRepository, DtoMapper dtoMapper,
                        SlotRepository slotRepository, SecurityUtil securityUtil,
                        InvitationRepository invitationRepository, BookingRepository bookingRepository,
                        DeleteValidationService deleteValidationService, ApplicationEventPublisher eventPublisher, RedisCacheService redisCacheService) {
                this.eventRepository = eventRepository;
                this.userRepository = userRepository;
                this.dtoMapper = dtoMapper;
                this.slotRepository = slotRepository;
                this.securityUtil = securityUtil;
                this.bookingRepository = bookingRepository;
                this.deleteValidationService = deleteValidationService;
                this.eventPublisher = eventPublisher;
                this.redisCacheService = redisCacheService;
        }

        @Override
        @PreAuthorize("@authz.isUser() and hasAnyAuthority('ROLE_ADMIN','ROLE_ORGANIZER')")
        @Transactional
        public EventResponseDto createEvent(EventRequestDto eRequestDto) {
                Long userId = this.securityUtil.requireUserId();
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND,
                                                ErrorCode.USER_NOT_FOUND));
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

                eventPublisher.publishEvent(EventServiceEvent.eventCreated(userId));

                return dtoMapper.toEventResponseDto(savedEvent);
        }

        @Override
        @PreAuthorize("@authz.isUser() and hasAnyAuthority('ROLE_ADMIN','ROLE_ORGANIZER')")
        public List<EventWithSlotCountReponseDto> getEvents() {

                Long userId = this.securityUtil.requireUserId();

                List<EventWithSlotCountReponseDto> cacheDtoList = redisCacheService.getList(
                                CacheKeys.eventWithSlotCountList(userId),
                                new TypeReference<List<EventWithSlotCountReponseDto>>() {
                                });

                if (cacheDtoList != null) {
                        return cacheDtoList;
                }

                log.info("DB CALLED FOR event_list - NOT FROM CACHE");

                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND,
                                                ErrorCode.USER_NOT_FOUND));

                List<Event> eventList = eventRepository.findByUser(user);

                if (eventList.isEmpty()) {
                        return Collections.emptyList();
                }

                List<Long> eventIds = eventList.stream()
                                .map(event -> event.getId())
                                .collect(Collectors.toList());

                List<EventSlotCount> slotCountList = slotRepository.countSlotForEvents(eventIds);
                Map<Long, Long> slotCountMap = slotCountList.stream()
                                .collect(Collectors.toMap(e -> e.eventId(), e -> e.slotCount()));
                List<EventWithSlotCountReponseDto> eResponseDtoList = eventList.stream()
                                .map(event -> dtoMapper.toEventWithSlotCountResponseDto(event,
                                                slotCountMap.getOrDefault(event.getId(), 0L)))
                                .collect(Collectors.toList());

                redisCacheService.set(CacheKeys.eventWithSlotCountList(userId), eResponseDtoList, Duration.ofMinutes(5));

                return eResponseDtoList;
        }

        @Override
        @PreAuthorize("@authz.isUser() and hasAnyAuthority('ROLE_ADMIN','ROLE_ORGANIZER')")
        public EventWithSlotCountReponseDto getEventById(Long eventId) {

                Long userId = this.securityUtil.requireUserId();

                EventWithSlotCountReponseDto cacheDto = redisCacheService.get(
                                CacheKeys.eventById(userId,eventId),
                               EventWithSlotCountReponseDto.class);

                if (cacheDto != null) {
                        return cacheDto;
                }

                log.info("DB CALLED FOR event_by_id - NOT FROM CACHE");

                User user = userRepository.findById(this.securityUtil.requireUserId())
                                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND,
                                                ErrorCode.USER_NOT_FOUND));
                Event event = eventRepository.findByIdAndUser(eventId, user)
                                .orElseThrow(() -> new AppException("Event not found with eventId and User",
                                                HttpStatus.NOT_FOUND, ErrorCode.EVENT_NOT_FOUND));
                long slotCount = slotRepository.countSlotByEventId(eventId);

                EventWithSlotCountReponseDto eReponseDto = dtoMapper.toEventWithSlotCountResponseDto(event, slotCount);

                redisCacheService.set(CacheKeys.eventById(userId,eventId), eReponseDto, Duration.ofMinutes(5));

                return eReponseDto;
        }

        @Override
        @PreAuthorize("@authz.isUser() and hasAnyAuthority('ROLE_ADMIN','ROLE_ORGANIZER')")
        @Transactional
        public EventResponseDto putEventById(Long eventId,
                        EventRequestDto eRequestDto) {

                Long userId = this.securityUtil.requireUserId();
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND,
                                                ErrorCode.USER_NOT_FOUND));
                Event event = eventRepository.findByIdAndUser(eventId, user)
                                .orElseThrow(() -> new AppException("Event not found with eventId and User",
                                                HttpStatus.NOT_FOUND, ErrorCode.EVENT_NOT_FOUND));
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

                eventPublisher.publishEvent(EventServiceEvent.eventUpdated(userId, updatedEvent.getId()));

                return dtoMapper.toEventResponseDto(updatedEvent);

        }

        @Override
        @PreAuthorize("@authz.isUser() and hasAnyAuthority('ROLE_ADMIN','ROLE_ORGANIZER')")
        @Transactional
        public Long deleteEventById(Long eventId) {

                Long userId = this.securityUtil.requireUserId();
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND,
                                                ErrorCode.USER_NOT_FOUND));

                eventRepository.findByIdAndUser(eventId, user)
                                .orElseThrow(() -> new AppException("Event not found with eventId and User",
                                                HttpStatus.NOT_FOUND, ErrorCode.EVENT_NOT_FOUND));

                List<Booking> bookingList = bookingRepository.findBySlot_Event_IdAndIsDeletedFalse(eventId);

                boolean canDelete = true;

                if (!bookingList.isEmpty()) {
                        canDelete = deleteValidationService.buildDeleteValidationResponseDto(bookingList).canDelete();
                }

                if (canDelete == false) {
                        throw new AppException(
                                        "Event cannot be deleted due to active bookings",
                                        HttpStatus.BAD_REQUEST,
                                        ErrorCode.EVENT_HAS_ACTIVE_BOOKINGS);
                }

                // // Not needed handle by CASCADETYPE.ALL in Event entity
                // List<Invitation> invitationList =
                // invitationRepository.findByEventIdWithSlotSet(eventId);

                // invitationList.forEach(invitation -> {
                // invitation.getSlotSet().clear();
                // invitationRepository.save(invitation);
                // invitationRepository.delete(invitation);
                // });

                int deleted = eventRepository.deleteByIdAndUserId(eventId, user.getId());

                if (deleted == 0) {
                        throw new AppException("Event not found with eventId and user", HttpStatus.NOT_FOUND,
                                        ErrorCode.EVENT_NOT_FOUND);
                }

                eventPublisher.publishEvent(EventServiceEvent.eventUpdated(userId, eventId));

                return eventId;
        }

        private EventType stringtoEventType(String slotName) {
                EventType eventTypeEnum;
                try {
                        eventTypeEnum = EventType.valueOf(slotName);
                } catch (IllegalArgumentException | NullPointerException ex) {
                        throw new AppException("Event type invalid", HttpStatus.BAD_REQUEST,
                                        ErrorCode.EVENT_TYPE_INVALID);
                }
                return eventTypeEnum;
        }

        @Override
        @PreAuthorize("@authz.isUser() and hasAnyAuthority('ROLE_ADMIN','ROLE_ORGANIZER')")
        public DeleteValidationResponseDto eventDeleteValidation(Long eventId) {
                User user = userRepository.findById(this.securityUtil.requireUserId())
                                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND,
                                                ErrorCode.USER_NOT_FOUND));
                eventRepository.findByIdAndUser(eventId, user)
                                .orElseThrow(() -> new AppException("Event not found with eventId and User",
                                                HttpStatus.NOT_FOUND, ErrorCode.EVENT_NOT_FOUND));

                List<Booking> bookingList = bookingRepository.findBySlot_Event_IdAndIsDeletedFalse(eventId);

                DeleteValidationResponseDto responseDto = deleteValidationService
                                .buildDeleteValidationResponseDto(bookingList);

                return responseDto;
        }

}
