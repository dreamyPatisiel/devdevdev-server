package com.dreamypatisiel.devdevdev.global.security.jwt.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.dreamypatisiel.devdevdev.domain.entity.SocialType;
import com.dreamypatisiel.devdevdev.exception.TokenInvalidException;
import com.dreamypatisiel.devdevdev.exception.TokenNotFoundException;
import com.dreamypatisiel.devdevdev.global.common.TimeProvider;
import com.dreamypatisiel.devdevdev.global.constant.SecurityConstant;
import com.dreamypatisiel.devdevdev.global.security.jwt.model.JwtClaimConstant;
import com.dreamypatisiel.devdevdev.global.security.jwt.model.Token;
import com.dreamypatisiel.devdevdev.global.security.jwt.model.TokenExpireTime;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.OAuth2UserProvider;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.UserPrincipal;
import io.jsonwebtoken.Claims;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@SpringBootTest
class TokenServiceTest {

    @Autowired
    TokenService tokenService;
    @MockBean
    TimeProvider timeProvider;

    String email = "dreamy5patisiel@kakao.com";

    @ParameterizedTest
    @CsvSource(value = {"KAKAO:ROLE_USER", "KAKAO:ROLE_ADMIN"}, delimiter = ':')
    @DisplayName("OAuth2UserProvider를 인자로 토큰을 생성한다.")
    void generateTokenByOAuth2UserProvider(String socialType, String role) {
        // given
        when(timeProvider.getDateNow()).thenReturn(new Date());

        OAuth2UserProvider oAuth2UserProvider = mock(OAuth2UserProvider.class);
        when(oAuth2UserProvider.getEmail()).thenReturn(email);
        when(oAuth2UserProvider.getSocialType()).thenReturn(SocialType.valueOf(socialType));
        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(role));
        when(oAuth2UserProvider.getAuthorities()).thenReturn((List) authorities);

        // when
        Token token = tokenService.generateTokenByOAuth2UserProvider(oAuth2UserProvider);

