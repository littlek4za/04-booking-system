package com.littlek4za.booking_system.services;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.littlek4za.booking_system.dtos.SlotResponseDto;
import com.littlek4za.booking_system.entities.Event;
import com.littlek4za.booking_system.entities.Slot;
import com.littlek4za.booking_system.entities.User;
import com.littlek4za.booking_system.exception.AppException;
import com.littlek4za.booking_system.repos.EventRepository;
import com.littlek4za.booking_system.repos.SlotRepository;
import com.littlek4za.booking_system.repos.UserRepository;
import com.littlek4za.booking_system.security.SecurityUtil;
import com.littlek4za.booking_system.utils.DtoMapper;

@Service
public class SlotServiceImpl implements SlotService {

    private final SecurityUtil sercurityUtil;
    private final SlotRepository slotRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final DtoMapper dtoMapper;

    public SlotServiceImpl(SecurityUtil sercurityUtil, SlotRepository slotRepository, EventRepository eventRepository,
            UserRepository userRepository, DtoMapper dtoMapper) {
        this.sercurityUtil = sercurityUtil;
        this.slotRepository = slotRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.dtoMapper = dtoMapper;
    }

    @Override
    public List<SlotResponseDto> getSlotsByEvent(Long eventId) {

        User user = userRepository.findById(this.sercurityUtil.getCurrentAuthUserId())
                                .orElseThrow(()-> new AppException("Unknow User", HttpStatus.NOT_FOUND));
        Event event = eventRepository.findByIdAndUser(eventId, user)
                                .orElseThrow(()-> new AppException("No event found with this Id and User", HttpStatus.NOT_FOUND));
            
        List<Slot> slotList = slotRepository.findByEvent(event);
        List<SlotResponseDto> slotResponseDtoList = slotList.stream()
                                .map(slot -> dtoMapper.toSlotResponseDto(slot))
                                .toList();
        
        return slotResponseDtoList;
    }

}
