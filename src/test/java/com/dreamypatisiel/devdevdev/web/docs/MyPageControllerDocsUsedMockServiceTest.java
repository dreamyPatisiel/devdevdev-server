package com.dreamypatisiel.devdevdev.web.docs;

import static com.dreamypatisiel.devdevdev.global.constant.SecurityConstant.AUTHORIZATION_HEADER;
import static com.dreamypatisiel.devdevdev.web.docs.format.ApiDocsFormatGenerator.authenticationType;
import static com.dreamypatisiel.devdevdev.web.docs.format.ApiDocsFormatGenerator.commentIdType;
import static com.dreamypatisiel.devdevdev.web.docs.format.ApiDocsFormatGenerator.myWrittenCommentSort;
import static com.dreamypatisiel.devdevdev.web.docs.format.ApiDocsFormatGenerator.stringOrNull;
import static com.dreamypatisiel.devdevdev.web.docs.format.ApiDocsFormatGenerator.uniqueCommentIdType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.JsonFieldType.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.enums.PickOptionType;
import com.dreamypatisiel.devdevdev.domain.entity.enums.Role;
import com.dreamypatisiel.devdevdev.domain.entity.enums.SocialType;
import com.dreamypatisiel.devdevdev.domain.repository.member.MemberRepository;
import com.dreamypatisiel.devdevdev.domain.service.member.MemberService;
import com.dreamypatisiel.devdevdev.global.constant.SecurityConstant;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.SocialMemberDto;
import com.dreamypatisiel.devdevdev.web.dto.SliceCustom;
import com.dreamypatisiel.devdevdev.web.dto.response.comment.MyWrittenCommentResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import com.dreamypatisiel.devdevdev.web.dto.response.subscription.SubscribedCompanyResponse;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;

public class MyPageControllerDocsUsedMockServiceTest extends SupportControllerDocsTest {

    @Autowired
    MemberRepository memberRepository;
    @MockBean
    MemberService memberService;

