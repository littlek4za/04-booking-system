package com.littlek4za.booking_system.services;

import com.littlek4za.booking_system.repos.InvitationRepository;
import com.littlek4za.booking_system.repos.SlotRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.littlek4za.booking_system.dtos.GuestAccessTokenDto;
import com.littlek4za.booking_system.dtos.GuestBookingCreateAccessRequestDto;
import com.littlek4za.booking_system.dtos.GuestBookingCreateInitRequestDto;
import com.littlek4za.booking_system.dtos.GuestBookingCreateInitResponseDto;
import com.littlek4za.booking_system.dtos.GuestBookingViewAccessRequestDto;
import com.littlek4za.booking_system.dtos.GuestBookingViewInitRequestDto;
import com.littlek4za.booking_system.dtos.GuestBookingViewInitResponseDto;
import com.littlek4za.booking_system.entities.Booking;
import com.littlek4za.booking_system.entities.Invitation;
import com.littlek4za.booking_system.entities.Slot;
import com.littlek4za.booking_system.exception.AppException;
import com.littlek4za.booking_system.exception.model.ErrorCode;
import com.littlek4za.booking_system.repos.BookingRepository;
import com.littlek4za.booking_system.security.JwtTokenService;
import com.littlek4za.booking_system.validators.BookingValidator;

@Service
public class GuestBookingAccessServiceImpl implements GuestBookingAccessService {

    private final InvitationRepository invitationRepository;
    private final SlotRepository slotRepository;
    private final BookingRepository bookingRepository;
    private final RedisRiskService riskService;
    private final CaptchaService captchaService;
    private final BookingValidator bookingValidator;
    private final JwtTokenService jwtTokenService;

    public GuestBookingAccessServiceImpl(BookingRepository bookingRepository, RedisRiskService riskService,
            CaptchaService captchaService, InvitationRepository invitationRepository,
            SlotRepository slotRepository, BookingValidator bookingValidator, JwtTokenService jwtTokenService) {
        this.slotRepository = slotRepository;
        this.bookingRepository = bookingRepository;
        this.riskService = riskService;
        this.captchaService = captchaService;
        this.invitationRepository = invitationRepository;
        this.bookingValidator = bookingValidator;
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    public GuestBookingViewInitResponseDto initGuestBookingViewAccess(GuestBookingViewInitRequestDto requestDto,
            String ip) {

        Boolean valid = true;
        try {
            validateBookingOrRecordFailure(requestDto.email(), requestDto.bookingToken(), ip);
        } catch (Exception e) {
            valid = false;
        }

        boolean captchaRequired = riskService.shouldLimiBookingtView(requestDto.email(), ip);

        if(captchaRequired){
            valid = null;
        }

        return new GuestBookingViewInitResponseDto(captchaRequired, valid);
    }

    @Override
    public GuestAccessTokenDto issueGuestBookingViewAccessToken(GuestBookingViewAccessRequestDto requestDto,
            String ip) {

        boolean captchaRequired = riskService.shouldLimiBookingtView(requestDto.email(), ip);

        if (captchaRequired) {
            if (requestDto.captchaToken() == null) {
                throw new AppException("Captcha is required", HttpStatus.FORBIDDEN, ErrorCode.CAPTCHA_REQUIRED);
            }

            boolean validCaptcha = captchaService.verify(requestDto.captchaToken());

            if (!validCaptcha) {
                riskService.recordAttemptForBookingView(requestDto.email(), ip);
                throw new AppException("Captcha invalid", HttpStatus.FORBIDDEN, ErrorCode.CAPTCHA_INVALID);
            }
        }

        try {
            validateBookingOrRecordFailure(requestDto.email(), requestDto.bookingToken(), ip);
        } catch (Exception e) {
            riskService.recordAttemptForBookingView(requestDto.email(), ip);
            throw e;
        }

        riskService.resetEmailIpForBookingView(requestDto.email(), ip);
        riskService.reduceIpPenaltyForBookingView(ip);

        System.out.println("Issuing guest token for: " + requestDto.email());
        System.out.println("Captcha required: " + captchaRequired);
        System.out.println("Captcha token: " + requestDto.captchaToken());

        return jwtTokenService.toGuestAccessTokenDto(jwtTokenService.createGuestBookingViewToken(requestDto.email(),requestDto.bookingToken()));

    }

    private void validateBookingOrRecordFailure(String email, String bookingToken, String ip) {
        Booking booking = bookingRepository.findByBookingToken(bookingToken)
                .orElseThrow(() -> {
                    riskService.recordAttemptForBookingView(email, ip);
                    return new AppException("Booking not found", HttpStatus.NOT_FOUND, ErrorCode.BOOKING_NOT_FOUND);
                });

        if (!booking.getUser().getEmail().equalsIgnoreCase(email)) {
            riskService.recordAttemptForBookingView(email, ip);
            throw new AppException("Booking not found", HttpStatus.NOT_FOUND, ErrorCode.BOOKING_NOT_FOUND);
        }
    }

    @Override
    public GuestBookingCreateInitResponseDto initGuestBookingCreateAccess(GuestBookingCreateInitRequestDto requestDto,
            String clientIp) {

        boolean captchaRequired = riskService.shouldLimitBookingCreate(requestDto.email(), clientIp);

        return new GuestBookingCreateInitResponseDto(captchaRequired);
    }

    @Override
    public GuestAccessTokenDto issueGuestBookingCreateAccessToken(GuestBookingCreateAccessRequestDto requestDto,
            String clientIp) {
        boolean captchaRequired = riskService.shouldLimitBookingCreate(requestDto.email(), clientIp);

        if (captchaRequired) {
            if (requestDto.captchaToken() == null) {
                throw new AppException("Captcha is required", HttpStatus.FORBIDDEN, ErrorCode.CAPTCHA_REQUIRED);
            }

            boolean validCaptcha = captchaService.verify(requestDto.captchaToken());

            if (!validCaptcha) {
                riskService.recordAttemptForBookingCreate(requestDto.email(), clientIp);
                throw new AppException("Captcha invalid",
                        HttpStatus.FORBIDDEN, ErrorCode.CAPTCHA_INVALID);
            }
        }

        // risk record
        Invitation invitation = invitationRepository.findByIdWithSlots(requestDto.invitationId())
                .orElseThrow(() -> {
                    riskService.recordAttemptForBookingCreate(requestDto.email(), clientIp);

                    return new AppException(
                            "Invitation not found",
                            HttpStatus.NOT_FOUND,
                            ErrorCode.INVITATION_NOT_FOUND);
                });
        Slot slot = slotRepository.findById(requestDto.slotId())
                .orElseThrow(() -> {
                    riskService.recordAttemptForBookingCreate(requestDto.email(), clientIp);

                    return new AppException(
                            "Slot not found",
                            HttpStatus.NOT_FOUND,
                            ErrorCode.SLOT_NOT_FOUND);
                });

        try {
            bookingValidator.validateSlotBelongsToInvitation(slot, invitation);
        } catch (AppException e) {

            if (e.getErrorCode() == ErrorCode.SLOT_INVITATION_MISMATCH) {
                riskService.recordAttemptForBookingCreate(requestDto.email(), clientIp);
            }

            throw e;
        }

        riskService.resetEmailIpForBookingCreate(requestDto.email(), clientIp);
        riskService.reduceIpPenaltyForBookingCreate(clientIp);

        return jwtTokenService.toGuestAccessTokenDto(jwtTokenService.createGuestBookingCreateToken(requestDto.email()));
    }

}
