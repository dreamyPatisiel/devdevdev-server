package com.dreamypatisiel.devdevdev.web.controller;

import static com.dreamypatisiel.devdevdev.global.constant.SecurityConstant.*;
import static com.dreamypatisiel.devdevdev.global.security.jwt.model.JwtCookieConstant.DEVDEVDEV_REFRESH_TOKEN;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.Role;
import com.dreamypatisiel.devdevdev.domain.entity.SocialType;
import com.dreamypatisiel.devdevdev.domain.repository.MemberRepository;
import com.dreamypatisiel.devdevdev.global.common.TimeProvider;
import com.dreamypatisiel.devdevdev.global.constant.SecurityConstant;
import com.dreamypatisiel.devdevdev.global.security.jwt.model.Token;
import com.dreamypatisiel.devdevdev.global.security.jwt.service.JwtMemberService;
import com.dreamypatisiel.devdevdev.global.security.jwt.service.TokenService;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.SocialMemberDto;
import com.dreamypatisiel.devdevdev.global.utils.CookieUtils;
import jakarta.servlet.http.Cookie;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;

@Transactional
class LogoutControllerTest extends SupportControllerTest {

    @Autowired
    MemberRepository memberRepository;

    @Test
    @DisplayName("로그아웃을 하면 회원의 리프레시 토큰이 비활성화 상태로 변경되고"
            + " 리프레시 토큰 쿠키를 초기화 하여 리다이렉트 한다.")
    void logout() throws Exception {
        // given
        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        member.updateRefreshToken(refreshToken);
        memberRepository.save(member);

        Cookie cookie = new Cookie(DEVDEVDEV_REFRESH_TOKEN, refreshToken);

        // when
        ResultActions actions = mockMvc.perform(post(DEFAULT_PATH_V1 + "/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .cookie(cookie)
                        .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken))
                .andDo(print())
                .andExpect(status().is3xxRedirection());

        // then
        MockHttpServletResponse response = actions.andReturn().getResponse();
        Cookie responseCookie = response.getCookie(DEVDEVDEV_REFRESH_TOKEN);
        assertThat(responseCookie).isNotNull();
        assertAll(
                () -> assertThat(responseCookie.getValue()).isEqualTo(CookieUtils.BLANK),
                () -> assertThat(responseCookie.getPath()).isEqualTo(CookieUtils.DEFAULT_PATH),
                () -> assertThat(responseCookie.getMaxAge()).isEqualTo(CookieUtils.DEFAULT_MIN_AGE)
        );

        Member findMember = memberRepository.findById(member.getId()).get();
        assertThat(findMember.getRefreshToken()).isEqualTo(Token.DISABLED);
    }

    private SocialMemberDto createSocialDto(String userId, String name, String nickName, String password, String email, String socialType, String role) {
        return SocialMemberDto.builder()
                .userId(userId)
                .name(name)
                .nickName(nickName)
                .password(password)
                .email(email)
                .socialType(SocialType.valueOf(socialType))
                .role(Role.valueOf(role))
                .build();
    }
}