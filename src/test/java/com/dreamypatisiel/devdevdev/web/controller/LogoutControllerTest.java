package com.dreamypatisiel.devdevdev.web.controller;

import static com.dreamypatisiel.devdevdev.global.constant.SecurityConstant.AUTHORIZATION_HEADER;
import static com.dreamypatisiel.devdevdev.global.constant.SecurityConstant.BEARER_PREFIX;
import static com.dreamypatisiel.devdevdev.global.security.jwt.model.JwtCookieConstant.DEVDEVDEV_LOGIN_STATUS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.enums.Role;
import com.dreamypatisiel.devdevdev.domain.entity.enums.SocialType;
import com.dreamypatisiel.devdevdev.domain.repository.member.MemberRepository;
import com.dreamypatisiel.devdevdev.global.security.jwt.model.Token;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.SocialMemberDto;
import com.dreamypatisiel.devdevdev.global.utils.CookieUtils;
import com.dreamypatisiel.devdevdev.web.dto.response.ResultType;
import jakarta.servlet.http.Cookie;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.ResultActions;

class LogoutControllerTest extends SupportControllerTest {

    @Autowired
    MemberRepository memberRepository;

    @Test
    @DisplayName("로그아웃을 하면 회원의 리프레시 토큰이 비활성화 상태로 변경되고"
            + " 리프레시 토큰 쿠키를 초기화 하고"
            + " 로그인 활성화 유무 쿠키를 비활성화 한다.")
    void logout() throws Exception {
        // given
        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        member.updateRefreshToken(refreshToken);
        memberRepository.save(member);

        // when
        ResultActions actions = mockMvc.perform(post(DEFAULT_PATH_V1 + "/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultType").value(ResultType.SUCCESS.name()));

        // then
        MockHttpServletResponse response = actions.andReturn().getResponse();
        Cookie responseLoginStatusCookie = response.getCookie(DEVDEVDEV_LOGIN_STATUS);
        assertThat(responseLoginStatusCookie).isNotNull();
        assertAll(
                () -> assertThat(responseLoginStatusCookie.getValue()).isEqualTo(CookieUtils.INACTIVE),
                () -> assertThat(responseLoginStatusCookie.getPath()).isEqualTo(CookieUtils.DEFAULT_PATH),
                () -> assertThat(responseLoginStatusCookie.getMaxAge()).isEqualTo(CookieUtils.DEFAULT_MAX_AGE),
                () -> assertThat(responseLoginStatusCookie.getDomain()).isEqualTo(CookieUtils.DEVDEVDEV_DOMAIN)
        );

        Member findMember = memberRepository.findById(member.getId()).get();
        assertThat(findMember.getRefreshToken()).isEqualTo(Token.DISABLED);
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