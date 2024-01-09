package com.dreamypatisiel.devdevdev.global.config.security.oauth2.service;

import com.dreamypatisiel.devdevdev.domain.entity.SocialType;
import com.dreamypatisiel.devdevdev.global.config.security.oauth2.model.KakaoMember;
import com.dreamypatisiel.devdevdev.global.config.security.oauth2.model.OAuth2UserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AppOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final AppOAuth2MemberService appOAuth2MemberService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        ClientRegistration clientRegistration = userRequest.getClientRegistration();
        OAuth2UserService<OAuth2UserRequest, OAuth2User> defaultOAuth2UserService = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = defaultOAuth2UserService.loadUser(userRequest);

        OAuth2UserProvider provider = getProvider(clientRegistration, oAuth2User);
        appOAuth2MemberService.register(provider);

        return oAuth2User;
    }

    private OAuth2UserProvider getProvider(ClientRegistration clientRegistration, OAuth2User oAuth2User) {
        if(clientRegistration.getRegistrationId().equalsIgnoreCase(SocialType.KAKAO.name())) {
            return new KakaoMember(oAuth2User, clientRegistration);
        }
        throw new IllegalArgumentException("잘못된 소셜로그인 접속 입니다.");
    }
}