        // then
        assertThat(token).isNotNull();
        assertAll(
                () -> assertThat(token.getAccessToken()).isNotNull(),
                () -> assertThat(token.getRefreshToken()).isNotNull(),
                () -> assertThat(token.getAccessToken()).isNotEqualTo(token.getRefreshToken())
        );
    }

    @ParameterizedTest
    @CsvSource(value = {"KAKAO:ROLE_USER", "KAKAO:ROLE_ADMIN"}, delimiter = ':')
    @DisplayName("email, socialType, role을 인자로 토큰을 생성한다.")
    void generateTokenBy(String socialType, String role) {
        // given
        when(timeProvider.getDateNow()).thenReturn(new Date());

        // when
        Token token = tokenService.generateTokenBy(email, socialType, role);

        // then
        assertThat(token).isNotNull();
        assertAll(
                () -> assertThat(token.getAccessToken()).isNotNull(),
                () -> assertThat(token.getRefreshToken()).isNotNull(),
                () -> assertThat(token.getAccessToken()).isNotEqualTo(token.getRefreshToken())
        );
    }

    @ParameterizedTest
    @CsvSource(value = {"KAKAO:ROLE_USER", "KAKAO:ROLE_ADMIN"}, delimiter = ':')
    @DisplayName("OAuth2UserProvider을 인자로 생성한 토큰을 검증하면 참을 반환한다.")
    void validateTokenCreateByOAuth2UserProvider(String socialType, String role) {
        // given
        when(timeProvider.getDateNow()).thenReturn(new Date());

        OAuth2UserProvider oAuth2UserProvider = mock(OAuth2UserProvider.class);
        when(oAuth2UserProvider.getEmail()).thenReturn(email);
        when(oAuth2UserProvider.getSocialType()).thenReturn(SocialType.valueOf(socialType));
        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(role));
        when(oAuth2UserProvider.getAuthorities()).thenReturn((List) authorities);

        Token token = tokenService.generateTokenByOAuth2UserProvider(oAuth2UserProvider);
        String accessToken = token.getAccessToken();
        String refreshToken = token.getRefreshToken();

        // when
        boolean accessTokenResult = tokenService.validateToken(accessToken);
        boolean refreshTokenResult = tokenService.validateToken(refreshToken);

        // then
        assertAll(
                () -> assertThat(accessTokenResult).isTrue(),
                () -> assertThat(refreshTokenResult).isTrue()
        );
    }

    @ParameterizedTest
    @CsvSource(value = {"KAKAO:ROLE_USER", "KAKAO:ROLE_ADMIN"}, delimiter = ':')
    @DisplayName("email, socialType, role을 인자로 생성한 토큰을 검증하면 참을 반환한다.")
    void validateTokenCreateByClaims(String socialType, String role) {
        // given
        when(timeProvider.getDateNow()).thenReturn(new Date());

        Token token = tokenService.generateTokenBy(email, socialType, role);
        String accessToken = token.getAccessToken();
        String refreshToken = token.getRefreshToken();

        // when
        boolean accessTokenResult = tokenService.validateToken(accessToken);
        boolean refreshTokenResult = tokenService.validateToken(refreshToken);

        // then
        assertAll(
                () -> assertThat(accessTokenResult).isTrue(),
                () -> assertThat(refreshTokenResult).isTrue()
        );
    }

    @ParameterizedTest
    @CsvSource(value = {"KAKAO:ROLE_USER", "KAKAO:ROLE_ADMIN"}, delimiter = ':')
    @DisplayName("유효기간이 지난 엑세스 토큰을 검증하면 예외가 발생한다.")
    void validationAccessTokenException_INVALID_EXPIRED_TOKEN_MESSAGE(String socialType, String role) {
        // given
        Date now = new Date();
        when(timeProvider.getDateNow()).thenReturn(new Date(now.getTime() - TokenExpireTime.ACCESS_TOKEN_EXPIRE_TIME));

        OAuth2UserProvider oAuth2UserProvider = mock(OAuth2UserProvider.class);
        when(oAuth2UserProvider.getEmail()).thenReturn(email);
        when(oAuth2UserProvider.getSocialType()).thenReturn(SocialType.valueOf(socialType));
        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(role));
        when(oAuth2UserProvider.getAuthorities()).thenReturn((List) authorities);

        Token tokenByOAuth2UserProvider = tokenService.generateTokenByOAuth2UserProvider(oAuth2UserProvider);
        Token generateTokenBy = tokenService.generateTokenBy(email, socialType, role);

        String accessTokenByOAuth2UserProvider = tokenByOAuth2UserProvider.getAccessToken();
        String accessTokenBy = generateTokenBy.getAccessToken();

        // when // then
        assertAll(
                () -> assertThatThrownBy(() -> tokenService.validateToken(accessTokenByOAuth2UserProvider))
                        .isInstanceOf(TokenInvalidException.class)
                        .hasMessage(TokenService.INVALID_EXPIRED_TOKEN_MESSAGE),
                () -> assertThatThrownBy(() -> tokenService.validateToken(accessTokenBy))
                        .isInstanceOf(TokenInvalidException.class)
                        .hasMessage(TokenService.INVALID_EXPIRED_TOKEN_MESSAGE)
                );
    }

    @ParameterizedTest
    @CsvSource(value = {"KAKAO:ROLE_USER", "KAKAO:ROLE_ADMIN"}, delimiter = ':')
    @DisplayName("유효기간이 지난 리프레시 토큰을 검증하면 예외가 발생한다.")
    void validationRefreshTokenException_INVALID_EXPIRED_TOKEN_MESSAGE(String socialType, String role) {
        // given
        Date now = new Date();
        when(timeProvider.getDateNow()).thenReturn(new Date(now.getTime() - TokenExpireTime.REFRESH_TOKEN_EXPIRE_TIME));

        OAuth2UserProvider oAuth2UserProvider = mock(OAuth2UserProvider.class);
        when(oAuth2UserProvider.getEmail()).thenReturn(email);
        when(oAuth2UserProvider.getSocialType()).thenReturn(SocialType.valueOf(socialType));
        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(role));
        when(oAuth2UserProvider.getAuthorities()).thenReturn((List) authorities);

        Token tokenByOAuth2UserProvider = tokenService.generateTokenByOAuth2UserProvider(oAuth2UserProvider);
        Token generateTokenBy = tokenService.generateTokenBy(email, socialType, role);

        String refreshTokenTokenByOAuth2UserProvider = tokenByOAuth2UserProvider.getRefreshToken();
        String refreshTokenBy = generateTokenBy.getRefreshToken();

        // when // then
        assertAll(
                () -> assertThatThrownBy(() -> tokenService.validateToken(refreshTokenTokenByOAuth2UserProvider))
                        .isInstanceOf(TokenInvalidException.class)
                        .hasMessage(TokenService.INVALID_EXPIRED_TOKEN_MESSAGE),
                () -> assertThatThrownBy(() -> tokenService.validateToken(refreshTokenBy))
                        .isInstanceOf(TokenInvalidException.class)
                        .hasMessage(TokenService.INVALID_EXPIRED_TOKEN_MESSAGE)
        );
    }

    @Test
    @DisplayName("잘못된 서명의(다른 암호화 알고리즘 형식의) 토큰을 검증하면 예외가 발생한다.")
    void validationTokenException_INVALID_TOKEN_SIGNATURE_MESSAGE() {
        // given
        String invalidToken = "eyJhbGciOiJIUzUxMiJ9"
                + ".eyJlbWFpbCI6ImRyZWFteTVwYXRpc2llbEBrYWthby5jb20iLCJzb2NpYWxUeXBlIjoiS0FLQU8iLCJyb2xlIjoiUk9MRV9VU0VSIiwiaWF0IjoxNzA2NzEwNjY4LCJleHAiOjE3MDY3MTI0Njh9"
                + ".JhivWSOkD4XAsj9f--IprnH4ImjtArGX2tBALev_-C1yG7vnW9I9Mi648XV2gl67MWGjYVurQL8lI47xcotZaQ";

        when(timeProvider.getDateNow()).thenReturn(new Date());

        // when // then
        assertThatThrownBy(() -> tokenService.validateToken(invalidToken))
                .isInstanceOf(TokenInvalidException.class)
                .hasMessage(TokenService.INVALID_TOKEN_SIGNATURE_MESSAGE);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " "})
    @DisplayName("토큰을 검증할 때 토큰이 빈 값이거나 공백이면 예외가 발생한다.")
    void validateTokenException_INVALID_TOKEN_NOT_FOUND_MESSAGE(String invalidToken) {
        // given
        when(timeProvider.getDateNow()).thenReturn(new Date());

        // when // then
        assertThatThrownBy(() -> tokenService.validateToken(invalidToken))
                .isInstanceOf(TokenNotFoundException.class)
                .hasMessage(TokenService.INVALID_TOKEN_NOT_FOUND_MESSAGE);
    }

    @Disabled
    @Test
    @DisplayName("지원하지 않는 토큰을 검증하면 예외가 발생한다.")
    void validateTokenException_INVALID_UNSUPPORTED_TOKEN_MESSAGE() {
        // given
        when(timeProvider.getDateNow()).thenReturn(new Date());

        String invalidToken = "eyJhbGciOiJIUzI1NiJ9"
                + ".eyJteV9lbWFpbCI6ImRyZWFteTVwYXRpc2llbEBrYWthby5jb20iLCJteV9zb2NpYWxUeXBlIjoiS0FLQU8iLCJteV9yb2xlIjoiUk9MRV9VU0VSIiwiaWF0IjoxNzA2MTA5Njc0LCJleHAiOjE3MDY3MTQ0NzR9"
                + ".i9qezdFUKoFGcCvarEFrtZ8zzUUwIQElwLqKO9jwXqI";

        // when // then
        assertThatThrownBy(() -> tokenService.validateToken(invalidToken))
                .isInstanceOf(TokenInvalidException.class)
                .hasMessage(TokenService.INVALID_UNSUPPORTED_TOKEN_MESSAGE);
    }

    @Test
    @DisplayName("요청 헤더(Authorization)에서 값(Bearer ~)에 해당하는 엑세스 토큰을 추출한다.")
    void getAccessTokenByHttpRequest() {
        // given
        String accessToken = "accessToken";

        MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
        mockHttpServletRequest.addHeader(SecurityConstant.AUTHORIZATION_HEADER,
                SecurityConstant.BEARER_PREFIX + accessToken);

        // when
        String accessTokenByHttpRequest = tokenService.getAccessTokenByHttpRequest(mockHttpServletRequest);

        // then
        assertThat(accessTokenByHttpRequest).isEqualTo(accessToken);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "accessToken"})
    @DisplayName("요청 헤더(Authorization)에서 값이 없거나(빈 값 또는 스페이스) 'Bearer '로 시작하지 않으면 null을 반환한다.")
    void getAccessTokenByHttpRequestNull(String accessToken) {
        // given
        MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
        mockHttpServletRequest.addHeader(SecurityConstant.AUTHORIZATION_HEADER, accessToken);

        // when
        String accessTokenByHttpRequest = tokenService.getAccessTokenByHttpRequest(mockHttpServletRequest);

        // then
        assertThat(accessTokenByHttpRequest).isNull();
    }

    @Test
    @DisplayName("엑세스 토큰 또는 리프레시 토큰을 인자로 사용하여 인증 객체(Authentication)를 생성한다.")
    void createAuthenticationByToken() {
        // given
        when(timeProvider.getDateNow()).thenReturn(new Date());
        SocialType socialType = SocialType.KAKAO;
        String role = "ROLE_USER";
        Token token = tokenService.generateTokenBy(email, socialType.name(), role);

        // when
        Authentication authenticationByAccessToken = tokenService.createAuthenticationByToken(token.getAccessToken());
        Authentication authenticationByRefreshToken = tokenService.createAuthenticationByToken(token.getRefreshToken());

        // then
        UserPrincipal principalByAccessToken = (UserPrincipal) authenticationByAccessToken.getPrincipal();
        assertAll(
                () -> assertThat(authenticationByAccessToken).isNotNull(),
                () -> assertThat(authenticationByAccessToken.getPrincipal()).isNotNull(),
                () -> assertThat(principalByAccessToken.getEmail()).isEqualTo(email),
                () -> assertThat(principalByAccessToken.getSocialType()).isEqualTo(socialType),
                () -> assertThat(principalByAccessToken.getAuthorities()).hasSize(1)
                        .extracting("authority")
                        .contains(role)
        );

        UserPrincipal principalByRefreshToken = (UserPrincipal) authenticationByRefreshToken.getPrincipal();
        assertAll(
                () -> assertThat(authenticationByAccessToken).isNotNull(),
                () -> assertThat(authenticationByAccessToken.getPrincipal()).isNotNull(),
                () -> assertThat(principalByRefreshToken.getEmail()).isEqualTo(email),
                () -> assertThat(principalByRefreshToken.getSocialType()).isEqualTo(socialType),
                () -> assertThat(principalByRefreshToken.getAuthorities()).hasSize(1)
                        .extracting("authority")
                        .contains(role)
        );
    }

    @Test
    @DisplayName("엑세스 토큰 또는 리프레시 토큰을 인자로 사용하여 클레임을 생성한다.")
    void getClaims() {
        // given
        when(timeProvider.getDateNow()).thenReturn(new Date());
        SocialType socialType = SocialType.KAKAO;
        String role = "ROLE_USER";
        Token token = tokenService.generateTokenBy(email, socialType.name(), role);

        // when
        Claims claimsByAccessToken = tokenService.getClaims(token.getAccessToken());
        Claims claimsByRefreshToken = tokenService.getClaims(token.getRefreshToken());

        // then
        assertThat(claimsByAccessToken).isNotNull();
        assertAll(
                () -> assertThat(claimsByAccessToken.get(JwtClaimConstant.email).toString()).isEqualTo(email),
                () -> assertThat(claimsByAccessToken.get(JwtClaimConstant.socialType).toString()).isEqualTo(socialType.name()),
                () -> assertThat(claimsByAccessToken.get(JwtClaimConstant.role).toString()).isEqualTo(role)
        );

        assertThat(claimsByRefreshToken).isNotNull();
        assertAll(
                () -> assertThat(claimsByRefreshToken.get(JwtClaimConstant.email).toString()).isEqualTo(email),
                () -> assertThat(claimsByRefreshToken.get(JwtClaimConstant.socialType).toString()).isEqualTo(socialType.name()),
                () -> assertThat(claimsByRefreshToken.get(JwtClaimConstant.role).toString()).isEqualTo(role)
        );
    }

    @Test
    @DisplayName("엑세스 토큰 또는 리프레시 토큰을 인자로 사용하여 socialType을 생성한다.")
    void getSocialType() {
        // given
        when(timeProvider.getDateNow()).thenReturn(new Date());
        SocialType socialType = SocialType.KAKAO;
        String role = "ROLE_USER";
        Token token = tokenService.generateTokenBy(email, socialType.name(), role);

        // when
        String result = tokenService.getSocialType(token.getAccessToken());

        // then
        assertThat(result).isEqualTo(socialType.name());
    }

    @Test
    @DisplayName("엑세스 토큰 또는 리프레시 토큰을 인자로 사용하여 email을 생성한다.")
    void getEmail() {
        // given
        when(timeProvider.getDateNow()).thenReturn(new Date());
        SocialType socialType = SocialType.KAKAO;
        String role = "ROLE_USER";
        Token token = tokenService.generateTokenBy(email, socialType.name(), role);

        // when
        String result = tokenService.getEmail(token.getAccessToken());

        // then
        assertThat(result).isEqualTo(email);
    }

    @Test
    @DisplayName("엑세스 토큰 또는 리프레시 토큰을 인자로 사용하여 role을 생성한다.")
    void getRole() {
        // given
        when(timeProvider.getDateNow()).thenReturn(new Date());
        SocialType socialType = SocialType.KAKAO;
        String role = "ROLE_USER";
        Token token = tokenService.generateTokenBy(email, socialType.name(), role);

        // when
        String result = tokenService.getRole(token.getAccessToken());

        // then
        assertThat(result).isEqualTo(role);
    }


}

