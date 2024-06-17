package com.dreamypatisiel.devdevdev.web.docs;

import static com.dreamypatisiel.devdevdev.global.constant.SecurityConstant.AUTHORIZATION_HEADER;
import static com.dreamypatisiel.devdevdev.global.constant.SecurityConstant.BEARER_PREFIX;
import static com.dreamypatisiel.devdevdev.web.docs.format.ApiDocsFormatGenerator.authenticationType;
import static com.dreamypatisiel.devdevdev.web.docs.format.ApiDocsFormatGenerator.pickOptionImageNameType;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.JsonFieldType.ARRAY;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.enums.Role;
import com.dreamypatisiel.devdevdev.domain.entity.enums.SocialType;
import com.dreamypatisiel.devdevdev.domain.repository.member.MemberRepository;
import com.dreamypatisiel.devdevdev.elastic.domain.document.ElasticKeyword;
import com.dreamypatisiel.devdevdev.elastic.domain.repository.ElasticKeywordRepository;
import com.dreamypatisiel.devdevdev.elastic.domain.service.ElasticKeywordService;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.SocialMemberDto;
import com.dreamypatisiel.devdevdev.web.response.ResultType;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

class KeywordControllerDocsTest extends SupportControllerDocsTest {

    @Autowired
    ElasticKeywordService elasticKeywordService;
    @Autowired
    ElasticKeywordRepository elasticKeywordRepository;
    @Autowired
    MemberRepository memberRepository;

    @AfterEach
    void afterEach() {
        elasticKeywordRepository.deleteAll();
    }

    @Test
    @DisplayName("기술블로그 키워드를 검색하면 자동완성 키워드 후보 리스트를 최대 20개 반환한다.")
    void autocompleteKeyword() throws Exception {
        // given
        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        member.updateRefreshToken(refreshToken);
        memberRepository.save(member);

        ElasticKeyword keyword1 = ElasticKeyword.create(List.of("자바", "Java"));
        ElasticKeyword keyword2 = ElasticKeyword.create(List.of("자바스크립트", "JavaScript"));
        ElasticKeyword keyword3 = ElasticKeyword.create(List.of("자바가 최고야", "Java is the best"));
        ElasticKeyword keyword4 = ElasticKeyword.create(List.of("스프링", "Spring"));
        ElasticKeyword keyword5 = ElasticKeyword.create(List.of("스프링부트", "SpringBoot"));
        List<ElasticKeyword> elasticKeywords = List.of(keyword1, keyword2, keyword3, keyword4, keyword5);
        elasticKeywordRepository.saveAll(elasticKeywords);

        String prefix = "자";

        // when // then
        ResultActions actions = mockMvc.perform(get(DEFAULT_PATH_V1 + "/keywords/auto-complete")
                        .queryParam("prefix", prefix)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultType").value(ResultType.SUCCESS.name()))
                .andExpect(jsonPath("$.datas").isArray())
                .andExpect(jsonPath("$.datas").isNotEmpty());

        // docs
        actions.andDo(document("tech-article-keywords-autocomplete",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                        headerWithName(AUTHORIZATION_HEADER).description("Bearer 엑세스 토큰")
                ),
                queryParameters(
                        parameterWithName("prefix").description("검색 키워드").attributes(pickOptionImageNameType())
                ),
                responseFields(
                        fieldWithPath("resultType").type(STRING).description("응답 결과")
                                .attributes(authenticationType()),
                        fieldWithPath("datas").type(ARRAY).description("응답 데이터")
                                .attributes(authenticationType())
                )
        ));
    }

    private SocialMemberDto createSocialDto(String userId, String name, String nickName, String password, String email,
                                            String socialType, String role) {
        return SocialMemberDto.builder()
                .userId(userId)
                .name(name)
                .nickname(nickName)
                .password(password)
                .email(email)
                .socialType(SocialType.valueOf(socialType))
                .role(Role.valueOf(role))
                .build();
    }

}