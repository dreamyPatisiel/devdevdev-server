package com.dreamypatisiel.devdevdev.web.docs;

import static com.dreamypatisiel.devdevdev.global.security.jwt.model.JwtCookieConstant.DEVDEVDEV_ACCESS_TOKEN;
import static com.dreamypatisiel.devdevdev.global.security.jwt.model.JwtCookieConstant.DEVDEVDEV_REFRESH_TOKEN;
import static org.mockito.Mockito.when;

import static org.springframework.restdocs.cookies.CookieDocumentation.cookieWithName;
import static org.springframework.restdocs.cookies.CookieDocumentation.requestCookies;
import static org.springframework.restdocs.cookies.CookieDocumentation.responseCookies;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.enums.Role;
import com.dreamypatisiel.devdevdev.domain.entity.enums.SocialType;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Email;
import com.dreamypatisiel.devdevdev.domain.repository.MemberRepository;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.SocialMemberDto;
import com.dreamypatisiel.devdevdev.web.response.ResultType;
import jakarta.servlet.http.Cookie;
import java.util.Date;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;

public class TokenControllerDocsTest extends SupportControllerDocsTest {

    @Autowired
    MemberRepository memberRepository;

    @Test
    @DisplayName("토큰 재발급을 요청할 때"
            + " 쿠키에 리프레시 토큰을 담아서 요청하면"
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
        ResultActions actions = mockMvc.perform(post("/devdevdev/api/v1/token/refresh")
                        .cookie(cookie)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());

        // Docs
        actions.andDo(document("token-refresh",
                preprocessResponse(prettyPrint()),
                requestCookies(
                        cookieWithName("DEVDEVDEV_REFRESH_TOKEN").description("리프레시 토큰")
                ),
                responseCookies(
                        cookieWithName("DEVDEVDEV_REFRESH_TOKEN").description("리프레시 토큰"),
                        cookieWithName("DEVDEVDEV_ACCESS_TOKEN").description("엑세스 토큰"),
                        cookieWithName("DEVDEVDEV_LOGIN_STATUS").description("로그인 활성 유무(active | inactive)")
                ),
                responseFields(
                        fieldWithPath("resultType").type(JsonFieldType.STRING).description("응답 결과")
                ))
        );
    }

