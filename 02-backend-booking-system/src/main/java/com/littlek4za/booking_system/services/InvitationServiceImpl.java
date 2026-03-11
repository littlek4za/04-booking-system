package com.littlek4za.booking_system.services;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.littlek4za.booking_system.dtos.InvitationRequestDto;
import com.littlek4za.booking_system.dtos.InvitationResponseDto;
import com.littlek4za.booking_system.dtos.InvitationValidationResponseDto;
import com.littlek4za.booking_system.entities.Event;
import com.littlek4za.booking_system.entities.Invitation;
import com.littlek4za.booking_system.entities.InvitationUsage;
import com.littlek4za.booking_system.entities.Slot;
import com.littlek4za.booking_system.entities.User;
import com.littlek4za.booking_system.exception.AppException;
import com.littlek4za.booking_system.models.SlotIncludeMode;
import com.littlek4za.booking_system.repos.EventRepository;
import com.littlek4za.booking_system.repos.InvitationRepository;
import com.littlek4za.booking_system.repos.InvitationUsageRepository;
import com.littlek4za.booking_system.repos.SlotRepository;
import com.littlek4za.booking_system.repos.UserRepository;
import com.littlek4za.booking_system.security.SecurityUtil;
import com.littlek4za.booking_system.utils.DtoMapper;

@Service
public class InvitationServiceImpl implements InvitationService {

    private final InvitationRepository invitationRepository;
    private final SecurityUtil securityUtil;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final SlotRepository slotRepository;
    private final InvitationUsageRepository invitationUsageRepository;
    private final DtoMapper dtoMapper;

    public InvitationServiceImpl(InvitationRepository invitationRepository, SecurityUtil securityUtil,
            UserRepository userRepository, EventRepository eventRepository, SlotRepository slotRepository,
            InvitationUsageRepository invitationUsageRepository, DtoMapper dtoMapper) {
        this.invitationRepository = invitationRepository;
        this.securityUtil = securityUtil;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.slotRepository = slotRepository;
        this.invitationUsageRepository = invitationUsageRepository;
        this.dtoMapper = dtoMapper;
    }

    @Override
    public InvitationResponseDto createInvitation(InvitationRequestDto invitationRequestDto, Long eventId) {

        User user = userRepository.findById(securityUtil.getCurrentAuthUserId())
                .orElseThrow(() -> new AppException("Unknow User", HttpStatus.NOT_FOUND));
        Event event = eventRepository.findByIdAndUser(eventId, user)
                .orElseThrow(() -> new AppException("No event found with this Id and User", HttpStatus.NOT_FOUND));

        Invitation newInvitation;
        Set<Slot> slotSet;
        if (SlotIncludeMode.SELECTED.name().equals(invitationRequestDto.slotIncludeMode())) {

            slotSet = slotRepository.findByIdInAndEventId(invitationRequestDto.slotIdList(), eventId);
            if (slotSet.size() != invitationRequestDto.slotIdList().size()) {
                throw new AppException("Some slots do not belong to this event", HttpStatus.NOT_FOUND);
            }

            if (slotSet.isEmpty()) {
                throw new AppException("No Slot found, Please create slot before create invitation",
                        HttpStatus.NOT_FOUND);
            }
            newInvitation = dtoMapper.toInvitation(invitationRequestDto, event, user);
            newInvitation.setSlotSet(slotSet);

        } else if (SlotIncludeMode.ALL_CURRENT.name().equals(invitationRequestDto.slotIncludeMode())) {

            slotSet = slotRepository.findByEvent(event);

            if (slotSet.isEmpty()) {
                throw new AppException("No Slot found, Please create slot before create invitation",
                        HttpStatus.NOT_FOUND);
            }

            newInvitation = dtoMapper.toInvitation(invitationRequestDto, event, user);
            newInvitation.setSlotSet(slotSet);

        } else if (SlotIncludeMode.ALL_AND_FUTURE.name().equals(invitationRequestDto.slotIncludeMode())) {

            slotSet = slotRepository.findByEvent(event);

            if (slotSet.isEmpty()) {
                throw new AppException("No Slot found, Please create slot before create invitation",
                        HttpStatus.NOT_FOUND);
            }

            newInvitation = dtoMapper.toInvitation(invitationRequestDto, event, user);

        } else {
            throw new AppException("Invalid slotIncludeMode", HttpStatus.BAD_REQUEST);
        }

        if (newInvitation.getExpiresAt() == null) {
            newInvitation.setExpiresAt(Instant.parse("9999-12-31T23:59:59Z"));
        }

        newInvitation.setAccessToken(generateUniqueAccessToken());

        Invitation savedInvitation = invitationRepository.save(newInvitation);

        return dtoMapper.toInvitationResponseDto(savedInvitation, slotSet);
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

        User user = userRepository.findById(securityUtil.getCurrentAuthUserId())
                .orElseThrow(() -> new AppException("Unknow User", HttpStatus.NOT_FOUND));
        Event event = eventRepository.findByIdAndUser(eventId, user)
                .orElseThrow(() -> new AppException("No event found with this Id and User", HttpStatus.NOT_FOUND));

        Set<Invitation> invitationSet = invitationRepository.findByEventWithSlotSet(event);
        Set<Slot> allEventSlots = slotRepository.findByEvent(event);

        List<InvitationResponseDto> invitationResponseDtoList = 
        invitationSet.stream().map(invitation -> {
            Set<Slot> slotSetToUse;
            if(invitation.getSlotIncludeMode() == SlotIncludeMode.ALL_AND_FUTURE) {
                slotSetToUse = allEventSlots; 
            } else {
                slotSetToUse = invitation.getSlotSet();
            }
            return dtoMapper.toInvitationResponseDto(invitation, slotSetToUse);
        }).collect(Collectors.toList());

        return invitationResponseDtoList;
    }

