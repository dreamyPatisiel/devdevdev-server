package com.dreamypatisiel.devdevdev.global.security.oauth2.handler;

import com.dreamypatisiel.devdevdev.domain.entity.SocialType;
import com.dreamypatisiel.devdevdev.global.security.jwt.CookieUtils;
import com.dreamypatisiel.devdevdev.global.security.jwt.TokenService;
import com.dreamypatisiel.devdevdev.global.security.jwt.model.Token;
import com.dreamypatisiel.devdevdev.global.security.jwt.service.JwtMemberService;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.KakaoMember;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.OAuth2UserProvider;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.SocialUserProvider;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * 로그인 순서(2)
 * OAuth 2.0 로그인(회원가입)이 성공하면 실행되는 핸들러
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private static final String TYPE_ACCESS = "access";
    private static final String TYPE_REFRESH = "refresh";

    @Value("${jwt.redirectUri.scheme}")
    private String scheme;
    @Value("${jwt.redirectUri.host}")
    private String host;
    @Value("${jwt.redirectUri.path}")
    private String path;

    private final TokenService tokenService;
    private final JwtMemberService jwtMemberService;

    // OAuth2.0 로그인 성공시 수행하는 로직
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        log.info("OAuth2SuccessHandler authentication={}",authentication);
        log.info("OAuth2SuccessHandler");
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        String socialType = oauthToken.getAuthorizedClientRegistrationId().toUpperCase();

        // 토큰 생성
        OAuth2UserProvider oAuth2UserProvider = OAuth2UserProvider.getOAuth2UserProvider(SocialType.valueOf(socialType), authentication);
        Token token = tokenService.generateToken(oAuth2UserProvider);
        log.info("accessToken={}", token.getAccessToken());
        log.info("refreshToken={}", token.getRefreshToken());

        // set cookie
        CookieUtils.configJwtCookie(response, token);

        // 리다이렉트 설정
        String redirectUri = getRedirectUri(scheme, host, path);
        //response.sendRedirect(redirectUri);
        //getRedirectStrategy().sendRedirect(request, response, redirectUri);

        // 리프레시 토큰 저장
        Claims claims = tokenService.getClaims(token.getAccessToken());
        jwtMemberService.updateMemberRefreshToken(token.getRefreshToken(), claims);
    }

    private void saveTokenAtCookie(HttpServletRequest request, HttpServletResponse response, Token token) {
        setCookie(response, token.getAccessToken(), "TYPE_ACCESS", false);
        setCookie(response, token.getRefreshToken(), "TYPE_REFRESH",true);
    }

    private void setCookie(HttpServletResponse response, String token, String name, boolean isHttpOnly) {
        Cookie cookie = new Cookie(name, token);
        cookie.setPath("/");
        if(isHttpOnly){
            cookie.setHttpOnly(true);
            cookie.setSecure(true);
        }
        response.addCookie(cookie);
    }

    private String getRedirectUri(String scheme, String host, String path) {
        return UriComponentsBuilder.newInstance()
                .scheme(scheme)
                .host(host)
                .path(path)
                .toUriString();
    }
}
