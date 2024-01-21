package com.dreamypatisiel.devdevdev.global.security;

public class SecurityConstant {
    public static final String SPRING_H2_CONSOLE_ENABLED = "spring.h2.console.enabled";
    public static final String WILDCARD_PATTERN = "/**";
    public static final long PREFLIGHT_MAX_AGE = 3600L;
    public static final String OAUTH2_LOGIN_URL_PREFIX = "/devdevdev/api/v1/oauth2/authorization/**";
    public static final String OAUTH2_REDIRECT_URL_PREFIX = "/devdevdev/api/v1/login/oauth2/code/**";
    public static final String[] WHITELIST_URL = new String[] {
            "/",
            "/error",
            "/favicon.ico",
            "/**.png",
            "/**.gif",
            "/**.svg",
            "/**.jpg",
            "/**.html",
            "/**.css",
            "/**.js",
            "/devdevdev/api/v1/logout",
            "/devdevdev/api/v1/oauth2/authorization/**",
            "/devdevdev/api/v1/oauth2/authorization/kakao",
            "/devdevdev/api/v1/login/oauth2/code/**",
            "/devdevdev/api/v1/login/oauth2/code/kakao",
            "/devdevdev/api/v1/members"
    };
    public static final String[] GET_WHITELIST = new String[]{
            "/oauth2/authorization/**",
            "/login/oauth2/code/**",
            "/devdevdev/api/v1/members"
    };

    public static final String[] POST_WHITELIST = new String[]{
    };
}
