package com.littlek4za.booking_system.security;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.littlek4za.booking_system.entities.User;
import com.littlek4za.booking_system.exception.filter.JwtAuthFilterException;
import com.littlek4za.booking_system.exception.model.ErrorCode;
import com.littlek4za.booking_system.models.RoleType;
import com.littlek4za.booking_system.models.TokenType;
import com.littlek4za.booking_system.repos.UserRepository;

// build authentication
@Component
public class JwtAuthenticationProvider {

        private final UserRepository userRepository;
        private final JwtTokenService jwtTokenService;

        public JwtAuthenticationProvider(UserRepository userRepository, JwtTokenService jwtTokenService) {
                this.userRepository = userRepository;
                this.jwtTokenService = jwtTokenService;
        }

        public Authentication authenticate(String token) {

                DecodedJWT decodedJWT = jwtTokenService.decodeAndVerify(token);

                TokenType tokenType = TokenType.valueOf(
                                decodedJWT.getClaim("tokenType").asString());

                // double check with db, and use db user to create loginResponseDto
                if (tokenType.isUserToken()) {
                        return buildUserAuthentication(decodedJWT);
                } else if (tokenType.isGuestToken()) {
                        return buildGuestAuthentication(decodedJWT);
                } else {
                        throw new JwtAuthFilterException("Invalid token type", HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED);
                }

        }

        private Authentication buildUserAuthentication(DecodedJWT decodedJWT) {
                User user = userRepository.findByUsername(decodedJWT.getSubject())
                                .orElseThrow(() -> new JwtAuthFilterException("Bad credentials", HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED));

                UserPrincipal userPrincipal = new UserPrincipal(
                                user.getId(),
                                user.getUsername(),
                                user.getEmail(),
                                user.getRoleSet()
                                                .stream()
                                                .map(role -> role.getRoleName())
                                                .collect(Collectors.toSet()),
                                TokenType.USER);

                Set<GrantedAuthority> authoritySet = user.getRoleSet()
                                .stream()
                                .map(role -> new SimpleGrantedAuthority(role.getRoleName().name()))
                                .collect(Collectors.toSet());

                return new UsernamePasswordAuthenticationToken(userPrincipal, null, authoritySet);
        }

        private Authentication buildGuestAuthentication(DecodedJWT decodedJWT) {
                String email = decodedJWT.getClaim("email").asString();

                TokenType tokenType = TokenType.valueOf(
                                decodedJWT.getClaim("tokenType").asString());

                String bookingToken = decodedJWT.getClaim("bookingToken").asString();

                GuestPrincipal guestPricipal = new GuestPrincipal(
                                email,
                                Set.of(RoleType.ROLE_ATTENDEE),
                                tokenType,
                                bookingToken);

                Set<GrantedAuthority> authoritySet = Set.of(
                                new SimpleGrantedAuthority(RoleType.ROLE_ATTENDEE.name()));

                return new UsernamePasswordAuthenticationToken(guestPricipal, null, authoritySet);

        }

}
