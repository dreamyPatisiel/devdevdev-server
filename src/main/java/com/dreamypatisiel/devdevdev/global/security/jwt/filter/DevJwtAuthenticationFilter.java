package com.dreamypatisiel.devdevdev.global.security.jwt.filter;


import static com.dreamypatisiel.devdevdev.limiter.LimiterPlan.GA;

import com.dreamypatisiel.devdevdev.exception.CookieException;
import com.dreamypatisiel.devdevdev.global.constant.SecurityConstant;
import com.dreamypatisiel.devdevdev.global.security.jwt.service.TokenService;
import com.dreamypatisiel.devdevdev.global.utils.CookieUtils;
import io.sentry.Sentry;
import io.sentry.protocol.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * (상시) API 요청 토큰을 검증하는 필터 JWT의 인증 정보를 검사해 현재 쓰레드의 SecurityContext에 저장하는 역할 수행
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DevJwtAuthenticationFilter extends OncePerRequestFilter {

    private final TokenService tokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        log.info("DevJwtAuthenticationFilter 시작");
        String accessToken = tokenService.getAccessTokenByHttpRequest(request);

        User sentryUser = new User();

        // JWT 토큰이 유효한 경우에만, Authentication 객체 셋팅
        if (StringUtils.hasText(accessToken) && tokenService.validateToken(accessToken)) {
            // JWT 기반으로 authentication 설정
            Authentication authenticationToken = tokenService.createAuthenticationByToken(accessToken);
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);

            // 센트리 이메일 설정
            String email = tokenService.getEmail(accessToken);
            sentryUser.setEmail(email);
            Sentry.setUser(sentryUser);
        } else {
            String ip = request.getRemoteAddr();
            try {
                String gaId = CookieUtils.getRequestCookieValueByName(request, GA);
                sentryUser.setEmail(gaId);
            } catch (CookieException e) {
                sentryUser.setEmail(ip);
            } finally {
                Sentry.setUser(sentryUser);
            }
        }

        // 다음 Filter 실행
        filterChain.doFilter(request, response);
    }

    /**
     * WHITELIST_URL은 JwtFilter를 실행하지 않는다.
     */
    @Override
    public boolean shouldNotFilter(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        return Arrays.stream(SecurityConstant.DEV_JWT_FILTER_WHITELIST_URL)
                .anyMatch(whiteList -> StringUtils.startsWithIgnoreCase(requestURI, whiteList));
    }
}