package com.dreamypatisiel.devdevdev.global.utils;

import com.dreamypatisiel.devdevdev.exception.UserPrincipalException;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuthenticationMemberUtils {

    public static final String ANONYMOUS_USER = "anonymousUser";
    public static final String INVALID_TYPE_CAST_USER_PRINCIPAL_MESSAGE = "인증객체 타입에 문제가 발생했습니다.";

    public static UserPrincipal getUserPrincipal() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if(!isUserPrincipalClass(principal)) {
            throw new UserPrincipalException(INVALID_TYPE_CAST_USER_PRINCIPAL_MESSAGE);
        }

        return (UserPrincipal) principal;
    }

    private static boolean isUserPrincipalClass(Object principal) {
        return principal instanceof UserPrincipal;
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
