package com.littlek4za.booking_system.services;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.littlek4za.booking_system.dtos.GuestAccessTokenDto;
import com.littlek4za.booking_system.dtos.GuestBookingViewAccessRequestDto;
import com.littlek4za.booking_system.dtos.GuestBookingViewInitRequestDto;
import com.littlek4za.booking_system.dtos.GuestBookingViewInitResponseDto;
import com.littlek4za.booking_system.entities.Booking;
import com.littlek4za.booking_system.exception.AppException;
import com.littlek4za.booking_system.exception.model.ErrorCode;
import com.littlek4za.booking_system.models.RoleName;
import com.littlek4za.booking_system.models.TokenType;
import com.littlek4za.booking_system.repos.BookingRepository;
import com.littlek4za.booking_system.security.UserAuthProvider;

@Service
public class GuestBookingAccessServiceImpl implements GuestBookingAccessService {

    private final BookingRepository bookingRepository;
    private final RiskService riskService;
    private final CaptchaService captchaService;
    private final UserAuthProvider userAuthProvider;

    @Value("${security.jwt.token.secret-key:dev-secret-key}")
    private String secretKey;
    @Value("${security.jwt.issuer:booking-system}")
    private String issuerString;

    public GuestBookingAccessServiceImpl(BookingRepository bookingRepository, RiskService riskService,
            CaptchaService captchaService, UserAuthProvider userAuthProvider) {
        this.bookingRepository = bookingRepository;
        this.riskService = riskService;
        this.captchaService = captchaService;
        this.userAuthProvider = userAuthProvider;
    }

    @Override
    public GuestBookingViewInitResponseDto initGuestBookingViewAccess(GuestBookingViewInitRequestDto requestDto) {

        validateBookingOrRecordFailure(requestDto.email(), requestDto.bookingToken());

        boolean captchaRequired = riskService.shouldRequireCaptcha(requestDto.email());

        return new GuestBookingViewInitResponseDto(captchaRequired);
    }

    @Override
    public GuestAccessTokenDto issueGuestBookingViewAccessToken(GuestBookingViewAccessRequestDto requestDto) {

        validateBookingOrRecordFailure(requestDto.email(), requestDto.bookingToken());

        boolean captchaRequired = riskService.shouldRequireCaptcha(requestDto.email());

        if (captchaRequired) {
            if (requestDto.captchaToken() == null) {
                throw new AppException("Captcha is required", HttpStatus.FORBIDDEN, ErrorCode.CAPTCHA_REQUIRED);
            }

            boolean validCaptcha = captchaService.verify(requestDto.captchaToken());

            if (!validCaptcha) {
                riskService.recordAttempt(requestDto.email());
                throw new AppException("Captcha invalid", HttpStatus.FORBIDDEN, ErrorCode.CAPTCHA_INVALID);
            }
        }

        riskService.reset(requestDto.email());

        Instant now = Instant.now();
        Instant expiry = now.plus(15, ChronoUnit.MINUTES);

        return userAuthProvider.toGuestAccessTokenDto(
                JWT.create()
                        .withIssuer(issuerString)
                        .withSubject(requestDto.email())
                        .withIssuedAt(now)
                        .withExpiresAt(expiry)
                        .withClaim("email", requestDto.email())
                        .withClaim("roles", RoleName.ROLE_ATTENDEE.name())
                        .withClaim("tokenType", TokenType.GUEST_BOOKING_VIEW.name())
                        .sign(Algorithm.HMAC256(secretKey)));

    }

    private void validateBookingOrRecordFailure(String email, String bookingToken) {
        Booking booking = bookingRepository.findByBookingToken(bookingToken)
                .orElseThrow(() -> {
                    riskService.recordAttempt(email);
                    return new AppException("Booking not found", HttpStatus.NOT_FOUND, ErrorCode.BOOKING_NOT_FOUND);
                });

        if (!booking.getUser().getEmail().equalsIgnoreCase(email)) {
            riskService.recordAttempt(email);
            throw new AppException("Booking not found", HttpStatus.NOT_FOUND, ErrorCode.BOOKING_NOT_FOUND);
        }
    }

}
