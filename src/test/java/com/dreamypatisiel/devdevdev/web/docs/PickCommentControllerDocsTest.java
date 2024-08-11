package com.dreamypatisiel.devdevdev.web.docs;

import static com.dreamypatisiel.devdevdev.global.constant.SecurityConstant.AUTHORIZATION_HEADER;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
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
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.Pick;
import com.dreamypatisiel.devdevdev.domain.entity.PickComment;
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
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickCommentRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickOptionImageRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickOptionRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickVoteRepository;
import com.dreamypatisiel.devdevdev.global.constant.SecurityConstant;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.SocialMemberDto;
import com.dreamypatisiel.devdevdev.web.controller.pick.request.ModifyPickCommentRequest;
import com.dreamypatisiel.devdevdev.web.controller.pick.request.ModifyPickOptionRequest;
import com.dreamypatisiel.devdevdev.web.controller.pick.request.ModifyPickRequest;
import com.dreamypatisiel.devdevdev.web.controller.pick.request.RegisterPickCommentRequest;
import com.dreamypatisiel.devdevdev.web.controller.pick.request.RegisterPickOptionRequest;
import com.dreamypatisiel.devdevdev.web.controller.pick.request.RegisterPickRequest;
import com.dreamypatisiel.devdevdev.web.response.ResultType;
import jakarta.persistence.EntityManager;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
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

        RegisterPickCommentRequest registerPickCommentRequest = new RegisterPickCommentRequest("안녕하세웅", true);

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
                        headerWithName(AUTHORIZATION_HEADER).optional().description("Bearer 엑세스 토큰")
                ),
                pathParameters(
                        parameterWithName("pickId").description("픽픽픽 아이디")
                ),
                requestFields(
                        fieldWithPath("contents").type(STRING).description("픽픽픽 댓글 내용(최소 1자 이상 최대 1,000자 이하)"),
                        fieldWithPath("pickOptionId").type(NUMBER).description("픽픽픽 선택지 아이디").optional(),
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
                        headerWithName(AUTHORIZATION_HEADER).optional().description("Bearer 엑세스 토큰")
                ),
                pathParameters(
                        parameterWithName("pickId").description("픽픽픽 아이디")
                ),
                requestFields(
                        fieldWithPath("contents").type(STRING).description("픽픽픽 댓글 내용(최소 1자 이상 최대 1,000자 이하)"),
                        fieldWithPath("pickOptionId").type(NUMBER).description("픽픽픽 선택지 아이디").optional(),
                        fieldWithPath("isPickVotePublic").type(NULL).description("픽픽픽 공개 여부")
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
                        headerWithName(AUTHORIZATION_HEADER).optional().description("Bearer 엑세스 토큰")
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
                        headerWithName(AUTHORIZATION_HEADER).optional().description("Bearer 엑세스 토큰")
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

    private PickComment createPickComment(CommentContents contents, Boolean isPublic, Member member, Pick pick) {
        PickComment pickComment = PickComment.builder()
                .contents(contents)
                .isPublic(isPublic)
                .member(member)
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
