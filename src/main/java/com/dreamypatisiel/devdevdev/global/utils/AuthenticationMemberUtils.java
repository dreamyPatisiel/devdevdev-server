package com.dreamypatisiel.devdevdev.global.utils;

import com.dreamypatisiel.devdevdev.global.security.oauth2.model.UserPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

public class AuthenticationMemberUtils {

    public static final String ANONYMOUS_USER = "anonymousUser";

    public static UserPrincipal getUserPrincipal() {
        return (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public static boolean isAnonymous() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return userDetails.getUsername().equals(ANONYMOUS_USER);
    }

    public static boolean isAnonymous(UserDetails userDetails) {
        return userDetails.getUsername().equals(ANONYMOUS_USER);
    }
}
