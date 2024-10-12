package com.dreamypatisiel.devdevdev.web.docs;

import static com.dreamypatisiel.devdevdev.global.constant.SecurityConstant.AUTHORIZATION_HEADER;
import static com.dreamypatisiel.devdevdev.web.docs.format.ApiDocsFormatGenerator.authenticationType;
import static com.dreamypatisiel.devdevdev.web.docs.format.ApiDocsFormatGenerator.pickCommentSortType;
import static com.dreamypatisiel.devdevdev.web.docs.format.ApiDocsFormatGenerator.pickOptionType;
import static com.dreamypatisiel.devdevdev.web.docs.format.ApiDocsFormatGenerator.stringOrNull;
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
import static org.springframework.restdocs.payload.JsonFieldType.ARRAY;
import static org.springframework.restdocs.payload.JsonFieldType.BOOLEAN;
import static org.springframework.restdocs.payload.JsonFieldType.NULL;
import static org.springframework.restdocs.payload.JsonFieldType.NUMBER;
import static org.springframework.restdocs.payload.JsonFieldType.OBJECT;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.Pick;
import com.dreamypatisiel.devdevdev.domain.entity.PickComment;
import com.dreamypatisiel.devdevdev.domain.entity.PickCommentRecommend;
import com.dreamypatisiel.devdevdev.domain.entity.PickOption;
import com.dreamypatisiel.devdevdev.domain.entity.PickOptionImage;
import com.dreamypatisiel.devdevdev.domain.entity.PickVote;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.CommentContents;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Count;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.PickOptionContents;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Title;
import com.dreamypatisiel.devdevdev.domain.entity.enums.ContentStatus;
import com.dreamypatisiel.devdevdev.domain.entity.enums.PickOptionType;
import com.dreamypatisiel.devdevdev.domain.entity.enums.Role;
import com.dreamypatisiel.devdevdev.domain.entity.enums.SocialType;
import com.dreamypatisiel.devdevdev.domain.policy.PickPopularScorePolicy;
import com.dreamypatisiel.devdevdev.domain.repository.member.MemberRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickCommentRecommendRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickCommentRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickCommentSort;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickOptionImageRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickOptionRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickVoteRepository;
import com.dreamypatisiel.devdevdev.global.constant.SecurityConstant;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.SocialMemberDto;
import com.dreamypatisiel.devdevdev.web.dto.request.pick.ModifyPickCommentRequest;
import com.dreamypatisiel.devdevdev.web.dto.request.pick.ModifyPickOptionRequest;
import com.dreamypatisiel.devdevdev.web.dto.request.pick.ModifyPickRequest;
import com.dreamypatisiel.devdevdev.web.dto.request.pick.RegisterPickCommentRequest;
import com.dreamypatisiel.devdevdev.web.dto.request.pick.RegisterPickOptionRequest;
import com.dreamypatisiel.devdevdev.web.dto.request.pick.RegisterPickRepliedCommentRequest;
import com.dreamypatisiel.devdevdev.web.dto.request.pick.RegisterPickRequest;
import com.dreamypatisiel.devdevdev.web.dto.response.ResultType;
import jakarta.persistence.EntityManager;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;
import org.junit.jupiter.params.provider.NullSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.multipart.MultipartFile;

public class PickCommentControllerDocsTest extends SupportControllerDocsTest {

    @Autowired
    PickRepository pickRepository;
    @Autowired
    PickOptionRepository pickOptionRepository;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    PickPopularScorePolicy pickPopularScorePolicy;
    @Autowired
    PickOptionImageRepository pickOptionImageRepository;
    @Autowired
    PickVoteRepository pickVoteRepository;
    @Autowired
    PickCommentRepository pickCommentRepository;
    @Autowired
    PickCommentRecommendRepository pickCommentRecommendRepository;
    @Autowired
    EntityManager em;
    @MockBean
    AmazonS3 amazonS3Client;

    @Test
    @DisplayName("회원이 승인 상태의 픽픽픽에 댓글을 작성한다.")
    void registerPickComment() throws Exception {
        // given
        // 회원 생성
        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        // 픽픽픽 생성
        Pick pick = createPick(new Title("픽픽픽 타이틀"), new Count(0L), new Count(0L), new Count(0L), new Count(0L),
                ContentStatus.APPROVAL, member);
        pickRepository.save(pick);

        // 픽픽픽 옵션 생성
        PickOption firstPickOption = createPickOption(pick, new Title("픽픽픽 옵션1 타이틀"),
                new PickOptionContents("픽픽픽 옵션1 컨텐츠"), PickOptionType.firstPickOption);
        PickOption secondPickOption = createPickOption(pick, new Title("픽픽픽 옵션2 타이틀"),
                new PickOptionContents("픽픽픽 옵션2 컨텐츠"), PickOptionType.secondPickOption);
        pickOptionRepository.saveAll(List.of(firstPickOption, secondPickOption));

        // 픽픽픽 이미지 생성
        PickOptionImage firstPickOptionImage = createPickOptionImage("firstPickOptionImage", firstPickOption);
        PickOptionImage secondPickOptionImage = createPickOptionImage("secondPickOptionImage", firstPickOption);
        pickOptionImageRepository.saveAll(List.of(firstPickOptionImage, secondPickOptionImage));

        // 픽픽픽 투표 생성
        PickVote pickVote = createPickVote(member, firstPickOption, pick);
        pickVoteRepository.save(pickVote);

        em.flush();
        em.clear();

        RegisterPickCommentRequest registerPickCommentRequest = new RegisterPickCommentRequest("안녕하세웅",
                true);

        // when // then
        ResultActions actions = mockMvc.perform(post("/devdevdev/api/v1/picks/{pickId}/comments", pick.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(om.writeValueAsString(registerPickCommentRequest)))
                .andDo(print())
                .andExpect(status().isOk());

        // docs
        actions.andDo(document("register-pick-comment",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                        headerWithName(AUTHORIZATION_HEADER).description("Bearer 엑세스 토큰")
                ),
                pathParameters(
                        parameterWithName("pickId").description("픽픽픽 아이디")
                ),
                requestFields(
                        fieldWithPath("contents").type(STRING).description("픽픽픽 댓글 내용(최소 1자 이상 최대 1,000자 이하)"),
                        fieldWithPath("isPickVotePublic").type(BOOLEAN).description("픽픽픽 공개 여부")
                ),
                responseFields(
                        fieldWithPath("resultType").type(STRING).description("응답 결과"),
                        fieldWithPath("data").type(OBJECT).description("응답 데이터"),
                        fieldWithPath("data.pickCommentId").type(NUMBER).description("픽픽픽 댓글 아이디")
                )
        ));
    }

