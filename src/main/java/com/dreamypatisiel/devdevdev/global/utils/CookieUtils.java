package com.dreamypatisiel.devdevdev.global.utils;

import com.dreamypatisiel.devdevdev.exception.CookieException;
import com.dreamypatisiel.devdevdev.global.security.jwt.model.JwtCookieConstant;
import com.dreamypatisiel.devdevdev.global.security.jwt.model.Token;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.ObjectUtils;
import org.springframework.util.SerializationUtils;

import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;

public class CookieUtils {

    public static final int DEFAULT_MAX_AGE = 180;
    public static final int DEFAULT_MIN_AGE = 0;
    public static final String DEFAULT_PATH = "/";
    public static final String BLANK = "";
    public static final String INVALID_NOT_FOUND_COOKIE_MESSAGE = "쿠키가 존재하지 않습니다.";


    public static Optional<Cookie> getRequestCookieByName(HttpServletRequest request, String name) {
        validationCookieEmpty(request.getCookies());
        return Arrays.stream(request.getCookies())
                .filter(cookie -> cookie.getName().equals(name))
                .findFirst();
    }

    public static Optional<String> getRequestCookieValueByName(HttpServletRequest request, String name) {
        validationCookieEmpty(request.getCookies());
        return Arrays.stream(request.getCookies())
                .filter(cookie -> name.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }

    public static void addCookieToResponse(HttpServletResponse response, String name, String value, int maxAge, boolean isHttpOnly, boolean isSecure) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath(DEFAULT_PATH);
        cookie.setHttpOnly(isHttpOnly);
        cookie.setSecure(isSecure);
        cookie.setMaxAge(maxAge);

        response.addCookie(cookie);
    }

    // 쿠키를 삭제하려면 클라이언트에게 해당 쿠키가 더 이상 유효하지 않음을 알려야 합니다.
    public static void deleteCookieFromResponse(HttpServletRequest request, HttpServletResponse response, String name) {
        validationCookieEmpty(request.getCookies());
        Arrays.stream(request.getCookies())
                .filter(cookie -> cookie.getName().equals(name))
                .forEach(cookie -> {
                    cookie.setValue(BLANK);
                    cookie.setPath(DEFAULT_PATH);
                    cookie.setMaxAge(DEFAULT_MIN_AGE);
                    response.addCookie(cookie);
                });
    }

    public static String serialize(Object object) {
        return Base64.getUrlEncoder()
                .encodeToString(SerializationUtils.serialize(object));
    }

    public static <T> T deserialize(Cookie cookie, Class<T> clazz) {
        byte[] decode = Base64.getUrlDecoder().decode(cookie.getValue());
        return clazz.cast(SerializationUtils.deserialize(decode));
    }

    public static void configJwtCookie(HttpServletResponse response, Token token) {
        CookieUtils.addCookieToResponse(response, JwtCookieConstant.DEVDEVDEV_ACCESS_TOKEN,
                token.getAccessToken(), CookieUtils.DEFAULT_MAX_AGE, false, false);
        CookieUtils.addCookieToResponse(response, JwtCookieConstant.DEVDEVDEV_REFRESH_TOKEN,
                token.getRefreshToken(), CookieUtils.DEFAULT_MAX_AGE, true, true);
    }

    private static void validationCookieEmpty(Cookie[] cookies) {
        if(ObjectUtils.isEmpty(cookies)) {
            throw new CookieException(INVALID_NOT_FOUND_COOKIE_MESSAGE);
        }
    }
}