package com.dreamypatisiel.devdevdev.web.controller;

import static com.dreamypatisiel.devdevdev.web.dto.response.ResultType.SUCCESS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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
import com.dreamypatisiel.devdevdev.web.dto.response.ResultType;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

public class MyPageControllerUsedMockServiceTest extends SupportControllerTest {

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
        mockMvc.perform(get("/devdevdev/api/v1/mypage/comments")
                        .queryParam("size", String.valueOf(pageable.getPageSize()))
                        .queryParam("pickCommentId", "1000")
                        .queryParam("techCommentId", "1000")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header(SecurityConstant.AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultType").value(SUCCESS.name()))
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.[0].uniqueCommentId").isString())
                .andExpect(jsonPath("$.data.content.[0].postId").isNumber())
                .andExpect(jsonPath("$.data.content.[0].postTitle").isString())
                .andExpect(jsonPath("$.data.content.[0].commentId").isNumber())
                .andExpect(jsonPath("$.data.content.[0].commentType").isString())
                .andExpect(jsonPath("$.data.content.[0].commentContents").isString())
                .andExpect(jsonPath("$.data.content.[0].commentRecommendTotalCount").isNumber())
                .andExpect(jsonPath("$.data.content.[0].commentCreatedAt").isString())
                .andExpect(jsonPath("$.data.content.[0].pickOptionTitle").isString())
                .andExpect(jsonPath("$.data.content.[0].pickOptionType").isString())
                .andExpect(jsonPath("$.data.content.[1].uniqueCommentId").isString())
                .andExpect(jsonPath("$.data.content.[1].postId").isNumber())
                .andExpect(jsonPath("$.data.content.[1].postTitle").isString())
                .andExpect(jsonPath("$.data.content.[1].commentId").isNumber())
                .andExpect(jsonPath("$.data.content.[1].commentType").isString())
                .andExpect(jsonPath("$.data.content.[1].commentContents").isString())
                .andExpect(jsonPath("$.data.content.[1].commentCreatedAt").isString())
                .andExpect(jsonPath("$.data.content.[1].pickOptionTitle").isEmpty())
                .andExpect(jsonPath("$.data.content.[1].pickOptionType").isEmpty())
                .andExpect(jsonPath("$.data.pageable.pageNumber").isNumber())
                .andExpect(jsonPath("$.data.pageable.pageSize").isNumber())
                .andExpect(jsonPath("$.data.pageable.sort").isNotEmpty())
                .andExpect(jsonPath("$.data.pageable.sort.empty").isBoolean())
                .andExpect(jsonPath("$.data.pageable.sort.sorted").isBoolean())
                .andExpect(jsonPath("$.data.pageable.sort.unsorted").isBoolean())
                .andExpect(jsonPath("$.data.pageable.offset").isNumber())
                .andExpect(jsonPath("$.data.pageable.paged").isBoolean())
                .andExpect(jsonPath("$.data.pageable.unpaged").isBoolean())
                .andExpect(jsonPath("$.data.totalElements").value(2))
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
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.resultType").value(ResultType.FAIL.name()))
                .andExpect(jsonPath("$.message").isString())
                .andExpect(jsonPath("$.errorCode").value(HttpStatus.UNAUTHORIZED.value()));
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
        mockMvc.perform(get("/devdevdev/api/v1/mypage/comments")
                        .queryParam("size", String.valueOf(pageable.getPageSize()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header(SecurityConstant.AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultType").value(ResultType.FAIL.name()))
                .andExpect(jsonPath("$.message").isString())
                .andExpect(jsonPath("$.errorCode").value(HttpStatus.BAD_REQUEST.value()));
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
        mockMvc.perform(get("/devdevdev/api/v1/mypage/comments")
                        .queryParam("size", String.valueOf(pageable.getPageSize()))
                        .queryParam("techCommentId", "1000")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header(SecurityConstant.AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultType").value(ResultType.FAIL.name()))
                .andExpect(jsonPath("$.message").isString())
                .andExpect(jsonPath("$.errorCode").value(HttpStatus.BAD_REQUEST.value()));
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
        mockMvc.perform(get(DEFAULT_PATH_V1 + "/mypage/subscriptions/companies")
                        .queryParam("size", String.valueOf(pageable.getPageSize()))
                        .queryParam("companyId", Long.toString(cursorCompanyId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(SecurityConstant.AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.[0].companyId").isNumber())
                .andExpect(jsonPath("$.data.content.[0].companyName").isString())
                .andExpect(jsonPath("$.data.content.[0].companyImageUrl").isString())
                .andExpect(jsonPath("$.data.content.[0].isSubscribed").isBoolean())
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
