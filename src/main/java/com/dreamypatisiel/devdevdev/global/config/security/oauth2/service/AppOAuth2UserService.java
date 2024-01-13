package com.dreamypatisiel.devdevdev.global.config.security.oauth2.service;

import com.dreamypatisiel.devdevdev.domain.entity.SocialType;
import com.dreamypatisiel.devdevdev.exception.OAuth2UserProviderException;
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

    public static final String INVALID_SOCIAL_LOGIN_SUPPORT_MESSAGE = "지원하지 않은 소셜로그인 입니다.";

    private final AppOAuth2MemberService appOAuth2MemberService;
    private final OAuth2UserService<OAuth2UserRequest, OAuth2User> defaultOAuth2UserService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        ClientRegistration clientRegistration = userRequest.getClientRegistration();
        OAuth2User oAuth2User = defaultOAuth2UserService.loadUser(userRequest);

        OAuth2UserProvider provider = getProvider(clientRegistration, oAuth2User);
        appOAuth2MemberService.register(provider);

        return oAuth2User;
    }

    private OAuth2UserProvider getProvider(ClientRegistration clientRegistration, OAuth2User oAuth2User) {
        if(clientRegistration.getRegistrationId().equalsIgnoreCase(SocialType.KAKAO.name())) {
            return new KakaoMember(oAuth2User, clientRegistration);
        }
        throw new OAuth2UserProviderException(INVALID_SOCIAL_LOGIN_SUPPORT_MESSAGE);
    }
}
