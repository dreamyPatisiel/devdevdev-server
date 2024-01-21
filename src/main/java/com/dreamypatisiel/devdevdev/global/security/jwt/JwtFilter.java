package com.dreamypatisiel.devdevdev.global.security.jwt;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * (상시)
 * API 요청 토큰을 검증하는 필터
 * JWT의 인증 정보를 검사해 현재 쓰레드의 SecurityContext에 저장하는 역할 수행
 */
@Slf4j
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final TokenService tokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        log.info("JwtFilter 시작");

        String token = tokenService.resolveToken(request);
        log.info("accessToken={}", token);

        if (tokenService.validateToken(token)) { // JWT 토큰이 유효한 경우에만, USER객체 셋팅
            Authentication authentication = tokenService.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.info("jwt success");
        }

        filterChain.doFilter(request, response); // 다음 Filter 실행
        log.info("JwtFilter 종료");
    }

}