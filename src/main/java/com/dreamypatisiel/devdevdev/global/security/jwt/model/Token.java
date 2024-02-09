package com.dreamypatisiel.devdevdev.global.security.jwt.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
public class Token {
    public static final String DISABLED = "DISABLED";

    private String accessToken;
    private String refreshToken;
}
