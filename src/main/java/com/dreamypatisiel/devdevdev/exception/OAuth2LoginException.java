package com.dreamypatisiel.devdevdev.exception;

import org.springframework.security.oauth2.core.OAuth2AuthenticationException;

public class OAuth2LoginException extends OAuth2AuthenticationException {
    public OAuth2LoginException(String message) {
        super(message);
    }
}
