package com.dreamypatisiel.devdevdev.exception;

public class CookieException extends IllegalArgumentException {
    public CookieException(String s) {
        super(s);
    }

    public CookieException(String message, Throwable cause) {
        super(message, cause);
    }
}