    @Test
    @DisplayName("토큰 재발급을 요청할 때 리프레시 토큰으로 회원을 찾을 수 없으면 예외가 발생한다.")
    void getRefreshTokenException() throws Exception {
        // given
        Cookie cookie = new Cookie(DEVDEVDEV_REFRESH_TOKEN, refreshToken);

        // when // then
        ResultActions actions = mockMvc.perform(post("/devdevdev/api/v1/token/refresh")
                        .cookie(cookie)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().is4xxClientError());

        // Docs
        actions.andDo(document("token-refresh-exception",
                preprocessResponse(prettyPrint()),
                responseFields(
                        fieldWithPath("resultType").type(JsonFieldType.STRING).description("응답 결과"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("에러 메시지"),
                        fieldWithPath("errorCode").type(JsonFieldType.NUMBER).description("에러 코드")
                ))
        );
    }

    @Test
    @DisplayName("요청에 리프레시 토큰 쿠키가 없으면 예외가 발생한다.")
    void getRefreshTokenCookieException() throws Exception {
        // given
        Cookie cookie = new Cookie(DEVDEVDEV_ACCESS_TOKEN, accessToken);

        // when // then
        ResultActions actions = mockMvc.perform(post("/devdevdev/api/v1/token/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(cookie))
                .andDo(print())
                .andExpect(status().isBadRequest());

        // Docs
        actions.andDo(document("token-refresh-cookie-exception",
                preprocessResponse(prettyPrint()),
                responseFields(
                        fieldWithPath("resultType").type(JsonFieldType.STRING).description("응답 결과"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("에러 메시지"),
                        fieldWithPath("errorCode").type(JsonFieldType.NUMBER).description("에러 코드")
                ))
        );
    }

    @Test
    @DisplayName("USER 테스트 계정의 토큰을 생성하고 해당 토큰을 응답한다.")
    void createUserToken() throws Exception {
        // given
        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", userEmail, socialType, userRole);

        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        // when // then
        Member findMember = memberRepository.findMemberByEmailAndSocialType(new Email(userEmail), SocialType.valueOf(socialType)).get();
        ResultActions actions = mockMvc.perform(get("/devdevdev/api/v1/token/test/user")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultType").value(ResultType.SUCCESS.name()))
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.refreshToken").exists())
                // 해당 테스트 계정의 리프레시 토큰이 갱신 되었는지 확인
                .andExpect(jsonPath("$.data.refreshToken").value(findMember.getRefreshToken()));

        // Docs
        actions.andDo(document("token-test-user",
                preprocessResponse(prettyPrint()),
                responseFields(
                        fieldWithPath("resultType").type(JsonFieldType.STRING).description("응답 결과"),
                        fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),
                        fieldWithPath("data.accessToken").type(JsonFieldType.STRING).description("엑세스 토큰"),
                        fieldWithPath("data.refreshToken").type(JsonFieldType.STRING).description("리프레시 토큰")
                ))
        );
    }

    @Test
    @DisplayName("USER 테스트 계정이 없으면 예외가 발생한다.")
    void createUserTokenException() throws Exception {
        // when // then
        ResultActions actions = mockMvc.perform(get("/devdevdev/api/v1/token/test/user")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().is4xxClientError());

        // Docs
        actions.andDo(document("token-test-user-exception",
                preprocessResponse(prettyPrint()),
                responseFields(
                        fieldWithPath("resultType").type(JsonFieldType.STRING).description("응답 결과"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("에러 메시지"),
                        fieldWithPath("errorCode").type(JsonFieldType.NUMBER).description("에러 코드")
                ))
        );
    }
    @Test
    @DisplayName("ADMIN 테스트 계정의 토큰을 생성하고 해당 토큰을 응답한다.")
    void createAdminToken() throws Exception {
        // given
        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", adminEmail, socialType, adminRole);

        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        // when // then
        Member findMember = memberRepository.findMemberByEmailAndSocialType(new Email(adminEmail), SocialType.valueOf(socialType)).get();
        ResultActions actions = mockMvc.perform(get("/devdevdev/api/v1/token/test/admin")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultType").value(ResultType.SUCCESS.name()))
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.refreshToken").exists())
                // 해당 테스트 계정의 리프레시 토큰이 갱신 되었는지 확인
                .andExpect(jsonPath("$.data.refreshToken").value(findMember.getRefreshToken()));

        // Docs
        actions.andDo(document("token-test-admin",
                preprocessResponse(prettyPrint()),
                responseFields(
                        fieldWithPath("resultType").type(JsonFieldType.STRING).description("응답 결과"),
                        fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),
                        fieldWithPath("data.accessToken").type(JsonFieldType.STRING).description("엑세스 토큰"),
                        fieldWithPath("data.refreshToken").type(JsonFieldType.STRING).description("리프레시 토큰")
                ))
        );
    }

    @Test
    @DisplayName("USER 테스트 계정이 없으면 예외가 발생한다.")
    void createAdminTokenException() throws Exception {
        // given // when // then
        ResultActions actions = mockMvc.perform(get("/devdevdev/api/v1/token/test/admin")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().is4xxClientError());

        // Docs
        actions.andDo(document("token-test-admin-exception",
                preprocessResponse(prettyPrint()),
                responseFields(
                        fieldWithPath("resultType").type(JsonFieldType.STRING).description("응답 결과"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("에러 메시지"),
                        fieldWithPath("errorCode").type(JsonFieldType.NUMBER).description("에러 코드")
                ))
        );
    }

    private SocialMemberDto createSocialDto(String userId, String name, String nickname, String password, String email, String socialType, String role) {
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
