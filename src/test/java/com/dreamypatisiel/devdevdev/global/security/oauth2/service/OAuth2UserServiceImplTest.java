package com.dreamypatisiel.devdevdev.global.security.oauth2.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.mockito.Mockito.when;

import com.dreamypatisiel.devdevdev.exception.OAuth2UserProviderException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;

@ExtendWith(MockitoExtension.class)
class OAuth2UserServiceImplTest {

    @InjectMocks
    OAuth2UserServiceImpl oAuth2UserService;
    @Mock
    OAuth2MemberService appOAuth2MemberService;
    @Mock
    DefaultOAuth2UserService defaultOAuth2UserService;
    @Mock
    OAuth2User oAuth2User;
    @Mock
    OAuth2UserRequest oAuth2UserRequest;
    @Mock
    ClientRegistration clientRegistration;

    @Test
    @DisplayName("카카오(kakao) 소셜 로그인일 경우 예외가 발생하지 않는다.")
    void loadUser() {
        // given
        when(oAuth2UserRequest.getClientRegistration()).thenReturn(clientRegistration);
        when(clientRegistration.getRegistrationId()).thenReturn("kakao");
        when(defaultOAuth2UserService.loadUser(oAuth2UserRequest)).thenReturn(oAuth2User);

        // when // then
        assertThatCode(() -> oAuth2UserService.loadUser(oAuth2UserRequest))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(strings = {"google", "naver", "facebook", "github", "apple"})
    @DisplayName("카카오(kakao) 소셜 로그인이 아닐 경우 예외가 발생한다.")
    void loadUserException(String registrationId) {
        // given
        when(oAuth2UserRequest.getClientRegistration()).thenReturn(clientRegistration);
        when(clientRegistration.getRegistrationId()).thenReturn(registrationId);
        when(defaultOAuth2UserService.loadUser(oAuth2UserRequest)).thenReturn(oAuth2User);

        // when // then
        assertThatThrownBy(() -> oAuth2UserService.loadUser(oAuth2UserRequest))
                .isInstanceOf(OAuth2UserProviderException.class)
                .hasMessage(OAuth2UserServiceImpl.INVALID_SOCIAL_LOGIN_SUPPORT_MESSAGE);
    }
}