package com.littlek4za.booking_system.security;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.littlek4za.booking_system.dtos.GuestAccessTokenDto;
import com.littlek4za.booking_system.dtos.UserAccessTokenDto;
import com.littlek4za.booking_system.dtos.UserDto;
import com.littlek4za.booking_system.entities.User;
import com.littlek4za.booking_system.exception.AppException;
import com.littlek4za.booking_system.exception.filter.JwtAuthFilterException;
import com.littlek4za.booking_system.exception.model.ErrorCode;
import com.littlek4za.booking_system.models.TokenType;
import com.littlek4za.booking_system.repos.UserRepository;

import jakarta.annotation.PostConstruct;

@Component
public class UserAuthProvider {

    private final UserRepository userRepository;

    @Value("${security.jwt.token.secret-key:dev-secret-key}")
    private String secretKey;
    @Value("${security.jwt.issuer:booking-system}")
    private String issuerString;

    public UserAuthProvider(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostConstruct // run after bean is created
    protected void init() {
        secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
    }

    public String createToken(UserDto userDto) {
        Instant now = Instant.now();
        Instant expiry = now.plus(10, ChronoUnit.HOURS);

        return JWT.create()
                .withIssuer(issuerString)
                .withSubject(userDto.username())
                .withIssuedAt(now)
                .withExpiresAt(expiry)
                .withClaim("firstName", userDto.firstName())
                .withClaim("lastName", userDto.lastName())
                .withClaim("email", userDto.email())
                .withClaim("roles", new ArrayList<>(userDto.roleSet()))
                .withClaim("tokenType", TokenType.USER.name())
                .sign(Algorithm.HMAC256(secretKey));

    }

    public Authentication validateTokenStrongly(String token) {
        Algorithm algorithm = Algorithm.HMAC256(secretKey);
        JWTVerifier verifier = JWT.require(algorithm)
                .withIssuer(issuerString)
                .build();
        DecodedJWT decodedJWT = verifier.verify(token);

        // double check with db, and use db user to create loginResponseDto
        User user = userRepository.findByUsername(decodedJWT.getSubject())
                .orElseThrow(() -> new JwtAuthFilterException("Unknown User", HttpStatus.NOT_FOUND));

        AuthUserPrincipal userAuthPrincipal = new AuthUserPrincipal(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRoleSet()
                        .stream()
                        .map(role -> role.getRoleName())
                        .collect(Collectors.toSet()));

        Set<GrantedAuthority> authoritySet = user.getRoleSet()
                .stream()
                .map(role -> new SimpleGrantedAuthority(role.getRoleName().name()))
                .collect(Collectors.toSet());

        return new UsernamePasswordAuthenticationToken(userAuthPrincipal, null, authoritySet);
    }

    public UserAccessTokenDto toUserAccessTokenDto(String token) {
        Algorithm algorithm = Algorithm.HMAC256(secretKey);
        JWTVerifier verifier = JWT.require(algorithm)
                .withIssuer(issuerString)
                .build();
        DecodedJWT decodedJWT = verifier.verify(token);

        Instant expiresAt = decodedJWT.getExpiresAt().toInstant();

        String tokenTypeStr = decodedJWT.getClaim("tokenType").asString();
        TokenType tokenType;
        try {
            tokenType = TokenType.valueOf(tokenTypeStr);
        } catch (Exception e) {
            throw new AppException("Invalid tokenType: ", HttpStatus.UNAUTHORIZED,
                    ErrorCode.TOKEN_TYPE_INVALID);
        }

        return new UserAccessTokenDto(token, expiresAt, tokenType);
    }

    public GuestAccessTokenDto toGuestAccessTokenDto(String guestToken) {
        Algorithm algorithm = Algorithm.HMAC256(secretKey);
        JWTVerifier verifier = JWT.require(algorithm)
                .withIssuer(issuerString)
                .build();
        DecodedJWT decodedJWT = verifier.verify(guestToken);

        Instant expiresAt = decodedJWT.getExpiresAt().toInstant();

        String tokenTypeStr = decodedJWT.getClaim("tokenType").asString();
        TokenType tokenType;
        try {
            tokenType = TokenType.valueOf(tokenTypeStr);
        } catch (Exception e) {
            throw new AppException("Invalid tokenType: ", HttpStatus.UNAUTHORIZED,
                    ErrorCode.TOKEN_TYPE_INVALID);
        }

        return new GuestAccessTokenDto(guestToken,
                expiresAt,
                tokenType);
    }

}
