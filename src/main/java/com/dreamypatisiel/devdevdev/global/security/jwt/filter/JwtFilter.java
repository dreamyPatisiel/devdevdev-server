package com.dreamypatisiel.devdevdev.global.security.jwt.filter;


import com.dreamypatisiel.devdevdev.global.constant.SecurityConstant;
import com.dreamypatisiel.devdevdev.global.security.jwt.model.JwtClaimConstant;
import com.dreamypatisiel.devdevdev.global.security.jwt.service.TokenService;
import com.dreamypatisiel.devdevdev.global.security.oauth2.service.CustomUserDetailsService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * (상시)
 * API 요청 토큰을 검증하는 필터
 * JWT의 인증 정보를 검사해 현재 쓰레드의 SecurityContext에 저장하는 역할 수행
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class JwtFilter extends OncePerRequestFilter {

    private final TokenService tokenService;
    private final CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String accessToken = tokenService.getAccessTokenByRequest(request);

        // JWT 토큰이 유효한 경우에만, Authentication 객체 셋팅
        if (tokenService.validateToken(accessToken)) {
            Claims claims = tokenService.getClaims(accessToken);
            String email = claims.get(JwtClaimConstant.email).toString();
            String socialType = claims.get(JwtClaimConstant.socialType).toString();

            // authentication JWT 기반으로 설정
            // 여기에 캐시를 사용해야겠다.
            UserDetails userDetails = customUserDetailsService.loadUserByEmailAndSocialType(email, socialType);
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
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
        return Arrays.stream(SecurityConstant.JWT_FILTER_WHITELIST_URL)
                .anyMatch(whiteList -> whiteList.contains(requestURI));
    }
}