    @ParameterizedTest
    @NullSource
    @DisplayName("회원이 승인 상태의 픽픽픽에 댓글을 작성할 때 픽픽픽 공개 여부가 null이면 예외가 발생한다.")
    void registerPickCommentBindExceptionPickVotePublicIsNull(Boolean isPickVotePublic) throws Exception {
        // given
        // 회원 생성
        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        // 픽픽픽 생성
        Pick pick = createPick(new Title("픽픽픽 타이틀"), ContentStatus.APPROVAL, member);
        pickRepository.save(pick);

        // 픽픽픽 옵션 생성
        PickOption firstPickOption = createPickOption(pick, new Title("픽픽픽 옵션1 타이틀"),
                new PickOptionContents("픽픽픽 옵션1 컨텐츠"), PickOptionType.firstPickOption);
        PickOption secondPickOption = createPickOption(pick, new Title("픽픽픽 옵션2 타이틀"),
                new PickOptionContents("픽픽픽 옵션2 컨텐츠"), PickOptionType.secondPickOption);
        pickOptionRepository.saveAll(List.of(firstPickOption, secondPickOption));

        // 픽픽픽 이미지 생성
        PickOptionImage firstPickOptionImage = createPickOptionImage("firstPickOptionImage", firstPickOption);
        PickOptionImage secondPickOptionImage = createPickOptionImage("secondPickOptionImage", firstPickOption);
        pickOptionImageRepository.saveAll(List.of(firstPickOptionImage, secondPickOptionImage));

        // 픽픽픽 투표 생성
        PickVote pickVote = createPickVote(member, firstPickOption, pick);
        pickVoteRepository.save(pickVote);

        em.flush();
        em.clear();

        RegisterPickCommentRequest registerPickCommentRequest = new RegisterPickCommentRequest("안녕하세웅",
                isPickVotePublic);

        // when // then
        ResultActions actions = mockMvc.perform(post("/devdevdev/api/v1/picks/{pickId}/comments", pick.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(om.writeValueAsString(registerPickCommentRequest)))
                .andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.resultType").value(ResultType.FAIL.name()))
                .andExpect(jsonPath("$.message").isString())
                .andExpect(jsonPath("$.errorCode").value(HttpStatus.BAD_REQUEST.value()));

        // docs
        actions.andDo(document("register-pick-comment-bind-exception-pick-vote-public-is-null",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                        headerWithName(AUTHORIZATION_HEADER).description("Bearer 엑세스 토큰")
                ),
                pathParameters(
                        parameterWithName("pickId").description("픽픽픽 아이디")
                ),
                requestFields(
                        fieldWithPath("contents").type(STRING).description("픽픽픽 댓글 내용(최소 1자 이상 최대 1,000자 이하)"),
                        fieldWithPath("isPickVotePublic").type(NULL).description("픽픽픽 공개 여부")
                ),
                exceptionResponseFields()
        ));
    }

