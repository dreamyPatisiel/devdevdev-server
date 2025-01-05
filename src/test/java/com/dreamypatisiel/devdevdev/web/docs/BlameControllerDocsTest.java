package com.dreamypatisiel.devdevdev.web.docs;

import static com.dreamypatisiel.devdevdev.global.constant.SecurityConstant.AUTHORIZATION_HEADER;
import static com.dreamypatisiel.devdevdev.web.docs.format.ApiDocsFormatGenerator.blamePathType;
import static com.dreamypatisiel.devdevdev.web.docs.format.ApiDocsFormatGenerator.numberOrNull;
import static com.dreamypatisiel.devdevdev.web.docs.format.ApiDocsFormatGenerator.stringOrNull;
import static com.dreamypatisiel.devdevdev.web.dto.response.ResultType.SUCCESS;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.JsonFieldType.ARRAY;
import static org.springframework.restdocs.payload.JsonFieldType.NULL;
import static org.springframework.restdocs.payload.JsonFieldType.NUMBER;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dreamypatisiel.devdevdev.domain.entity.Blame;
import com.dreamypatisiel.devdevdev.domain.entity.BlameType;
import com.dreamypatisiel.devdevdev.domain.entity.Company;
import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.Pick;
import com.dreamypatisiel.devdevdev.domain.entity.PickComment;
import com.dreamypatisiel.devdevdev.domain.entity.TechArticle;
import com.dreamypatisiel.devdevdev.domain.entity.TechComment;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.CommentContents;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.CompanyName;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Count;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Title;
import com.dreamypatisiel.devdevdev.domain.entity.enums.ContentStatus;
import com.dreamypatisiel.devdevdev.domain.entity.enums.Role;
import com.dreamypatisiel.devdevdev.domain.entity.enums.SocialType;
import com.dreamypatisiel.devdevdev.domain.repository.BlameTypeRepository;
import com.dreamypatisiel.devdevdev.domain.repository.CompanyRepository;
import com.dreamypatisiel.devdevdev.domain.repository.blame.BlameRepository;
import com.dreamypatisiel.devdevdev.domain.repository.member.MemberRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickCommentRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickRepository;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechArticleRepository;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechCommentRepository;
import com.dreamypatisiel.devdevdev.global.constant.SecurityConstant;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.SocialMemberDto;
import com.dreamypatisiel.devdevdev.web.dto.request.common.BlamePathType;
import com.dreamypatisiel.devdevdev.web.dto.request.common.BlameRequest;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

public class BlameControllerDocsTest extends SupportControllerDocsTest {

    @Autowired
    BlameTypeRepository blameTypeRepository;
    @Autowired
    BlameRepository blameRepository;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    PickRepository pickRepository;
    @Autowired
    PickCommentRepository pickCommentRepository;
    @Autowired
    CompanyRepository companyRepository;
    @Autowired
    TechArticleRepository techArticleRepository;
    @Autowired
    TechCommentRepository techCommentRepository;

