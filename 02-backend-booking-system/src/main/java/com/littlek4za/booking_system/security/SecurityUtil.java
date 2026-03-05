package com.littlek4za.booking_system.security;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.littlek4za.booking_system.exception.AppException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class SecurityUtil {

    public AuthUserPrincipal getCurrentAuthUser() {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthUserPrincipal principal)) {
            throw new AppException("Unauthenticated", HttpStatus.UNAUTHORIZED);
        }

        return principal;
    }

    public Long getCurrentAuthUserId() {

        return getCurrentAuthUser().getId();
    }

    public Long getCurrentAuthUserIdOrNull() {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();
        // if (authentication == null ||
        // !authentication.isAuthenticated() ||
        // authentication instanceof AnonymousAuthenticationToken ||
        // !(authentication.getPrincipal() instanceof AuthUserPrincipal principal)) {
        // return null;
        // }
        log.debug("Authentication object: " + authentication);
        if (authentication == null) {
            log.debug("Authentication is NULL");
            return null;
        }

        log.debug("Authentication class: " + authentication.getClass().getName());
        log.debug("Principal: " + authentication.getPrincipal());
        log.debug("isAuthenticated(): " + authentication.isAuthenticated());

        if (authentication instanceof AnonymousAuthenticationToken) {
            log.debug(">>> This is AnonymousAuthenticationToken");
            return null;
        }

        if (!(authentication.getPrincipal() instanceof AuthUserPrincipal principal)) {
            log.debug("Principal is not AuthUserPrincipal");
            return null;
        }

        return principal.getId();
    }

}
