package com.dreamypatisiel.devdevdev.web.docs;

import static com.dreamypatisiel.devdevdev.global.constant.SecurityConstant.AUTHORIZATION_HEADER;
import static com.dreamypatisiel.devdevdev.global.constant.SecurityConstant.BEARER_PREFIX;
import static com.dreamypatisiel.devdevdev.global.security.jwt.model.JwtCookieConstant.DEVDEVDEV_LOGIN_STATUS;
import static com.dreamypatisiel.devdevdev.global.security.jwt.model.JwtCookieConstant.DEVDEVDEV_REFRESH_TOKEN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.restdocs.cookies.CookieDocumentation.cookieWithName;
import static org.springframework.restdocs.cookies.CookieDocumentation.requestCookies;
import static org.springframework.restdocs.cookies.CookieDocumentation.responseCookies;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.Role;
import com.dreamypatisiel.devdevdev.domain.entity.SocialType;
import com.dreamypatisiel.devdevdev.domain.repository.MemberRepository;
import com.dreamypatisiel.devdevdev.global.security.jwt.model.Token;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.SocialMemberDto;
import com.dreamypatisiel.devdevdev.global.utils.CookieUtils;
import jakarta.servlet.http.Cookie;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class LogoutControllerDocsTest extends SupportControllerDocsTest {

    @Autowired
    MemberRepository memberRepository;

    @Test
    @DisplayName("로그아웃을 하면 회원의 리프레시 토큰이 비활성화 상태로 변경되고 리다이렉트 한다.")
    void logout() throws Exception {
        // given
        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        member.updateRefreshToken(refreshToken);
        memberRepository.save(member);

        Cookie refreshTokenCookie = new Cookie(DEVDEVDEV_REFRESH_TOKEN, refreshToken);

        // when // then
        ResultActions actions = mockMvc.perform(post(DEFAULT_PATH_V1 + "/logout")
                        .cookie(refreshTokenCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                )
                .andDo(print())
                .andExpect(status().isOk());

        // docs
        actions.andDo(document("logout",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                        headerWithName(AUTHORIZATION_HEADER).description("Bearer 엑세스 토큰")
                ),
                requestCookies(
                        cookieWithName(DEVDEVDEV_REFRESH_TOKEN).description("리프레시 토큰")
                ),
                responseCookies(
                        cookieWithName(DEVDEVDEV_REFRESH_TOKEN).description("리프레시 토큰"),
                        cookieWithName(DEVDEVDEV_LOGIN_STATUS).description("로그인 활성화 유뮤(active | inactive)")
                ),
                responseFields(
                        fieldWithPath("resultType").type(JsonFieldType.STRING).description("응답 결과"),
                        fieldWithPath("errorCode").type(JsonFieldType.NUMBER).description("에러 코드")
                )
        ));
    }

    @Test
    @DisplayName("로그아웃할 때 쿠키에 담긴 리프레시 토큰을 보유한 회원이 없으면 예외가 발생한다.")
    void logoutException() throws Exception {
        // given
        Cookie cookie = new Cookie(DEVDEVDEV_REFRESH_TOKEN, refreshToken);

        // when
        ResultActions actions = mockMvc.perform(post(DEFAULT_PATH_V1 + "/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .cookie(cookie)
                        .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken))
                .andDo(print())
                .andExpect(status().is4xxClientError());

        // docs
        actions.andDo(document("logout-exception",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                responseFields(
                        fieldWithPath("resultType").type(JsonFieldType.STRING).description("응답 결과"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("에러 메시지"),
                        fieldWithPath("errorCode").type(JsonFieldType.NUMBER).description("에러 코드")
                )
        ));
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
