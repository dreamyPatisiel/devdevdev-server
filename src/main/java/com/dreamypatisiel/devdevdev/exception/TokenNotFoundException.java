package com.dreamypatisiel.devdevdev.exception;

public class TokenNotFoundException extends IllegalArgumentException {
    public static final String TOKEN_NOT_FOUND_EXCEPTION_MESSAGE = "토큰이 존재하지 않습니다.";

    public TokenNotFoundException(String s) {
        super(s);
    }
}
