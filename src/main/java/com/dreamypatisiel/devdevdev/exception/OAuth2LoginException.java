package com.dreamypatisiel.devdevdev.exception;

import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;

public class OAuth2LoginException extends IllegalArgumentException {
    public OAuth2LoginException(String message) {
        super(message);
    }
}
