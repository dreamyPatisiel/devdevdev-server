package com.dreamypatisiel.devdevdev.global.security.jwt.model;

public class TokenExpireTime {
    public static final long ACCESS_TOKEN_EXPIRE_TIME = 1_000 * 60 * 30; // 30분
    public static final long REFRESH_TOKEN_EXPIRE_TIME = 1_000 * 60 * 60 * 24 * 7; // 7일
}
