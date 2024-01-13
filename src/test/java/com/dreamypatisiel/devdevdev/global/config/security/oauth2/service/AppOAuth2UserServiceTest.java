package com.dreamypatisiel.devdevdev.global.config.security.oauth2.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.mockito.Mockito.when;

import com.dreamypatisiel.devdevdev.domain.repository.MemberRepository;
import com.dreamypatisiel.devdevdev.exception.OAuth2UserProviderException;
import com.nimbusds.oauth2.sdk.token.AccessToken;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AppOAuth2UserServiceTest {

    @InjectMocks
    AppOAuth2UserService appOAuth2UserService;
    @Mock
    AppOAuth2MemberService appOAuth2MemberService;
    @Mock
    DefaultOAuth2UserService defaultOAuth2UserService;
    @Mock
    OAuth2User oAuth2User;
    @Mock
    OAuth2UserRequest oAuth2UserRequest;
    @Mock
    ClientRegistration clientRegistration;

    /**
     * 1. 리소스 서버(시간 + 리소스) -> 네네네네네네네 좋은 거 같다.
     */

    @Test
    @DisplayName("카카오(kakao) 소셜 로그인일 경우 예외가 발생하지 않는다.")
    void loadUser() {
        // given
        when(oAuth2UserRequest.getClientRegistration()).thenReturn(clientRegistration);
        when(clientRegistration.getRegistrationId()).thenReturn("kakao");
        when(defaultOAuth2UserService.loadUser(oAuth2UserRequest)).thenReturn(oAuth2User);

        // when // then
        assertThatCode(() -> appOAuth2UserService.loadUser(oAuth2UserRequest))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("카카오(kakao) 소셜 로그인이 아닐 경우 예외가 발생한다.")
    void loadUserException() {
        // given
        when(oAuth2UserRequest.getClientRegistration()).thenReturn(clientRegistration);
        when(clientRegistration.getRegistrationId()).thenReturn("google");
        when(defaultOAuth2UserService.loadUser(oAuth2UserRequest)).thenReturn(oAuth2User);

        // when // then
        assertThatThrownBy(() -> appOAuth2UserService.loadUser(oAuth2UserRequest))
                .isInstanceOf(OAuth2UserProviderException.class)
                .hasMessage(AppOAuth2UserService.INVALID_SOCIAL_LOGIN_SUPPORT_MESSAGE);
    }
}