    @Test
    @DisplayName("회원이 신고 사유를 조회한다.")
    void getBlames() throws Exception {
        // given
        BlameType blameType = createBlameType("욕설1", 0);
        blameTypeRepository.save(blameType);

        // when // then
        ResultActions actions = mockMvc.perform(get("/devdevdev/api/v1/blames")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header(SecurityConstant.AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken))
                .andDo(print())
                .andExpect(status().isOk());

        // docs
        actions.andDo(document("get-blame",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                        headerWithName(AUTHORIZATION_HEADER).description("Bearer 엑세스 토큰")
                ),
                responseFields(
                        fieldWithPath("resultType").type(STRING).description("응답 결과"),
                        fieldWithPath("datas").type(ARRAY).description("응답 데이터"),
                        fieldWithPath("datas.[].id").type(NUMBER).description("신고 종류 아이디"),
                        fieldWithPath("datas.[].reason").type(STRING).description("신고 사유"),
                        fieldWithPath("datas.[].sortOrder").type(NUMBER).description("정렬 순서")
                )
        ));
    }

    @Test
    @DisplayName("회원이 아니면 신고사유를 조회할 때 예외가 발생한다.")
    void getBlamesAuthorizationException() throws Exception {
        // given
        BlameType blameType = createBlameType("욕설1", 0);
        blameTypeRepository.save(blameType);

        // when // then
        ResultActions actions = mockMvc.perform(get("/devdevdev/api/v1/blames")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().isUnauthorized());

        // docs
        actions.andDo(document("get-blame-authorization-exception",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                responseFields(
                        fieldWithPath("resultType").type(STRING).description("응답 결과"),
                        fieldWithPath("message").type(STRING).description("에러 메시지"),
                        fieldWithPath("errorCode").type(NUMBER).description("에러 코드")
                )
        ));
    }

    @Test
    @DisplayName("회원이 승인상태의 픽픽픽 게시글을 신고한다.")
    void blamePick() throws Exception {
        // given
        // 회원 생성
        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘", "꿈빛파티시엘", "1234", email,
                socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        // 픽픽픽 생성
        Pick pick = createPick(member, "픽픽픽", ContentStatus.APPROVAL, new Count(0L));
        pickRepository.save(pick);

        // 신고 종류 생성
        BlameType blameType = new BlameType("욕설1", 0);
        blameTypeRepository.save(blameType);

        BlameRequest blameRequest = createBlameRequestBy(pick.getId(), null, null, null, blameType.getId());

        // when // then
        ResultActions actions = mockMvc.perform(post("/devdevdev/api/v1/blames/{blamePathType}", BlamePathType.PICK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header(AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                        .content(om.writeValueAsString(blameRequest)))
                .andDo(print())
                .andExpect(status().isOk());

        // docs
        actions.andDo(document("blame-pick",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                        headerWithName(AUTHORIZATION_HEADER).description("Bearer 엑세스 토큰")
                ),
                pathParameters(
                        parameterWithName("blamePathType").description("댑댑댑 신고 경로 타입").attributes(blamePathType())
                ),
                requestFields(
                        fieldWithPath("pickId").type(NUMBER).optional().description("픽픽픽 아이디")
                                .attributes(numberOrNull()),
                        fieldWithPath("pickCommentId").type(NUMBER).optional().description("픽픽픽 댓글 아이디")
                                .attributes(numberOrNull()),
                        fieldWithPath("techArticleId").type(NUMBER).optional().description("기술 블로그 아이디")
                                .attributes(numberOrNull()),
                        fieldWithPath("techArticleCommentId").type(NUMBER).optional().description("기술 블로그 댓글 아이디")
                                .attributes(numberOrNull()),
                        fieldWithPath("blameTypeId").type(NUMBER).description("신고 사유 종류 아이디")
                                .attributes(numberOrNull()),
                        fieldWithPath("customReason").type(STRING).optional()
                                .description("신고 사유 직접 작성 값(신고 사유 종류가 기타일 때)")
                                .attributes(stringOrNull())
                ),
                responseFields(
                        fieldWithPath("resultType").type(STRING).description("응답 결과"),
                        fieldWithPath("data.blameId").type(NUMBER).description("신고 아이디")
                )
        ));
    }

    @Test
    @DisplayName("회원이 승인상태의 픽픽픽 댓글을 신고한다.")
    void blamePickComment() throws Exception {
        // given
        // 회원 생성
        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘", "꿈빛파티시엘", "1234", email,
                socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        // 픽픽픽 생성
        Pick pick = createPick(member, "픽픽픽", ContentStatus.APPROVAL, new Count(0L));
        pickRepository.save(pick);

        // 픽픽픽 댓글 생성
        PickComment pickComment = createPickComment(pick, member, "픽픽픽 댓글1");
        pickCommentRepository.save(pickComment);

        // 신고 종류 생성
        BlameType blameType = new BlameType("욕설1", 0);
        blameTypeRepository.save(blameType);

        BlameRequest blameRequest = createBlameRequestBy(pick.getId(), pickComment.getId(), null, null,
                blameType.getId());

        // when // then
        ResultActions actions = mockMvc.perform(post("/devdevdev/api/v1/blames/{blamePathType}", BlamePathType.PICK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header(AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                        .content(om.writeValueAsString(blameRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultType").value(SUCCESS.name()))
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andExpect(jsonPath("$.data.blameId").isNumber());

        // docs
        actions.andDo(document("blame-pick-comment",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                        headerWithName(AUTHORIZATION_HEADER).description("Bearer 엑세스 토큰")
                ),
                pathParameters(
                        parameterWithName("blamePathType").description("댑댑댑 신고 경로 타입").attributes(blamePathType())
                ),
                requestFields(
                        fieldWithPath("pickId").type(NUMBER).optional().description("픽픽픽 아이디")
                                .attributes(numberOrNull()),
                        fieldWithPath("pickCommentId").type(NUMBER).optional().description("픽픽픽 댓글 아이디")
                                .attributes(numberOrNull()),
                        fieldWithPath("techArticleId").type(NUMBER).optional().description("기술 블로그 아이디")
                                .attributes(numberOrNull()),
                        fieldWithPath("techArticleCommentId").type(NUMBER).optional().description("기술 블로그 댓글 아이디")
                                .attributes(numberOrNull()),
                        fieldWithPath("blameTypeId").type(NUMBER).description("신고 사유 종류 아이디")
                                .attributes(numberOrNull()),
                        fieldWithPath("customReason").type(STRING).optional()
                                .description("신고 사유 직접 작성 값(신고 사유 종류가 기타일 때)")
                                .attributes(stringOrNull())
                ),
                responseFields(
                        fieldWithPath("resultType").type(STRING).description("응답 결과"),
                        fieldWithPath("data.blameId").type(NUMBER).description("신고 아이디")
                )
        ));
    }

    @Test
    @DisplayName("회원이 기술 블로그 댓글을 신고한다.")
    void blameTechArticleComment() throws Exception {
        // given
        // 회원 생성
        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘", "꿈빛파티시엘", "1234", email,
                socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        // 회사 생성
        Company company = createCompany("회사1");
        companyRepository.save(company);

        // 기술 블로그 생성
        TechArticle techArticle = createTechArticle(company);
        techArticleRepository.save(techArticle);

        // 기술 블로그 댓글 생성
        TechComment techComment = createTechComment(new Count(0L), techArticle, member);
        techCommentRepository.save(techComment);

        // 신고 종류 생성
        BlameType blameType = new BlameType("욕설1", 0);
        blameTypeRepository.save(blameType);

        BlameRequest blameRequest = createBlameRequestBy(null, null, techArticle.getId(), techComment.getId(),
                blameType.getId());

        // when // then
        ResultActions actions = mockMvc.perform(
                        post("/devdevdev/api/v1/blames/{blamePathType}", BlamePathType.TECH_ARTICLE)
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding(StandardCharsets.UTF_8)
                                .header(AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                                .content(om.writeValueAsString(blameRequest)))
                .andDo(print())
                .andExpect(status().isOk());

        // docs
        actions.andDo(document("blame-tech-article-comment",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                        headerWithName(AUTHORIZATION_HEADER).description("Bearer 엑세스 토큰")
                ),
                pathParameters(
                        parameterWithName("blamePathType").description("댑댑댑 신고 경로 타입").attributes(blamePathType())
                ),
                requestFields(
                        fieldWithPath("pickId").type(NUMBER).optional().description("픽픽픽 아이디")
                                .attributes(numberOrNull()),
                        fieldWithPath("pickCommentId").type(NUMBER).optional().description("픽픽픽 댓글 아이디")
                                .attributes(numberOrNull()),
                        fieldWithPath("techArticleId").type(NUMBER).optional().description("기술 블로그 아이디")
                                .attributes(numberOrNull()),
                        fieldWithPath("techArticleCommentId").type(NUMBER).optional().description("기술 블로그 댓글 아이디")
                                .attributes(numberOrNull()),
                        fieldWithPath("blameTypeId").type(NUMBER).description("신고 사유 종류 아이디")
                                .attributes(numberOrNull()),
                        fieldWithPath("customReason").type(STRING).optional()
                                .description("신고 사유 직접 작성 값(신고 사유 종류가 기타일 때)")
                                .attributes(stringOrNull())
                ),
                responseFields(
                        fieldWithPath("resultType").type(STRING).description("응답 결과"),
                        fieldWithPath("data.blameId").type(NUMBER).description("신고 아이디")
                )
        ));
    }

    @ParameterizedTest
    @EnumSource(BlamePathType.class)
    @DisplayName("댑댑댑 서비스에 신고할 때 신고 사유 아이디가 없으면 예외가 발생한다.")
    void blameBindException(BlamePathType blamePathType) throws Exception {
        // given
        // 회원 생성
        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘", "꿈빛파티시엘", "1234", email,
                socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        BlameRequest blameRequest = createBlameRequestBy(null, null, null, null, null);

        // when // then
        ResultActions actions = mockMvc.perform(post("/devdevdev/api/v1/blames/{blamePathType}", blamePathType)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header(AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                        .content(om.writeValueAsString(blameRequest)))
                .andDo(print())
                .andExpect(status().is4xxClientError());

        // docs
        actions.andDo(document("blame-bind-exception",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                        headerWithName(AUTHORIZATION_HEADER).description("Bearer 엑세스 토큰")
                ),
                pathParameters(
                        parameterWithName("blamePathType").description("댑댑댑 신고 경로 타입").attributes(blamePathType())
                ),
                requestFields(
                        fieldWithPath("pickId").type(NUMBER).optional().description("픽픽픽 아이디")
                                .attributes(numberOrNull()),
                        fieldWithPath("pickCommentId").type(NUMBER).optional().description("픽픽픽 댓글 아이디")
                                .attributes(numberOrNull()),
                        fieldWithPath("techArticleId").type(NUMBER).optional().description("기술 블로그 아이디")
                                .attributes(numberOrNull()),
                        fieldWithPath("techArticleCommentId").type(NUMBER).optional().description("기술 블로그 댓글 아이디")
                                .attributes(numberOrNull()),
                        fieldWithPath("blameTypeId").type(NULL).description("신고 사유 종류 아이디")
                                .attributes(numberOrNull()),
                        fieldWithPath("customReason").type(STRING).optional()
                                .description("신고 사유 직접 작성 값(신고 사유 종류가 기타일 때)")
                                .attributes(stringOrNull())
                ),
                exceptionResponseFields()
        ));
    }

    private TechComment createTechComment(Count blameTotalCount, TechArticle techArticle, Member createdBy) {
        return TechComment.builder()
                .blameTotalCount(blameTotalCount)
                .techArticle(techArticle)
                .createdBy(createdBy)
                .build();
    }

    private Company createCompany(String name) {
        return Company.builder()
                .name(new CompanyName(name))
                .build();
    }

    private TechArticle createTechArticle(Company company) {
        return TechArticle.builder()
                .company(company)
                .build();
    }

    private Blame createBlame(Pick pick, PickComment pickComment, TechArticle techArticle, TechComment techComment,
                              BlameType blameType, Member member) {
        return Blame.builder()
                .pick(pick)
                .pickComment(pickComment)
                .techArticle(techArticle)
                .techComment(techComment)
                .blameType(blameType)
                .member(member)
                .build();
    }

    private BlameRequest createBlameRequestBy(Long pickId, Long pickCommentId, Long techArticleId,
                                              Long techArticleCommentId, Long blameTypeId) {
        return BlameRequest.builder()
                .pickId(pickId)
                .pickCommentId(pickCommentId)
                .techArticleId(techArticleId)
                .techArticleCommentId(techArticleCommentId)
                .blameTypeId(blameTypeId)
                .build();
    }

    private PickComment createPickComment(Pick pick, Member createdBy, String contents) {
        PickComment pickComment = PickComment.builder()
                .pick(pick)
                .createdBy(createdBy)
                .isPublic(true)
                .contents(new CommentContents(contents))
                .build();

        pickComment.changePick(pick);

        return pickComment;
    }

    private Pick createPick(Member member, String title, ContentStatus contentStatus, Count commentTotalCount) {
        return Pick.builder()
                .member(member)
                .title(new Title(title))
                .contentStatus(contentStatus)
                .commentTotalCount(commentTotalCount)
                .build();
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

    private BlameType createBlameType(String reason, int sortOrder) {
        return new BlameType(reason, sortOrder);
    }
}
