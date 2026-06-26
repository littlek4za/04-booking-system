package com.littlek4za.booking_system.security;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.littlek4za.booking_system.features.auth.dto.UserAccessTokenDto;
import com.littlek4za.booking_system.features.auth.dto.UserDto;
import com.littlek4za.booking_system.features.auth.model.RoleType;
import com.littlek4za.booking_system.features.auth.model.TokenType;
import com.littlek4za.booking_system.features.guest_access.dto.GuestAccessTokenDto;

import jakarta.annotation.PostConstruct;

// handle jwtoken
@Service
public class JwtTokenService {

    @Value("${security.jwt.token.secret-key:dev-secret-key}")
    private String secretKey;

    @Value("${security.jwt.issuer:booking-system}")
    private String issuer;

    private Algorithm algorithm;

    private JWTVerifier verifier;

    @PostConstruct
    public void init() {
        secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
        algorithm = Algorithm.HMAC256(secretKey);
        verifier = JWT.require(algorithm)
                .withIssuer(issuer)
                .build();
    }

    // token generate
    public String createUserToken(UserDto userDto) {
        Instant now = Instant.now();
        Instant expiry = now.plus(10, ChronoUnit.HOURS);

        return JWT.create()
                .withIssuer(issuer)
                .withSubject(userDto.username())
                .withIssuedAt(now)
                .withExpiresAt(expiry)
                .withClaim("firstName", userDto.firstName())
                .withClaim("lastName", userDto.lastName())
                .withClaim("email", userDto.email())
                .withClaim("roles", new ArrayList<>(
                        userDto.roleSet()
                                .stream()
                                .map(Enum::name)
                                .toList()))
                .withClaim("tokenType", TokenType.USER.name())
                .sign(algorithm);
    }

    public String createGuestBookingViewToken(String email, String bookingToken) {
        return createGuestToken(email, TokenType.GUEST_BOOKING_VIEW, bookingToken);
    }

    public String createGuestBookingCreateToken(String email) {
        return createGuestToken(email, TokenType.GUEST_BOOKING_CREATE,null);
    }

    private String createGuestToken(String email, TokenType tokenType, String bookingToken) {

        if (!tokenType.isGuestToken()) {
            throw new IllegalArgumentException("Invalid guest token type");
        }
        Instant now = Instant.now();
        Instant expiry = now.plus(15, ChronoUnit.MINUTES);

        JWTCreator.Builder builder = JWT.create()
                .withIssuer(issuer)
                .withSubject(email)
                .withIssuedAt(now)
                .withExpiresAt(expiry)
                .withClaim("email", email)
                .withClaim("roles", RoleType.ROLE_ATTENDEE.name())
                .withClaim("tokenType", tokenType.name());

        if (bookingToken != null){
            builder.withClaim("bookingToken", bookingToken);
        }
        
        return builder.sign(algorithm);

    }

    // token decode + verify

    public DecodedJWT decodeAndVerify(String token) {
        return verifier.verify(token);
    }

    // getter
    public TokenType getTokenType(String token) {
        return TokenType.valueOf(
                decodeAndVerify(token)
                        .getClaim("tokenType")
                        .asString());
    }

    public Instant getExpiresAt(String token) {
        return decodeAndVerify(token).getExpiresAtAsInstant();
    }

    // conversion to accesstokendto

    public UserAccessTokenDto toUserAccessTokenDto(String token) {

        DecodedJWT decodedJWT = decodeAndVerify(token);

        Instant expiresAt = decodedJWT.getExpiresAtAsInstant();

        TokenType tokenType = TokenType.valueOf(decodedJWT.getClaim("tokenType").asString());

        return new UserAccessTokenDto(token, expiresAt, tokenType);
    }

    public GuestAccessTokenDto toGuestAccessTokenDto(String guestToken) {

        DecodedJWT decodedJWT = decodeAndVerify(guestToken);

        Instant expiresAt = decodedJWT.getExpiresAtAsInstant();

        TokenType tokenType = TokenType.valueOf(decodedJWT.getClaim("tokenType").asString());

        return new GuestAccessTokenDto(guestToken, expiresAt, tokenType);
    }

}
