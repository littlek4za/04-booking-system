package com.littlek4za.booking_system.services;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.littlek4za.booking_system.dtos.DeleteValidationResponseDto;
import com.littlek4za.booking_system.dtos.SlotRequestDto;
import com.littlek4za.booking_system.dtos.SlotResponseDto;
import com.littlek4za.booking_system.entities.Booking;
import com.littlek4za.booking_system.entities.Event;
import com.littlek4za.booking_system.entities.Invitation;
import com.littlek4za.booking_system.entities.Slot;
import com.littlek4za.booking_system.entities.User;
import com.littlek4za.booking_system.exception.AppException;
import com.littlek4za.booking_system.exception.model.ErrorCode;
import com.littlek4za.booking_system.models.CacheKeys;
import com.littlek4za.booking_system.models.SlotIncludeMode;
import com.littlek4za.booking_system.repos.BookingRepository;
import com.littlek4za.booking_system.repos.EventRepository;
import com.littlek4za.booking_system.repos.InvitationRepository;
import com.littlek4za.booking_system.repos.SlotRepository;
import com.littlek4za.booking_system.repos.UserRepository;
import com.littlek4za.booking_system.security.SecurityUtil;
import com.littlek4za.booking_system.services.event.SlotServiceEvent;
import com.littlek4za.booking_system.utils.DtoMapper;
import com.littlek4za.booking_system.validators.SlotValidator;

import jakarta.transaction.Transactional;

@Service
public class SlotServiceImpl implements SlotService {

        private final SecurityUtil securityUtil;
        private final SlotRepository slotRepository;
        private final EventRepository eventRepository;
        private final UserRepository userRepository;
        private final DtoMapper dtoMapper;
        private final SlotValidator slotValidator;
        private final BookingRepository bookingRepository;
        private final InvitationRepository invitationRepository;
        private final DeleteValidationService deleteValidationService;
        private final RedisCacheService redisCacheService;
        private final ApplicationEventPublisher eventPublisher;

        public SlotServiceImpl(SecurityUtil sercurityUtil, SlotRepository slotRepository,
                        EventRepository eventRepository,
                        UserRepository userRepository, DtoMapper dtoMapper, SlotValidator slotValidator,
                        BookingRepository bookingRepository, InvitationRepository invitationRepository,
                        DeleteValidationService deleteValidationService, RedisCacheService redisCacheService, ApplicationEventPublisher eventPublisher) {
                this.securityUtil = sercurityUtil;
                this.slotRepository = slotRepository;
                this.eventRepository = eventRepository;
                this.userRepository = userRepository;
                this.dtoMapper = dtoMapper;
                this.slotValidator = slotValidator;
                this.bookingRepository = bookingRepository;
                this.invitationRepository = invitationRepository;
                this.deleteValidationService = deleteValidationService;
                this.redisCacheService = redisCacheService;
                this.eventPublisher = eventPublisher;
        }