    @Override
    public Long deleteInvitationByEventAndId(Long eventId, Long invitationId) {
        User user = userRepository.findById(securityUtil.getCurrentAuthUserId())
                .orElseThrow(() -> new AppException("Unknow User", HttpStatus.NOT_FOUND));
        Event event = eventRepository.findByIdAndUser(eventId, user)
                .orElseThrow(() -> new AppException("No event found with this Id and User", HttpStatus.NOT_FOUND));
        Invitation invitation = invitationRepository.findByEventAndId(event, invitationId)
                .orElseThrow(() -> new AppException("No invitation found", HttpStatus.NOT_FOUND));

        invitationRepository.delete(invitation);

        return invitationId;
    }

    @Override
    public InvitationValidationResponseDto validateAccessToken(String token) {
        Invitation invitation = invitationRepository.findByAccessTokenWithEvent(token)
                .orElseThrow(() -> new AppException("Unknown token", HttpStatus.NOT_FOUND));

        Instant now = Instant.now();

        if (now.isAfter(invitation.getExpiresAt())) {
            return buildInvitationValidationResponseDto(
                    false,
                    invitation,
                    "TOKEN EXPIRED");
        }

        if (invitation.getMaxUsage() != null && invitation.getUsedCount() >= invitation.getMaxUsage()) {
            return buildInvitationValidationResponseDto(
                    false,
                    invitation,
                    "REACHED MAXIMUM USAGE");
        }

        if (invitation.getMaxUsagePerUser() != null) {

            Long userId = securityUtil.getCurrentAuthUserIdOrNull();

            if (userId != null) {
                Optional<InvitationUsage> invitationUsage = invitationUsageRepository
                        .findByUserIdAndInvitationId(userId, invitation.getId());

                // int usageCount = 0;
                // if(invitationUsage.isPresent()){
                // usageCount = invitationUsage.get().getUsageCount();
                // }

                int usageCount = invitationUsage.map(u -> u.getUsageCount()).orElse(0);

                if (usageCount >= invitation.getMaxUsagePerUser()) {
                    return buildInvitationValidationResponseDto(
                            false,
                            invitation,
                            "REACHED MAXIMUM USAGE PER USER");
                }
            }

        }

        return buildInvitationValidationResponseDto(
                true,
                invitation,
                null);

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
                        .orElseThrow(()-> new AppException("no Invitation Found", HttpStatus.NOT_FOUND));

        Set<Slot> slotSet;
        if(invitation.getSlotIncludeMode() == SlotIncludeMode.ALL_AND_FUTURE){
            slotSet = slotRepository.findByEvent(invitation.getEvent());
        } else {
            slotSet = invitation.getSlotSet();
        }
        return dtoMapper.toInvitationResponseDto(invitation,slotSet);
    }
}
