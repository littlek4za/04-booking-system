package com.littlek4za.booking_system.services;

import java.util.List;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.littlek4za.booking_system.dtos.SlotRequestDto;
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

    public SlotServiceImpl(SecurityUtil sercurityUtil, SlotRepository slotRepository, EventRepository eventRepository,
            UserRepository userRepository, DtoMapper dtoMapper, SlotValidator slotValidator) {
        this.securityUtil = sercurityUtil;
        this.slotRepository = slotRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.dtoMapper = dtoMapper;
        this.slotValidator = slotValidator;
    }

    @Override
    public List<SlotResponseDto> getSlotsByEvent(Long eventId) {

        User user = userRepository.findById(this.securityUtil.getCurrentAuthUserId())
                .orElseThrow(() -> new AppException("Unknow User", HttpStatus.NOT_FOUND));
        Event event = eventRepository.findByIdAndUser(eventId, user)
                .orElseThrow(() -> new AppException("No event found with this Id and User", HttpStatus.NOT_FOUND));

        Set<Slot> slotSet = slotRepository.findByEvent(event);
        List<SlotResponseDto> slotResponseDtoList = slotSet.stream()
                .map(slot -> dtoMapper.toSlotResponseDto(slot))
                .toList();

        return slotResponseDtoList;
    }

    @Transactional
    @Override
    public SlotResponseDto createSlotByEvent(Long eventId, SlotRequestDto slotRequestDto) {

        User user = userRepository.findById(this.securityUtil.getCurrentAuthUserId())
                .orElseThrow(() -> new AppException("Unknow User", HttpStatus.NOT_FOUND));
        Event event = eventRepository.findByIdAndUser(eventId, user)
                .orElseThrow(() -> new AppException("No event found with this Id and User", HttpStatus.NOT_FOUND));

        slotValidator.validate(event.getEventType(), slotRequestDto);

        Slot newSlot = dtoMapper.toSlot(slotRequestDto, event);

        if (!event.getEventType().supportMaxBookPerInterval()) {
            if (newSlot.getMaxBookPerInterval() == null) {
                newSlot.setMaxBookPerInterval(1);
            }
        }

        Slot savedSlot = slotRepository.save(newSlot);

        return dtoMapper.toSlotResponseDto(savedSlot);
    }

    @Override
    @Transactional
    public Long deleteSlotByEventAndSlot(Long eventId, Long slotId) {
        User user = userRepository.findById(securityUtil.getCurrentAuthUserId())
                .orElseThrow(() -> new AppException("Unknown User", HttpStatus.NOT_FOUND));
        Event event = eventRepository.findByIdAndUser(eventId, user)
                .orElseThrow(() -> new AppException("No event found with eventId and User",
                        HttpStatus.NOT_FOUND));

        int deleted = this.slotRepository.deleteByIdAndEventId(slotId, event.getId());

        if (deleted == 0) {
            throw new AppException("No slot found with slotId and Event",
                    HttpStatus.NOT_FOUND);
        }

        return slotId;
    }

    @Override
    public SlotResponseDto getSlotById(Long eventId, Long slotId) {
        User user = userRepository.findById(securityUtil.getCurrentAuthUserId())
                .orElseThrow(() -> new AppException("Unknown User", HttpStatus.NOT_FOUND));
        Event event = eventRepository.findByIdAndUser(eventId, user)
                .orElseThrow(() -> new AppException("No event found with eventId and User",
                        HttpStatus.NOT_FOUND));
        Slot slot = slotRepository.findByIdAndEventId(slotId, event.getId())
                .orElseThrow(() -> new AppException("No slot found with slotId and eventId",
                        HttpStatus.NOT_FOUND));

        return dtoMapper.toSlotResponseDto(slot);
    }

    @Override
    public SlotResponseDto putSlotByIdAndEventId(Long slotId, Long eventId, SlotRequestDto slotRequestDto) {
        User user = userRepository.findById(securityUtil.getCurrentAuthUserId())
                .orElseThrow(() -> new AppException("Unknown User", HttpStatus.NOT_FOUND));
        Event event = eventRepository.findByIdAndUser(eventId, user)
                .orElseThrow(() -> new AppException("No event found with eventId and User",
                        HttpStatus.NOT_FOUND));

        Slot existingSlot = slotRepository.findByIdAndEventId(slotId, event.getId())
                .orElseThrow(() -> new AppException("No slot found with slotId and eventId",
                        HttpStatus.NOT_FOUND));

        slotValidator.validate(event.getEventType(), slotRequestDto);

        existingSlot.setSlotName(slotRequestDto.slotName());
        existingSlot.setSlotDescription(slotRequestDto.slotDescription());
        existingSlot.setSlotStartTime(slotRequestDto.slotStartTime());
        existingSlot.setSlotEndTime(slotRequestDto.slotEndTime());
        existingSlot.setMaxBookPerInterval(slotRequestDto.maxBookPerInterval());
        existingSlot.setSlotIntervalMinutes(slotRequestDto.slotIntervalMinutes());
        existingSlot.setSlotFrequencyIntervalMinutes(slotRequestDto.slotFrequencyIntervalMinutes());
        existingSlot.setBusinessDaysHours(slotRequestDto.businessDaysHours());

        Slot updatedSlot = slotRepository.save(existingSlot);

        return dtoMapper.toSlotResponseDto(updatedSlot);
    }
}
