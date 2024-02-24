package com.dreamypatisiel.devdevdev.global.security.jwt.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class Token {
    public static final String DISABLED = "DISABLED";

    private final String accessToken;
    private final String refreshToken;
}
