package com.littlek4za.booking_system.config;


import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;
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
import com.littlek4za.booking_system.dao.UserRepository;
import com.littlek4za.booking_system.dto.LoginResponseDto;
import com.littlek4za.booking_system.entities.User;
import com.littlek4za.booking_system.exception.JwtAuthException;
import com.littlek4za.booking_system.mapper.DtoMapper;

import jakarta.annotation.PostConstruct;

@Component
public class UserAuthProvider {

    private final UserRepository userRepository;
    private final DtoMapper dtoMapper;

    @Value("${security.jwt.token.secret-key:dev-secret-key}")
    private String secretKey;
    private String issuerString = "booking-system";

    public UserAuthProvider(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.dtoMapper = null;
    }

    @PostConstruct // run after bean is created
    protected void init() {
        secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
    }

    public String createToken(LoginResponseDto loginResponseDto){
        Instant now = Instant.now();
        Instant expiry = now.plus(1, ChronoUnit.HOURS);

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

    public Authentication validateToken(String token) { // use for Http reuqest GET, speedy
        Algorithm algorithm = Algorithm.HMAC256(secretKey);
        JWTVerifier verifier = JWT.require(algorithm).withIssuer(issuerString).build();
        DecodedJWT decodedJWT = verifier.verify(token);

        List<String> rolesFromToken = decodedJWT.getClaim("roleSet").asList(String.class);
        Set<String>roleSet = new HashSet<>(rolesFromToken);

        // directly assign decodedJWT as LoginResponseDto wihtout touching the db
        LoginResponseDto loginResponseDto = LoginResponseDto.builder()
                                    .email(decodedJWT.getClaim("email").asString())
                                    .firstName(decodedJWT.getClaim("firstName").asString())
                                    .lastName(decodedJWT.getClaim("lastName").asString())
                                    .roleSet(roleSet)
                                    .username(decodedJWT.getSubject())
                                    .build();
        
        Set<GrantedAuthority> authoritySet = loginResponseDto.getRoleSet()
                                                        .stream()
                                                        .map(role -> new SimpleGrantedAuthority(role))
                                                        .collect(Collectors.toSet());
        
        return new UsernamePasswordAuthenticationToken(loginResponseDto, null, authoritySet);
        
    }

    public @Nullable Authentication validateTokenStrongly(String token) { // use for Http request other than GET, slower but secure
        Algorithm algorithm = Algorithm.HMAC256(secretKey);
        JWTVerifier verifier = JWT.require(algorithm).withIssuer(issuerString).build();
        DecodedJWT decodedJWT = verifier.verify(token);

        // double check with db, and use db user to create loginResponseDto
        User user = userRepository.findByUsername(decodedJWT.getSubject())
                                    .orElseThrow(()-> new JwtAuthException("Unknown User", HttpStatus.UNAUTHORIZED));
        
        LoginResponseDto loginResponseDto = dtoMapper.userToLoginResponseDto(user);
        
        Set<GrantedAuthority> authoritySet = user.getRoleSet()
                                                        .stream()
                                                        .map(role -> new SimpleGrantedAuthority(role.getRoleName()))
                                                        .collect(Collectors.toSet());
        
        return new UsernamePasswordAuthenticationToken(loginResponseDto, null, authoritySet);
    }

}
