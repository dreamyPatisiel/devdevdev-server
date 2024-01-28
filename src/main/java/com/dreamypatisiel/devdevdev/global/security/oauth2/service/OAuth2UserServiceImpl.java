package com.dreamypatisiel.devdevdev.global.security.oauth2.service;

import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.OAuth2UserProvider;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.SocialUserProvider;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.UserPrincipal;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

/**
 * 로그인 순서(1)
 * userRequest를 이용해서 서버에 회원 정보 저장
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OAuth2UserServiceImpl implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    public static final String INVALID_SOCIAL_LOGIN_SUPPORT_MESSAGE = "지원하지 않은 소셜로그인 입니다.";

    private final OAuth2MemberService oAuth2MemberService;
    private final OAuth2UserService<OAuth2UserRequest, OAuth2User> defaultOAuth2UserService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        ClientRegistration clientRegistration = userRequest.getClientRegistration();
        OAuth2User oAuth2User = defaultOAuth2UserService.loadUser(userRequest);

        // 회원 저장
        OAuth2UserProvider provider = OAuth2UserProvider.getOAuth2UserProvider(clientRegistration, oAuth2User);
        return oAuth2MemberService.register(provider, oAuth2User);
    }
}
