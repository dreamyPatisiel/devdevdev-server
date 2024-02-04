package com.dreamypatisiel.devdevdev.global.security.oauth2.handler;

import com.dreamypatisiel.devdevdev.exception.OAuth2LoginException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
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
                                        AuthenticationException exception) throws IOException, ServletException {
        throw new OAuth2LoginException(INVALID_OAUTH2_AUTHENTICATION_FAIL_MESSAGE);
    }
}
