package com.dreamypatisiel.devdevdev.global.security.oauth2.model;

import static com.dreamypatisiel.devdevdev.global.security.oauth2.service.OAuth2UserServiceImpl.INVALID_SOCIAL_LOGIN_SUPPORT_MESSAGE;

import com.dreamypatisiel.devdevdev.domain.entity.Role;
import com.dreamypatisiel.devdevdev.domain.entity.SocialType;
import com.dreamypatisiel.devdevdev.exception.OAuth2UserProviderException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.user.OAuth2User;

public interface OAuth2UserProvider extends OAuth2User {
    String getId();
    String getUserName();
    String getEmail();
    Role getRole();
    List<? extends GrantedAuthority> getAuthorities();
    Map<String, Object> getAttributes(); // 서비스 제공자로 부터 받는 값들
    String getSocialUserProvider();
    SocialType getSocialType();
    Collection<? extends GrantedAuthority> addAuthorities(Role role);

    static OAuth2UserProvider getOAuth2UserProvider(ClientRegistration clientRegistration, OAuth2User oAuth2User) {
        if(clientRegistration.getRegistrationId().equalsIgnoreCase(SocialType.KAKAO.name())) {
            return new KakaoMember(oAuth2User);
        }
        throw new OAuth2UserProviderException(INVALID_SOCIAL_LOGIN_SUPPORT_MESSAGE);
    }

    static OAuth2UserProvider getOAuth2UserProvider(SocialType socialType, Authentication authentication) {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        if(socialType.equals(SocialType.KAKAO)) {
            return new KakaoMember(oAuth2User);
        }
        throw new OAuth2UserProviderException(INVALID_SOCIAL_LOGIN_SUPPORT_MESSAGE);
    }
}