        @Override
        @PreAuthorize("@authz.isUser() and hasAnyAuthority('ROLE_ADMIN','ROLE_ORGANIZER')")
        public List<SlotResponseDto> getSlotsByEventId(Long eventId) {
                Long userId = securityUtil.requireUserId();

                List<SlotResponseDto> cacheDtoList = redisCacheService.getList(CacheKeys.slotListByEventId(userId,eventId),
                        new TypeReference<List<SlotResponseDto>>() {});

                if(cacheDtoList != null) {
                        return cacheDtoList;
                }

                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND,
                                                ErrorCode.USER_NOT_FOUND));
                Event event = eventRepository.findByIdAndUser(eventId, user)
                                .orElseThrow(() -> new AppException("Event not found with eventId and user",
                                                HttpStatus.NOT_FOUND, ErrorCode.EVENT_NOT_FOUND));
                Map<Long, Long> bookingsCountMap = bookingRepository.countBookingsByEventGrouped(eventId)
                                .stream()
                                .collect(Collectors.toMap(
                                                row -> (Long) row[0],
                                                row -> (Long) row[1]));
                Set<Slot> slotSet = slotRepository.findByEventWithEvent(event);
                List<SlotResponseDto> slotResponseDtoList = slotSet.stream()
                                .map(slot -> {
                                        Long bookingsCount = bookingsCountMap.getOrDefault(slot.getId(), 0L);
                                        return dtoMapper.toSlotResponseDto(slot, bookingsCount);
                                })
                                .toList();

                redisCacheService.set(CacheKeys.slotListByEventId(userId, eventId), slotResponseDtoList, Duration.ofMinutes(5));

                return slotResponseDtoList;
        }

        @Override
        @Transactional
        @PreAuthorize("@authz.isUser() and hasAnyAuthority('ROLE_ADMIN','ROLE_ORGANIZER')")
        public SlotResponseDto createSlotByEvent(Long eventId, SlotRequestDto slotRequestDto) {

                Long userId = securityUtil.requireUserId();

                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND,
                                                ErrorCode.USER_NOT_FOUND));
                Event event = eventRepository.findByIdAndUser(eventId, user)
                                .orElseThrow(() -> new AppException("Event not found with eventId and user",
                                                HttpStatus.NOT_FOUND, ErrorCode.EVENT_NOT_FOUND));

                slotValidator.validate(event.getEventType(), slotRequestDto);

                Slot newSlot = dtoMapper.toSlot(slotRequestDto, event);

                if (!event.getEventType().supportMaxBookPerInterval()) {
                        if (newSlot.getMaxBookPerInterval() == null) {
                                newSlot.setMaxBookPerInterval(1);
                        }
                }

                Slot savedSlot = slotRepository.save(newSlot);

                eventPublisher.publishEvent(SlotServiceEvent.slotCreated(userId, eventId));

                return dtoMapper.toSlotResponseDto(savedSlot);
        }

        @Override
        @Transactional
        @PreAuthorize("@authz.isUser() and hasAnyAuthority('ROLE_ADMIN','ROLE_ORGANIZER')")
        public Long deleteSlotByEventAndSlot(Long eventId, Long slotId) {

                Long userId = securityUtil.requireUserId();

                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND,
                                                ErrorCode.USER_NOT_FOUND));
                Event event = eventRepository.findByIdAndUser(eventId, user)
                                .orElseThrow(() -> new AppException("Event not found with eventId and user",
                                                HttpStatus.NOT_FOUND, ErrorCode.EVENT_NOT_FOUND));
                // handle invitation deletion
                Slot slot = slotRepository.findByIdAndEventIdWithInvitationSet(slotId, event.getId())
                                .orElseThrow(() -> new AppException("Slot not found with slotId and eventId",
                                                HttpStatus.NOT_FOUND, ErrorCode.SLOT_NOT_FOUND));

                List<Booking> bookingList = bookingRepository.findBySlot_IdAndSlot_Event_IdAndIsDeletedFalse(slotId,
                                eventId);

                boolean canDelete = true;

                if (!bookingList.isEmpty()) {
                        canDelete = deleteValidationService.canDelete(bookingList);
                }

                if (canDelete == false) {
                        throw new AppException(
                                        "Slot cannot be deleted due to active bookings",
                                        HttpStatus.BAD_REQUEST,
                                        ErrorCode.SLOT_HAS_ACTIVE_BOOKINGS);
                }

                for (Invitation invitation : slot.getInvitationSet()) {
                        if (invitation.getSlotIncludeMode() == SlotIncludeMode.SELECTED) {

                                invitation.getSlotSet().remove(slot);

                                if (invitation.getSlotSet().isEmpty()) {
                                        invitationRepository.delete(invitation);
                                } else {
                                        invitationRepository.save(invitation);
                                }
                        }
                }

                int deleted = this.slotRepository.deleteByIdAndEventId(slotId, event.getId());

                eventPublisher.publishEvent(SlotServiceEvent.slotUpdated(userId, eventId, slotId));

                if (deleted == 0) {
                        throw new AppException("Slot not found with slotId and eventId", HttpStatus.NOT_FOUND,
                                        ErrorCode.SLOT_NOT_FOUND);
                }

                return slotId;
        }

        @Override
        @PreAuthorize("@authz.isUser() and hasAnyAuthority('ROLE_ADMIN','ROLE_ORGANIZER')")
        public SlotResponseDto getSlotByIdAndEventId(Long eventId, Long slotId) {

                Long userId = securityUtil.requireUserId();

                SlotResponseDto cacheDto = redisCacheService.get(CacheKeys.slotById(userId, eventId, slotId), SlotResponseDto.class);

                if(cacheDto != null) {
                        return cacheDto;
                } 

                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND,
                                                ErrorCode.USER_NOT_FOUND));
                Event event = eventRepository.findByIdAndUser(eventId, user)
                                .orElseThrow(() -> new AppException("Event not found with eventId and user",
                                                HttpStatus.NOT_FOUND, ErrorCode.EVENT_NOT_FOUND));
                Slot slot = slotRepository.findByIdAndEventId(slotId, event.getId())
                                .orElseThrow(() -> new AppException("Slot not found with slotId and eventId",
                                                HttpStatus.NOT_FOUND, ErrorCode.SLOT_NOT_FOUND));

                Long bookingsCount = bookingRepository.countBySlotId(slotId);

                SlotResponseDto slotRequestDto = dtoMapper.toSlotResponseDto(slot, bookingsCount);

                redisCacheService.set(CacheKeys.slotById(userId, eventId, slotId), slotRequestDto, Duration.ofMinutes(5));

                return slotRequestDto;
        }

        @Override
        @PreAuthorize("@authz.isUser() and hasAnyAuthority('ROLE_ADMIN','ROLE_ORGANIZER')")
        @Transactional
        public SlotResponseDto putSlotByIdAndEventId(Long slotId, Long eventId, SlotRequestDto slotRequestDto) {

                Long userId = securityUtil.requireUserId();

                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND,
                                                ErrorCode.USER_NOT_FOUND));
                Event event = eventRepository.findByIdAndUser(eventId, user)
                                .orElseThrow(() -> new AppException("Event not found with eventId and user",
                                                HttpStatus.NOT_FOUND, ErrorCode.EVENT_NOT_FOUND));

                Slot existingSlot = slotRepository.findByIdAndEventId(slotId, event.getId())
                                .orElseThrow(() -> new AppException("Slot not found with slotId and eventId",
                                                HttpStatus.NOT_FOUND, ErrorCode.SLOT_NOT_FOUND));

                slotValidator.validate(event.getEventType(), slotRequestDto);

                Long bookingsCount = bookingRepository.countBySlotId(slotId);

                slotValidator.validateForUpdate(event.getEventType(), slotRequestDto, existingSlot, bookingsCount);

                existingSlot.setSlotName(slotRequestDto.slotName());
                existingSlot.setSlotDescription(slotRequestDto.slotDescription());
                existingSlot.setSlotStartTime(slotRequestDto.slotStartTime());
                existingSlot.setSlotEndTime(slotRequestDto.slotEndTime());
                existingSlot.setMaxBookingsPerIdentity(slotRequestDto.maxBookingsPerIdentity());
                existingSlot.setMaxBookPerInterval(slotRequestDto.maxBookPerInterval());
                existingSlot.setSlotIntervalMinutes(slotRequestDto.slotIntervalMinutes());
                existingSlot.setSlotFrequencyIntervalMinutes(slotRequestDto.slotFrequencyIntervalMinutes());
                existingSlot.setBusinessDaysHours(slotRequestDto.businessDaysHours());
                existingSlot.setBusinessTimeZone(slotRequestDto.businessTimeZone());
                existingSlot.setBusinessAllowOT(slotRequestDto.businessAllowOt());
                existingSlot.setFlexibleDaysHours(slotRequestDto.flexibleDaysHours());

                if (!event.getEventType().supportMaxBookPerInterval()) {
                        if (existingSlot.getMaxBookPerInterval() == null) {
                                existingSlot.setMaxBookPerInterval(1);
                        }
                }

                Slot updatedSlot = slotRepository.save(existingSlot);

                eventPublisher.publishEvent(SlotServiceEvent.slotUpdated(userId, eventId, slotId));

                return dtoMapper.toSlotResponseDto(updatedSlot);
        }

        @Override
        @PreAuthorize("@authz.isUser() and hasAnyAuthority('ROLE_ADMIN','ROLE_ORGANIZER')")
        public DeleteValidationResponseDto slotDeleteValidation(Long eventId, Long slotId) {
                User user = userRepository.findById(this.securityUtil.requireUserId())
                                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND,
                                                ErrorCode.USER_NOT_FOUND));
                eventRepository.findByIdAndUser(eventId, user)
                                .orElseThrow(() -> new AppException("Event not found with eventId and User",
                                                HttpStatus.NOT_FOUND, ErrorCode.EVENT_NOT_FOUND));

                slotRepository.findByIdAndEventId(slotId, eventId)
                                .orElseThrow(() -> new AppException("Slot not found with slotId and eventId",
                                                HttpStatus.NOT_FOUND, ErrorCode.SLOT_NOT_FOUND));

                List<Booking> bookingList = bookingRepository.findBySlot_IdAndSlot_Event_IdAndIsDeletedFalse(slotId,
                                eventId);

                DeleteValidationResponseDto responseDto = deleteValidationService
                                .buildDeleteValidationResponseDto(bookingList);

                return responseDto;
        }

}
