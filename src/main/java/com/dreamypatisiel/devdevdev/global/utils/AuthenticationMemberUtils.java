package com.dreamypatisiel.devdevdev.global.utils;

import com.dreamypatisiel.devdevdev.global.security.oauth2.model.UserPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuthenticationMemberUtils {
    public UserPrincipal getUserPrincipal() {
        return (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
