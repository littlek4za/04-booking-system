package com.littlek4za.booking_system.services;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.littlek4za.booking_system.dtos.InvitationRequestDto;
import com.littlek4za.booking_system.dtos.InvitationResponseDto;
import com.littlek4za.booking_system.entities.Event;
import com.littlek4za.booking_system.entities.Invitation;
import com.littlek4za.booking_system.entities.Slot;
import com.littlek4za.booking_system.entities.User;
import com.littlek4za.booking_system.exception.AppException;
import com.littlek4za.booking_system.models.SlotIncludeMode;
import com.littlek4za.booking_system.repos.EventRepository;
import com.littlek4za.booking_system.repos.InvitationRepository;
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
    private final DtoMapper dtoMapper;

    public InvitationServiceImpl(InvitationRepository invitationRepository, SecurityUtil securityUtil,
            UserRepository userRepository, EventRepository eventRepository, SlotRepository slotRepository,
            DtoMapper dtoMapper) {
        this.invitationRepository = invitationRepository;
        this.securityUtil = securityUtil;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.slotRepository = slotRepository;
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
            
            slotSet = slotRepository.findByIdInAndEvent(invitationRequestDto.slotIdList(), eventId);
            if (slotSet.size() != invitationRequestDto.slotIdList().size()) {
                throw new AppException("Some slots do not belong to this event", HttpStatus.NOT_FOUND);
            }
            newInvitation = dtoMapper.toInvitation(invitationRequestDto, event, user);
            newInvitation.setSlotSet(slotSet);

        } else if (SlotIncludeMode.ALL_CURRENT.name().equals(invitationRequestDto.slotIncludeMode())) {

            List<Slot>slotList = slotRepository.findByEvent(event);
            slotSet = new HashSet<>(slotList);
            newInvitation = dtoMapper.toInvitation(invitationRequestDto, event, user);
            newInvitation.setSlotSet(slotSet);

        } else if (SlotIncludeMode.ALL_AND_FUTURE.name().equals(invitationRequestDto.slotIncludeMode())) {
           
            newInvitation = dtoMapper.toInvitation(invitationRequestDto, event, user);

        } else {
            throw new AppException("Invalid slotIncludeMode", HttpStatus.BAD_REQUEST);
        }

        if (newInvitation.getExpiresAt() == null) {
            newInvitation.setExpiresAt(Instant.parse("9999-12-31T23:59:59Z"));
        }

        Invitation savedInvitation = invitationRepository.save(newInvitation);

        return dtoMapper.toInvitationResponseDto(savedInvitation);

    }

}
