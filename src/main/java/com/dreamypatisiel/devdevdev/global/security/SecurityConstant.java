package com.dreamypatisiel.devdevdev.global.security;

public class SecurityConstant {
    public static final String SPRING_H2_CONSOLE_ENABLED = "spring.h2.console.enabled";
    public static final String WILDCARD_PATTERN = "/**";
    public static final long PREFLIGHT_MAX_AGE = 3600L;
    public static final String[] GET_WHITELIST = new String[]{
            "/",
            "/login**",
            "/auth**",
            "/oauth2/authorize",
            "/error",
            "/favicon.ico",
            "/**/*.png",
            "/**/*.gif",
            "/**/*.svg",
            "/**/*.jpg",
            "/**/*.html",
            "/**/*.css",
            "/**/*.js"
    };

    public static final String[] POST_WHITELIST = new String[]{
    };
}
