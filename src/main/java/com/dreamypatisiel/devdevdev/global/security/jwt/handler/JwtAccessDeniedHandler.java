package com.dreamypatisiel.devdevdev.global.security.jwt.handler;

import com.dreamypatisiel.devdevdev.exception.JwtAccessDeniedException;
import com.dreamypatisiel.devdevdev.exception.JwtAuthenticationException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

/**
 * 인증, 인가 단계에서 발생한 에러를 처리하는 클래스
 * 권한이 부여되지 않은 상태에서 접근 시 403 FOBIDDEN 응답
 */
@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException {
        throw new JwtAccessDeniedException("해당 자원에 접근 권한이 없습니다.");
    }
}