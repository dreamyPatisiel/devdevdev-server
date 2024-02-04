package com.dreamypatisiel.devdevdev.global.utils;

import static org.junit.jupiter.api.Assertions.*;

import com.dreamypatisiel.devdevdev.exception.CookieException;
import com.dreamypatisiel.devdevdev.global.security.jwt.model.JwtCookieConstant;
import com.dreamypatisiel.devdevdev.global.security.jwt.model.Token;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.*;

class CookieUtilsTest {

    @Test
    @DisplayName("요청에 있는 쿠키를 추출할 때 쿠키가 없으면 예외가 발생한다.")
    void getRequestCookieByNameException_INVALID_NOT_FOUND_COOKIE_MESSAGE() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        String name = "ChocolateChipCookie";

        // when // then
        assertThatThrownBy(() -> CookieUtils.getRequestCookieByName(request, name))
                .isInstanceOf(CookieException.class)
                .hasMessage(CookieUtils.INVALID_NOT_FOUND_COOKIE_MESSAGE);
    }

    @Test
    @DisplayName("요청에 있는 쿠키를 추출할 때 알맞은 이름의 쿠키가 없으면 예외가 발생한다.")
    void getRequestCookieByNameException_INVALID_NOT_FOUND_COOKIE_BY_NAME_MESSAGE() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        String name = "ChocolateChipCookie";
        String value = "yummy";
        Cookie cookie = new Cookie(name, value);
        request.setCookies(cookie);

        String otherName = "ButterChipCookie";

        // when // then
        assertThatThrownBy(() -> CookieUtils.getRequestCookieByName(request, otherName))
                .isInstanceOf(CookieException.class)
                .hasMessage(CookieUtils.INVALID_NOT_FOUND_COOKIE_BY_NAME_MESSAGE);
    }

    @Test
    @DisplayName("요청에 있는 쿠키를 쿠키 이름으로 추출한다.")
    void getRequestCookieByName() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();

        String name = "ChocolateChipCookie";
        String value = "yummy";
        Cookie cookie = new Cookie(name, value);
        request.setCookies(cookie);

        // when
        Cookie result = CookieUtils.getRequestCookieByName(request, name);

        // then
        assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.getName()).isEqualTo(name),
                () -> assertThat(result.getValue()).isEqualTo(value)
        );
    }

    @Test
    @DisplayName("요청에 있는 쿠키의 값을 추출할 때 쿠키가 없으면 예외가 발생한다.")
    void getRequestCookieValueByNameException() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        String name = "ChocolateChipCookie";

        // when // then
        assertThatThrownBy(() -> CookieUtils.getRequestCookieValueByName(request, name))
                .isInstanceOf(CookieException.class)
                .hasMessage(CookieUtils.INVALID_NOT_FOUND_COOKIE_MESSAGE);
    }

    @Test
    @DisplayName("요청에 있는 쿠키값을 추출할 때 알맞은 이름의 쿠키가 없으면 예외가 발생한다.")
    void getRequestCookieByNameException_INVALID_NOT_FOUND_COOKIE_VALUE_BY_NAME_MESSAGE() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        String name = "ChocolateChipCookie";
        String value = "yummy";
        Cookie cookie = new Cookie(name, value);
        request.setCookies(cookie);

        String otherName = "ButterChipCookie";

        // when // then
        assertThatThrownBy(() -> CookieUtils.getRequestCookieValueByName(request, otherName))
                .isInstanceOf(CookieException.class)
                .hasMessage(CookieUtils.INVALID_NOT_FOUND_COOKIE_VALUE_BY_NAME_MESSAGE);
    }

    @Test
    @DisplayName("요청에 있는 쿠키의 값을 쿠키 이름으로 추출한다.")
    void getRequestCookieValueByName() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();

        String name = "ChocolateChipCookie";
        String value = "yummy";
        Cookie cookie = new Cookie(name, value);
        request.setCookies(cookie);

        // when
        String result = CookieUtils.getRequestCookieValueByName(request, name);

        // then
        assertThat(result).isEqualTo(value);
    }


    @Test
    @DisplayName("쿠키를 삭제할 경우"
            + " 요청에 있는 쿠키의 이름과 일치하는 쿠키를 찾아"
            + " 쿠키의 값을 빈값, 경로를 /, 수명을 0으로 설정하여"
            + " 클라이언트에게 응답으로 준다.")
    void deleteCookie() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        String name = "ChocolateChipCookie";
        String value = "yummy";
        Cookie cookie = new Cookie(name, value);
        request.setCookies(cookie);

        // when
        CookieUtils.deleteCookieFromResponse(request, response, name);

        // then
        Cookie responseCookie = response.getCookie(name);
        assertThat(responseCookie).isNotNull();
        assertAll(
                () -> assertThat(responseCookie.getName()).isEqualTo(name),
                () -> assertThat(responseCookie.getValue()).isEqualTo(""),
                () -> assertThat(responseCookie.getPath()).isEqualTo("/"),
                () -> assertThat(responseCookie.getMaxAge()).isEqualTo(0)
        );
    }

    @Test
    @DisplayName("쿠키를 삭제할 경우 요청 값에 쿠키가 없으면 예외가 발생한다.")
    void deleteCookieException() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        String name = "ChocolateChipCookie";

        // when // then
        assertThatThrownBy(() -> CookieUtils.deleteCookieFromResponse(request, response, name))
                .isInstanceOf(CookieException.class)
                .hasMessage(CookieUtils.INVALID_NOT_FOUND_COOKIE_MESSAGE);
    }

    @Test
    @DisplayName("응답에 이름, 값, 수명, httpOnly, secure를 설정하여 쿠키를 추가할 수 있다.")
    void addCookie() {
        // given
        MockHttpServletResponse response = new MockHttpServletResponse();

        String name = "ChocolateChipCookie";
        String value = "yummy";
        int maxAge = 100;
        boolean isHttpOnly = true;
        boolean isSecure = false;

        // when
        CookieUtils.addCookieToResponse(response, name, value, maxAge, isHttpOnly, isSecure);

        // then
        Cookie cookie = response.getCookie(name);
        assertThat(cookie).isNotNull();
        assertAll(
                () -> assertThat(cookie.getName()).isEqualTo(name),
                () -> assertThat(cookie.getValue()).isEqualTo(value),
                () -> assertThat(cookie.getMaxAge()).isEqualTo(maxAge),
                () -> assertThat(cookie.isHttpOnly()).isEqualTo(isHttpOnly),
                () -> assertThat(cookie.getSecure()).isEqualTo(isSecure)
        );
    }

    @Test
    @DisplayName("응답 정보에 JWT 관련 쿠키를 설정할 때 토큰 정보를 저장한다."
            + " (엑세스 토큰은 secure은 false, httpOnly은 false로 저장하고"
            + " 리프레시 토큰은 secure은 true, httpOnly은 true 저장한다.)")
    void configJwtCookie() {
        // given
        MockHttpServletResponse response = new MockHttpServletResponse();
        String accessToken = "accessToken";
        String refreshToken = "refreshToken";
        Token token = new Token(accessToken, refreshToken);

        // when
        CookieUtils.configJwtCookie(response, token);

        // then
        Cookie accessTokenCookie = response.getCookie(JwtCookieConstant.DEVDEVDEV_ACCESS_TOKEN);
        Cookie refreshTokenCookie = response.getCookie(JwtCookieConstant.DEVDEVDEV_REFRESH_TOKEN);

        assertAll(
                () -> assertThat(accessTokenCookie).isNotNull(),
                () -> assertThat(refreshTokenCookie).isNotNull()
        );

        assertAll(
                () -> assertThat(accessTokenCookie.getName()).isEqualTo(JwtCookieConstant.DEVDEVDEV_ACCESS_TOKEN),
                () -> assertThat(accessTokenCookie.getValue()).isEqualTo(accessToken),
                () -> assertThat(accessTokenCookie.getMaxAge()).isEqualTo(CookieUtils.DEFAULT_MAX_AGE),
                () -> assertThat(accessTokenCookie.getSecure()).isFalse(),
                () -> assertThat(accessTokenCookie.isHttpOnly()).isFalse()
        );

        assertAll(
                () -> assertThat(refreshTokenCookie.getName()).isEqualTo(JwtCookieConstant.DEVDEVDEV_REFRESH_TOKEN),
                () -> assertThat(refreshTokenCookie.getValue()).isEqualTo(refreshToken),
                () -> assertThat(refreshTokenCookie.getMaxAge()).isEqualTo(CookieUtils.DEFAULT_MAX_AGE),
                () -> assertThat(refreshTokenCookie.getSecure()).isTrue(),
                () -> assertThat(refreshTokenCookie.isHttpOnly()).isTrue()
        );
    }

}