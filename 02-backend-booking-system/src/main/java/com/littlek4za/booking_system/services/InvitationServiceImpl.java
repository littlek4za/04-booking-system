package com.littlek4za.booking_system.services;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.littlek4za.booking_system.dtos.InvitationRequestDto;
import com.littlek4za.booking_system.dtos.InvitationResponseDto;
import com.littlek4za.booking_system.dtos.InvitationValidationResponseDto;
import com.littlek4za.booking_system.entities.Event;
import com.littlek4za.booking_system.entities.Invitation;
import com.littlek4za.booking_system.entities.Slot;
import com.littlek4za.booking_system.entities.User;
import com.littlek4za.booking_system.exception.AppException;
import com.littlek4za.booking_system.exception.model.ErrorCode;
import com.littlek4za.booking_system.models.SlotIncludeMode;
import com.littlek4za.booking_system.models.ValidationResult;
import com.littlek4za.booking_system.repos.EventRepository;
import com.littlek4za.booking_system.repos.InvitationRepository;
import com.littlek4za.booking_system.repos.SlotRepository;
import com.littlek4za.booking_system.repos.UserRepository;
import com.littlek4za.booking_system.security.SecurityUtil;
import com.littlek4za.booking_system.utils.DtoMapper;
import com.littlek4za.booking_system.validators.InvitationValidator;

import jakarta.transaction.Transactional;

@Service
public class InvitationServiceImpl implements InvitationService {

    private final InvitationRepository invitationRepository;
    private final SecurityUtil securityUtil;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final SlotRepository slotRepository;
    private final DtoMapper dtoMapper;
    private final InvitationValidator invitationValidator;

    public InvitationServiceImpl(InvitationRepository invitationRepository, SecurityUtil securityUtil,
            UserRepository userRepository, EventRepository eventRepository, SlotRepository slotRepository,
            DtoMapper dtoMapper, InvitationValidator invitationValidator) {
        this.invitationRepository = invitationRepository;
        this.securityUtil = securityUtil;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.slotRepository = slotRepository;
        this.dtoMapper = dtoMapper;
        this.invitationValidator = invitationValidator;
    }

    @Override
    @Transactional
    public InvitationResponseDto createInvitation(InvitationRequestDto invitationRequestDto, Long eventId) {

        User user = userRepository.findById(securityUtil.requireUserId())
                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND, ErrorCode.USER_NOT_FOUND));
        Event event = eventRepository.findByIdAndUser(eventId, user)
                .orElseThrow(() -> new AppException("Event not found with eventId and user", HttpStatus.NOT_FOUND, ErrorCode.EVENT_NOT_FOUND));

        Invitation newInvitation;
        Set<Slot> slotSet;
        if (SlotIncludeMode.SELECTED.name().equals(invitationRequestDto.slotIncludeMode())) {

            slotSet = slotRepository.findByIdInAndEventIdWithEvent(invitationRequestDto.slotIdList(), eventId);
            if (slotSet.size() != invitationRequestDto.slotIdList().size()) {
                throw new AppException("Some slots do not belong to this event", HttpStatus.BAD_REQUEST, ErrorCode.SLOT_EVENT_MISMATCH);
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
            throw new AppException("Slot include mode invalid", HttpStatus.BAD_REQUEST, ErrorCode.SLOT_INCLUDE_MODE_INVALID);
        }

        if (newInvitation.getExpiresAt() == null) {
            newInvitation.setExpiresAt(Instant.parse("9999-12-31T23:59:59Z"));
        }

        newInvitation.setAccessToken(generateUniqueAccessToken());

        Invitation savedInvitation = invitationRepository.save(newInvitation);

        Invitation invitationForResponse = invitationRepository
        .findByAccessTokenWithEventAndSlotSet(savedInvitation.getAccessToken())
        .orElseThrow(() -> new AppException("Invitation not found ", HttpStatus.NOT_FOUND, ErrorCode.INVITATION_NOT_FOUND));

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
    public List<InvitationResponseDto> getInvitationsByEventId(Long eventId) {

        User user = userRepository.findById(securityUtil.requireUserId())
                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND, ErrorCode.USER_NOT_FOUND));
        Event event = eventRepository.findByIdAndUser(eventId, user)
                .orElseThrow(() -> new AppException("Event not found with eventId and user", HttpStatus.NOT_FOUND, ErrorCode.EVENT_NOT_FOUND));

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

        return invitationResponseDtoList;
    }

    @Override
    public List<InvitationResponseDto> getInvitationsByEventIdAndSlotId(Long eventId, Long slotId) {
        User user = userRepository.findById(securityUtil.requireUserId())
                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND, ErrorCode.USER_NOT_FOUND));
        Event event = eventRepository.findByIdAndUser(eventId, user)
                .orElseThrow(() -> new AppException("Event not found with eventId and user", HttpStatus.NOT_FOUND, ErrorCode.EVENT_NOT_FOUND));

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

        return invitationResponseDtoList;
    }

    @Override
    @Transactional
    public Long deleteInvitationByEventAndId(Long eventId, Long invitationId) {
        User user = userRepository.findById(securityUtil.requireUserId())
                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND, ErrorCode.USER_NOT_FOUND));
        Event event = eventRepository.findByIdAndUser(eventId, user)
                .orElseThrow(() -> new AppException("Event not found with eventId and user", HttpStatus.NOT_FOUND, ErrorCode.EVENT_NOT_FOUND));
        Invitation invitation = invitationRepository.findByEventAndId(event, invitationId)
                .orElseThrow(() -> new AppException("Invitation not found with event and invitationId", HttpStatus.NOT_FOUND, ErrorCode.INVITATION_NOT_FOUND));

        invitationRepository.delete(invitation);

        return invitationId;
    }

    // guest and user share
    @Override
    public InvitationValidationResponseDto validateInvitationAccess(String token) {
        Invitation invitation = invitationRepository.findByAccessTokenWithEvent(token)
                .orElseThrow(() -> new AppException("Invitation not found with token", HttpStatus.NOT_FOUND, ErrorCode.INVITATION_NOT_FOUND));

        Long userId = securityUtil.getUserIdOrNull();

        ValidationResult result = invitationValidator.validateAccess(invitation, userId);

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

    @Override
    public InvitationResponseDto getInvitationByToken(String token) {
        Invitation invitation = invitationRepository.findByAccessTokenWithEventAndSlotSet(token)
                .orElseThrow(() -> new AppException("Invitation not found with token", HttpStatus.NOT_FOUND, ErrorCode.INVITATION_NOT_FOUND));

        Set<Slot> slotSet;
        if (invitation.getSlotIncludeMode() == SlotIncludeMode.ALL_AND_FUTURE) {
            slotSet = slotRepository.findByEventWithEvent(invitation.getEvent());
        } else {
            slotSet = invitation.getSlotSet();
        }
        return dtoMapper.toInvitationResponseDto(invitation, slotSet);
    }

}
