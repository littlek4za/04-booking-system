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
import com.littlek4za.booking_system.dtos.LoginResponseDto;
import com.littlek4za.booking_system.entities.User;
import com.littlek4za.booking_system.exception.filter.JwtAuthFilterException;
import com.littlek4za.booking_system.repos.UserRepository;

import jakarta.annotation.PostConstruct;

@Component
public class UserAuthProvider {

    private final UserRepository userRepository;

    @Value("${security.jwt.token.secret-key:dev-secret-key}")
    private String secretKey;
    private String issuerString = "booking-system";

    public UserAuthProvider(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostConstruct // run after bean is created
    protected void init() {
        secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
    }

    public String createToken(LoginResponseDto loginResponseDto){
        Instant now = Instant.now();
        Instant expiry = now.plus(10, ChronoUnit.HOURS);

        return JWT.create()
                    .withIssuer(issuerString)
                    .withSubject(loginResponseDto.getUsername())
                    .withIssuedAt(now)
                    .withExpiresAt(expiry)
                    .withClaim("firstName", loginResponseDto.getFirstName())
                    .withClaim("lastName", loginResponseDto.getLastName())
                    .withClaim("email",loginResponseDto.getEmail())
                    .withClaim("roles", new ArrayList<>(loginResponseDto.getRoleSet()))
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
                                    .orElseThrow(()-> new JwtAuthFilterException("Unknown User", HttpStatus.NOT_FOUND));

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
                                                        .map(role -> new SimpleGrantedAuthority(role.getRoleName()))
                                                        .collect(Collectors.toSet());
        
        return new UsernamePasswordAuthenticationToken(userAuthPrincipal, null, authoritySet);
    }

}
