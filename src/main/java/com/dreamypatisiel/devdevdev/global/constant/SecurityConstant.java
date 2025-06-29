package com.dreamypatisiel.devdevdev.global.constant;

public class SecurityConstant {
    public static final String SPRING_H2_CONSOLE_ENABLED = "spring.h2.console.enabled";
    public static final String WILDCARD_PATTERN = "/**";
    public static final long PREFLIGHT_MAX_AGE = 3600L;
    public static final String OAUTH2_LOGIN_URL_PREFIX = "/devdevdev/api/v1/oauth2/authorization/**";
    public static final String OAUTH2_REDIRECT_URL_PREFIX = "/devdevdev/api/v1/login/oauth2/code/**";
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String[] DEV_WHITELIST_URL = new String[]{
            "/",
            "/docs/index.html",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/error",
            "/favicon.ico",
            "/**.png",
            "/**.gif",
            "/**.svg",
            "/**.jpg",
            "/**.html",
            "/**.css",
            "/**.js",
            "/devdevdev/api/v1/oauth2/authorization/**",
            "/devdevdev/api/v1/oauth2/authorization/kakao",
            "/devdevdev/api/v1/login/oauth2/code/**",
            "/devdevdev/api/v1/login/oauth2/code/kakao",
            "/h2-console/**",
            "/h2-console",
            "/devdevdev/api/v1/test/**",
            "/devdevdev/api/v1/token/**",
            "/devdevdev/api/v1/picks/**",
            "/devdevdev/api/v1/articles/**",
            "/devdevdev/api/v1/keywords/**",
            "/devdevdev/api/v1/subscriptions/**",
            "/devdevdev/api/v1/notifications/**",
            "/devdevdev/api/v1/nickname/**"
    };

    public static final String[] DEV_JWT_FILTER_WHITELIST_URL = new String[]{
            "/docs/index.html",
            "/swagger-ui",
            "/v3/api-docs",
            "/h2-console",
            "/error",
            "/favicon.ico",
            "/devdevdev/api/v1/oauth2/authorization",
            "/devdevdev/api/v1/login/oauth2/code",
            "/devdevdev/api/v1/test",
            "/devdevdev/api/v1/token",
            "/devdevdev/api/v1/notifications/SUBSCRIPTION"
    };

    public static final String[] PROD_WHITELIST_URL = new String[]{
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
            "/devdevdev/api/v1/oauth2/authorization/**",
            "/devdevdev/api/v1/oauth2/authorization/kakao",
            "/devdevdev/api/v1/login/oauth2/code/**",
            "/devdevdev/api/v1/login/oauth2/code/kakao",
            "/devdevdev/api/v1/token/**",
            "/devdevdev/api/v1/picks/**",
            "/devdevdev/api/v1/articles/**",
            "/devdevdev/api/v1/keywords/**",
            "/devdevdev/api/v1/subscriptions/**",
            "/devdevdev/api/v1/notifications/**",
            "/devdevdev/api/v1/nickname/**"
    };

    public static final String[] PROD_JWT_FILTER_WHITELIST_URL = new String[]{
            "/error",
            "/favicon.ico",
            "/devdevdev/api/v1/oauth2/authorization",
            "/devdevdev/api/v1/login/oauth2/code",
            "/devdevdev/api/v1/token"
    };
}
