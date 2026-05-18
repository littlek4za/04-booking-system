package com.littlek4za.booking_system.services;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

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
import com.littlek4za.booking_system.models.SlotIncludeMode;
import com.littlek4za.booking_system.repos.BookingRepository;
import com.littlek4za.booking_system.repos.EventRepository;
import com.littlek4za.booking_system.repos.InvitationRepository;
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
        private final BookingRepository bookingRepository;
        private final InvitationRepository invitationRepository;
        private final DeleteValidationService deleteValidationService;

        public SlotServiceImpl(SecurityUtil sercurityUtil, SlotRepository slotRepository,
                        EventRepository eventRepository,
                        UserRepository userRepository, DtoMapper dtoMapper, SlotValidator slotValidator,
                        BookingRepository bookingRepository, InvitationRepository invitationRepository,
                        DeleteValidationService deleteValidationService) {
                this.securityUtil = sercurityUtil;
                this.slotRepository = slotRepository;
                this.eventRepository = eventRepository;
                this.userRepository = userRepository;
                this.dtoMapper = dtoMapper;
                this.slotValidator = slotValidator;
                this.bookingRepository = bookingRepository;
                this.invitationRepository = invitationRepository;
                this.deleteValidationService = deleteValidationService;
        }

        @Override
        public List<SlotResponseDto> getSlotsByEventId(Long eventId) {

                User user = userRepository.findById(this.securityUtil.requireUserId())
                                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND,
                                                ErrorCode.USER_NOT_FOUND));
                Event event = eventRepository.findByIdAndUser(eventId, user)
                                .orElseThrow(() -> new AppException("Event not found with eventId and user",
                                                HttpStatus.NOT_FOUND, ErrorCode.EVENT_NOT_FOUND));
                Map<Long, Long> bookingsCountMap = this.bookingRepository.countBookingsByEventGrouped(eventId)
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

                return slotResponseDtoList;
        }

        @Override
        @Transactional
        public SlotResponseDto createSlotByEvent(Long eventId, SlotRequestDto slotRequestDto) {

                User user = userRepository.findById(this.securityUtil.requireUserId())
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

                return dtoMapper.toSlotResponseDto(savedSlot);
        }

        @Override
        @Transactional
        public Long deleteSlotByEventAndSlot(Long eventId, Long slotId) {
                User user = userRepository.findById(securityUtil.requireUserId())
                                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND,
                                                ErrorCode.USER_NOT_FOUND));
                Event event = eventRepository.findByIdAndUser(eventId, user)
                                .orElseThrow(() -> new AppException("Event not found with eventId and user",
                                                HttpStatus.NOT_FOUND, ErrorCode.EVENT_NOT_FOUND));
                // handle invitation deletion
                Slot slot = slotRepository.findByIdAndEventIdWithInvitationSet(slotId, event.getId())
                                .orElseThrow(() -> new AppException("Slot not found with slotId and eventId",
                                                HttpStatus.NOT_FOUND, ErrorCode.SLOT_NOT_FOUND));

                List<Booking> bookingList = bookingRepository.findBySlot_IdAndSlot_Event_IdAndIsDeletedFalse(slotId, eventId);

                boolean canDelete = true;

                if (!bookingList.isEmpty()) {
                        canDelete = deleteValidationService.buildDeleteValidationResponseDto(bookingList).canDelete();
                }

                if (canDelete == false) {
                        throw new AppException(
                                        "Slot cannot be deleted due to active bookings",
                                        HttpStatus.BAD_REQUEST,
                                        ErrorCode.SLOT_HAS_ACTIVE_BOOKINGS);
                }

                for (Invitation invitation : slot.getInvitationSet()) {
                        if (invitation.getSlotIncludeMode() == SlotIncludeMode.SELECTED) {
                                if (invitation.getSlotSet().size() == 1) {
                                        invitationRepository.delete(invitation);
                                } else {
                                        invitation.getSlotSet().remove(slot);
                                        invitationRepository.save(invitation);
                                }
                        }
                }

                int deleted = this.slotRepository.deleteByIdAndEventId(slotId, event.getId());

                if (deleted == 0) {
                        throw new AppException("Slot not found with slotId and eventId", HttpStatus.NOT_FOUND,
                                        ErrorCode.SLOT_NOT_FOUND);
                }

                return slotId;
        }

        @Override
        public SlotResponseDto getSlotByIdAndEventId(Long eventId, Long slotId) {
                User user = userRepository.findById(securityUtil.requireUserId())
                                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND,
                                                ErrorCode.USER_NOT_FOUND));
                Event event = eventRepository.findByIdAndUser(eventId, user)
                                .orElseThrow(() -> new AppException("Event not found with eventId and user",
                                                HttpStatus.NOT_FOUND, ErrorCode.EVENT_NOT_FOUND));
                Slot slot = slotRepository.findByIdAndEventId(slotId, event.getId())
                                .orElseThrow(() -> new AppException("Slot not found with slotId and eventId",
                                                HttpStatus.NOT_FOUND, ErrorCode.SLOT_NOT_FOUND));

                Long bookingsCount = bookingRepository.countBySlotId(slotId);

                return dtoMapper.toSlotResponseDto(slot, bookingsCount);
        }

        @Override
        @Transactional
        public SlotResponseDto putSlotByIdAndEventId(Long slotId, Long eventId, SlotRequestDto slotRequestDto) {
                User user = userRepository.findById(securityUtil.requireUserId())
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

                return dtoMapper.toSlotResponseDto(updatedSlot);
        }

        @Override
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

                List<Booking> bookingList = bookingRepository.findBySlot_IdAndSlot_Event_IdAndIsDeletedFalse(slotId, eventId);

                DeleteValidationResponseDto responseDto = deleteValidationService
                                .buildDeleteValidationResponseDto(bookingList);

                return responseDto;
        }

}
