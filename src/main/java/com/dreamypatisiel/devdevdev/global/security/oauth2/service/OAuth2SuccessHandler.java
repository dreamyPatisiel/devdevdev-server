package com.dreamypatisiel.devdevdev.global.security.oauth2.service;

import com.dreamypatisiel.devdevdev.domain.entity.SocialType;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Email;
import com.dreamypatisiel.devdevdev.global.security.jwt.CookieUtils;
import com.dreamypatisiel.devdevdev.global.security.jwt.TokenService;
import com.dreamypatisiel.devdevdev.global.security.jwt.model.JwtClaimConstant;
import com.dreamypatisiel.devdevdev.global.security.jwt.model.JwtCookieConstant;
import com.dreamypatisiel.devdevdev.global.security.jwt.model.Token;
import com.dreamypatisiel.devdevdev.global.security.jwt.service.JwtMemberService;
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
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Map;

/**
 * 로그인 순서(2)
 * OAuth 2.0 로그인(회원가입)이 성공하면 실행되는 핸들러
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Value("${jwt.redirectUri.scheme}")
    private String redirectUriScheme;
    @Value("${jwt.redirectUri.host}")
    private String redirectUriHost;
    @Value("${jwt.redirectUri.path}")
    private String redirectUriPath;

    private final static String KAKAO_ACCOUNT = "kakao_account";

    private final TokenService tokenService;
    private final JwtMemberService jwtMemberService;

    @SuppressWarnings("unchecked")
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        Map<String, Object> kakaoAccountAttributes = (Map<String, Object>) (oAuth2User.getAttributes().get(KAKAO_ACCOUNT));
        String email = kakaoAccountAttributes.get(JwtClaimConstant.email).toString();
        String socialType = oauthToken.getAuthorizedClientRegistrationId().toUpperCase();

        // 토큰 생성
        Token newToken = tokenService.generateToken(email, socialType);

        // cookie 설정
        CookieUtils.configJwtCookie(response, newToken);

        // 리다이렉트 설정 및 리다이렉트
        UriComponents redirectUri = configRedirect(response);
        response.sendRedirect(redirectUri.toUriString());

        // save refresh token
        log.info(oAuth2User.getAttributes().toString());
        log.info("refresh 저장 "+email+", "+socialType);
        jwtMemberService.findMemberByEmailAndSocialTypeAndSaveRefreshToken(new Email(email), SocialType.valueOf(socialType), newToken.getRefreshToken());
    }

    private UriComponents configRedirect(HttpServletResponse response) {
        response.setStatus(HttpStatus.SEE_OTHER.value());
        UriComponents redirectUri = UriComponentsBuilder.newInstance()
                .scheme(redirectUriScheme)
                .host(redirectUriHost)
                .path(redirectUriPath)
                .build();
        return redirectUri;
    }
}
