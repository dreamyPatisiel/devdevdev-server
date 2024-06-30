package com.dreamypatisiel.devdevdev.global.security.jwt.handler;

import com.dreamypatisiel.devdevdev.exception.JwtAuthenticationException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

/**
 * 인증이 안된 익명의 사용자가 인증이 필요한 엔드포인트로 접근하게 된다면
 * Spring Security의 기본 설정으로는 HttpStatus 401과 함께 스프링의 기본 오류페이지를 보여준다.
 * 정상적인 JWT가 오지 않은 경우에 대해 필터링하는 클래스
 * 비정상적인 JWT를 가지고 접근 시 401 UNAUTHORIZED 응답
 */
@Slf4j
@Component
public class JwtAuthenticationEntryPointHandler implements AuthenticationEntryPoint {

    public static final String INVALID_MEMBER_MESSAGE = "유효하지 않은 회원 입니다.";

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) {
        throw new JwtAuthenticationException(INVALID_MEMBER_MESSAGE);
    }
}
