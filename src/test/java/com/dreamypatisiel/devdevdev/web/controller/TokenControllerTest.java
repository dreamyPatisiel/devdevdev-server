package com.dreamypatisiel.devdevdev.web.controller;

import static com.dreamypatisiel.devdevdev.global.security.jwt.model.JwtCookieConstant.DEVDEVDEV_ACCESS_TOKEN;
import static com.dreamypatisiel.devdevdev.global.security.jwt.model.JwtCookieConstant.DEVDEVDEV_REFRESH_TOKEN;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.Role;
import com.dreamypatisiel.devdevdev.domain.entity.SocialType;
import com.dreamypatisiel.devdevdev.domain.repository.MemberRepository;
import com.dreamypatisiel.devdevdev.global.common.TimeProvider;
import com.dreamypatisiel.devdevdev.global.security.jwt.model.Token;
import com.dreamypatisiel.devdevdev.global.security.jwt.service.TokenService;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.SocialMemberDto;
import com.dreamypatisiel.devdevdev.web.response.ResultType;
import jakarta.servlet.http.Cookie;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import static org.assertj.core.api.Assertions.*;

@Transactional
class TokenControllerTest extends SupportControllerTest {

    @Autowired
    MemberRepository memberRepository;
    @Autowired
    TokenService tokenService;
    @MockBean
    TimeProvider timeProvider;

    String refreshToken;
    String email = "dreamy5patisiel@kakao.com";
    String socialType = SocialType.KAKAO.name();
    String role = Role.ROLE_USER.name();
    Date date = new Date();

    @BeforeEach
    void setupRefreshToken() {
        when(timeProvider.getDateNow()).thenReturn(date);

        Token token = tokenService.generateTokenBy(email, socialType, role);
        refreshToken = token.getRefreshToken();
    }

    @Test
    @DisplayName("요청에 쿠키에 리프레시 토큰을 담아서 요청하면"
            + " 토큰을 재발급하고 재발급한 토큰을 쿠키에 담아 응답을 준다.")
    void getRefreshToken() throws Exception {
        // given
        when(timeProvider.getDateNow()).thenReturn(new Date());

        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        member.updateRefreshToken(refreshToken);
        memberRepository.save(member);

        Cookie cookie = new Cookie(DEVDEVDEV_REFRESH_TOKEN, refreshToken);

        // when // then
        ResultActions actions = mockMvc.perform(get("/devdevdev/api/v1/token/refresh")
                        .cookie(cookie)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(cookie().exists(DEVDEVDEV_REFRESH_TOKEN))
                .andExpect(cookie().httpOnly(DEVDEVDEV_REFRESH_TOKEN, true))
                .andExpect(cookie().secure(DEVDEVDEV_REFRESH_TOKEN, false))
                .andExpect(cookie().exists(DEVDEVDEV_ACCESS_TOKEN))
                .andExpect(cookie().httpOnly(DEVDEVDEV_ACCESS_TOKEN, false))
                .andExpect(cookie().secure(DEVDEVDEV_ACCESS_TOKEN, false))
                .andExpect(jsonPath("$.resultType").value(ResultType.SUCCESS.name()))
                .andExpect(jsonPath("$.errorCode").value(0));

        // 쿠키에 있는 리프레시 토큰이 저장되었는지 검증
        Cookie refreshTokenCookie = actions.andReturn().getResponse().getCookie(DEVDEVDEV_REFRESH_TOKEN);
        String value = refreshTokenCookie.getValue();

        Member findMember = memberRepository.findById(member.getId()).get();
        assertThat(findMember.isRefreshTokenEquals(value)).isTrue();
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