    @Test
    @DisplayName("픽픽픽 답글을 작성한다.")
    void registerPickRepliedComment() throws Exception {
        // given
        // 회원 생성
        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        // 픽픽픽 작성자 생성
        SocialMemberDto authorSocialMemberDto = createSocialDto("authorId", "author",
                "꿈빛맛티시엘", "1234", "authorDreamy5patisiel@kakao.com", socialType, role);
        Member author = Member.createMemberBy(authorSocialMemberDto);
        memberRepository.save(author);

        // 픽픽픽 생성
        Pick pick = createPick(new Title("픽픽픽 타이틀"), new Count(0L), new Count(0L), new Count(0L), new Count(0L),
                ContentStatus.APPROVAL, author);
        pickRepository.save(pick);

        // 픽픽픽 댓글 생성
        PickComment pickComment = createPickComment(new CommentContents("댓글1"), false, member, pick);
        pickCommentRepository.save(pickComment);

        // 픽픽픽 답글 생성
        PickComment replidPickComment = createReplidPickComment(new CommentContents("댓글1의 답글1"), false, member, pick,
                pickComment, pickComment);
        pickCommentRepository.save(replidPickComment);

        RegisterPickRepliedCommentRequest request = new RegisterPickRepliedCommentRequest("댓글1의 답글1의 답글");

        // when // then
        ResultActions actions = mockMvc.perform(
                        post(
                                "/devdevdev/api/v1/picks/{pickId}/comments/{pickCommentOriginParentId}/{pickCommentParentId}",
                                pick.getId(), pickComment.getId(), replidPickComment.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                                .characterEncoding(StandardCharsets.UTF_8)
                                .content(om.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk());

        // docs
        actions.andDo(document("register-pick-comment-reply",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                        headerWithName(AUTHORIZATION_HEADER).description("Bearer 엑세스 토큰")
                ),
                pathParameters(
                        parameterWithName("pickId").description("픽픽픽 아이디"),
                        parameterWithName("pickCommentOriginParentId").description("픽픽픽 최상단 댓글 아이디"),
                        parameterWithName("pickCommentParentId").description("픽픽픽 답글 대상의 댓글 아이디")
                ),
                requestFields(
                        fieldWithPath("contents").type(STRING).description("픽픽픽 댓글 내용(최소 1자 이상 최대 1,000자 이하)")
                ),
                responseFields(
                        fieldWithPath("resultType").type(STRING).description("응답 결과"),
                        fieldWithPath("data").type(OBJECT).description("응답 데이터"),
                        fieldWithPath("data.pickCommentId").type(NUMBER).description("픽픽픽 답글 아이디")
                )
        ));
    }

    @ParameterizedTest
    @EmptySource
    @DisplayName("픽픽픽 답글을 작성할 때 답글 내용이 공백이면 예외가 발생한다.")
    void registerPickRepliedCommentBindException(String contents) throws Exception {
        // given
        // 회원 생성
        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        // 픽픽픽 작성자 생성
        SocialMemberDto authorSocialMemberDto = createSocialDto("authorId", "author",
                "꿈빛맛티시엘", "1234", "authorDreamy5patisiel@kakao.com", socialType, role);
        Member author = Member.createMemberBy(authorSocialMemberDto);
        memberRepository.save(author);

        // 픽픽픽 생성
        Pick pick = createPick(new Title("픽픽픽 타이틀"), ContentStatus.APPROVAL, author);
        pickRepository.save(pick);

        // 픽픽픽 댓글 생성
        PickComment pickComment = createPickComment(new CommentContents("댓글1"), false, member, pick);
        pickCommentRepository.save(pickComment);

        // 픽픽픽 답글 생성
        PickComment replidPickComment = createReplidPickComment(new CommentContents("댓글1의 답글1"), false, member, pick,
                pickComment, pickComment);
        pickCommentRepository.save(replidPickComment);

        RegisterPickRepliedCommentRequest request = new RegisterPickRepliedCommentRequest(contents);

        // when // then
        ResultActions actions = mockMvc.perform(
                        post("/devdevdev/api/v1/picks/{pickId}/comments/{pickCommentOriginParentId}/{pickCommentParentId}",
                                pick.getId(), pickComment.getId(), replidPickComment.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                                .characterEncoding(StandardCharsets.UTF_8)
                                .content(om.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().is4xxClientError());

        // docs
        actions.andDo(document("register-pick-comment-reply-bind-exception",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                        headerWithName(AUTHORIZATION_HEADER).description("Bearer 엑세스 토큰")
                ),
                pathParameters(
                        parameterWithName("pickId").description("픽픽픽 아이디"),
                        parameterWithName("pickCommentOriginParentId").description("픽픽픽 최상단 댓글 아이디"),
                        parameterWithName("pickCommentParentId").description("픽픽픽 답글 대상의 댓글 아이디")
                ),
                requestFields(
                        fieldWithPath("contents").type(STRING).description("픽픽픽 댓글 내용(최소 1자 이상 최대 1,000자 이하)")
                ),
                exceptionResponseFields()
        ));
    }

    @Test
    @DisplayName("승인 상태이고 회원 본인이 작성한 삭제되지 않은 픽픽픽 댓글을 수정한다.")
    void modifyPickComment() throws Exception {
        // given
        // 회원 생성
        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        // 픽픽픽 생성
        Pick pick = createPick(new Title("픽픽픽 타이틀"), ContentStatus.APPROVAL, member);
        pickRepository.save(pick);

        // 픽픽픽 댓글 생성
        PickComment pickComment = createPickComment(new CommentContents("안녕하세웅"), false, member, pick);
        pickCommentRepository.save(pickComment);

        em.flush();
        em.clear();

        ModifyPickCommentRequest request = new ModifyPickCommentRequest("주무세웅");

        // when // then
        ResultActions actions = mockMvc.perform(patch("/devdevdev/api/v1/picks/{pickId}/comments/{pickCommentId}",
                        pick.getId(), pickComment.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(om.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk());

        // docs
        actions.andDo(document("modify-pick-comment",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                        headerWithName(AUTHORIZATION_HEADER).description("Bearer 엑세스 토큰")
                ),
                pathParameters(
                        parameterWithName("pickId").description("픽픽픽 아이디"),
                        parameterWithName("pickCommentId").description("픽픽픽 댓글 아이디")
                ),
                requestFields(
                        fieldWithPath("contents").type(STRING).description("픽픽픽 댓글 내용(최소 1자 이상 최대 1,000자 이하)")
                ),
                responseFields(
                        fieldWithPath("resultType").type(STRING).description("응답 결과"),
                        fieldWithPath("data").type(OBJECT).description("응답 데이터"),
                        fieldWithPath("data.pickCommentId").type(NUMBER).description("픽픽픽 댓글 아이디")
                )
        ));
    }

    @ParameterizedTest
    @EmptySource
    @DisplayName("승인 상태이고 회원 본인이 작성한 삭제되지 않은 픽픽픽 댓글을 수정할 때 수정할 내용이 공백이면 예외가 발생한다.")
    void modifyPickCommentBindException(String contents) throws Exception {
        // given
        // 회원 생성
        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        // 픽픽픽 생성
        Pick pick = createPick(new Title("픽픽픽 타이틀"), ContentStatus.APPROVAL, member);
        pickRepository.save(pick);

        // 픽픽픽 댓글 생성
        PickComment pickComment = createPickComment(new CommentContents("안녕하세웅"), false, member, pick);
        pickCommentRepository.save(pickComment);

        em.flush();
        em.clear();

        ModifyPickCommentRequest request = new ModifyPickCommentRequest(contents);

        // when // then
        ResultActions actions = mockMvc.perform(patch("/devdevdev/api/v1/picks/{pickId}/comments/{pickCommentId}",
                        pick.getId(), pickComment.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(om.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().is4xxClientError());

        // docs
        actions.andDo(document("modify-pick-comment-bind-exception",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                        headerWithName(AUTHORIZATION_HEADER).description("Bearer 엑세스 토큰")
                ),
                pathParameters(
                        parameterWithName("pickId").description("픽픽픽 아이디"),
                        parameterWithName("pickCommentId").description("픽픽픽 댓글 아이디")
                ),
                requestFields(
                        fieldWithPath("contents").type(STRING).description("픽픽픽 댓글 내용(최소 1자 이상 최대 1,000자 이하)")
                ),
                exceptionResponseFields()
        ));
    }

    @Test
    @DisplayName("회원 본인이 작성한 승인 상태의 픽픽픽의 삭제상태가 아닌 댓글을 삭제한다.")
    void deletePickComment() throws Exception {
        // given
        // 회원 생성
        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        // 픽픽픽 생성
        Pick pick = createPick(new Title("픽픽픽 타이틀"), ContentStatus.APPROVAL, member);
        pickRepository.save(pick);

        // 픽픽픽 댓글 생성
        PickComment pickComment = createPickComment(new CommentContents("안녕하세웅"), false, member, pick);
        pickCommentRepository.save(pickComment);

        em.flush();
        em.clear();

        // when // then
        ResultActions actions = mockMvc.perform(delete("/devdevdev/api/v1/picks/{pickId}/comments/{pickCommentId}",
                        pick.getId(), pickComment.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().isOk());

        // docs
        actions.andDo(document("delete-pick-comment",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                        headerWithName(AUTHORIZATION_HEADER).description("Bearer 엑세스 토큰")
                ),
                pathParameters(
                        parameterWithName("pickId").description("픽픽픽 아이디"),
                        parameterWithName("pickCommentId").description("픽픽픽 댓글 아이디")
                ),
                responseFields(
                        fieldWithPath("resultType").type(STRING).description("응답 결과"),
                        fieldWithPath("data").type(OBJECT).description("응답 데이터"),
                        fieldWithPath("data.pickCommentId").type(NUMBER).description("픽픽픽 댓글 아이디")
                )
        ));
    }

    @Test
    @DisplayName("픽픽픽 댓글을 삭제할 때 본인이 작성한 댓글이 아니면 예외가 발생한다.")
    void deletePickCommentOtherMemberException() throws Exception {
        // given
        // 회원 생성
        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        // 픽픽픽 생성
        Pick pick = createPick(new Title("픽픽픽 타이틀"), ContentStatus.APPROVAL, member);
        pickRepository.save(pick);

        // 다른 회원 생성
        SocialMemberDto otherSocialMemberDto = createSocialDto("helloWorld", "헬로월드",
                "헬로월드", "1234", "helloWorld@kakao.com", socialType, role);
        Member ohterMember = Member.createMemberBy(otherSocialMemberDto);
        memberRepository.save(ohterMember);

        // 다른 회원이 작성한 픽픽픽 댓글 생성
        PickComment pickComment = createPickComment(new CommentContents("안녕하세웅"), false, ohterMember, pick);
        pickCommentRepository.save(pickComment);

        em.flush();
        em.clear();

        // when // then
        ResultActions actions = mockMvc.perform(delete("/devdevdev/api/v1/picks/{pickId}/comments/{pickCommentId}",
                        pick.getId(), pickComment.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().is4xxClientError());

        // docs
        actions.andDo(document("delete-pick-comment-not-found-exception",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                        headerWithName(AUTHORIZATION_HEADER).description("Bearer 엑세스 토큰")
                ),
                pathParameters(
                        parameterWithName("pickId").description("픽픽픽 아이디"),
                        parameterWithName("pickCommentId").description("픽픽픽 댓글 아이디")
                ),
                exceptionResponseFields()
        ));
    }

    @ParameterizedTest
    @EnumSource(PickCommentSort.class)
    @DisplayName("픽픽픽 댓글/답글을 정렬 조건에 따라서 조회한다.")
    void getPickComments(PickCommentSort pickCommentSort) throws Exception {
        // given
        // 회원 생성
        SocialMemberDto socialMemberDto1 = createSocialDto("user1", "user1", "미뇽냥녕냥녕", "1234", "alsdudr97@naver.com",
                socialType, role);
        SocialMemberDto socialMemberDto2 = createSocialDto("user2", "user2", "야임마", "1234", "wlgks555@naver.com",
                socialType, role);
        SocialMemberDto socialMemberDto3 = createSocialDto("user3", "user3", "아이러브 손흥민", "1234", "mmj9908@naver.com",
                socialType, role);
        SocialMemberDto socialMemberDto4 = createSocialDto("user4", "user4", "소영소", "1234", "merooongg@naver.com",
                socialType, role);
        SocialMemberDto socialMemberDto5 = createSocialDto("user5", "user5", "장세웅", "1234", "howisitgoing@kakao.com",
                socialType, Role.ROLE_ADMIN.name());
        SocialMemberDto socialMemberDto6 = createSocialDto("user6", "user6", "nickname", "1234", "user6@gmail.com",
                socialType, role);
        SocialMemberDto socialMemberDto7 = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member1 = Member.createMemberBy(socialMemberDto1);
        Member member2 = Member.createMemberBy(socialMemberDto2);
        Member member3 = Member.createMemberBy(socialMemberDto3);
        Member member4 = Member.createMemberBy(socialMemberDto4);
        Member member5 = Member.createMemberBy(socialMemberDto5);
        Member member6 = Member.createMemberBy(socialMemberDto6);
        Member member7 = Member.createMemberBy(socialMemberDto7);
        memberRepository.saveAll(List.of(member1, member2, member3, member4, member5, member6, member7));

        // 픽픽픽 생성
        Pick pick = createPick(new Title("꿈파 워크샵 어디로 갈까요?"), ContentStatus.APPROVAL, new Count(6), member1);
        pickRepository.save(pick);

        // 픽픽픽 옵션 생성
        PickOption firstPickOption = createPickOption(new Title("휴양지의 근본 제주도!"), new Count(2), pick,
                PickOptionType.firstPickOption);
        PickOption secondPickOption = createPickOption(new Title("한국의 알프스 강원도!"), new Count(2), pick,
                PickOptionType.secondPickOption);
        pickOptionRepository.saveAll(List.of(firstPickOption, secondPickOption));

        // 픽픽픽 투표 생성
        PickVote member1PickVote = createPickVote(member1, firstPickOption, pick);
        PickVote member2PickVote = createPickVote(member2, firstPickOption, pick);
        PickVote member3PickVote = createPickVote(member3, secondPickOption, pick);
        PickVote member4PickVote = createPickVote(member4, secondPickOption, pick);
        pickVoteRepository.saveAll(List.of(member1PickVote, member2PickVote, member3PickVote, member4PickVote));

        // 픽픽픽 최초 댓글 생성
        PickComment originParentPickComment1 = createPickComment(new CommentContents("나는 미뇽냥녕뇽이다!"), true, new Count(2),
                new Count(2), member1, pick, member1PickVote);
        PickComment originParentPickComment2 = createPickComment(new CommentContents("임하하하하하"), true, new Count(1),
                new Count(1), member2, pick, member2PickVote);
        PickComment originParentPickComment3 = createPickComment(new CommentContents("손흥민 최고다!"), true, new Count(0),
                new Count(0), member3, pick, member3PickVote);
        PickComment originParentPickComment4 = createPickComment(new CommentContents("나는 소영소"), false, new Count(0),
                new Count(0), member4, pick, member4PickVote);
        PickComment originParentPickComment5 = createPickComment(new CommentContents("힘들면 힘을내자!"), false, new Count(0),
                new Count(0), member5, pick, null);
        PickComment originParentPickComment6 = createPickComment(new CommentContents("댓글6"), false, new Count(0),
                new Count(0), member6, pick, null);
        pickCommentRepository.saveAll(
                List.of(originParentPickComment6, originParentPickComment5, originParentPickComment4,
                        originParentPickComment3, originParentPickComment2, originParentPickComment1));

        // 픽픽픽 답글 생성
        PickComment pickReply1 = createReplidPickComment(new CommentContents("누가 빨래좀 대신 개주세여..."), member2, pick,
                originParentPickComment1, originParentPickComment1);
        PickComment pickReply2 = createReplidPickComment(new CommentContents("손흥민 사랑해~!"), member3, pick,
                originParentPickComment1, pickReply1);
        PickComment pickReply3 = createReplidPickComment(new CommentContents("소주 없이는 못살아!!!!"), member4, pick,
                originParentPickComment2, originParentPickComment2);
        PickComment pickReply4 = createReplidPickComment(new CommentContents("벌써 9월이당"), member5, pick,
                originParentPickComment2, originParentPickComment2);
        pickReply4.changeDeletedAt(LocalDateTime.now(), member5);
        pickCommentRepository.saveAll(List.of(pickReply4, pickReply3, pickReply2, pickReply1));

        em.flush();
        em.clear();

        Pageable pageable = PageRequest.of(0, 5);

        // when // then
        ResultActions actions = mockMvc.perform(get("/devdevdev/api/v1/picks/{pickId}/comments",
                        pick.getId())
                        .queryParam("pickCommentId", String.valueOf(Long.MAX_VALUE))
                        .queryParam("size", String.valueOf(pageable.getPageSize()))
                        .queryParam("pickCommentSort", pickCommentSort.name())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().isOk());

        // docs
        actions.andDo(document("get-pick-comments",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                        headerWithName(AUTHORIZATION_HEADER).optional().description("Bearer 엑세스 토큰")
                ),
                pathParameters(
                        parameterWithName("pickId").description("픽픽픽 아이디")
                ),
                queryParameters(
                        parameterWithName("pickCommentId").optional().description("픽픽픽 댓글 아이디"),
                        parameterWithName("size").optional().description("조회되는 데이터 수"),
                        parameterWithName("pickCommentSort").optional().description("픽픽픽 댓글 정렬 조건")
                                .attributes(pickCommentSortType()),
                        parameterWithName("pickOptionType").optional().description("픽픽픽 댓글 필터링 옵션 타입(체크박스)")
                                .attributes(pickOptionType())
                ),
                responseFields(
                        fieldWithPath("resultType").type(STRING).description("응답 결과"),
                        fieldWithPath("data").type(OBJECT).description("응답 데이터"),

                        fieldWithPath("data.content").type(ARRAY).description("픽픽픽 댓글/답글 메인 배열"),
                        fieldWithPath("data.content[].pickCommentId").type(NUMBER).description("픽픽픽 댓글 아이디"),
                        fieldWithPath("data.content[].createdAt").type(STRING).description("픽픽픽 댓글 작성일시"),
                        fieldWithPath("data.content[].memberId").type(NUMBER).description("픽픽픽 댓글 작성자 아이디"),
                        fieldWithPath("data.content[].author").type(STRING).description("픽픽픽 댓글 작성자 닉네임"),
                        fieldWithPath("data.content[].isCommentOfPickAuthor").type(BOOLEAN)
                                .description("댓글 작성자가 픽픽픽 작성자인지 여부"),
                        fieldWithPath("data.content[].isCommentAuthor").type(BOOLEAN)
                                .description("로그인한 회원이 댓글 작성자인지 여부"),
                        fieldWithPath("data.content[].isRecommended").type(BOOLEAN)
                                .description("로그인한 회원이 댓글 추천 여부"),
                        fieldWithPath("data.content[].maskedEmail").type(STRING).description("픽픽픽 댓글 작성자 이메일"),
                        fieldWithPath("data.content[].votedPickOption").optional().type(STRING)
                                .description("픽픽픽 투표 선택 타입").attributes(pickOptionType()),
                        fieldWithPath("data.content[].votedPickOptionTitle").optional().type(STRING)
                                .description("픽픽픽 투표 선택 타입 제목").attributes(stringOrNull()),
                        fieldWithPath("data.content[].contents").type(STRING).description("픽픽픽 댓글 내용"),
                        fieldWithPath("data.content[].replyTotalCount").type(NUMBER)
                                .description("픽픽픽 댓글의 답글 총 갯수"),
                        fieldWithPath("data.content[].likeTotalCount").type(NUMBER)
                                .description("픽픽픽 댓글 좋아요 총 갯수"),
                        fieldWithPath("data.content[].isDeleted").type(BOOLEAN)
                                .description("픽픽픽 댓글 삭제 여부"),
                        fieldWithPath("data.content[].isModified").type(BOOLEAN)
                                .description("픽픽픽 댓글 수정 여부"),

                        fieldWithPath("data.content[].replies").type(ARRAY).description("픽픽픽 답글 배열"),
                        fieldWithPath("data.content[].replies[].pickCommentId").type(NUMBER).description("픽픽픽 답글 아이디"),
                        fieldWithPath("data.content[].replies[].memberId").type(NUMBER).description("픽픽픽 답글 작성자 아이디"),
                        fieldWithPath("data.content[].replies[].pickCommentParentId").type(NUMBER)
                                .description("픽픽픽 답글의 부모 댓글 아이디"),
                        fieldWithPath("data.content[].replies[].pickCommentOriginParentId").type(NUMBER)
                                .description("픽픽픽 답글의 최상위 부모 댓글 아이디"),
                        fieldWithPath("data.content[].replies[].createdAt").type(STRING).description("픽픽픽 답글 작성일시"),
                        fieldWithPath("data.content[].replies[].isCommentOfPickAuthor").type(BOOLEAN)
                                .description("답글 작성자가 픽픽픽 작성자인지 여부"),
                        fieldWithPath("data.content[].replies[].isCommentAuthor").type(BOOLEAN)
                                .description("로그인한 회원이 답글 작성자인지 여부"),
                        fieldWithPath("data.content[].replies[].isRecommended").type(BOOLEAN)
                                .description("로그인한 회원이 답글 추천 여부"),
                        fieldWithPath("data.content[].replies[].author").type(STRING).description("픽픽픽 답글 작성자 닉네임"),
                        fieldWithPath("data.content[].replies[].maskedEmail").type(STRING)
                                .description("픽픽픽 답글 작성자 이메일"),
                        fieldWithPath("data.content[].replies[].contents").type(STRING).description("픽픽픽 답글 내용"),
                        fieldWithPath("data.content[].replies[].likeTotalCount").type(NUMBER)
                                .description("픽픽픽 답글 좋아요 총 갯수"),
                        fieldWithPath("data.content[].replies[].isDeleted").type(BOOLEAN)
                                .description("픽픽픽 답글 삭제 여부"),
                        fieldWithPath("data.content[].replies[].isModified").type(BOOLEAN)
                                .description("픽픽픽 답글 수정 여부"),
                        fieldWithPath("data.content[].replies[].parentCommentMemberId").type(NUMBER)
                                .description("픽픽픽 부모 댓글 작성자 아이디"),
                        fieldWithPath("data.content[].replies[].parentCommentAuthor").type(STRING)
                                .description("픽픽픽 부모 댓글 작성자 닉네임"),

                        fieldWithPath("data.pageable").type(OBJECT).description("픽픽픽 메인 페이지네이션 정보"),
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
    @DisplayName("픽픽픽 댓글/답글을 추천한다.")
    void recommendPickComment() throws Exception {
        // given
        // 회원 생성
        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        // 픽픽픽 생성
        Pick pick = createPick(new Title("픽픽픽 타이틀"), ContentStatus.APPROVAL, member);
        pickRepository.save(pick);

        // 픽픽픽 댓글 생성
        PickComment pickComment = createPickComment(new CommentContents("픽픽픽 댓글"), true, new Count(0), member, pick);
        pickCommentRepository.save(pickComment);

        em.flush();
        em.clear();

        // when // then
        ResultActions actions = mockMvc.perform(
                        post("/devdevdev/api/v1/picks/{pickId}/comments/{pickCommentId}/recommends",
                                pick.getId(), pickComment.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                                .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().isOk());

        // docs
        actions.andDo(document("recommend-pick-comment",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                        headerWithName(AUTHORIZATION_HEADER).description("Bearer 엑세스 토큰")
                ),
                pathParameters(
                        parameterWithName("pickId").description("픽픽픽 아이디"),
                        parameterWithName("pickCommentId").description("픽픽픽 댓글/답글 아이디")
                ),
                responseFields(
                        fieldWithPath("resultType").type(STRING).description("응답 결과"),
                        fieldWithPath("data").type(OBJECT).description("응답 데이터").attributes(authenticationType()),
                        fieldWithPath("data.recommendStatus").type(BOOLEAN).description("픽픽픽 댓글/답글 추천 상태")
                                .attributes(authenticationType()),
                        fieldWithPath("data.recommendTotalCount").type(NUMBER).description("픽픽픽 댓글/답글 추천 총 갯수")
                                .attributes(authenticationType())
                )
        ));
    }

    @ParameterizedTest
    @EnumSource(value = ContentStatus.class, names = {"APPROVAL"}, mode = Mode.EXCLUDE)
    @DisplayName("픽픽픽 댓글을 추천할 때 픽픽픽이 승인상태가 아니면 예외가 발생한다.")
    void recommendPickCommentPickIsNotApproval(ContentStatus contentStatus) throws Exception {
        // given
        // 회원 생성
        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        // 픽픽픽 생성
        Pick pick = createPick(new Title("픽픽픽 타이틀"), contentStatus, member);
        pickRepository.save(pick);

        // 픽픽픽 댓글 생성
        PickComment pickComment = createPickComment(new CommentContents("픽픽픽 댓글"), true, new Count(0), member, pick);
        pickCommentRepository.save(pickComment);

        em.flush();
        em.clear();

        // when // then
        ResultActions actions = mockMvc.perform(
                        post("/devdevdev/api/v1/picks/{pickId}/comments/{pickCommentId}/recommends",
                                pick.getId(), pickComment.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                                .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().is4xxClientError());

        // docs
        actions.andDo(document("recommend-pick-comment-exception",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                        headerWithName(AUTHORIZATION_HEADER).description("Bearer 엑세스 토큰")
                ),
                pathParameters(
                        parameterWithName("pickId").description("픽픽픽 아이디"),
                        parameterWithName("pickCommentId").description("픽픽픽 댓글/답글 아이디")
                ),
                exceptionResponseFields()
        ));
    }

    @Test
    @DisplayName("회원이 픽픽픽 베스트 댓글을 조회한다.")
    void findPickBestComments() throws Exception {
        // given
        // 회원 생성
        SocialMemberDto socialMemberDto1 = createSocialDto("user1", "user1", "김미뇽뇽뇽뇽", "password",
                "alsdudr97@naver.com",
                socialType, role);
        SocialMemberDto socialMemberDto2 = createSocialDto("user2", "user2", "이임하하하하하", "password",
                "wlgks555@naver.com",
                socialType, role);
        SocialMemberDto socialMemberDto3 = createSocialDto("user3", "user3", "문밍주주주주주", "password", "mmj9908@naver.com",
                socialType, role);
        SocialMemberDto socialMemberDto4 = createSocialDto("user4", "user4", "유소영영영영", "password",
                "merooongg@naver.com",
                socialType, role);
        SocialMemberDto socialMemberDto5 = createSocialDto("user5", "user5", "장세웅", "password",
                "howisitgoing@kakao.com",
                socialType, role);
        SocialMemberDto socialMemberDto6 = createSocialDto("user6", "user6", "nickname", "password", "user6@gmail.com",
                socialType, role);
        SocialMemberDto socialMemberDto7 = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member1 = Member.createMemberBy(socialMemberDto1);
        Member member2 = Member.createMemberBy(socialMemberDto2);
        Member member3 = Member.createMemberBy(socialMemberDto3);
        Member member4 = Member.createMemberBy(socialMemberDto4);
        Member member5 = Member.createMemberBy(socialMemberDto5);
        Member member6 = Member.createMemberBy(socialMemberDto6);
        Member member7 = Member.createMemberBy(socialMemberDto7);
        memberRepository.saveAll(List.of(member1, member2, member3, member4, member5, member6, member7));

        // 픽픽픽 생성
        Pick pick = createPick(new Title("무엇이 정답일까요?"), ContentStatus.APPROVAL, new Count(6), member1);
        pickRepository.save(pick);

        // 픽픽픽 옵션 생성
        PickOption firstPickOption = createPickOption(new Title("꿈파 최고!"), new Count(0), pick,
                PickOptionType.firstPickOption);
        PickOption secondPickOption = createPickOption(new Title("사랑해 꿈파!"), new Count(0), pick,
                PickOptionType.secondPickOption);
        pickOptionRepository.saveAll(List.of(firstPickOption, secondPickOption));

        // 픽픽픽 투표 생성
        PickVote member1PickVote = createPickVote(member1, firstPickOption, pick);
        PickVote member2PickVote = createPickVote(member2, firstPickOption, pick);
        PickVote member3PickVote = createPickVote(member3, secondPickOption, pick);
        PickVote member4PickVote = createPickVote(member4, secondPickOption, pick);
        pickVoteRepository.saveAll(List.of(member1PickVote, member2PickVote, member3PickVote, member4PickVote));

        // 픽픽픽 최초 댓글 생성
        PickComment originParentPickComment1 = createPickComment(new CommentContents("여기가 꿈파?"), true, new Count(2),
                new Count(3), member1, pick, member1PickVote);
        originParentPickComment1.modifyCommentContents(new CommentContents("행복한~"), LocalDateTime.now());
        PickComment originParentPickComment2 = createPickComment(new CommentContents("꿈빛!"), true, new Count(1),
                new Count(2), member2, pick, member2PickVote);
        PickComment originParentPickComment3 = createPickComment(new CommentContents("파티시엘~!"), true, new Count(0),
                new Count(1), member3, pick, member3PickVote);
        PickComment originParentPickComment4 = createPickComment(new CommentContents("댓글4"), false, new Count(0),
                new Count(0), member4, pick, member4PickVote);
        PickComment originParentPickComment5 = createPickComment(new CommentContents("댓글5"), false, new Count(0),
                new Count(0), member5, pick, null);
        PickComment originParentPickComment6 = createPickComment(new CommentContents("댓글6"), false, new Count(0),
                new Count(0), member6, pick, null);
        pickCommentRepository.saveAll(
                List.of(originParentPickComment6, originParentPickComment5, originParentPickComment4,
                        originParentPickComment3, originParentPickComment2, originParentPickComment1));

        // 픽픽픽 답글 생성
        PickComment pickReply1 = createReplidPickComment(new CommentContents("진짜 너무 좋아"), member1, pick,
                originParentPickComment1, originParentPickComment1);
        PickComment pickReply2 = createReplidPickComment(new CommentContents("너무 행복하다"), member6, pick,
                originParentPickComment1, pickReply1);
        pickReply2.changeDeletedAt(LocalDateTime.now(), member1);
        PickComment pickReply3 = createReplidPickComment(new CommentContents("사랑해요~"), member6, pick,
                originParentPickComment2, originParentPickComment2);
        pickCommentRepository.saveAll(List.of(pickReply1, pickReply2, pickReply3));

        // 추천 생성
        PickCommentRecommend pickCommentRecommend = createPickCommentRecommend(originParentPickComment1, member1, true);
        pickCommentRecommendRepository.save(pickCommentRecommend);

        em.flush();
        em.clear();

        // when // then
        ResultActions actions = mockMvc.perform(get("/devdevdev/api/v1/picks/{pickId}/comments/best",
                        pick.getId())
                        .queryParam("size", "3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().isOk());

        // docs
        actions.andDo(document("get-pick-best-comments",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                        headerWithName(AUTHORIZATION_HEADER).optional().description("Bearer 엑세스 토큰")
                ),
                pathParameters(
                        parameterWithName("pickId").description("픽픽픽 아이디")
                ),
                queryParameters(
                        parameterWithName("size").optional().description("조회되는 데이터 수(min=3, max=10)")
                ),
                responseFields(
                        fieldWithPath("resultType").type(STRING).description("응답 결과"),
                        fieldWithPath("datas").type(ARRAY).description("응답 데이터"),

                        fieldWithPath("datas.[].pickCommentId").type(NUMBER).description("픽픽픽 댓글 아이디"),
                        fieldWithPath("datas.[].createdAt").type(STRING).description("픽픽픽 댓글 작성일시"),
                        fieldWithPath("datas.[].memberId").type(NUMBER).description("픽픽픽 댓글 작성자 아이디"),
                        fieldWithPath("datas.[].author").type(STRING).description("픽픽픽 댓글 작성자 닉네임"),
                        fieldWithPath("datas.[].isCommentOfPickAuthor").type(BOOLEAN)
                                .description("댓글 작성자가 픽픽픽 작성자인지 여부"),
                        fieldWithPath("datas.[].isCommentAuthor").type(BOOLEAN)
                                .description("로그인한 회원이 댓글 작성자인지 여부"),
                        fieldWithPath("datas.[].isRecommended").type(BOOLEAN)
                                .description("로그인한 회원이 댓글 추천 여부"),
                        fieldWithPath("datas.[].maskedEmail").type(STRING).description("픽픽픽 댓글 작성자 이메일"),
                        fieldWithPath("datas.[].votedPickOption").optional().type(STRING)
                                .description("픽픽픽 투표 선택 타입").attributes(pickOptionType()),
                        fieldWithPath("datas.[].votedPickOptionTitle").optional().type(STRING)
                                .description("픽픽픽 투표 선택 타입 제목").attributes(stringOrNull()),
                        fieldWithPath("datas.[].contents").type(STRING).description("픽픽픽 댓글 내용"),
                        fieldWithPath("datas.[].replyTotalCount").type(NUMBER)
                                .description("픽픽픽 댓글의 답글 총 갯수"),
                        fieldWithPath("datas.[].likeTotalCount").type(NUMBER)
                                .description("픽픽픽 댓글 좋아요 총 갯수"),
                        fieldWithPath("datas.[].isDeleted").type(BOOLEAN)
                                .description("픽픽픽 댓글 삭제 여부"),
                        fieldWithPath("datas.[].isModified").type(BOOLEAN)
                                .description("픽픽픽 댓글 수정 여부"),

                        fieldWithPath("datas.[].replies").type(ARRAY).description("픽픽픽 답글 배열"),
                        fieldWithPath("datas.[].replies[].pickCommentId").type(NUMBER).description("픽픽픽 답글 아이디"),
                        fieldWithPath("datas.[].replies[].memberId").type(NUMBER).description("픽픽픽 답글 작성자 아이디"),
                        fieldWithPath("datas.[].replies[].pickCommentParentId").type(NUMBER)
                                .description("픽픽픽 답글의 부모 댓글 아이디"),
                        fieldWithPath("datas.[].replies[].pickCommentOriginParentId").type(NUMBER)
                                .description("픽픽픽 답글의 최상위 부모 댓글 아이디"),
                        fieldWithPath("datas.[].replies[].createdAt").type(STRING).description("픽픽픽 답글 작성일시"),
                        fieldWithPath("datas.[].replies[].isCommentOfPickAuthor").type(BOOLEAN)
                                .description("답글 작성자가 픽픽픽 작성자인지 여부"),
                        fieldWithPath("datas.[].replies[].isCommentAuthor").type(BOOLEAN)
                                .description("로그인한 회원이 답글 작성자인지 여부"),
                        fieldWithPath("datas.[].replies[].isRecommended").type(BOOLEAN)
                                .description("로그인한 회원이 답글 추천 여부"),
                        fieldWithPath("datas.[].replies[].author").type(STRING).description("픽픽픽 답글 작성자 닉네임"),
                        fieldWithPath("datas.[].replies[].maskedEmail").type(STRING)
                                .description("픽픽픽 답글 작성자 이메일"),
                        fieldWithPath("datas.[].replies[].contents").type(STRING).description("픽픽픽 답글 내용"),
                        fieldWithPath("datas.[].replies[].likeTotalCount").type(NUMBER)
                                .description("픽픽픽 답글 좋아요 총 갯수"),
                        fieldWithPath("datas.[].replies[].isDeleted").type(BOOLEAN)
                                .description("픽픽픽 답글 삭제 여부"),
                        fieldWithPath("datas.[].replies[].isModified").type(BOOLEAN)
                                .description("픽픽픽 답글 수정 여부"),
                        fieldWithPath("datas.[].replies[].parentCommentMemberId").type(NUMBER)
                                .description("픽픽픽 부모 댓글 작성자 아이디"),
                        fieldWithPath("datas.[].replies[].parentCommentAuthor").type(STRING)
                                .description("픽픽픽 부모 댓글 작성자 닉네임")
                )
        ));
    }

    private PickCommentRecommend createPickCommentRecommend(PickComment pickComment, Member member,
                                                            Boolean recommendedStatus) {
        PickCommentRecommend pickCommentRecommend = PickCommentRecommend.builder()
                .member(member)
                .recommendedStatus(recommendedStatus)
                .build();

        pickCommentRecommend.changePickComment(pickComment);

        return pickCommentRecommend;
    }

    private PickComment createPickComment(CommentContents contents, Boolean isPublic, Count recommendTotalCount,
                                          Member member, Pick pick) {
        PickComment pickComment = PickComment.builder()
                .contents(contents)
                .isPublic(isPublic)
                .createdBy(member)
                .recommendTotalCount(recommendTotalCount)
                .build();

        pickComment.changePick(pick);

        return pickComment;
    }

    private PickOption createPickOption(Title title, Count voteTotalCount, Pick pick, PickOptionType pickOptionType) {
        PickOption pickOption = PickOption.builder()
                .title(title)
                .voteTotalCount(voteTotalCount)
                .pickOptionType(pickOptionType)
                .build();

        pickOption.changePick(pick);

        return pickOption;
    }

    private Pick createPick(Title title, ContentStatus contentStatus, Count commentTotalCount, Member member) {
        return Pick.builder()
                .title(title)
                .contentStatus(contentStatus)
                .commentTotalCount(commentTotalCount)
                .member(member)
                .build();
    }

    private PickComment createPickComment(CommentContents contents, Boolean isPublic, Count replyTotalCount,
                                          Count recommendTotalCount, Member member, Pick pick, PickVote pickVote) {
        PickComment pickComment = PickComment.builder()
                .contents(contents)
                .isPublic(isPublic)
                .createdBy(member)
                .replyTotalCount(replyTotalCount)
                .recommendTotalCount(recommendTotalCount)
                .pick(pick)
                .pickVote(pickVote)
                .build();

        pickComment.changePick(pick);

        return pickComment;
    }

    private PickComment createReplidPickComment(CommentContents contents, Member member, Pick pick,
                                                PickComment originParent, PickComment parent) {
        PickComment pickComment = PickComment.builder()
                .contents(contents)
                .createdBy(member)
                .pick(pick)
                .originParent(originParent)
                .isPublic(false)
                .parent(parent)
                .recommendTotalCount(new Count(0))
                .replyTotalCount(new Count(0))
                .build();

        pickComment.changePick(pick);

        return pickComment;
    }

    private PickComment createReplidPickComment(CommentContents contents, Boolean isPublic, Member member, Pick pick,
                                                PickComment originParent, PickComment parent) {
        PickComment pickComment = PickComment.builder()
                .contents(contents)
                .isPublic(isPublic)
                .createdBy(member)
                .pick(pick)
                .originParent(originParent)
                .parent(parent)
                .replyTotalCount(new Count(0))
                .build();

        pickComment.changePick(pick);

        return pickComment;
    }

    private PickComment createPickComment(CommentContents contents, Boolean isPublic, Member member, Pick pick) {
        PickComment pickComment = PickComment.builder()
                .contents(contents)
                .isPublic(isPublic)
                .replyTotalCount(new Count(0))
                .createdBy(member)
                .pick(pick)
                .build();

        pickComment.changePick(pick);

        return pickComment;
    }

    private Pick createPick(Title title, ContentStatus contentStatus, Member member) {
        return Pick.builder()
                .title(title)
                .contentStatus(contentStatus)
                .member(member)
                .build();
    }

    private Pick createPick(Title title, Count pickVoteCount, Count commentTotalCount, Member member,
                            ContentStatus contentStatus, List<Double> embeddings) {
        return Pick.builder()
                .title(title)
                .voteTotalCount(pickVoteCount)
                .commentTotalCount(commentTotalCount)
                .member(member)
                .contentStatus(contentStatus)
                .embeddings(embeddings)
                .build();
    }

    private PickOption createPickOption(Title title, Count voteTotalCount, PickOptionType pickOptionType, Pick pick) {
        PickOption pickOption = PickOption.builder()
                .title(title)
                .voteTotalCount(voteTotalCount)
                .pickOptionType(pickOptionType)
                .build();

        pickOption.changePick(pick);

        return pickOption;
    }

    private Pick createPick(Title title, Count viewTotalCount, Count commentTotalCount, Count voteTotalCount,
                            Count poplarScore, ContentStatus contentStatus, Member member) {
        return Pick.builder()
                .title(title)
                .viewTotalCount(viewTotalCount)
                .voteTotalCount(voteTotalCount)
                .commentTotalCount(commentTotalCount)
                .popularScore(poplarScore)
                .contentStatus(contentStatus)
                .member(member)
                .build();
    }

    private PickOptionImage createPickOptionImage(String name, String imageUrl, PickOption pickOption) {
        PickOptionImage pickOptionImage = PickOptionImage.builder()
                .name(name)
                .imageUrl(imageUrl)
                .imageKey("imageKey")
                .build();

        pickOptionImage.changePickOption(pickOption);

        return pickOptionImage;
    }

    private PickVote createPickVote(Member member, PickOption pickOption, Pick pick) {
        return PickVote.builder()
                .member(member)
                .pickOption(pickOption)
                .pick(pick)
                .build();
    }

    private Pick createPick(Title title, Count count, Member member) {
        return Pick.builder()
                .title(title)
                .voteTotalCount(count)
                .member(member)
                .build();
    }

    private Pick createPick(Title title, Member member) {
        return Pick.builder()
                .title(title)
                .member(member)
                .build();
    }

    private PickOptionImage createPickOptionImage(String name) {
        return PickOptionImage.builder()
                .name(name)
                .imageUrl("imageUrl")
                .imageKey("imageKey")
                .build();
    }

    private PickOption createPickOption(Pick pick, Title title, PickOptionContents pickOptionContents, Count count,
                                        PickOptionType pickOptionType) {
        PickOption pickOption = PickOption.builder()
                .title(title)
                .contents(pickOptionContents)
                .voteTotalCount(count)
                .pickOptionType(pickOptionType)
                .build();

        pickOption.changePick(pick);

        return pickOption;
    }

    private PickOptionImage createPickOptionImage(String name, PickOption pickOption) {
        PickOptionImage pickOptionImage = PickOptionImage.builder()
                .name(name)
                .imageUrl("imageUrl")
                .imageKey("imageKey")
                .build();

        pickOptionImage.changePickOption(pickOption);

        return pickOptionImage;
    }


    private ModifyPickRequest createModifyPickRequest(String pickTitle,
                                                      Map<PickOptionType, ModifyPickOptionRequest> modifyPickOptionRequests) {
        return ModifyPickRequest.builder()
                .pickTitle(pickTitle)
                .pickOptions(modifyPickOptionRequests)
                .build();
    }

    private PickOptionImage createPickOptionImage(String name, String imageUrl, String imageKey) {
        return PickOptionImage.builder()
                .name(name)
                .imageUrl(imageUrl)
                .imageKey(imageKey)
                .build();
    }

    private RegisterPickRequest createPickRegisterRequest(String pickTitle,
                                                          Map<PickOptionType, RegisterPickOptionRequest> pickOptions) {
        return RegisterPickRequest.builder()
                .pickTitle(pickTitle)
                .pickOptions(pickOptions)
                .build();
    }

    private RegisterPickOptionRequest createPickOptionRequest(String pickOptionTitle, String pickOptionContent,
                                                              List<Long> pickOptionImageIds) {
        return RegisterPickOptionRequest.builder()
                .pickOptionTitle(pickOptionTitle)
                .pickOptionContent(pickOptionContent)
                .pickOptionImageIds(pickOptionImageIds)
                .build();
    }


    private ObjectMetadata createObjectMetadataByMultipartFile(MultipartFile multipartFile) {
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(multipartFile.getContentType());
        objectMetadata.setContentLength(multipartFile.getSize());

        return objectMetadata;
    }

    private PutObjectRequest createPutObjectRequest(String bucket, String key, MultipartFile multipartFile,
                                                    ObjectMetadata objectMetadata) throws IOException {

        return new PutObjectRequest(bucket, key, multipartFile.getInputStream(), objectMetadata);
    }

    private MockMultipartFile createMockMultipartFile(String name, String originalFilename) {
        return new MockMultipartFile(
                name,
                originalFilename,
                MediaType.IMAGE_PNG_VALUE,
                name.getBytes()
        );
    }

    private MockMultipartFile createMockMultipartFile(String name, String originalFilename, String mediaType) {
        return new MockMultipartFile(
                name,
                originalFilename,
                mediaType,
                name.getBytes()
        );
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

    private Pick createPick(Title title, Count pickVoteTotalCount, Count pickViewTotalCount,
                            Count pickcommentTotalCount, String thumbnailUrl, String author,
                            ContentStatus contentStatus,
                            List<PickOption> pickOptions, List<PickVote> pickVotes
    ) {

        Pick pick = Pick.builder()
                .title(title)
                .voteTotalCount(pickVoteTotalCount)
                .viewTotalCount(pickViewTotalCount)
                .commentTotalCount(pickcommentTotalCount)
                .thumbnailUrl(thumbnailUrl)
                .author(author)
                .contentStatus(contentStatus)
                .build();

        pick.changePickOptions(pickOptions);
        pick.changePickVote(pickVotes);

        return pick;
    }

    private PickOption createPickOption(Title title, PickOptionContents pickOptionContents, Count voteTotalCount) {
        return PickOption.builder()
                .title(title)
                .contents(pickOptionContents)
                .voteTotalCount(voteTotalCount)
                .build();
    }

    private PickOption createPickOption(Pick pick, Title title, PickOptionContents pickOptionContents,
                                        PickOptionType pickOptionType) {
        PickOption pickOption = PickOption.builder()
                .title(title)
                .contents(pickOptionContents)
                .pickOptionType(pickOptionType)
                .build();

        pickOption.changePick(pick);

        return pickOption;
    }

    private PickOptionImage createPickOptionImage(String imageUrl, String imageKey) {
        return PickOptionImage.builder()
                .name("imageName")
                .imageUrl(imageUrl)
                .imageKey(imageKey)
                .build();
    }
}
