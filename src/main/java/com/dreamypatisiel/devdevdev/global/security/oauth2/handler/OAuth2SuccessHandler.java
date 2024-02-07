package com.dreamypatisiel.devdevdev.global.security.oauth2.handler;

import com.dreamypatisiel.devdevdev.domain.entity.SocialType;
import com.dreamypatisiel.devdevdev.global.utils.CookieUtils;
import com.dreamypatisiel.devdevdev.global.security.jwt.service.TokenService;
import com.dreamypatisiel.devdevdev.global.security.jwt.model.Token;
import com.dreamypatisiel.devdevdev.global.security.jwt.service.JwtMemberService;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.OAuth2UserProvider;
import com.dreamypatisiel.devdevdev.global.utils.UriUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 로그인 순서(2)
 * OAuth 2.0 로그인(회원가입)이 성공하면 실행되는 핸들러
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Value("${domain.frontend.host}")
    private String domain;
    @Value("${jwt.redirectUri.path}")
    private String endpoint;

    private final TokenService tokenService;
    private final JwtMemberService jwtMemberService;

    // OAuth2.0 로그인 성공시 수행하는 로직
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        String socialType = oauthToken.getAuthorizedClientRegistrationId().toUpperCase();

        // 토큰 생성
        OAuth2UserProvider oAuth2UserProvider = OAuth2UserProvider.getOAuth2UserProvider(SocialType.valueOf(socialType), authentication);
        Token token = tokenService.generateTokenByOAuth2UserProvider(oAuth2UserProvider);

        // 토큰을 쿠키에 저장
        CookieUtils.configJwtCookie(response, token);

        // 리다이렉트 설정
        String redirectUri = UriUtils.createUriByDomainAndEndpoint(domain, endpoint);
        getRedirectStrategy().sendRedirect(request, response, redirectUri);

        // 리프레시 토큰 저장
        jwtMemberService.updateMemberRefreshToken(token.getRefreshToken());
        log.info("OAuth2SuccessHandler accessToken={}", token.getAccessToken());
    }
}
