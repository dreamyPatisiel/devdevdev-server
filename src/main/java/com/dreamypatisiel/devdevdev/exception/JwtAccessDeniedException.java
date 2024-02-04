package com.dreamypatisiel.devdevdev.exception;

import io.jsonwebtoken.JwtException;

public class JwtAccessDeniedException extends JwtException {

    public JwtAccessDeniedException(String message) {
        super(message);
    }

    public JwtAccessDeniedException(String message, Throwable cause) {
        super(message, cause);
    }
}
