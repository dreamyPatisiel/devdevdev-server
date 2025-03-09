package com.dreamypatisiel.devdevdev.web.docs;

import com.dreamypatisiel.devdevdev.domain.entity.Company;
import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.TechArticle;
import com.dreamypatisiel.devdevdev.domain.entity.TechComment;
import com.dreamypatisiel.devdevdev.domain.entity.TechCommentRecommend;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.CommentContents;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.CompanyName;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Count;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Title;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Url;
import com.dreamypatisiel.devdevdev.domain.entity.enums.Role;
import com.dreamypatisiel.devdevdev.domain.entity.enums.SocialType;
import static com.dreamypatisiel.devdevdev.domain.exception.MemberExceptionMessage.INVALID_MEMBER_NOT_FOUND_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.TechArticleExceptionMessage.NOT_FOUND_TECH_ARTICLE_MESSAGE;
import com.dreamypatisiel.devdevdev.domain.repository.CompanyRepository;
import com.dreamypatisiel.devdevdev.domain.repository.member.MemberRepository;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechArticleRepository;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechCommentRecommendRepository;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechCommentRepository;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechCommentSort;
import com.dreamypatisiel.devdevdev.global.constant.SecurityConstant;
import static com.dreamypatisiel.devdevdev.global.constant.SecurityConstant.AUTHORIZATION_HEADER;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.SocialMemberDto;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.UserPrincipal;
import static com.dreamypatisiel.devdevdev.web.docs.format.ApiDocsFormatGenerator.authenticationType;
import static com.dreamypatisiel.devdevdev.web.docs.format.ApiDocsFormatGenerator.techCommentSortType;
import com.dreamypatisiel.devdevdev.web.dto.request.techArticle.ModifyTechCommentRequest;
import com.dreamypatisiel.devdevdev.web.dto.request.techArticle.RegisterTechCommentRequest;
import com.dreamypatisiel.devdevdev.web.dto.response.ResultType;
import jakarta.persistence.EntityManager;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import org.springframework.restdocs.payload.JsonFieldType;
import static org.springframework.restdocs.payload.JsonFieldType.ARRAY;
import static org.springframework.restdocs.payload.JsonFieldType.BOOLEAN;
import static org.springframework.restdocs.payload.JsonFieldType.NUMBER;
import static org.springframework.restdocs.payload.JsonFieldType.OBJECT;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.test.web.servlet.ResultActions;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TechArticleCommentControllerDocsTest extends SupportControllerDocsTest {

    @Autowired
    TechArticleRepository techArticleRepository;

    @Autowired
    CompanyRepository companyRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    TechCommentRepository techCommentRepository;

    @Autowired
    TechCommentRecommendRepository techCommentRecommendRepository;

    @Autowired
    EntityManager em;

    @Test
    @DisplayName("익명 사용자는 기술블로그 댓글을 작성할 수 없다.")
    void registerTechCommentByAnonymous() throws Exception {
        // given
        Company company = createCompany("꿈빛 파티시엘", "https://example.png", "https://example.com", "https://example.com");
        companyRepository.save(company);

        TechArticle techArticle = TechArticle.createTechArticle(new Title("기술블로그 제목"), new Url("https://example.com"),
                new Count(1L),
                new Count(1L), new Count(1L), new Count(1L), null, company);
        techArticleRepository.save(techArticle);
        Long id = techArticle.getId();

        RegisterTechCommentRequest registerTechCommentRequest = new RegisterTechCommentRequest("댓글 내용입니다.");

        // when // then
        ResultActions actions = mockMvc.perform(
                        post("/devdevdev/api/v1/articles/{techArticleId}/comments", id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding(StandardCharsets.UTF_8)
                                .content(om.writeValueAsString(registerTechCommentRequest)))
                .andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.resultType").value(ResultType.FAIL.name()))
                .andExpect(jsonPath("$.message").isString())
                .andExpect(jsonPath("$.errorCode").value(HttpStatus.FORBIDDEN.value()));

        // Docs
        actions.andDo(document("tech-article-comments-anonymous-exception",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                pathParameters(
                        parameterWithName("techArticleId").description("기술블로그 아이디")
                ),
                responseFields(
                        fieldWithPath("resultType").type(JsonFieldType.STRING).description("응답 결과"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("에러 메시지"),
                        fieldWithPath("errorCode").type(JsonFieldType.NUMBER).description("에러 코드")
                )
        ));
    }

    @Test
    @DisplayName("회원은 기술블로그 댓글을 작성할 수 있다.")
    void registerTechComment() throws Exception {
        // given
        Company company = createCompany("꿈빛 파티시엘", "https://example.png", "https://example.com", "https://example.com");
        companyRepository.save(company);

        TechArticle techArticle = TechArticle.createTechArticle(new Title("기술블로그 제목"), new Url("https://example.com"),
                new Count(1L),
                new Count(1L),
                new Count(1L),
                new Count(1L), null, company);
        techArticleRepository.save(techArticle);
        Long id = techArticle.getId();

        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        member.updateRefreshToken(refreshToken);
        memberRepository.save(member);

        RegisterTechCommentRequest registerTechCommentRequest = new RegisterTechCommentRequest("댓글 내용입니다.");

        // when // then
        ResultActions actions = mockMvc.perform(
                        post("/devdevdev/api/v1/articles/{techArticleId}/comments", id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding(StandardCharsets.UTF_8)
                                .header(SecurityConstant.AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                                .content(om.writeValueAsString(registerTechCommentRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultType").value(ResultType.SUCCESS.name()))
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andExpect(jsonPath("$.data").isMap())
                .andExpect(jsonPath("$.data.techCommentId").isNumber());

        // Docs
        actions.andDo(document("tech-article-comments",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                        headerWithName(AUTHORIZATION_HEADER).description("Bearer 엑세스 토큰")
                ),
                pathParameters(
                        parameterWithName("techArticleId").description("기술블로그 아이디")
                ),
                requestFields(
                        fieldWithPath("contents").type(STRING).description("기술블로그 댓글 내용(최소 1자 이상 최대 1,000자 이하)")
                ),
                responseFields(
                        fieldWithPath("resultType").type(JsonFieldType.STRING).description("응답 결과"),
                        fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),
                        fieldWithPath("data.techCommentId").type(JsonFieldType.NUMBER).description("기술블로그 댓글 아이디")
                )
        ));
    }

    @Test
    @DisplayName("회원이 기술블로그 댓글을 작성할 때 존재하지 않는 기술블로그라면 예외가 발생한다.")
    void registerTechCommentNotFoundTechArticleException() throws Exception {
        // given
        Company company = createCompany("꿈빛 파티시엘", "https://example.png", "https://example.com", "https://example.com");
        companyRepository.save(company);

        TechArticle techArticle = TechArticle.createTechArticle(new Title("기술블로그 제목"), new Url("https://example.com"),
                new Count(1L),
                new Count(1L),
                new Count(1L),
                new Count(1L), null, company);
        techArticleRepository.save(techArticle);
        Long id = techArticle.getId() + 1;

        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        member.updateRefreshToken(refreshToken);
        memberRepository.save(member);

        RegisterTechCommentRequest registerTechCommentRequest = new RegisterTechCommentRequest("댓글 내용입니다.");

        // when // then
        ResultActions actions = mockMvc.perform(
                        post("/devdevdev/api/v1/articles/{techArticleId}/comments", id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding(StandardCharsets.UTF_8)
                                .header(SecurityConstant.AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                                .content(om.writeValueAsString(registerTechCommentRequest)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultType").value(ResultType.FAIL.name()))
                .andExpect(jsonPath("$.message").value(NOT_FOUND_TECH_ARTICLE_MESSAGE))
                .andExpect(jsonPath("$.errorCode").value(HttpStatus.NOT_FOUND.value()));

        // Docs
        actions.andDo(document("tech-article-comments-not-found-tech-article-exception",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                pathParameters(
                        parameterWithName("techArticleId").description("기술블로그 아이디")
                ),
                responseFields(
                        fieldWithPath("resultType").type(JsonFieldType.STRING).description("응답 결과"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("에러 메시지"),
                        fieldWithPath("errorCode").type(JsonFieldType.NUMBER).description("에러 코드")
                )
        ));
    }

    @Test
    @DisplayName("회원이 기술블로그 댓글을 작성할 때 존재하지 않는 회원이라면 예외가 발생한다.")
    void registerTechCommentNotFoundMemberException() throws Exception {
        // given
        Company company = createCompany("꿈빛 파티시엘", "https://example.png", "https://example.com", "https://example.com");
        companyRepository.save(company);

        TechArticle techArticle = TechArticle.createTechArticle(new Title("기술블로그 제목"), new Url("https://example.com"),
                new Count(1L),
                new Count(1L),
                new Count(1L),
                new Count(1L), null, company);
        techArticleRepository.save(techArticle);
        Long id = techArticle.getId();

        RegisterTechCommentRequest registerTechCommentRequest = new RegisterTechCommentRequest("댓글 내용입니다.");

        // when // then
        ResultActions actions = mockMvc.perform(
                        post("/devdevdev/api/v1/articles/{techArticleId}/comments", id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding(StandardCharsets.UTF_8)
                                .header(SecurityConstant.AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                                .content(om.writeValueAsString(registerTechCommentRequest)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultType").value(ResultType.FAIL.name()))
                .andExpect(jsonPath("$.message").value(INVALID_MEMBER_NOT_FOUND_MESSAGE))
                .andExpect(jsonPath("$.errorCode").value(HttpStatus.NOT_FOUND.value()));

        // Docs
        actions.andDo(document("tech-article-comments-not-found-member-exception",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                pathParameters(
                        parameterWithName("techArticleId").description("기술블로그 아이디")
                ),
                responseFields(
                        fieldWithPath("resultType").type(JsonFieldType.STRING).description("응답 결과"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("에러 메시지"),
                        fieldWithPath("errorCode").type(JsonFieldType.NUMBER).description("에러 코드")
                )
        ));
    }


    @ParameterizedTest
    @EmptySource
    @DisplayName("회원이 기술블로그 댓글을 작성할 때 댓글 내용이 공백이라면 예외가 발생한다.")
    void registerTechCommentContentsIsNullException(String contents) throws Exception {
        // given
        Company company = createCompany("꿈빛 파티시엘", "https://example.png", "https://example.com", "https://example.com");
        companyRepository.save(company);

        TechArticle techArticle = TechArticle.createTechArticle(new Title("기술블로그 제목"), new Url("https://example.com"),
                new Count(1L), new Count(1L), new Count(1L), new Count(1L), null, company);
        TechArticle savedTechArticle = techArticleRepository.save(techArticle);
        Long techArticleId = savedTechArticle.getId();

        RegisterTechCommentRequest registerTechCommentRequest = new RegisterTechCommentRequest(contents);

        // when // then
        ResultActions actions = mockMvc.perform(
                        post("/devdevdev/api/v1/articles/{techArticleId}/comments", techArticleId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding(StandardCharsets.UTF_8)
                                .header(AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                                .content(om.writeValueAsString(registerTechCommentRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultType").value(ResultType.FAIL.name()))
                .andExpect(jsonPath("$.message").isString())
                .andExpect(jsonPath("$.errorCode").value(HttpStatus.BAD_REQUEST.value()));

        // Docs
        actions.andDo(document("tech-article-comments-null-exception",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                pathParameters(
                        parameterWithName("techArticleId").description("기술블로그 아이디")
                ),
                responseFields(
                        fieldWithPath("resultType").type(JsonFieldType.STRING).description("응답 결과"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("에러 메시지"),
                        fieldWithPath("errorCode").type(JsonFieldType.NUMBER).description("에러 코드")
                )
        ));
    }

    @Test
    @DisplayName("회원은 기술블로그 댓글을 수정할 수 있다.")
    void modifyTechComment() throws Exception {
        // given
        Company company = createCompany("꿈빛 파티시엘", "https://example.png", "https://example.com", "https://example.com");
        companyRepository.save(company);

        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        member.updateRefreshToken(refreshToken);
        memberRepository.save(member);

        TechArticle techArticle = TechArticle.createTechArticle(new Title("기술블로그 제목"), new Url("https://example.com"),
                new Count(1L),
                new Count(1L), new Count(1L), new Count(1L), null, company);
        techArticleRepository.save(techArticle);
        Long techArticleId = techArticle.getId();

        TechComment techComment = TechComment.createMainTechComment(new CommentContents("댓글입니다"), member, techArticle);
        techCommentRepository.save(techComment);
        Long techCommentId = techComment.getId();

        ModifyTechCommentRequest modifyTechCommentRequest = new ModifyTechCommentRequest("댓글 수정입니다.");

        // when // then
        ResultActions actions = mockMvc.perform(
                        patch("/devdevdev/api/v1/articles/{techArticleId}/comments/{techCommentId}", techArticleId,
                                techCommentId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding(StandardCharsets.UTF_8)
                                .header(AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                                .content(om.writeValueAsString(modifyTechCommentRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultType").value(ResultType.SUCCESS.name()))
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andExpect(jsonPath("$.data").isMap())
                .andExpect(jsonPath("$.data.techCommentId").isNumber());

        // Docs
        actions.andDo(document("modify-tech-article-comments",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                        headerWithName(AUTHORIZATION_HEADER).description("Bearer 엑세스 토큰")
                ),
                pathParameters(
                        parameterWithName("techArticleId").description("기술블로그 아이디"),
                        parameterWithName("techCommentId").description("기술블로그 댓글 아이디")
                ),
                requestFields(
                        fieldWithPath("contents").type(STRING).description("기술블로그 댓글 내용(최소 1자 이상 최대 1,000자 이하)")
                ),
                responseFields(
                        fieldWithPath("resultType").type(JsonFieldType.STRING).description("응답 결과"),
                        fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),
                        fieldWithPath("data.techCommentId").type(JsonFieldType.NUMBER).description("기술블로그 댓글 아이디")
                )
        ));
    }

    @ParameterizedTest
    @EmptySource
    @DisplayName("회원이 기술블로그 댓글을 수정할 때 댓글 내용이 공백이라면 예외가 발생한다.")
    void modifyTechCommentContentsIsNullException(String contents) throws Exception {
        // given
        Company company = createCompany("꿈빛 파티시엘", "https://example.png", "https://example.com", "https://example.com");
        companyRepository.save(company);

        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        member.updateRefreshToken(refreshToken);
        memberRepository.save(member);

        TechArticle techArticle = TechArticle.createTechArticle(new Title("기술블로그 제목"), new Url("https://example.com"),
                new Count(1L),
                new Count(1L), new Count(1L), new Count(1L), null, company);
        techArticleRepository.save(techArticle);
        Long techArticleId = techArticle.getId();

        TechComment techComment = TechComment.createMainTechComment(new CommentContents("댓글입니다"), member, techArticle);
        techCommentRepository.save(techComment);
        Long techCommentId = techComment.getId();

        ModifyTechCommentRequest modifyTechCommentRequest = new ModifyTechCommentRequest(contents);

        // when // then
        ResultActions actions = mockMvc.perform(
                        patch("/devdevdev/api/v1/articles/{techArticleId}/comments/{techCommentId}", techArticleId,
                                techCommentId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding(StandardCharsets.UTF_8)
                                .header(AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                                .content(om.writeValueAsString(modifyTechCommentRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultType").value(ResultType.FAIL.name()))
                .andExpect(jsonPath("$.message").isString())
                .andExpect(jsonPath("$.errorCode").value(HttpStatus.BAD_REQUEST.value()));

        // Docs
        actions.andDo(document("modify-tech-article-comments-null-exception",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                pathParameters(
                        parameterWithName("techArticleId").description("기술블로그 아이디"),
                        parameterWithName("techCommentId").description("기술블로그 댓글 아이디")
                ),
                responseFields(
                        fieldWithPath("resultType").type(JsonFieldType.STRING).description("응답 결과"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("에러 메시지"),
                        fieldWithPath("errorCode").type(JsonFieldType.NUMBER).description("에러 코드")
                )
        ));
    }

    @Test
    @DisplayName("회원이 기술블로그 댓글을 수정할 때 댓글이 존재하지 않으면 예외가 발생한다.")
    void modifyTechCommentNotFoundException() throws Exception {
        // given
        Company company = createCompany("꿈빛 파티시엘", "https://example.png", "https://example.com", "https://example.com");
        companyRepository.save(company);

        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        member.updateRefreshToken(refreshToken);
        memberRepository.save(member);

        TechArticle techArticle = TechArticle.createTechArticle(new Title("기술블로그 제목"), new Url("https://example.com"),
                new Count(1L),
                new Count(1L), new Count(1L), new Count(1L), null, company);
        techArticleRepository.save(techArticle);
        Long techArticleId = techArticle.getId();

        ModifyTechCommentRequest modifyTechCommentRequest = new ModifyTechCommentRequest("댓글 수정입니다");

        // when // then
        ResultActions actions = mockMvc.perform(
                        patch("/devdevdev/api/v1/articles/{techArticleId}/comments/{techCommentId}", techArticleId, 0L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding(StandardCharsets.UTF_8)
                                .header(AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                                .content(om.writeValueAsString(modifyTechCommentRequest)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultType").value(ResultType.FAIL.name()))
                .andExpect(jsonPath("$.message").isString())
                .andExpect(jsonPath("$.errorCode").value(HttpStatus.NOT_FOUND.value()));

        // Docs
        actions.andDo(document("modify-tech-article-comments-not-found-exception",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                pathParameters(
                        parameterWithName("techArticleId").description("기술블로그 아이디"),
                        parameterWithName("techCommentId").description("기술블로그 댓글 아이디")
                ),
                responseFields(
                        fieldWithPath("resultType").type(JsonFieldType.STRING).description("응답 결과"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("에러 메시지"),
                        fieldWithPath("errorCode").type(JsonFieldType.NUMBER).description("에러 코드")
                )
        ));
    }

    @Test
    @DisplayName("회원은 본인이 작성한 기술블로그 댓글을 삭제할 수 있다.")
    void deleteTechComment() throws Exception {
        // given
        Company company = createCompany("꿈빛 파티시엘", "https://example.png", "https://example.com", "https://example.com");
        companyRepository.save(company);

        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        member.updateRefreshToken(refreshToken);
        memberRepository.save(member);

        TechArticle techArticle = TechArticle.createTechArticle(new Title("기술블로그 제목"), new Url("https://example.com"),
                new Count(1L),
                new Count(1L), new Count(1L), new Count(1L), null, company);
        techArticleRepository.save(techArticle);
        Long techArticleId = techArticle.getId();

        TechComment techComment = TechComment.createMainTechComment(new CommentContents("댓글입니다"), member, techArticle);
        techCommentRepository.save(techComment);
        Long techCommentId = techComment.getId();

        // when // then
        ResultActions actions = mockMvc.perform(
                        delete("/devdevdev/api/v1/articles/{techArticleId}/comments/{techCommentId}", techArticleId,
                                techCommentId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding(StandardCharsets.UTF_8)
                                .header(AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultType").value(ResultType.SUCCESS.name()))
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andExpect(jsonPath("$.data").isMap())
                .andExpect(jsonPath("$.data.techCommentId").isNumber());

        // Docs
        actions.andDo(document("delete-tech-article-comments",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                        headerWithName(AUTHORIZATION_HEADER).description("Bearer 엑세스 토큰")
                ),
                pathParameters(
                        parameterWithName("techArticleId").description("기술블로그 아이디"),
                        parameterWithName("techCommentId").description("기술블로그 댓글 아이디")
                ),
                responseFields(
                        fieldWithPath("resultType").type(JsonFieldType.STRING).description("응답 결과"),
                        fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),
                        fieldWithPath("data.techCommentId").type(JsonFieldType.NUMBER).description("기술블로그 댓글 아이디")
                )
        ));
    }

    @Test
    @DisplayName("회원이 기술블로그 댓글을 삭제할 때 댓글이 존재하지 않으면 예외가 발생한다.")
    void deleteTechCommentNotFoundException() throws Exception {
        // given
        Company company = createCompany("꿈빛 파티시엘", "https://example.png", "https://example.com", "https://example.com");
        companyRepository.save(company);

        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        member.updateRefreshToken(refreshToken);
        memberRepository.save(member);

        TechArticle techArticle = TechArticle.createTechArticle(new Title("기술블로그 제목"), new Url("https://example.com"),
                new Count(1L),
                new Count(1L), new Count(1L), new Count(1L), null, company);
        techArticleRepository.save(techArticle);
        Long techArticleId = techArticle.getId();

        TechComment techComment = TechComment.createMainTechComment(new CommentContents("댓글입니다"), member, techArticle);
        techCommentRepository.save(techComment);

        // when // then
        ResultActions actions = mockMvc.perform(
                        delete("/devdevdev/api/v1/articles/{techArticleId}/comments/{techCommentId}", techArticleId, 0L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding(StandardCharsets.UTF_8)
                                .header(AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultType").value(ResultType.FAIL.name()))
                .andExpect(jsonPath("$.message").isString())
                .andExpect(jsonPath("$.errorCode").value(HttpStatus.NOT_FOUND.value()));

        // Docs
        actions.andDo(document("delete-tech-article-comments-not-found-exception",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                pathParameters(
                        parameterWithName("techArticleId").description("기술블로그 아이디"),
                        parameterWithName("techCommentId").description("기술블로그 댓글 아이디")
                ),
                responseFields(
                        fieldWithPath("resultType").type(JsonFieldType.STRING).description("응답 결과"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("에러 메시지"),
                        fieldWithPath("errorCode").type(JsonFieldType.NUMBER).description("에러 코드")
                )
        ));
    }

    @Test
    @DisplayName("회원은 기술블로그 댓글에 답글을 작성할 수 있다.")
    void registerTechReply() throws Exception {
        // given
        Company company = createCompany("꿈빛 파티시엘", "https://example.png", "https://example.com", "https://example.com");
        companyRepository.save(company);

        TechArticle techArticle = TechArticle.createTechArticle(new Title("기술블로그 제목"), new Url("https://example.com"),
                new Count(1L),
                new Count(1L), new Count(1L), new Count(1L), null, company);
        techArticleRepository.save(techArticle);
        Long techArticleId = techArticle.getId();

        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        member.updateRefreshToken(refreshToken);
        memberRepository.save(member);

        TechComment originParentTechComment = TechComment.createMainTechComment(new CommentContents("댓글입니다."), member,
                techArticle);
        techCommentRepository.save(originParentTechComment);
        Long originParentTechCommentId = originParentTechComment.getId();

        TechComment parentTechComment = TechComment.createMainTechComment(new CommentContents("답글입니다."), member,
                techArticle);
        techCommentRepository.save(parentTechComment);
        Long parentTechCommentId = parentTechComment.getId();

        RegisterTechCommentRequest registerRepliedTechCommentRequest = new RegisterTechCommentRequest("답글에 대한 답글입니다.");

        // when // then
        ResultActions actions = mockMvc.perform(
                        post("/devdevdev/api/v1/articles/{techArticleId}/comments/{originParentTechCommentId}/{parentTechCommentId}",
                                techArticleId, originParentTechCommentId, parentTechCommentId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding(StandardCharsets.UTF_8)
                                .header(AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                                .content(om.writeValueAsString(registerRepliedTechCommentRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultType").value(ResultType.SUCCESS.name()))
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andExpect(jsonPath("$.data").isMap())
                .andExpect(jsonPath("$.data.techCommentId").isNumber());

        // Docs
        actions.andDo(document("register-tech-article-reply",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                        headerWithName(AUTHORIZATION_HEADER).description("Bearer 엑세스 토큰")
                ),
                pathParameters(
                        parameterWithName("techArticleId").description("기술블로그 아이디"),
                        parameterWithName("originParentTechCommentId").description("기술블로그 최상단 댓글 아이디"),
                        parameterWithName("parentTechCommentId").description("기술블로그 답글 대상의 댓글 아이디")
                ),
                requestFields(
                        fieldWithPath("contents").type(STRING).description("기술블로그 댓글 내용(최소 1자 이상 최대 1,000자 이하)")
                ),
                responseFields(
                        fieldWithPath("resultType").type(JsonFieldType.STRING).description("응답 결과"),
                        fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),
                        fieldWithPath("data.techCommentId").type(JsonFieldType.NUMBER).description("기술블로그 댓글 아이디")
                )
        ));
    }

    @ParameterizedTest
    @EmptySource
    @DisplayName("회원이 기술블로그 댓글에 답글을 작성할 때 답글 내용이 공백이라면 예외가 발생한다.")
    void registerTechReplyContentsIsNullException(String contents) throws Exception {
        // given
        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        member.updateRefreshToken(refreshToken);
        memberRepository.save(member);

        Company company = createCompany("꿈빛 파티시엘", "https://example.png", "https://example.com", "https://example.com");
        companyRepository.save(company);

        TechArticle techArticle = TechArticle.createTechArticle(new Title("기술블로그 제목"), new Url("https://example.com"),
                new Count(1L),
                new Count(1L), new Count(1L), new Count(1L), null, company);
        TechArticle savedTechArticle = techArticleRepository.save(techArticle);
        Long techArticleId = savedTechArticle.getId();

        TechComment originParentTechComment = TechComment.createMainTechComment(new CommentContents("댓글입니다."), member,
                techArticle);
        techCommentRepository.save(originParentTechComment);
        Long originParentTechCommentId = originParentTechComment.getId();

        TechComment parentTechComment = TechComment.createMainTechComment(new CommentContents("답글입니다."), member,
                techArticle);
        techCommentRepository.save(parentTechComment);
        Long parentTechCommentId = parentTechComment.getId();

        // when // then
        ResultActions actions = mockMvc.perform(
                        post("/devdevdev/api/v1/articles/{techArticleId}/comments/{originParentTechCommentId}/{parentTechCommentId}",
                                techArticleId, originParentTechCommentId, parentTechCommentId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding(StandardCharsets.UTF_8)
                                .header(AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                                .content(om.writeValueAsString(contents)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultType").value(ResultType.FAIL.name()))
                .andExpect(jsonPath("$.message").isString())
                .andExpect(jsonPath("$.errorCode").value(HttpStatus.BAD_REQUEST.value()));

        // Docs
        actions.andDo(document("register-tech-article-reply-null-exception",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                pathParameters(
                        parameterWithName("techArticleId").description("기술블로그 아이디"),
                        parameterWithName("originParentTechCommentId").description("기술블로그 최상단 댓글 아이디"),
                        parameterWithName("parentTechCommentId").description("기술블로그 답글 대상의 댓글 아이디")
                ),
                responseFields(
                        fieldWithPath("resultType").type(JsonFieldType.STRING).description("응답 결과"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("에러 메시지"),
                        fieldWithPath("errorCode").type(JsonFieldType.NUMBER).description("에러 코드")
                )
        ));
    }

    @Test
    @DisplayName("기술블로그 댓글/답글을 정렬 조건에 따라서 조회한다.")
    void getTechComments() throws Exception {
        // given
        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Company company = createCompany("꿈빛 파티시엘", "https://example.png", "https://example.com", "https://example.com");
        companyRepository.save(company);

        TechArticle techArticle = TechArticle.createTechArticle(new Title("기술블로그 제목"), new Url("https://example.com"),
                new Count(1L),
                new Count(1L), new Count(12L), new Count(1L), null, company);
        techArticleRepository.save(techArticle);
        Long techArticleId = techArticle.getId();

        TechComment originParentTechComment1 = createMainTechComment(new CommentContents("최상위 댓글1"), member,
                techArticle, new Count(0L), new Count(0L), new Count(0L));
        TechComment originParentTechComment2 = createMainTechComment(new CommentContents("최상위 댓글2"), member,
                techArticle, new Count(0L), new Count(0L), new Count(0L));
        TechComment originParentTechComment3 = createMainTechComment(new CommentContents("최상위 댓글3"), member,
                techArticle, new Count(0L), new Count(0L), new Count(0L));
        TechComment originParentTechComment4 = createMainTechComment(new CommentContents("최상위 댓글4"), member,
                techArticle, new Count(0L), new Count(0L), new Count(0L));
        TechComment originParentTechComment5 = createMainTechComment(new CommentContents("최상위 댓글5"), member,
                techArticle, new Count(0L), new Count(0L), new Count(0L));
        TechComment originParentTechComment6 = createMainTechComment(new CommentContents("최상위 댓글6"), member,
                techArticle, new Count(0L), new Count(0L), new Count(0L));

        TechComment parentTechComment1 = createRepliedTechComment(new CommentContents("최상위 댓글1의 답글1"), member,
                techArticle, originParentTechComment1, originParentTechComment1, new Count(0L), new Count(0L),
                new Count(0L));
        TechComment parentTechComment2 = createRepliedTechComment(new CommentContents("최상위 댓글1의 답글2"), member,
                techArticle, originParentTechComment1, originParentTechComment1, new Count(0L), new Count(0L),
                new Count(0L));
        TechComment parentTechComment3 = createRepliedTechComment(new CommentContents("최상위 댓글2의 답글1"), member,
                techArticle, originParentTechComment2, originParentTechComment2, new Count(0L), new Count(0L),
                new Count(0L));
        TechComment parentTechComment4 = createRepliedTechComment(new CommentContents("최상위 댓글2의 답글2"), member,
                techArticle, originParentTechComment2, originParentTechComment2, new Count(0L), new Count(0L),
                new Count(0L));

        TechComment techComment1 = createRepliedTechComment(new CommentContents("최상위 댓글1의 답글1의 답글"), member,
                techArticle, originParentTechComment1, parentTechComment1, new Count(0L), new Count(0L), new Count(0L));
        TechComment techComment2 = createRepliedTechComment(new CommentContents("최상위 댓글1의 답글2의 답글"), member,
                techArticle, originParentTechComment1, parentTechComment2, new Count(0L), new Count(0L), new Count(0L));
        TechComment techcomment1 = createRepliedTechComment(new CommentContents("최상위 댓글1의 답글1의 답글"), member,
                techArticle, originParentTechComment1, parentTechComment1, new Count(0L), new Count(0L), new Count(0L));
        TechComment techcomment2 = createRepliedTechComment(new CommentContents("최상위 댓글1의 답글2의 답글"), member,
                techArticle, originParentTechComment1, parentTechComment2, new Count(0L), new Count(0L), new Count(0L));

        techCommentRepository.saveAll(List.of(
                originParentTechComment1, originParentTechComment2, originParentTechComment3,
                originParentTechComment4, originParentTechComment5, originParentTechComment6,
                parentTechComment1, parentTechComment2, parentTechComment3, parentTechComment4,
                techComment1, techComment2
        ));

        TechCommentRecommend techCommentRecommend1 = TechCommentRecommend.create(originParentTechComment2, member);
        TechCommentRecommend techCommentRecommend2 = TechCommentRecommend.create(parentTechComment3, member);

        techCommentRecommendRepository.saveAll(List.of(techCommentRecommend1, techCommentRecommend2));

        em.flush();
        em.clear();

        Pageable pageable = PageRequest.of(0, 5);

        // when // then
        ResultActions actions = mockMvc.perform(
                        get("/devdevdev/api/v1/articles/{techArticleId}/comments", techArticleId)
                                .queryParam("techCommentId", originParentTechComment1.getId().toString())
                                .queryParam("size", String.valueOf(pageable.getPageSize()))
                                .queryParam("techCommentSort", TechCommentSort.OLDEST.name())
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                                .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultType").value(ResultType.SUCCESS.name()))
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.[0].techCommentId").isNumber())
                .andExpect(jsonPath("$.data.content.[0].createdAt").isString())
                .andExpect(jsonPath("$.data.content.[0].memberId").isNumber())
                .andExpect(jsonPath("$.data.content.[0].author").isString())
                .andExpect(jsonPath("$.data.content.[0].maskedEmail").isString())
                .andExpect(jsonPath("$.data.content.[0].contents").isString())
                .andExpect(jsonPath("$.data.content.[0].replyTotalCount").isNumber())
                .andExpect(jsonPath("$.data.content.[0].recommendTotalCount").isNumber())
                .andExpect(jsonPath("$.data.content.[0].isDeleted").isBoolean())
                .andExpect(jsonPath("$.data.content.[0].isRecommended").isBoolean())
                .andExpect(jsonPath("$.data.content.[0].replies.[0].techCommentId").isNumber())
                .andExpect(jsonPath("$.data.content.[0].replies.[0].memberId").isNumber())
                .andExpect(jsonPath("$.data.content.[0].replies.[0].techParentCommentId").isNumber())
                .andExpect(jsonPath("$.data.content.[0].replies.[0].techOriginParentCommentId").isNumber())
                .andExpect(jsonPath("$.data.content.[0].replies.[0].createdAt").isString())
                .andExpect(jsonPath("$.data.content.[0].replies.[0].author").isString())
                .andExpect(jsonPath("$.data.content.[0].replies.[0].maskedEmail").isString())
                .andExpect(jsonPath("$.data.content.[0].replies.[0].contents").isString())
                .andExpect(jsonPath("$.data.content.[0].replies.[0].techParentCommentMemberId").isNumber())
                .andExpect(jsonPath("$.data.content.[0].replies.[0].techParentCommentAuthor").isString())
                .andExpect(jsonPath("$.data.content.[0].replies.[0].recommendTotalCount").isNumber())
                .andExpect(jsonPath("$.data.content.[0].replies.[0].isDeleted").isBoolean())
                .andExpect(jsonPath("$.data.content.[0].replies.[0].isRecommended").isBoolean())
                .andExpect(jsonPath("$.data.pageable").isNotEmpty())
                .andExpect(jsonPath("$.data.pageable.pageNumber").isNumber())
                .andExpect(jsonPath("$.data.pageable.pageSize").isNumber())
                .andExpect(jsonPath("$.data.pageable.sort").isNotEmpty())
                .andExpect(jsonPath("$.data.pageable.sort.empty").isBoolean())
                .andExpect(jsonPath("$.data.pageable.sort.sorted").isBoolean())
                .andExpect(jsonPath("$.data.pageable.sort.unsorted").isBoolean())
                .andExpect(jsonPath("$.data.pageable.offset").isNumber())
                .andExpect(jsonPath("$.data.pageable.paged").isBoolean())
                .andExpect(jsonPath("$.data.pageable.unpaged").isBoolean())
                .andExpect(jsonPath("$.data.first").isBoolean())
                .andExpect(jsonPath("$.data.last").isBoolean())
                .andExpect(jsonPath("$.data.size").isNumber())
                .andExpect(jsonPath("$.data.number").isNumber())
                .andExpect(jsonPath("$.data.sort").isNotEmpty())
                .andExpect(jsonPath("$.data.sort.empty").isBoolean())
                .andExpect(jsonPath("$.data.sort.sorted").isBoolean())
                .andExpect(jsonPath("$.data.sort.unsorted").isBoolean())
                .andExpect(jsonPath("$.data.numberOfElements").isNumber())
                .andExpect(jsonPath("$.data.empty").isBoolean());

        // docs
        actions.andDo(document("get-tech-comments",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                        headerWithName(AUTHORIZATION_HEADER).optional().description("Bearer 엑세스 토큰")
                ),
                pathParameters(
                        parameterWithName("techArticleId").description("기술블로그 아이디")
                ),
                queryParameters(
                        parameterWithName("techCommentId").optional().description("기술블로그 댓글 아이디"),
                        parameterWithName("size").optional().description("조회 데이터 수"),
                        parameterWithName("techCommentSort").optional().description("기술블로그 댓글 정렬 조건")
                                .attributes(techCommentSortType())
                ),
                responseFields(
                        fieldWithPath("resultType").type(STRING).description("응답 결과"),
                        fieldWithPath("data").type(OBJECT).description("응답 데이터"),

                        fieldWithPath("data.content").type(ARRAY).description("기술블로그 댓글/답글 메인 배열"),
                        fieldWithPath("data.content[].techCommentId").type(NUMBER).description("기술블로그 댓글 아이디"),
                        fieldWithPath("data.content[].createdAt").type(STRING).description("기술블로그 댓글 작성일시"),
                        fieldWithPath("data.content[].memberId").type(NUMBER).description("기술블로그 댓글 작성자 아이디"),
                        fieldWithPath("data.content[].author").type(STRING).description("기술블로그 댓글 작성자 닉네임"),
                        fieldWithPath("data.content[].maskedEmail").type(STRING).description("기술블로그 댓글 작성자 이메일"),
                        fieldWithPath("data.content[].contents").type(STRING).description("기술블로그 댓글 내용"),
                        fieldWithPath("data.content[].isCommentAuthor").type(BOOLEAN)
                                .description("회원의 기술블로그 댓글 작성자 여부"),
                        fieldWithPath("data.content[].replyTotalCount").type(NUMBER)
                                .description("기술블로그 댓글의 답글 총 갯수"),
                        fieldWithPath("data.content[].recommendTotalCount").type(NUMBER)
                                .description("기술블로그 댓글 좋아요 총 갯수"),
                        fieldWithPath("data.content[].isDeleted").type(BOOLEAN)
                                .description("기술블로그 댓글 삭제 여부"),
                        fieldWithPath("data.content[].isModified").type(BOOLEAN)
                                .description("기술블로그 댓글 편집 여부"),
                        fieldWithPath("data.content[].isRecommended").type(BOOLEAN)
                                .description("기술블로그 댓글 좋아요 여부"),

                        fieldWithPath("data.content[].replies").type(ARRAY).description("기술블로그 답글 배열"),
                        fieldWithPath("data.content[].replies[].techCommentId").type(NUMBER)
                                .description("기술블로그 답글 아이디"),
                        fieldWithPath("data.content[].replies[].memberId").type(NUMBER).description("기술블로그 답글 작성자 아이디"),
                        fieldWithPath("data.content[].replies[].techParentCommentId").type(NUMBER)
                                .description("기술블로그 답글의 부모 댓글 아이디"),
                        fieldWithPath("data.content[].replies[].techOriginParentCommentId").type(NUMBER)
                                .description("기술블로그 답글의 최상위 부모 댓글 아이디"),
                        fieldWithPath("data.content[].replies[].createdAt").type(STRING).description("기술블로그 답글 작성일시"),
                        fieldWithPath("data.content[].replies[].techParentCommentMemberId").type(NUMBER)
                                .description("기술블로그 답글의 부모 댓글 작성자 아이디"),
                        fieldWithPath("data.content[].replies[].techParentCommentAuthor").type(STRING)
                                .description("기술블로그 답글의 부모 댓글 작성자 닉네임"),
                        fieldWithPath("data.content[].replies[].author").type(STRING).description("기술블로그 답글 작성자 닉네임"),
                        fieldWithPath("data.content[].replies[].isCommentAuthor").type(BOOLEAN)
                                .description("회원의 기술블로그 답글 작성자 여부"),
                        fieldWithPath("data.content[].replies[].maskedEmail").type(STRING)
                                .description("기술블로그 답글 작성자 이메일"),
                        fieldWithPath("data.content[].replies[].contents").type(STRING).description("기술블로그 답글 내용"),
                        fieldWithPath("data.content[].replies[].recommendTotalCount").type(NUMBER)
                                .description("기술블로그 답글 좋아요 총 갯수"),
                        fieldWithPath("data.content[].replies[].isDeleted").type(BOOLEAN)
                                .description("기술블로그 댓글 삭제 여부"),
                        fieldWithPath("data.content[].replies[].isModified").type(BOOLEAN)
                                .description("기술블로그 댓글 편집 여부"),
                        fieldWithPath("data.content[].replies[].isRecommended").type(BOOLEAN)
                                .description("기술블로그 댓글 좋아요 여부"),

                        fieldWithPath("data.pageable").type(OBJECT).description("기술블로그 메인 페이지네이션 정보"),
                        fieldWithPath("data.pageable.pageNumber").type(NUMBER).description("페이지 번호"),
                        fieldWithPath("data.pageable.pageSize").type(NUMBER).description("페이지 사이즈"),

                        fieldWithPath("data.pageable.sort").type(OBJECT).description("정렬 정보"),
                        fieldWithPath("data.pageable.sort.empty").type(BOOLEAN).description("정렬 정보가 비어있는지 여부"),
                        fieldWithPath("data.pageable.sort.sorted").type(BOOLEAN).description("정렬 여부"),
                        fieldWithPath("data.pageable.sort.unsorted").type(BOOLEAN).description("비정렬 여부"),

                        fieldWithPath("data.pageable.offset").type(NUMBER).description("페이지 오프셋 (페이지 크기 * 페이지 번호)"),
                        fieldWithPath("data.pageable.paged").type(BOOLEAN).description("페이지 정보 포함 여부"),
                        fieldWithPath("data.pageable.unpaged").type(BOOLEAN).description("페이지 정보 비포함 여부"),

                        fieldWithPath("data.totalElements").type(NUMBER).description("전체 댓글 수"),
                        fieldWithPath("data.totalOriginParentComments").type(NUMBER).description("삭제 되지 않은 최상위 댓글 수"),
                        fieldWithPath("data.first").type(BOOLEAN).description("현재 페이지가 첫 페이지 여부"),
                        fieldWithPath("data.last").type(BOOLEAN).description("현재 페이지가 마지막 페이지 여부"),
                        fieldWithPath("data.size").type(NUMBER).description("페이지 크기"),
                        fieldWithPath("data.number").type(NUMBER).description("현재 페이지"),

                        fieldWithPath("data.sort").type(OBJECT).description("정렬 정보"),
                        fieldWithPath("data.sort.empty").type(BOOLEAN).description("정렬 정보가 비어있는지 여부"),
                        fieldWithPath("data.sort.sorted").type(BOOLEAN).description("정렬 상태 여부"),
                        fieldWithPath("data.sort.unsorted").type(BOOLEAN).description("비정렬 상태 여부"),
                        fieldWithPath("data.numberOfElements").type(NUMBER).description("현재 페이지 데이터 수"),
                        fieldWithPath("data.empty").type(BOOLEAN).description("현재 빈 페이지 여부")
                )
        ));
    }

    @Test
    @DisplayName("기술블로그 댓글/답글을 추천한다.")
    void recommendTechComment() throws Exception {
        // given
        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        member.updateRefreshToken(refreshToken);
        memberRepository.save(member);

        Company company = createCompany("꿈빛 파티시엘", "https://example.png", "https://example.com", "https://example.com");
        companyRepository.save(company);

        TechArticle techArticle = TechArticle.createTechArticle(new Title("기술블로그 제목"), new Url("https://example.com"),
                new Count(1L),
                new Count(1L), new Count(1L), new Count(1L), null, company);
        techArticleRepository.save(techArticle);

        TechComment techComment = TechComment.createMainTechComment(new CommentContents("댓글입니다."), member, techArticle);
        techCommentRepository.save(techComment);

        // when // then
        ResultActions actions = mockMvc.perform(
                        post("/devdevdev/api/v1/articles/{techArticleId}/comments/{techCommentId}/recommends",
                                techArticle.getId(), techComment.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                                .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultType").value(ResultType.SUCCESS.name()))
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andExpect(jsonPath("$.data.isRecommended").isBoolean())
                .andExpect(jsonPath("$.data.recommendTotalCount").isNumber());

        // docs
        actions.andDo(document("recommend-tech-comment",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                        headerWithName(AUTHORIZATION_HEADER).description("Bearer 엑세스 토큰")
                ),
                pathParameters(
                        parameterWithName("techArticleId").description("기술블로그 아이디"),
                        parameterWithName("techCommentId").description("기술블로그 댓글/답글 아이디")
                ),
                responseFields(
                        fieldWithPath("resultType").type(STRING).description("응답 결과"),
                        fieldWithPath("data").type(OBJECT).description("응답 데이터").attributes(authenticationType()),
                        fieldWithPath("data.isRecommended").type(BOOLEAN).description("로그인한 회원의 기술블로그 댓글/답글 추천 여부")
                                .attributes(authenticationType()),
                        fieldWithPath("data.recommendTotalCount").type(NUMBER).description("기술블로그 댓글/답글 추천 총 갯수")
                                .attributes(authenticationType())
                )
        ));
    }

    @Test
    @DisplayName("존재하지 않는 기술블로그 댓글/답글을 추천하면 예외가 발생한다.")
    void recommendTechCommentNotFoundTechComment() throws Exception {
        // given
        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        member.updateRefreshToken(refreshToken);
        memberRepository.save(member);

        Company company = createCompany("꿈빛 파티시엘", "https://example.png", "https://example.com", "https://example.com");
        companyRepository.save(company);

        TechArticle techArticle = TechArticle.createTechArticle(new Title("기술블로그 제목"), new Url("https://example.com"),
                new Count(1L),
                new Count(1L), new Count(1L), new Count(1L), null, company);
        techArticleRepository.save(techArticle);

        TechComment techComment = TechComment.createMainTechComment(new CommentContents("댓글입니다."), member, techArticle);
        techCommentRepository.save(techComment);

        // when // then
        ResultActions actions = mockMvc.perform(
                        post("/devdevdev/api/v1/articles/{techArticleId}/comments/{techCommentId}/recommends",
                                techArticle.getId(), techComment.getId() + 1)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                                .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().isNotFound());

        // Docs
        actions.andDo(document("recommend-tech-comment-not-found-exception",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                pathParameters(
                        parameterWithName("techArticleId").description("기술블로그 아이디"),
                        parameterWithName("techCommentId").description("기술블로그 댓글/답글 아이디")
                ),
                responseFields(
                        fieldWithPath("resultType").type(JsonFieldType.STRING).description("응답 결과"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("에러 메시지"),
                        fieldWithPath("errorCode").type(JsonFieldType.NUMBER).description("에러 코드")
                )
        ));
    }

    @Test
    @DisplayName("회원이 기술블로그 베스트 댓글을 조회한다.")
    void getTechBestComments() throws Exception {
        // given
        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        SocialMemberDto socialMemberDto1 = createSocialDto("user1", "user1", "김민영", "password1", "alsdudr97@naver.com",
                socialType, role);
        SocialMemberDto socialMemberDto2 = createSocialDto("user2", "user2", "이임하", "password2", "wlgks555@naver.com",
                socialType, role);
        SocialMemberDto socialMemberDto3 = createSocialDto("user3", "user3", "문민주", "password3", "mmj9908@naver.com",
                socialType, role);
        Member member1 = Member.createMemberBy(socialMemberDto1);
        Member member2 = Member.createMemberBy(socialMemberDto2);
        Member member3 = Member.createMemberBy(socialMemberDto3);
        memberRepository.saveAll(List.of(member1, member2, member3));

        // 회사 생성
        Company company = createCompany("꿈빛 파티시엘", "https://example.png", "https://example.com", "https://example.com");
        companyRepository.save(company);

        // 기술 블로그 생성
        TechArticle techArticle = TechArticle.createTechArticle(new Title("기술블로그 제목"), new Url("https://example.com"),
                new Count(1L),
                new Count(1L), new Count(12L), new Count(1L), null, company);
        techArticleRepository.save(techArticle);

        // 댓글 생성
        TechComment originParentTechComment1 = createMainTechComment(new CommentContents("최상위 댓글1"), member1,
                techArticle, new Count(0L), new Count(3L), new Count(0L));
        originParentTechComment1.modifyCommentContents(new CommentContents("최상위 댓글1 수정"), LocalDateTime.now());
        TechComment originParentTechComment2 = createMainTechComment(new CommentContents("최상위 댓글1"), member2,
                techArticle, new Count(0L), new Count(2L), new Count(0L));
        TechComment originParentTechComment3 = createMainTechComment(new CommentContents("최상위 댓글1"), member3,
                techArticle, new Count(0L), new Count(1L), new Count(0L));
        techCommentRepository.saveAll(
                List.of(originParentTechComment1, originParentTechComment2, originParentTechComment3));

        // 추천 생성
        TechCommentRecommend techCommentRecommend = createTechCommentRecommend(true, originParentTechComment1, member1);
        techCommentRecommendRepository.save(techCommentRecommend);

        // 답글 생성
        TechComment repliedTechComment = createRepliedTechComment(new CommentContents("최상위 댓글1의 답글1"), member3,
                techArticle, originParentTechComment1, originParentTechComment1, new Count(0L), new Count(0L),
                new Count(0L));
        techCommentRepository.save(repliedTechComment);

        // when // then
        ResultActions actions = mockMvc.perform(get("/devdevdev/api/v1/articles/{techArticleId}/comments/best",
                        techArticle.getId())
                        .queryParam("size", "3")
                        .header(AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().isOk());

        // docs
        actions.andDo(document("get-tech-best-comments",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                        headerWithName(AUTHORIZATION_HEADER).optional().description("Bearer 엑세스 토큰")
                ),
                pathParameters(
                        parameterWithName("techArticleId").description("기술블로그 아이디")
                ),
                queryParameters(
                        parameterWithName("size").optional().description("조회되는 데이터 수(min=3, max=10)")
                ),
                responseFields(
                        fieldWithPath("resultType").type(STRING).description("응답 결과"),
                        fieldWithPath("datas").type(ARRAY).description("응답 데이터"),

                        fieldWithPath("datas.[].techCommentId").type(NUMBER).description("기술블로그 댓글 아이디"),
                        fieldWithPath("datas.[].createdAt").type(STRING).description("기술블로그 댓글 작성일시"),
                        fieldWithPath("datas.[].memberId").type(NUMBER).description("기술블로그 댓글 작성자 아이디"),
                        fieldWithPath("datas.[].author").type(STRING).description("기술블로그 댓글 작성자 닉네임"),
                        fieldWithPath("datas.[].isCommentAuthor").type(BOOLEAN)
                                .description("로그인한 회원이 댓글 작성자인지 여부"),
                        fieldWithPath("datas.[].isRecommended").type(BOOLEAN)
                                .description("로그인한 회원이 댓글 추천 여부"),
                        fieldWithPath("datas.[].maskedEmail").type(STRING).description("기술블로그 댓글 작성자 이메일"),
                        fieldWithPath("datas.[].contents").type(STRING).description("기술블로그 댓글 내용"),
                        fieldWithPath("datas.[].replyTotalCount").type(NUMBER)
                                .description("기술블로그 댓글의 답글 총 갯수"),
                        fieldWithPath("datas.[].recommendTotalCount").type(NUMBER)
                                .description("기술블로그 댓글 좋아요 총 갯수"),
                        fieldWithPath("datas.[].isDeleted").type(BOOLEAN)
                                .description("기술블로그 댓글 삭제 여부"),
                        fieldWithPath("datas.[].isModified").type(BOOLEAN)
                                .description("기술블로그 댓글 수정 여부"),

                        fieldWithPath("datas.[].replies").type(ARRAY).description("기술블로그 답글 배열"),
                        fieldWithPath("datas.[].replies[].techCommentId").type(NUMBER).description("기술블로그 답글 아이디"),
                        fieldWithPath("datas.[].replies[].memberId").type(NUMBER).description("기술블로그 답글 작성자 아이디"),
                        fieldWithPath("datas.[].replies[].techParentCommentId").type(NUMBER)
                                .description("기술블로그 답글의 부모 댓글 아이디"),
                        fieldWithPath("datas.[].replies[].techOriginParentCommentId").type(NUMBER)
                                .description("기술블로그 답글의 최상위 부모 댓글 아이디"),
                        fieldWithPath("datas.[].replies[].createdAt").type(STRING).description("기술블로그 답글 작성일시"),
                        fieldWithPath("datas.[].replies[].isCommentAuthor").type(BOOLEAN)
                                .description("로그인한 회원이 답글 작성자인지 여부"),
                        fieldWithPath("datas.[].replies[].isRecommended").type(BOOLEAN)
                                .description("로그인한 회원이 답글 추천 여부"),
                        fieldWithPath("datas.[].replies[].author").type(STRING).description("기술블로그 답글 작성자 닉네임"),
                        fieldWithPath("datas.[].replies[].maskedEmail").type(STRING)
                                .description("기술블로그 답글 작성자 이메일"),
                        fieldWithPath("datas.[].replies[].contents").type(STRING).description("기술블로그 답글 내용"),
                        fieldWithPath("datas.[].replies[].recommendTotalCount").type(NUMBER)
                                .description("기술블로그 답글 좋아요 총 갯수"),
                        fieldWithPath("datas.[].replies[].isDeleted").type(BOOLEAN)
                                .description("기술블로그 답글 삭제 여부"),
                        fieldWithPath("datas.[].replies[].isModified").type(BOOLEAN)
                                .description("기술블로그 답글 수정 여부"),
                        fieldWithPath("datas.[].replies[].techParentCommentMemberId").type(NUMBER)
                                .description("기술블로그 부모 댓글 작성자 아이디"),
                        fieldWithPath("datas.[].replies[].techParentCommentAuthor").type(STRING)
                                .description("기술블로그 부모 댓글 작성자 닉네임")
                )
        ));
    }

    private TechCommentRecommend createTechCommentRecommend(Boolean recommendedStatus, TechComment techComment,
                                                            Member member) {
        TechCommentRecommend techCommentRecommend = TechCommentRecommend.builder()
                .recommendedStatus(recommendedStatus)
                .techComment(techComment)
                .member(member)
                .build();

        techCommentRecommend.changeTechComment(techComment);

        return techCommentRecommend;
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

    private static Company createCompany(String companyName, String officialImageUrl, String officialUrl,
                                         String careerUrl) {
        return Company.builder()
                .name(new CompanyName(companyName))
                .officialImageUrl(new Url(officialImageUrl))
                .careerUrl(new Url(careerUrl))
                .officialUrl(new Url(officialUrl))
                .build();
    }

    private static TechComment createMainTechComment(CommentContents contents, Member createdBy,
                                                     TechArticle techArticle,
                                                     Count blameTotalCount, Count recommendTotalCount,
                                                     Count replyTotalCount) {
        return TechComment.builder()
                .contents(contents)
                .createdBy(createdBy)
                .techArticle(techArticle)
                .blameTotalCount(blameTotalCount)
                .recommendTotalCount(recommendTotalCount)
                .replyTotalCount(replyTotalCount)
                .build();
    }

    private static TechComment createRepliedTechComment(CommentContents contents, Member createdBy,
                                                        TechArticle techArticle,
                                                        TechComment originParent, TechComment parent,
                                                        Count blameTotalCount, Count recommendTotalCount,
                                                        Count replyTotalCount) {
        return TechComment.builder()
                .contents(contents)
                .createdBy(createdBy)
                .techArticle(techArticle)
                .blameTotalCount(blameTotalCount)
                .recommendTotalCount(recommendTotalCount)
                .replyTotalCount(replyTotalCount)
                .originParent(originParent)
                .parent(parent)
                .build();
    }
}
