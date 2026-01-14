package com.littlek4za.booking_system.security;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.littlek4za.booking_system.exception.AppException;

@Component
public class SecurityUtil {

    public AuthUserPrincipal getCurrentAuthUser() {
        Authentication authentication = SecurityContextHolder
                                            .getContext()
                                            .getAuthentication();
        if(authentication == null || !(authentication.getPrincipal() instanceof AuthUserPrincipal principal)) {
            throw new AppException("Unauthenticated", HttpStatus.UNAUTHORIZED);
        }

        return principal;
    }

    public Long getCurrentAuthUserId(){
        return getCurrentAuthUser().getId();
    }

}
