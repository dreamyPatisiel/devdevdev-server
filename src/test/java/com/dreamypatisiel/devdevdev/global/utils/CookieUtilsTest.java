package com.dreamypatisiel.devdevdev.global.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.enums.Role;
import com.dreamypatisiel.devdevdev.domain.entity.enums.SocialType;
import com.dreamypatisiel.devdevdev.exception.CookieException;
import com.dreamypatisiel.devdevdev.global.security.jwt.model.JwtCookieConstant;
import com.dreamypatisiel.devdevdev.global.security.jwt.model.Token;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.SocialMemberDto;
import jakarta.servlet.http.Cookie;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class CookieUtilsTest {

    @Test
    @DisplayName("요청에 있는 쿠키를 추출할 때 쿠키가 없으면 예외가 발생한다.")
    void getRequestCookieByNameException_INVALID_NOT_FOUND_COOKIE_MESSAGE() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        String name = "ChocolateChipCookie";

        // when // then
        assertThatThrownBy(() -> CookieUtils.getRequestCookieByName(request, name))
                .isInstanceOf(CookieException.class)
                .hasMessage(CookieUtils.INVALID_NOT_FOUND_COOKIE_MESSAGE);
    }

    @Test
    @DisplayName("요청에 있는 쿠키를 추출할 때 알맞은 이름의 쿠키가 없으면 예외가 발생한다.")
    void getRequestCookieByNameException_INVALID_NOT_FOUND_COOKIE_BY_NAME_MESSAGE() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        String name = "ChocolateChipCookie";
        String value = "yummy";
        Cookie cookie = new Cookie(name, value);
        request.setCookies(cookie);

        String otherName = "ButterChipCookie";

        // when // then
        assertThatThrownBy(() -> CookieUtils.getRequestCookieByName(request, otherName))
                .isInstanceOf(CookieException.class)
                .hasMessage(CookieUtils.INVALID_NOT_FOUND_COOKIE_BY_NAME_MESSAGE);
    }

    @Test
    @DisplayName("요청에 있는 쿠키를 쿠키 이름으로 추출한다.")
    void getRequestCookieByName() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();

        String name = "ChocolateChipCookie";
        String value = "yummy";
        Cookie cookie = new Cookie(name, value);
        request.setCookies(cookie);

        // when
        Cookie result = CookieUtils.getRequestCookieByName(request, name);

        // then
        assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.getName()).isEqualTo(name),
                () -> assertThat(result.getValue()).isEqualTo(value)
        );
    }

    @Test
    @DisplayName("요청에 있는 쿠키의 값을 추출할 때 쿠키가 없으면 예외가 발생한다.")
    void getRequestCookieValueByNameException() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        String name = "ChocolateChipCookie";

        // when // then
        assertThatThrownBy(() -> CookieUtils.getRequestCookieValueByName(request, name))
                .isInstanceOf(CookieException.class)
                .hasMessage(CookieUtils.INVALID_NOT_FOUND_COOKIE_MESSAGE);
    }

    @Test
    @DisplayName("요청에 있는 쿠키값을 추출할 때 알맞은 이름의 쿠키가 없으면 예외가 발생한다.")
    void getRequestCookieByNameException_INVALID_NOT_FOUND_COOKIE_VALUE_BY_NAME_MESSAGE() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        String name = "ChocolateChipCookie";
        String value = "yummy";
        Cookie cookie = new Cookie(name, value);
        request.setCookies(cookie);

        String otherName = "ButterChipCookie";

        // when // then
        assertThatThrownBy(() -> CookieUtils.getRequestCookieValueByName(request, otherName))
                .isInstanceOf(CookieException.class)
                .hasMessage(CookieUtils.INVALID_NOT_FOUND_COOKIE_VALUE_BY_NAME_MESSAGE);
    }

    @Test
    @DisplayName("요청에 있는 쿠키의 값을 쿠키 이름으로 추출한다.")
    void getRequestCookieValueByName() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();

        String name = "ChocolateChipCookie";
        String value = "yummy";
        Cookie cookie = new Cookie(name, value);
        request.setCookies(cookie);

        // when
        String result = CookieUtils.getRequestCookieValueByName(request, name);

        // then
        assertThat(result).isEqualTo(value);
    }


    @Test
    @DisplayName("쿠키를 삭제할 경우"
            + " 요청에 있는 쿠키의 이름과 일치하는 쿠키를 찾아"
            + " 쿠키의 값을 빈값, 경로를 /, 수명을 0으로 설정하여"
            + " 클라이언트에게 응답으로 준다.")
    void deleteCookie() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        String name = "ChocolateChipCookie";
        String value = "yummy";
        Cookie cookie = new Cookie(name, value);
        request.setCookies(cookie);

        // when
        CookieUtils.deleteCookieFromResponse(request, response, name);

        // then
        Cookie responseCookie = response.getCookie(name);
        assertThat(responseCookie).isNotNull();
        assertAll(
                () -> assertThat(responseCookie.getName()).isEqualTo(name),
                () -> assertThat(responseCookie.getValue()).isEqualTo(""),
                () -> assertThat(responseCookie.getPath()).isEqualTo("/"),
                () -> assertThat(responseCookie.getMaxAge()).isEqualTo(0)
        );
    }

    @Disabled
    @Test
    @DisplayName("쿠키를 삭제할 경우 요청 값에 쿠키가 없으면 예외가 발생한다.")
    void deleteCookieException() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        String name = "ChocolateChipCookie";

        // when // then
        assertThatThrownBy(() -> CookieUtils.deleteCookieFromResponse(request, response, name))
                .isInstanceOf(CookieException.class)
                .hasMessage(CookieUtils.INVALID_NOT_FOUND_COOKIE_MESSAGE);
    }

    @Test
    @DisplayName("응답에 이름, 값, 수명, httpOnly, secure를 설정하여 쿠키를 추가할 수 있다.")
    void addCookie() {
        // given
        MockHttpServletResponse response = new MockHttpServletResponse();

        String name = "ChocolateChipCookie";
        String value = "yummy";
        int maxAge = 100;
        boolean isHttpOnly = true;
        boolean isSecure = false;
        String sameSite = "None";

        // when
        CookieUtils.addCookieToResponse(response, name, value, maxAge, isHttpOnly, isSecure);

        // then
        Cookie cookie = response.getCookie(name);
        assertThat(cookie).isNotNull();
        assertAll(
                () -> assertThat(cookie.getName()).isEqualTo(name),
                () -> assertThat(cookie.getValue()).isEqualTo(value),
                () -> assertThat(cookie.getMaxAge()).isEqualTo(maxAge),
                () -> assertThat(cookie.isHttpOnly()).isEqualTo(isHttpOnly),
                () -> assertThat(cookie.getSecure()).isEqualTo(isSecure),
                () -> assertThat(cookie.getAttribute("SameSite")).isEqualTo(sameSite)
        );
    }

    @Test
    @DisplayName("응답 정보에 JWT 관련 쿠키를 설정할 때 토큰 정보를 저장한다."
            + " (엑세스 토큰은 secure은 true, httpOnly은 false로 저장하고"
            + " 리프레시 토큰은 secure은 true, httpOnly은 true 저장한다.)")
    void configJwtCookie() {
        // given
        MockHttpServletResponse response = new MockHttpServletResponse();
        String accessToken = "accessToken";
        String refreshToken = "refreshToken";
        Token token = new Token(accessToken, refreshToken);

        // when
        CookieUtils.configJwtCookie(response, token);

        // then
        Cookie accessTokenCookie = response.getCookie(JwtCookieConstant.DEVDEVDEV_ACCESS_TOKEN);
        Cookie refreshTokenCookie = response.getCookie(JwtCookieConstant.DEVDEVDEV_REFRESH_TOKEN);
        Cookie loginStatusCookie = response.getCookie(JwtCookieConstant.DEVDEVDEV_LOGIN_STATUS);

        assertAll(
                () -> assertThat(accessTokenCookie).isNotNull(),
                () -> assertThat(refreshTokenCookie).isNotNull(),
                () -> assertThat(loginStatusCookie).isNotNull()
        );

        assertAll(
                () -> assertThat(accessTokenCookie.getName()).isEqualTo(JwtCookieConstant.DEVDEVDEV_ACCESS_TOKEN),
                () -> assertThat(accessTokenCookie.getValue()).isEqualTo(accessToken),
                () -> assertThat(accessTokenCookie.getMaxAge()).isEqualTo(CookieUtils.DEFAULT_MAX_AGE),
                () -> assertThat(accessTokenCookie.getSecure()).isTrue(),
                () -> assertThat(accessTokenCookie.isHttpOnly()).isFalse()
        );

        assertAll(
                () -> assertThat(refreshTokenCookie.getName()).isEqualTo(JwtCookieConstant.DEVDEVDEV_REFRESH_TOKEN),
                () -> assertThat(refreshTokenCookie.getValue()).isEqualTo(refreshToken),
                () -> assertThat(refreshTokenCookie.getMaxAge()).isEqualTo(CookieUtils.REFRESH_MAX_AGE),
                () -> assertThat(refreshTokenCookie.getSecure()).isTrue(),
                () -> assertThat(refreshTokenCookie.isHttpOnly()).isTrue()
        );

        assertAll(
                () -> assertThat(loginStatusCookie.getName()).isEqualTo(JwtCookieConstant.DEVDEVDEV_LOGIN_STATUS),
                () -> assertThat(loginStatusCookie.getValue()).isEqualTo(CookieUtils.ACTIVE),
                () -> assertThat(loginStatusCookie.getMaxAge()).isEqualTo(CookieUtils.DEFAULT_MAX_AGE),
                () -> assertThat(loginStatusCookie.getSecure()).isTrue(),
                () -> assertThat(loginStatusCookie.isHttpOnly()).isFalse()
        );
    }

    @ParameterizedTest
    @CsvSource({
            "ROLE_USER, false",
            "ROLE_ADMIN, true"
    })
    @DisplayName("응답 정보에 멤버 관련 쿠키를 설정한다."
            + " (유저 닉네임은 UTF-8로 인코딩되고 이메일은 인코딩되지 않는다."
            + " secure은 true, httpOnly은 false로 저장한다.)")
    void configMemberCookie(String inputIsAdmin, String expectedIsAdmin) {
        // given
        MockHttpServletResponse response = new MockHttpServletResponse();
        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", "email@gmail.com", SocialType.KAKAO.name(), inputIsAdmin);
        Member member = Member.createMemberBy(socialMemberDto);
        String encodedNickname = URLEncoder.encode(member.getNicknameAsString(), StandardCharsets.UTF_8);
        String email = member.getEmailAsString();

        // when
        CookieUtils.configMemberCookie(response, member);

        // then
        Cookie nicknameCookie = response.getCookie(JwtCookieConstant.DEVDEVDEV_MEMBER_NICKNAME);
        Cookie emailCookie = response.getCookie(JwtCookieConstant.DEVDEVDEV_MEMBER_EMAIL);
        Cookie isAdmin = response.getCookie(JwtCookieConstant.DEVDEVDEV_MEMBER_IS_ADMIN);

        assertAll(
                () -> assertThat(nicknameCookie).isNotNull(),
                () -> assertThat(emailCookie).isNotNull(),
                () -> assertThat(isAdmin).isNotNull()
        );

        assertAll(
                () -> assertThat(nicknameCookie.getName()).isEqualTo(JwtCookieConstant.DEVDEVDEV_MEMBER_NICKNAME),
                () -> assertThat(nicknameCookie.getValue()).isEqualTo(encodedNickname),
                () -> assertThat(nicknameCookie.getMaxAge()).isEqualTo(CookieUtils.DEFAULT_MAX_AGE),
                () -> assertThat(nicknameCookie.getSecure()).isTrue(),
                () -> assertThat(nicknameCookie.isHttpOnly()).isFalse()
        );

        assertAll(
                () -> assertThat(emailCookie.getName()).isEqualTo(JwtCookieConstant.DEVDEVDEV_MEMBER_EMAIL),
                () -> assertThat(emailCookie.getValue()).isEqualTo(email),
                () -> assertThat(emailCookie.getMaxAge()).isEqualTo(CookieUtils.DEFAULT_MAX_AGE),
                () -> assertThat(emailCookie.getSecure()).isTrue(),
                () -> assertThat(emailCookie.isHttpOnly()).isFalse()
        );

        assertAll(
                () -> assertThat(isAdmin.getName()).isEqualTo(JwtCookieConstant.DEVDEVDEV_MEMBER_IS_ADMIN),
                () -> assertThat(isAdmin.getValue()).isEqualTo(expectedIsAdmin),
                () -> assertThat(isAdmin.getMaxAge()).isEqualTo(CookieUtils.DEFAULT_MAX_AGE),
                () -> assertThat(isAdmin.getSecure()).isTrue(),
                () -> assertThat(isAdmin.isHttpOnly()).isFalse()
        );
    }

    private SocialMemberDto createSocialDto(String userId, String name, String nickname, String password, String email,
                                            String socialType, String role) {
        return SocialMemberDto.builder()
                .userId(userId)
                .name(name)
                .nickname(nickname)
                .password(password)
                .email(email)
                .socialType(SocialType.valueOf(socialType))
                .role(Role.valueOf(role))
                .build();
    }
}