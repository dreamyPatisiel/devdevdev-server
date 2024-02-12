package com.dreamypatisiel.devdevdev.global.security.oauth2.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;

import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.Role;
import com.dreamypatisiel.devdevdev.domain.entity.SocialType;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Email;
import com.dreamypatisiel.devdevdev.domain.repository.MemberRepository;
import com.dreamypatisiel.devdevdev.exception.MemberException;
import com.dreamypatisiel.devdevdev.global.security.jwt.model.JwtCookieConstant;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.KakaoMember;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.SocialMemberDto;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.UserPrincipal;
import jakarta.servlet.http.Cookie;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class OAuth2SuccessHandlerTest {

    @Autowired
    OAuth2SuccessHandler oAuth2SuccessHandler;
    @Autowired
    MemberRepository memberRepository;

    String userId = "userId";
    String email = "dreamy5patisiel@kakao.com";
    String password = "1234";
    SocialType socialType = SocialType.KAKAO;
    Role role = Role.ROLE_USER;

    @BeforeEach
    void simulateOAuth2Login() {
        Member member = createMember();

        // Authentication에 필요한 UserPrincipal를 생성
        Map<String, Object> attributes = new HashMap<>();
        Map<String, Object> kakaoAttributes = new HashMap<>();
        kakaoAttributes.put(KakaoMember.EMAIL, email);
        attributes.put(KakaoMember.KAKAO_ACCOUNT, kakaoAttributes);
        UserPrincipal userPrincipal = UserPrincipal.createByMemberAndAttributes(member, attributes);

        // OAuth2AuthenticationToken 생성
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
    }

    @Test
    @DisplayName("OAuth2.0 로그인 성공 시 데이터베이스에 회원이 없으면 예외가 발생한다.")
    public void onAuthenticationSuccessException() throws IOException {
        // given
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        // when // then
        assertThatThrownBy(() -> oAuth2SuccessHandler.onAuthenticationSuccess(request, response, authentication))
                .isInstanceOf(MemberException.class)
                .hasMessage(MemberException.INVALID_MEMBER_NOT_FOUND_MESSAGE);
    }

    @Test
    @DisplayName("OAuth2.0 로그인 성공 시"
            + " 토큰을 생성하고 토큰을 쿠키에 저장하고"
            + " 리다이렉트를 설정하고"
            + " 회원에 리프레시 토큰을 저장한다.")
    void onAuthenticationSuccess() throws IOException {
        // given
        Member member = createMember();
        memberRepository.save(member);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        oAuth2SuccessHandler.onAuthenticationSuccess(request, response, authentication);

        // then
        Member findMember = memberRepository.findMemberByEmailAndSocialType(new Email(email), socialType).get();
        Cookie accessCookie = response.getCookie(JwtCookieConstant.DEVDEVDEV_ACCESS_TOKEN);
        Cookie refreshCookie = response.getCookie(JwtCookieConstant.DEVDEVDEV_REFRESH_TOKEN);

        assertAll(
                () -> assertThat(accessCookie).isNotNull(),
                () -> assertThat(refreshCookie).isNotNull()
        );
        assertAll(
                () -> assertThat(accessCookie.isHttpOnly()).isFalse(),
                () -> assertThat(accessCookie.getSecure()).isFalse(),
                () -> assertThat(refreshCookie.isHttpOnly()).isTrue(),
                () -> assertThat(refreshCookie.getSecure()).isFalse()
        );
        assertAll(
                () -> assertThat(response.getRedirectedUrl()).isNotEmpty(),
                () -> assertThat(findMember.getRefreshToken()).isNotEmpty()
        );
    }

    private Member createMember() {
        SocialMemberDto socialMemberDto = createSocialMemberDto(userId, email, password, socialType, role);
        return Member.createMemberBy(socialMemberDto);
    }

    private static SocialMemberDto createSocialMemberDto(String userId, String email, String password,
                                                      SocialType socialType, Role role) {
        return SocialMemberDto.builder()
                .userId(userId)
                .name(userId)
                .email(email)
                .nickname(userId)
                .password(password)
                .socialType(socialType)
                .role(role)
                .build();
    }

}