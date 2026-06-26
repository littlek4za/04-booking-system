package com.littlek4za.booking_system.features.invitation;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.littlek4za.booking_system.common.model.CacheKeys;
import com.littlek4za.booking_system.common.service.RedisCacheService;
import com.littlek4za.booking_system.common.service.event.InvitationServiceEvent;
import com.littlek4za.booking_system.common.utils.DtoMapper;
import com.littlek4za.booking_system.exception.AppException;
import com.littlek4za.booking_system.exception.model.ErrorCode;
import com.littlek4za.booking_system.features.auth.entity.User;
import com.littlek4za.booking_system.features.auth.repo.UserRepository;
import com.littlek4za.booking_system.features.event.Event;
import com.littlek4za.booking_system.features.event.EventRepository;
import com.littlek4za.booking_system.features.invitation.dto.InvitationRequestDto;
import com.littlek4za.booking_system.features.invitation.dto.InvitationResponseDto;
import com.littlek4za.booking_system.features.invitation.dto.InvitationValidationResponseDto;
import com.littlek4za.booking_system.features.invitation.enitity.Invitation;
import com.littlek4za.booking_system.features.invitation.enitity.InvitationUsage;
import com.littlek4za.booking_system.features.invitation.enitity.InvitationUsageId;
import com.littlek4za.booking_system.features.invitation.model.SlotIncludeMode;
import com.littlek4za.booking_system.features.invitation.model.ValidationResult;
import com.littlek4za.booking_system.features.invitation.validator.InvitationValidator;
import com.littlek4za.booking_system.features.slot.SlotRepository;
import com.littlek4za.booking_system.features.slot.entity.Slot;
import com.littlek4za.booking_system.security.SecurityUtil;

import jakarta.transaction.Transactional;

@Service
public class InvitationServiceImpl implements InvitationService {

    private final InvitationRepository invitationRepository;
    private final InvitationUsageRepository invitationUsageRepository;
    private final SecurityUtil securityUtil;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final SlotRepository slotRepository;
    private final DtoMapper dtoMapper;
    private final InvitationValidator invitationValidator;
    private final ApplicationEventPublisher eventPublisher;
    private final RedisCacheService redisCacheService;

    public InvitationServiceImpl(InvitationRepository invitationRepository, SecurityUtil securityUtil,
            UserRepository userRepository, EventRepository eventRepository, SlotRepository slotRepository,
            DtoMapper dtoMapper, InvitationValidator invitationValidator,
            InvitationUsageRepository invitationUsageRepository, ApplicationEventPublisher eventPublisher,
            RedisCacheService redisCacheService) {
        this.invitationRepository = invitationRepository;
        this.invitationUsageRepository = invitationUsageRepository;
        this.securityUtil = securityUtil;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.slotRepository = slotRepository;
        this.dtoMapper = dtoMapper;
        this.invitationValidator = invitationValidator;
        this.eventPublisher = eventPublisher;
        this.redisCacheService = redisCacheService;
    }

