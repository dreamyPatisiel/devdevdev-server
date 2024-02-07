package com.dreamypatisiel.devdevdev.global.security.oauth2.handler;

import static com.dreamypatisiel.devdevdev.global.utils.CookieUtils.INACTIVE;

import ch.qos.logback.core.spi.ErrorCodes;
import com.dreamypatisiel.devdevdev.exception.OAuth2LoginException;
import com.dreamypatisiel.devdevdev.global.security.jwt.model.JwtCookieConstant;
import com.dreamypatisiel.devdevdev.global.utils.CookieUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

/**
 * OAuth 2.0 실패 핸들러
 */
@Slf4j
@Component
public class OAuth2AuthenticationFailureHandler implements AuthenticationFailureHandler {

    public static final String INVALID_OAUTH2_AUTHENTICATION_FAIL_MESSAGE = "소셜 로그인에 실패하였습니다. 관리자에게 문의 주세요.";

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) {
        // 응답 쿠키 설정
        CookieUtils.addCookieToResponse(response, JwtCookieConstant.DEVDEVDEV_LOGIN_STATUS,
                INACTIVE, CookieUtils.DEFAULT_MAX_AGE, false, false);
        throw new OAuth2LoginException(INVALID_OAUTH2_AUTHENTICATION_FAIL_MESSAGE);
    }
}
