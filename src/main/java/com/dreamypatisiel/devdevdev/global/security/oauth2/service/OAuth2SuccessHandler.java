package com.dreamypatisiel.devdevdev.global.security.oauth2.service;

import com.dreamypatisiel.devdevdev.global.security.jwt.CookieUtils;
import com.dreamypatisiel.devdevdev.global.security.jwt.TokenService;
import com.dreamypatisiel.devdevdev.global.security.jwt.model.JwtCookieConstant;
import com.dreamypatisiel.devdevdev.global.security.jwt.model.Token;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.KakaoMember;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.OAuth2UserProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

/**
 * 로그인 순서(2)
 * OAuth 2.0 로그인(회원가입)이 성공하면 실행되는 핸들러
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Value("${jwt.redirectUri}")
    private String redirectUri;
    private static final String TYPE_ACCESS = "access";
    private static final String TYPE_REFRESH = "refresh";
    private final TokenService tokenService;
    private final AppOAuth2MemberService appOAuth2MemberService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        OAuth2UserProvider provider = (OAuth2UserProvider) authentication.getPrincipal();
        //OAuth2UserRequest oAuth2UserRequest = (OAuth2UserRequest) authentication.getPrincipal();

        log.info(provider.toString());

        // 토큰 생성
        Token token = tokenService.generateToken(oAuth2User);

        // set cookie
        saveTokenAtCookie(request, response, token);

        // 리다이렉트 설정
        configRedirect(response);

        // save refresh token
        String email = oAuth2User.getAttribute("email");

        appOAuth2MemberService.findMemberByEmailAndSaveRefreshToken(email, token.getRefreshToken());
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
}
