package com.dreamypatisiel.devdevdev.web.docs;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.enums.Role;
import com.dreamypatisiel.devdevdev.domain.entity.enums.SocialType;
import com.dreamypatisiel.devdevdev.domain.repository.member.MemberRepository;
import com.dreamypatisiel.devdevdev.global.constant.SecurityConstant;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.SocialMemberDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;

public class JwtExceptionDocsTest extends SupportControllerDocsTest {

    @Autowired
    MemberRepository memberRepository;

    @Test
    @DisplayName("잘못된 서명을 가진 토큰이면 예외가 발생한다.")
    void invalidSignatureAccessToken() throws Exception {
        // given
        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", adminEmail, socialType, userRole);

        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        // when // then
        ResultActions actions = mockMvc.perform(get("/devdevdev/api/v1/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(SecurityConstant.AUTHORIZATION_HEADER,
                                SecurityConstant.BEARER_PREFIX + invalidSignatureAccessToken))
                .andDo(print())
                .andExpect(status().isUnauthorized());

        // Docs
        actions.andDo(document("token-invalid-signature-accessToken-exception",
                preprocessResponse(prettyPrint()),
                responseFields(
                        fieldWithPath("resultType").type(JsonFieldType.STRING).description("응답 결과"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("에러 메시지"),
                        fieldWithPath("errorCode").type(JsonFieldType.NUMBER).description("에러 코드")
                ))
        );
    }

    @Test
    @DisplayName("만료된 토큰이면 예외가 발생한다.")
    void invalidExpiredAccessToken() throws Exception {
        // given
        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", adminEmail, socialType, userRole);

        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        // when // then
        ResultActions actions = mockMvc.perform(get("/devdevdev/api/v1/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(SecurityConstant.AUTHORIZATION_HEADER,
                                SecurityConstant.BEARER_PREFIX + invalidExpiredAccessToken))
                .andDo(print())
                .andExpect(status().isUnauthorized());

        // Docs
        actions.andDo(document("token-invalid-expired-accessToken-exception",
                preprocessResponse(prettyPrint()),
                responseFields(
                        fieldWithPath("resultType").type(JsonFieldType.STRING).description("응답 결과"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("에러 메시지"),
                        fieldWithPath("errorCode").type(JsonFieldType.NUMBER).description("에러 코드")
                ))
        );
    }

    @Test
    @DisplayName("헤더에 토큰이 빈값이면 예외가 발생한다.")
    void blankedAccessToken() throws Exception {
        // given
        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", adminEmail, socialType, userRole);

        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        // when // then
        ResultActions actions = mockMvc.perform(get("/devdevdev/api/v1/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(SecurityConstant.AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + " "))
                .andDo(print())
                .andExpect(status().isUnauthorized());

        // Docs
        actions.andDo(document("token-invalid-blanked-accessToken-exception",
                preprocessResponse(prettyPrint()),
                responseFields(
                        fieldWithPath("resultType").type(JsonFieldType.STRING).description("응답 결과"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("에러 메시지"),
                        fieldWithPath("errorCode").type(JsonFieldType.NUMBER).description("에러 코드")
                ))
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
