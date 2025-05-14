package com.dreamypatisiel.devdevdev.global.utils;

import com.dreamypatisiel.devdevdev.domain.entity.enums.SocialType;
import com.dreamypatisiel.devdevdev.exception.UserPrincipalException;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.UserPrincipal;
import static com.dreamypatisiel.devdevdev.global.utils.AuthenticationMemberUtils.ANONYMOUS_USER;
import static com.dreamypatisiel.devdevdev.global.utils.AuthenticationMemberUtils.INVALID_METHODS_CALL_MESSAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

class AuthenticationMemberUtilsTest {

    @Test
    @DisplayName("Authentication 객체가 UserPrincipal 타입이 아니면 예외가 발생한다.")
    void getUserPrincipalException() {
        // given
        Object invalidPrincipal = new Object();

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(invalidPrincipal);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // when // then
        assertThatThrownBy(AuthenticationMemberUtils::getUserPrincipal)
                .isInstanceOf(UserPrincipalException.class)
                .hasMessage(AuthenticationMemberUtils.INVALID_TYPE_CAST_USER_PRINCIPAL_MESSAGE);
    }

    @Test
    @DisplayName("Authentication에 있는 UserPrincipal 객체를 반환한다.")
    void getUserPrincipal() {
        // given
        UserPrincipal userPrincipal = UserPrincipal.createByEmailAndRoleAndSocialType(
                "email", "role", SocialType.KAKAO.name());
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // when
        UserPrincipal result = AuthenticationMemberUtils.getUserPrincipal();

        // then
        assertAll(
                () -> assertThat(result.getEmail()).isEqualTo(userPrincipal.getEmail()),
                () -> assertThat(result.getAuthorities()).isEqualTo(userPrincipal.getAuthorities()),
                () -> assertThat(result.getSocialType()).isEqualTo(userPrincipal.getSocialType())
        );
    }

    @Test
    @DisplayName("익명 사용자이면 참을 반환한다.")
    void isAnonymousFalse() {
        // given
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn("anonymousUser");

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // when
        boolean anonymous = AuthenticationMemberUtils.isAnonymous();
        boolean anonymousByAuthentication = AuthenticationMemberUtils.isAnonymous(authentication);

        // then
        assertThat(anonymous).isTrue();
        assertThat(anonymousByAuthentication).isTrue();
    }

    @Test
    @DisplayName("익명 사용자가 아니면 거짓을 반환한다.")
    void isAnonymousTrue() {
        // given
        UserPrincipal userPrincipal = UserPrincipal.createByEmailAndRoleAndSocialType(
                "email", "role", SocialType.KAKAO.name());
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // when
        boolean anonymous = AuthenticationMemberUtils.isAnonymous();
        boolean anonymousByAuthentication = AuthenticationMemberUtils.isAnonymous(authentication);

        // then
        assertThat(anonymous).isFalse();
        assertThat(anonymousByAuthentication).isFalse();
    }

    @Test
    @DisplayName("익명 사용자이면 예외가 발생하지 않는다.")
    void validateAnonymousMethodCall() {
        // given
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(ANONYMOUS_USER);

        // when // then
        assertThatCode(() -> AuthenticationMemberUtils.validateAnonymousMethodCall(authentication))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(strings = {"anonymous-user", "anonymousMember", "anonymous-member", "anonymous"})
    @DisplayName("익명 사용자이면 예외가 발생하지 않는다.")
    void validateAnonymousMethodCallException(String principle) {
        // given
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(principle);

        // when // then
        assertThatCode(() -> AuthenticationMemberUtils.validateAnonymousMethodCall(authentication))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(INVALID_METHODS_CALL_MESSAGE);
    }
}