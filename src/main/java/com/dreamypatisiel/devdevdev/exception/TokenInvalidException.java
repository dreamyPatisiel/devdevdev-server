package com.dreamypatisiel.devdevdev.exception;

public class TokenInvalidException extends IllegalArgumentException {
    public static final String REFRESH_TOKEN_INVALID_EXCEPTION_MESSAGE = "유효하지 않은 리프레시 토큰 입니다.";

    public TokenInvalidException(String s) {
        super(s);
    }
}