    @Test
    @DisplayName("회원이 내가 썼어요 댓글을 조회한다.")
    void getMyWrittenComments() throws Exception {
        // given
        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        Pageable pageable = PageRequest.of(0, 6);
        LocalDateTime now = LocalDateTime.of(2025, 1, 1, 0, 0, 0);

        // 응답 생성
        MyWrittenCommentResponse pickWrittenComment = createMyWrittenCommentResponse(
                "PICK_1_1", 1L, "픽픽픽 제목", 1L, "PICK", "픽픽픽 댓글입니다.", 111L, now, "픽픽픽 A",
                PickOptionType.firstPickOption.name());
        MyWrittenCommentResponse techWrittenComment = createMyWrittenCommentResponse(
                "TECH_1_1", 1L, "기술블로그 제목", 1L, "TECH", "기술블로그 댓글입니다.", 54321L, now, null, null);
        List<MyWrittenCommentResponse> myWrittenComments = List.of(pickWrittenComment, techWrittenComment);
        SliceCustom<MyWrittenCommentResponse> result = new SliceCustom<>(myWrittenComments, pageable, false, 2L);

        // when
        when(memberService.findMyWrittenComments(eq(pageable), any(), any())).thenReturn(result);

        // then
        ResultActions actions = mockMvc.perform(get("/devdevdev/api/v1/mypage/comments")
                        .queryParam("size", String.valueOf(pageable.getPageSize()))
                        .queryParam("pickCommentId", "1000")
                        .queryParam("techCommentId", "1000")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header(SecurityConstant.AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken))
                .andDo(print())
                .andExpect(status().isOk());

        // docs
        actions.andDo(document("mypage-comments",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                        headerWithName(AUTHORIZATION_HEADER).description("Bearer 엑세스 토큰")
                ),
                queryParameters(
                        parameterWithName("size").optional().description("조회되는 데이터 수"),
                        parameterWithName("pickCommentId").optional().description("가장 작은 픽픽픽 아이디"),
                        parameterWithName("techCommentId").optional().description("가장 작은 기술블로그 아이디"),
                        parameterWithName("commentFilter").optional().description("댓글 정렬 기준")
                                .attributes(myWrittenCommentSort())
                ),
                responseFields(
                        fieldWithPath("resultType").type(JsonFieldType.STRING).description("응답 결과"),
                        fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),

                        fieldWithPath("data.content").type(JsonFieldType.ARRAY).description("기술블로그 메인 배열"),
                        fieldWithPath("data.content.[].uniqueCommentId").type(JsonFieldType.STRING)
                                .description("댓글 유니크 아이디").attributes(uniqueCommentIdType(), authenticationType()),
                        fieldWithPath("data.content.[].postId").type(JsonFieldType.NUMBER)
                                .description("픽픽픽 | 기술블로그 아이디").attributes(authenticationType()),
                        fieldWithPath("data.content.[].postTitle").type(JsonFieldType.STRING)
                                .description("픽픽픽 | 기술블로그 제목").attributes(authenticationType()),
                        fieldWithPath("data.content.[].commentId").type(JsonFieldType.NUMBER)
                                .description("픽픽픽 | 기술블로그 댓글 아이디").attributes(authenticationType()),
                        fieldWithPath("data.content.[].commentType").type(JsonFieldType.STRING)
                                .description("픽픽픽 | 기술블로그 댓글 타입").attributes(commentIdType(), authenticationType()),
                        fieldWithPath("data.content.[].commentContents").type(JsonFieldType.STRING)
                                .description("픽픽픽 | 기술블로그 댓글 내용").attributes(authenticationType()),
                        fieldWithPath("data.content.[].commentRecommendTotalCount").type(JsonFieldType.NUMBER)
                                .description("픽픽픽 | 기술블로그 댓글 추천 갯수").attributes(authenticationType()),
                        fieldWithPath("data.content.[].commentCreatedAt").type(JsonFieldType.STRING)
                                .description("픽픽픽 | 기술블로그 댓글 생성일").attributes(authenticationType()),
                        fieldWithPath("data.content.[].pickOptionTitle").optional().type(JsonFieldType.STRING)
                                .description("픽픽픽 옵션 제목").attributes(stringOrNull(), authenticationType()),
                        fieldWithPath("data.content.[].pickOptionType").optional().type(JsonFieldType.STRING)
                                .description("픽픽픽 옵션 타입").attributes(stringOrNull(), authenticationType()),

                        fieldWithPath("data.pageable").type(JsonFieldType.OBJECT).description("페이지네이션 정보")
                                .attributes(authenticationType()),
                        fieldWithPath("data.pageable.pageNumber").type(JsonFieldType.NUMBER).description("페이지 번호")
                                .attributes(authenticationType()),
                        fieldWithPath("data.pageable.pageSize").type(JsonFieldType.NUMBER).description("페이지 사이즈")
                                .attributes(authenticationType()),

                        fieldWithPath("data.pageable.sort").type(JsonFieldType.OBJECT).description("정렬 정보")
                                .attributes(authenticationType()),
                        fieldWithPath("data.pageable.sort.empty").type(JsonFieldType.BOOLEAN)
                                .description("정렬 정보가 비어있는지 여부").attributes(authenticationType()),
                        fieldWithPath("data.pageable.sort.sorted").type(JsonFieldType.BOOLEAN).description("정렬 여부")
                                .attributes(authenticationType()),
                        fieldWithPath("data.pageable.sort.unsorted").type(JsonFieldType.BOOLEAN).description("비정렬 여부")
                                .attributes(authenticationType()),

                        fieldWithPath("data.pageable.offset").type(JsonFieldType.NUMBER)
                                .description("페이지 오프셋 (페이지 크기 * 페이지 번호)").attributes(authenticationType()),
                        fieldWithPath("data.pageable.paged").type(JsonFieldType.BOOLEAN).description("페이지 정보 포함 여부")
                                .attributes(authenticationType()),
                        fieldWithPath("data.pageable.unpaged").type(JsonFieldType.BOOLEAN).description("페이지 정보 비포함 여부")
                                .attributes(authenticationType()),

                        fieldWithPath("data.totalElements").type(JsonFieldType.NUMBER).description("데이터 전체 갯수")
                                .attributes(authenticationType()),
                        fieldWithPath("data.first").type(JsonFieldType.BOOLEAN).description("현재 페이지가 첫 페이지 여부")
                                .attributes(authenticationType()),
                        fieldWithPath("data.last").type(JsonFieldType.BOOLEAN).description("현재 페이지가 마지막 페이지 여부")
                                .attributes(authenticationType()),
                        fieldWithPath("data.size").type(JsonFieldType.NUMBER).description("페이지 크기")
                                .attributes(authenticationType()),
                        fieldWithPath("data.number").type(JsonFieldType.NUMBER).description("현재 페이지")
                                .attributes(authenticationType()),

                        fieldWithPath("data.sort").type(JsonFieldType.OBJECT).description("정렬 정보")
                                .attributes(authenticationType()),
                        fieldWithPath("data.sort.empty").type(JsonFieldType.BOOLEAN).description("정렬 정보가 비어있는지 여부")
                                .attributes(authenticationType()),
                        fieldWithPath("data.sort.sorted").type(JsonFieldType.BOOLEAN).description("정렬 상태 여부")
                                .attributes(authenticationType()),
                        fieldWithPath("data.sort.unsorted").type(JsonFieldType.BOOLEAN).description("비정렬 상태 여부")
                                .attributes(authenticationType()),
                        fieldWithPath("data.numberOfElements").type(JsonFieldType.NUMBER).description("현재 페이지 데이터 수")
                                .attributes(authenticationType()),
                        fieldWithPath("data.empty").type(JsonFieldType.BOOLEAN).description("현재 빈 페이지 여부")
                                .attributes(authenticationType())
                )
        ));
    }

    @Test
    @DisplayName("회원이 작성한 댓글을 조회할 때 회원이 유효하지 않으면 예외가 발생한다.")
    void getMyWrittenCommentsMemberException() throws Exception {
        // given
        Pageable pageable = PageRequest.of(0, 6);

        // when // then
        ResultActions actions = mockMvc.perform(get("/devdevdev/api/v1/mypage/comments")
                        .queryParam("size", String.valueOf(pageable.getPageSize()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().isUnauthorized());

        // docs
        actions.andDo(document("mypage-comments-member-exception",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                responseFields(
                        fieldWithPath("resultType").type(JsonFieldType.STRING).description("응답 결과"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("에러 메시지"),
                        fieldWithPath("errorCode").type(JsonFieldType.NUMBER).description("에러 코드")
                )
        ));
    }

    @Disabled
    @Test
    @DisplayName("회원이 작성한 댓글을 조회할 때 기술블로그 댓글 아이디가 없으면 예외가 발생한다.")
    void getMyWrittenCommentsTechCommentIdBindException() throws Exception {
        // given
        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        Pageable pageable = PageRequest.of(0, 6);

        // when // then
        ResultActions actions = mockMvc.perform(get("/devdevdev/api/v1/mypage/comments")
                        .queryParam("size", String.valueOf(pageable.getPageSize()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header(AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken))
                .andDo(print())
                .andExpect(status().isBadRequest());

        // docs
        actions.andDo(document("mypage-comments-tech-comment-id-bind-exception",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                        headerWithName(AUTHORIZATION_HEADER).description("Bearer 엑세스 토큰")
                ),
                responseFields(
                        fieldWithPath("resultType").type(JsonFieldType.STRING).description("응답 결과"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("에러 메시지"),
                        fieldWithPath("errorCode").type(JsonFieldType.NUMBER).description("에러 코드")
                )
        ));
    }

    @Disabled
    @Test
    @DisplayName("회원이 작성한 댓글을 조회할 때 픽픽픽 댓글 아이디가 없으면 예외가 발생한다.")
    void getMyWrittenCommentsPickCommentIdBindException() throws Exception {
        // given
        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        Pageable pageable = PageRequest.of(0, 6);

        // when // then
        ResultActions actions = mockMvc.perform(get("/devdevdev/api/v1/mypage/comments")
                        .queryParam("size", String.valueOf(pageable.getPageSize()))
                        .queryParam("techCommentId", "1000")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header(AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken))
                .andDo(print())
                .andExpect(status().isBadRequest());

        // docs
        actions.andDo(document("mypage-comments-pick-comment-id-bind-exception",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                        headerWithName(AUTHORIZATION_HEADER).description("Bearer 엑세스 토큰")
                ),
                responseFields(
                        fieldWithPath("resultType").type(JsonFieldType.STRING).description("응답 결과"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("에러 메시지"),
                        fieldWithPath("errorCode").type(JsonFieldType.NUMBER).description("에러 코드")
                )
        ));
    }

    @Test
    @DisplayName("회원이 커서 방식으로 다음페이지의 자신이 구독한 기업 목록을 조회하여 응답을 생성한다.")
    void findMySubscribedCompaniesByCursor() throws Exception {
        // given
        Pageable pageable = PageRequest.of(0, 1);
        long cursorCompanyId = 999L;
        SubscribedCompanyResponse subscribedCompanyResponse = new SubscribedCompanyResponse(
                1L, "Toss", "https://image.net/image.png", true);
        List<SubscribedCompanyResponse> content = List.of(subscribedCompanyResponse);
        SliceCustom<SubscribedCompanyResponse> response = new SliceCustom<>(content, pageable, 1L);

        when(memberService.findMySubscribedCompanies(any(), any(), any())).thenReturn(response);

        // when
        ResultActions actions = mockMvc.perform(get(DEFAULT_PATH_V1 + "/mypage/subscriptions/companies")
                        .queryParam("size", String.valueOf(pageable.getPageSize()))
                        .queryParam("companyId", Long.toString(cursorCompanyId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(SecurityConstant.AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().isOk());

        actions.andDo(document("subscribed-companies",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                        headerWithName(SecurityConstant.AUTHORIZATION_HEADER).description("Bearer 엑세스 토큰")
                ),
                queryParameters(
                        parameterWithName("size").optional().description("조회되는 데이터 수"),
                        parameterWithName("companyId").optional().description("커서(마지막 기업 아이디)")
                ),
                responseFields(
                        fieldWithPath("resultType").type(STRING).description("응답 결과"),
                        fieldWithPath("data").type(OBJECT).description("응답 데이터"),

                        fieldWithPath("data.content").type(ARRAY).description("구독한 기업 목록 메인 배열"),
                        fieldWithPath("data.content[].companyId").type(NUMBER).description("기업 아이디"),
                        fieldWithPath("data.content[].companyName").type(STRING).description("기업 이름"),
                        fieldWithPath("data.content[].companyImageUrl").type(STRING).description("기업 로고 이미지 url"),
                        fieldWithPath("data.content[].isSubscribed").type(JsonFieldType.BOOLEAN).description("회원의 구독 여부"),

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

                        fieldWithPath("data.first").type(BOOLEAN).description("현재 페이지가 첫 페이지 여부"),
                        fieldWithPath("data.last").type(BOOLEAN).description("현재 페이지가 마지막 페이지 여부"),
                        fieldWithPath("data.size").type(NUMBER).description("페이지 크기"),
                        fieldWithPath("data.number").type(NUMBER).description("현재 페이지"),

                        fieldWithPath("data.sort").type(OBJECT).description("정렬 정보"),
                        fieldWithPath("data.sort.empty").type(BOOLEAN).description("정렬 정보가 비어있는지 여부"),
                        fieldWithPath("data.sort.sorted").type(BOOLEAN).description("정렬 상태 여부"),
                        fieldWithPath("data.sort.unsorted").type(BOOLEAN).description("비정렬 상태 여부"),
                        fieldWithPath("data.numberOfElements").type(NUMBER).description("현재 페이지 데이터 수"),
                        fieldWithPath("data.totalElements").type(NUMBER).description("전체 데이터 수"),
                        fieldWithPath("data.empty").type(BOOLEAN).description("현재 빈 페이지 여부")
                )
        ));
    }

    private static MyWrittenCommentResponse createMyWrittenCommentResponse(String uniqueCommentId, Long postId,
                                                                           String postTitle,
                                                                           Long commentId,
                                                                           String commentType,
                                                                           String commentContents,
                                                                           Long commentRecommendTotalCount,
                                                                           LocalDateTime commentCreatedAt,
                                                                           String pickOptionTitle,
                                                                           String pickOptionType) {
        return MyWrittenCommentResponse.builder()
                .uniqueCommentId(uniqueCommentId)
                .postId(postId)
                .postTitle(postTitle)
                .commentId(commentId)
                .commentType(commentType)
                .commentContents(commentContents)
                .commentRecommendTotalCount(commentRecommendTotalCount)
                .commentCreatedAt(commentCreatedAt)
                .pickOptionTitle(pickOptionTitle)
                .pickOptionType(pickOptionType)
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
}
