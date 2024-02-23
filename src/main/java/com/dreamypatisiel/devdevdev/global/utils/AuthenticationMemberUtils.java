package com.dreamypatisiel.devdevdev.global.utils;

import com.dreamypatisiel.devdevdev.global.security.oauth2.model.UserPrincipal;
import java.security.Principal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

public class AuthenticationMemberUtils {

    public static final String ANONYMOUS_USER = "anonymousUser";

    public static UserPrincipal getUserPrincipal() {
        return (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    public static boolean isAnonymous() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return principal instanceof String && principal.equals(ANONYMOUS_USER);
    }

    public static boolean isAnonymous(Authentication authentication) {
        return authentication.getPrincipal().equals(ANONYMOUS_USER);
    }
}
