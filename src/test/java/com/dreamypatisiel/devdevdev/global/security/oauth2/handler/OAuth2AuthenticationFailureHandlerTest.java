package com.dreamypatisiel.devdevdev.global.security.oauth2.handler;

import static org.junit.jupiter.api.Assertions.*;

import com.dreamypatisiel.devdevdev.exception.OAuth2LoginException;
import com.dreamypatisiel.devdevdev.global.security.jwt.model.JwtCookieConstant;
import com.dreamypatisiel.devdevdev.global.utils.CookieUtils;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class OAuth2AuthenticationFailureHandlerTest {

    @Autowired
    OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;

    @Test
    @DisplayName("OAuth2.0 로그인에 실패하면 예외가 발생하고 응답 쿠키에 로그인 상태를 비활성화로 설정한다.")
    void onAuthenticationFailure() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        AuthenticationException exception = new BadCredentialsException("예외 발생");

        // when // then
        assertThatThrownBy(() -> oAuth2AuthenticationFailureHandler.onAuthenticationFailure(request, response, exception))
                .isInstanceOf(OAuth2LoginException.class)
                .hasMessage(OAuth2AuthenticationFailureHandler.INVALID_OAUTH2_AUTHENTICATION_FAIL_MESSAGE);

        Cookie cookie = response.getCookie(JwtCookieConstant.DEVDEVDEV_LOGIN_STATUS);
        assertThat(cookie).isNotNull();
        assertAll(
                () -> assertThat(cookie.getValue()).isEqualTo(CookieUtils.INACTIVE),
                () -> assertThat(cookie.getMaxAge()).isEqualTo(CookieUtils.DEFAULT_MAX_AGE),
                () -> assertThat(cookie.getSecure()).isFalse(),
                () -> assertThat(cookie.isHttpOnly()).isFalse()
        );
    }
}