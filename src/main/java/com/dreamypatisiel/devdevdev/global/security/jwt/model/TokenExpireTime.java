package com.dreamypatisiel.devdevdev.global.security.jwt.model;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TokenExpireTime {
    public static long ACCESS_TOKEN_EXPIRE_TIME;
    public static long REFRESH_TOKEN_EXPIRE_TIME;

    public TokenExpireTime(
            @Value("${token.expire.access}") long accessTokenExpireTime,
            @Value("${token.expire.refresh}") long refreshTokenExpireTime
    ) {
        ACCESS_TOKEN_EXPIRE_TIME = accessTokenExpireTime;
        REFRESH_TOKEN_EXPIRE_TIME = refreshTokenExpireTime;
    }
}