    @Override
    @PreAuthorize("@authz.isUser() and hasAnyAuthority('ROLE_ADMIN','ROLE_ORGANIZER')")
    @Transactional
    public InvitationResponseDto createInvitation(InvitationRequestDto invitationRequestDto, Long eventId) {

        Long userId = securityUtil.requireUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND, ErrorCode.USER_NOT_FOUND));
        Event event = eventRepository.findByIdAndUser(eventId, user)
                .orElseThrow(() -> new AppException("Event not found with eventId and user", HttpStatus.NOT_FOUND,
                        ErrorCode.EVENT_NOT_FOUND));

        Invitation newInvitation;
        Set<Slot> slotSet;
        if (SlotIncludeMode.SELECTED.name().equals(invitationRequestDto.slotIncludeMode())) {

            slotSet = slotRepository.findByIdInAndEventIdWithEvent(invitationRequestDto.slotIdList(), eventId);
            if (slotSet.size() != invitationRequestDto.slotIdList().size()) {
                throw new AppException("Some slots do not belong to this event", HttpStatus.BAD_REQUEST,
                        ErrorCode.SLOT_EVENT_MISMATCH);
            }

            if (slotSet.isEmpty()) {
                throw new AppException("Slots not found with slotIdList and eventId",
                        HttpStatus.NOT_FOUND, ErrorCode.SLOT_NOT_FOUND);
            }
            newInvitation = dtoMapper.toInvitation(invitationRequestDto, event, user);
            newInvitation.setSlotSet(slotSet);

        } else if (SlotIncludeMode.ALL_CURRENT.name().equals(invitationRequestDto.slotIncludeMode())) {

            slotSet = slotRepository.findByEventWithEvent(event);

            if (slotSet.isEmpty()) {
                throw new AppException("Slot not found with event, Please create slot before create invitation",
                        HttpStatus.NOT_FOUND, ErrorCode.SLOT_NOT_FOUND);
            }

            newInvitation = dtoMapper.toInvitation(invitationRequestDto, event, user);
            newInvitation.setSlotSet(slotSet);
            newInvitation.setSlotIncludeMode(SlotIncludeMode.SELECTED);

        } else if (SlotIncludeMode.ALL_AND_FUTURE.name().equals(invitationRequestDto.slotIncludeMode())) {

            slotSet = slotRepository.findByEventWithEvent(event);

            if (slotSet.isEmpty()) {
                throw new AppException("No Slot found, Please create slot before create invitation",
                        HttpStatus.NOT_FOUND, ErrorCode.SLOT_NOT_FOUND);
            }

            newInvitation = dtoMapper.toInvitation(invitationRequestDto, event, user);

        } else {
            throw new AppException("Slot include mode invalid", HttpStatus.BAD_REQUEST,
                    ErrorCode.SLOT_INCLUDE_MODE_INVALID);
        }

        if (newInvitation.getExpiresAt() == null) {
            newInvitation.setExpiresAt(Instant.parse("9999-12-31T23:59:59Z"));
        }

        newInvitation.setAccessToken(generateUniqueAccessToken());

        Invitation savedInvitation = invitationRepository.save(newInvitation);

        Invitation invitationForResponse = invitationRepository
                .findByAccessTokenWithEventAndSlotSet(savedInvitation.getAccessToken())
                .orElseThrow(() -> new AppException("Invitation not found ", HttpStatus.NOT_FOUND,
                        ErrorCode.INVITATION_NOT_FOUND));

        eventPublisher.publishEvent(InvitationServiceEvent.invitationCreated(userId, eventId));

        return dtoMapper.toInvitationResponseDto(invitationForResponse, slotSet);
    }

    private String generateUniqueAccessToken() {
        String token;
        do {
            token = generateAccessToken(6);
        } while (invitationRepository.existsByAccessToken(token));
        return token;
    }

    private String generateAccessToken(int length) {
        String CHAR_POOL = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder token = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            token.append(CHAR_POOL.charAt(random.nextInt(CHAR_POOL.length())));
        }
        return token.toString();
    }

    @Override
    @PreAuthorize("@authz.isUser() and hasAnyAuthority('ROLE_ADMIN','ROLE_ORGANIZER')")
    public List<InvitationResponseDto> getInvitationsByEventId(Long eventId) {

        Long userId = securityUtil.requireUserId();

        List<InvitationResponseDto> cacheDtoList = redisCacheService.getList(
                CacheKeys.invitationListByEventId(userId, eventId),
                new TypeReference<List<InvitationResponseDto>>() {
                });

        if (cacheDtoList != null) {
            return cacheDtoList;
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND, ErrorCode.USER_NOT_FOUND));
        Event event = eventRepository.findByIdAndUser(eventId, user)
                .orElseThrow(() -> new AppException("Event not found with eventId and user", HttpStatus.NOT_FOUND,
                        ErrorCode.EVENT_NOT_FOUND));

        Set<Invitation> invitationSet = invitationRepository.findByEventWithSlotSet(event);
        Set<Slot> allEventSlots = slotRepository.findByEventWithEvent(event);

        List<InvitationResponseDto> invitationResponseDtoList = invitationSet.stream().map(invitation -> {
            Set<Slot> slotSetToUse;
            if (invitation.getSlotIncludeMode() == SlotIncludeMode.ALL_AND_FUTURE) {
                slotSetToUse = allEventSlots;
            } else {
                slotSetToUse = invitation.getSlotSet();
            }
            return dtoMapper.toInvitationResponseDto(invitation, slotSetToUse);
        }).collect(Collectors.toList());

        redisCacheService.set(CacheKeys.invitationListByEventId(userId, eventId), invitationResponseDtoList,
                Duration.ofMinutes(5));

        return invitationResponseDtoList;
    }

    @Override
    @PreAuthorize("@authz.isUser() and hasAnyAuthority('ROLE_ADMIN','ROLE_ORGANIZER')")
    public List<InvitationResponseDto> getInvitationsByEventIdAndSlotId(Long eventId, Long slotId) {
        Long userId = securityUtil.requireUserId();

        List<InvitationResponseDto> cacheDtoList = redisCacheService.getList(
                CacheKeys.invitationListBySlotId(userId, eventId, slotId),
                new TypeReference<List<InvitationResponseDto>>() {
                });

        if (cacheDtoList != null) {
            return cacheDtoList;
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND, ErrorCode.USER_NOT_FOUND));
        Event event = eventRepository.findByIdAndUser(eventId, user)
                .orElseThrow(() -> new AppException("Event not found with eventId and user", HttpStatus.NOT_FOUND,
                        ErrorCode.EVENT_NOT_FOUND));

        Set<Invitation> invitationSet = invitationRepository.findInvitationsApplicableToSlot(eventId, slotId,
                SlotIncludeMode.ALL_AND_FUTURE);
        Set<Slot> allEventSlots = slotRepository.findByEventWithEvent(event);

        List<InvitationResponseDto> invitationResponseDtoList = invitationSet.stream().map(invitation -> {
            Set<Slot> slotSetToUse;
            if (invitation.getSlotIncludeMode() == SlotIncludeMode.ALL_AND_FUTURE) {
                slotSetToUse = allEventSlots;
            } else {
                slotSetToUse = invitation.getSlotSet();
            }
            return dtoMapper.toInvitationResponseDto(invitation, slotSetToUse);
        }).collect(Collectors.toList());

        redisCacheService.set(CacheKeys.invitationListBySlotId(userId, eventId, slotId), invitationResponseDtoList,
                Duration.ofMinutes(5));

        return invitationResponseDtoList;
    }

    @Override
    @PreAuthorize("@authz.isUser() and hasAnyAuthority('ROLE_ADMIN','ROLE_ORGANIZER')")
    @Transactional
    public Long deleteInvitationByEventAndId(Long eventId, Long invitationId) {
        Long userId = securityUtil.requireUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND, ErrorCode.USER_NOT_FOUND));
        eventRepository.findByIdAndUser(eventId, user)
                .orElseThrow(() -> new AppException("Event not found with eventId and user", HttpStatus.NOT_FOUND,
                        ErrorCode.EVENT_NOT_FOUND));
        Invitation invitation = invitationRepository.findByEventIdAndId(eventId, invitationId)
                .orElseThrow(() -> new AppException("Invitation not found with event and invitationId",
                        HttpStatus.NOT_FOUND, ErrorCode.INVITATION_NOT_FOUND));

        List<Long> slotIds = invitation.getSlotSet().stream().map(slot -> slot.getId()).toList();

        String invitationToken = invitation.getAccessToken();

        invitationRepository.delete(invitation);

        eventPublisher.publishEvent(InvitationServiceEvent.invitationUpdated(userId, eventId, slotIds, invitationToken));

        return invitationId;
    }

    // public access
    @Override
    public InvitationValidationResponseDto validateInvitationAccess(String token) {
        Invitation invitation = invitationRepository.findByAccessTokenWithEvent(token)
                .orElseThrow(() -> new AppException("Invitation not found with token", HttpStatus.NOT_FOUND,
                        ErrorCode.INVITATION_NOT_FOUND));

        Long userId = securityUtil.getUserIdOrNull();

        InvitationUsage invitationUsage = null;
        if (userId != null) {
            InvitationUsageId invitationUsageId = new InvitationUsageId(invitation.getId(),
                    this.securityUtil.requireUserId());

            invitationUsage = invitationUsageRepository.findById(invitationUsageId).orElse(null);
        } 

        ValidationResult result = invitationValidator.validateAccess(invitation, userId, invitationUsage);

        return buildInvitationValidationResponseDto(
                result.isValid(),
                invitation,
                result.getMessage());

    }

    private InvitationValidationResponseDto buildInvitationValidationResponseDto(Boolean validity, Invitation inv,
            String reason) {
        return new InvitationValidationResponseDto(
                validity,
                inv.isRequiredLogin(),
                inv.getAccessToken(),
                inv.getEvent().getEventName(),
                inv.getExpiresAt(),
                reason);
    }

    //public access
    @Override
    public InvitationResponseDto getInvitationByToken(String token) {

        InvitationResponseDto cacheDto = redisCacheService.get(CacheKeys.invitationByToken(token), InvitationResponseDto.class);

        if(cacheDto!= null){
            return cacheDto;
        }

        Invitation invitation = invitationRepository.findByAccessTokenWithEventAndSlotSet(token)
                .orElseThrow(() -> new AppException("Invitation not found with token", HttpStatus.NOT_FOUND,
                        ErrorCode.INVITATION_NOT_FOUND));

        Set<Slot> slotSet;
        if (invitation.getSlotIncludeMode() == SlotIncludeMode.ALL_AND_FUTURE) {
            slotSet = slotRepository.findByEventWithEvent(invitation.getEvent( ));
        } else {
            slotSet = invitation.getSlotSet();
        }

        InvitationResponseDto invitationResponseDto = dtoMapper.toInvitationResponseDto(invitation, slotSet);

        Long eventId = invitation.getEvent().getId();

        redisCacheService.setAndGroup(
            CacheKeys.invitationByToken(token), 
            invitationResponseDto, 
            Duration.ofMinutes(5), 
            CacheKeys.groupInvitationTokens(eventId),
            token
        );

        return invitationResponseDto;
    }

}
