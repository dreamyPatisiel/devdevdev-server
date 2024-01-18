package com.dreamypatisiel.devdevdev.global.config.jwt;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
public class JwtFilter extends OncePerRequestFilter {

    private TokenService tokenService;

    public JwtFilter(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    //jwt 토큰의 인증정보를 SecurityContext에 저장하는 역할 수행
    @Override
    protected void doFilterInternal(HttpServletRequest servletRequest,
                                    HttpServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException, ServletException, IOException {
        HttpServletRequest httpServletRequest = servletRequest;
        String token = httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION);

        if (tokenService.validateToken(token)) { // JWT 토큰이 유효한 경우에만, USER객체 셋팅
            Authentication authentication = tokenService.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.info("jwt success");
        }
        filterChain.doFilter(servletRequest,
                servletResponse); // 다음 Filter를 실행(마지막 필터라면 필터 실행 후 리소스를 반환)
    }
}