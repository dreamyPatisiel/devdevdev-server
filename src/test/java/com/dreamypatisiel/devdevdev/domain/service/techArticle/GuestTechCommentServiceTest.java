package com.dreamypatisiel.devdevdev.domain.service.techArticle;

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
import static com.dreamypatisiel.devdevdev.domain.exception.GuestExceptionMessage.INVALID_ANONYMOUS_CAN_NOT_USE_THIS_FUNCTION_MESSAGE;
import com.dreamypatisiel.devdevdev.domain.repository.CompanyRepository;
import com.dreamypatisiel.devdevdev.domain.repository.member.MemberRepository;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechArticleRepository;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechCommentRecommendRepository;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechCommentRepository;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechCommentSort;
import com.dreamypatisiel.devdevdev.domain.service.techArticle.techComment.GuestTechCommentService;
import com.dreamypatisiel.devdevdev.global.common.TimeProvider;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.SocialMemberDto;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.UserPrincipal;
import com.dreamypatisiel.devdevdev.global.utils.AuthenticationMemberUtils;
import static com.dreamypatisiel.devdevdev.global.utils.AuthenticationMemberUtils.INVALID_METHODS_CALL_MESSAGE;
import com.dreamypatisiel.devdevdev.web.dto.SliceCommentCustom;
import com.dreamypatisiel.devdevdev.web.dto.request.techArticle.RegisterTechCommentRequest;
import com.dreamypatisiel.devdevdev.web.dto.response.techArticle.TechCommentsResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.techArticle.TechRepliedCommentsResponse;
import com.dreamypatisiel.devdevdev.web.dto.util.CommonResponseUtil;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
public class GuestTechCommentServiceTest {

    @Autowired
    GuestTechCommentService guestTechCommentService;

    @Autowired
    TechArticleRepository techArticleRepository;

    @Autowired
    TechCommentRepository techCommentRepository;

    @Autowired
    CompanyRepository companyRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    TechCommentRecommendRepository techCommentRecommendRepository;

    @Autowired
    TimeProvider timeProvider;

    @Autowired
    EntityManager em;

    String userId = "dreamy5patisiel";
    String name = "꿈빛파티시엘";
    String nickname = "행복한 꿈빛파티시엘";
    String email = "dreamy5patisiel@kakao.com";
    String password = "password";
    String socialType = SocialType.KAKAO.name();
    String role = Role.ROLE_USER.name();

    @Test
    @DisplayName("익명 회원은 기술블로그 댓글을 작성할 수 없다.")
    void registerTechComment() {
        // given
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        Authentication authentication = mock(Authentication.class);

        Company company = createCompany("꿈빛 파티시엘", "https://example.png", "https://example.com", "https://example.com");
        companyRepository.save(company);

        TechArticle techArticle = TechArticle.createTechArticle(new Title("기술블로그 제목"), new Url("https://example.com"),
                new Count(1L),
                new Count(1L), new Count(1L), new Count(1L), null, company);
        TechArticle savedTechArticle = techArticleRepository.save(techArticle);
        Long id = savedTechArticle.getId();

        RegisterTechCommentRequest registerTechCommentRequest = new RegisterTechCommentRequest("댓글입니다.");

        // when // then
        assertThatThrownBy(() -> guestTechCommentService.registerMainTechComment(
                id, registerTechCommentRequest, authentication))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage(INVALID_ANONYMOUS_CAN_NOT_USE_THIS_FUNCTION_MESSAGE);
    }

    @Test
    @DisplayName("익명 회원은 기술블로그 댓글에 답글을 작성할 수 없다.")
    void registerRepliedTechComment() {
        // given
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        Authentication authentication = mock(Authentication.class);

        Company company = createCompany("꿈빛 파티시엘", "https://example.png", "https://example.com", "https://example.com");
        companyRepository.save(company);

        TechArticle techArticle = TechArticle.createTechArticle(new Title("기술블로그 제목"), new Url("https://example.com"),
                new Count(1L),
                new Count(1L), new Count(1L), new Count(1L), null, company);
        techArticleRepository.save(techArticle);
        Long techArticleId = techArticle.getId();

        TechComment parentTechComment = TechComment.createMainTechComment(new CommentContents("댓글입니다."), member,
                techArticle);
        techCommentRepository.save(parentTechComment);
        Long parentTechCommentId = parentTechComment.getId();

        RegisterTechCommentRequest registerRepliedTechComment = new RegisterTechCommentRequest("답글입니다.");

        // when // then
        assertThatThrownBy(() -> guestTechCommentService.registerRepliedTechComment(
                techArticleId, parentTechCommentId, parentTechCommentId, registerRepliedTechComment, authentication))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage(INVALID_ANONYMOUS_CAN_NOT_USE_THIS_FUNCTION_MESSAGE);
    }

    @Test
    @DisplayName("익명 회원은 기술블로그 댓글을 추천할 수 없다.")
    void recommendTechComment() {
        // given
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        Authentication authentication = mock(Authentication.class);

        Company company = createCompany("꿈빛 파티시엘", "https://example.png", "https://example.com", "https://example.com");
        companyRepository.save(company);

        TechArticle techArticle = TechArticle.createTechArticle(new Title("기술블로그 제목"), new Url("https://example.com"),
                new Count(1L),
                new Count(1L), new Count(2L), new Count(1L), null, company);
        techArticleRepository.save(techArticle);
        Long techArticleId = techArticle.getId();

        TechComment techComment = TechComment.createMainTechComment(new CommentContents("댓글입니다."), member, techArticle);
        techCommentRepository.save(techComment);

        // when // then
        assertThatThrownBy(() -> guestTechCommentService.recommendTechComment(
                techArticleId, techComment.getId(), authentication))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage(INVALID_ANONYMOUS_CAN_NOT_USE_THIS_FUNCTION_MESSAGE);
    }

    @Test
    @DisplayName("익명 회원은 커서 방식으로 기술블로그 댓글/답글을 조회할 수 있다. (등록순)")
    void getTechCommentsSortByOLDEST() {
        // given
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(AuthenticationMemberUtils.ANONYMOUS_USER);

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

        TechComment techcomment1 = createRepliedTechComment(new CommentContents("최상위 댓글1의 답글1의 답글"), member,
                techArticle, originParentTechComment1, parentTechComment1, new Count(0L), new Count(0L), new Count(0L));
        TechComment techcomment2 = createRepliedTechComment(new CommentContents("최상위 댓글1의 답글2의 답글"), member,
                techArticle, originParentTechComment1, parentTechComment2, new Count(0L), new Count(0L), new Count(0L));

        techCommentRepository.saveAll(List.of(
                originParentTechComment1, originParentTechComment2, originParentTechComment3,
                originParentTechComment4, originParentTechComment5, originParentTechComment6,
                parentTechComment1, parentTechComment2, parentTechComment3, parentTechComment4,
                techcomment1, techcomment2
        ));

        Pageable pageable = PageRequest.of(0, 5);

        em.flush();
        em.clear();

        // when
        SliceCommentCustom<TechCommentsResponse> response = guestTechCommentService.getTechComments(techArticleId,
                null, TechCommentSort.OLDEST, pageable, authentication);

        // then
        assertThat(response.getTotalOriginParentComments()).isEqualTo(6L);
        assertThat(response).hasSizeLessThanOrEqualTo(pageable.getPageSize())
                .extracting(
                        "techCommentId",
                        "memberId",
                        "author",
                        "maskedEmail",
                        "contents",
                        "replyTotalCount",
                        "recommendTotalCount",
                        "isCommentAuthor",
                        "isRecommended",
                        "isModified",
                        "isDeleted"
                )
                .containsExactly(
                        Tuple.tuple(originParentTechComment1.getId(),
                                member.getId(),
                                member.getNickname().getNickname(),
                                CommonResponseUtil.sliceAndMaskEmail(member.getEmailAsString()),
                                originParentTechComment1.getContents().getCommentContents(),
                                originParentTechComment1.getReplyTotalCount().getCount(),
                                originParentTechComment1.getRecommendTotalCount().getCount(),
                                false,
                                false,
                                false,
                                false
                        ),
                        Tuple.tuple(originParentTechComment2.getId(),
                                member.getId(),
                                member.getNickname().getNickname(),
                                CommonResponseUtil.sliceAndMaskEmail(member.getEmailAsString()),
                                originParentTechComment2.getContents().getCommentContents(),
                                originParentTechComment2.getReplyTotalCount().getCount(),
                                originParentTechComment2.getRecommendTotalCount().getCount(),
                                false,
                                false,
                                false,
                                false
                        ),
                        Tuple.tuple(originParentTechComment3.getId(),
                                member.getId(),
                                member.getNickname().getNickname(),
                                CommonResponseUtil.sliceAndMaskEmail(member.getEmailAsString()),
                                originParentTechComment3.getContents().getCommentContents(),
                                originParentTechComment3.getReplyTotalCount().getCount(),
                                originParentTechComment3.getRecommendTotalCount().getCount(),
                                false,
                                false,
                                false,
                                false
                        ),
                        Tuple.tuple(originParentTechComment4.getId(),
                                member.getId(),
                                member.getNickname().getNickname(),
                                CommonResponseUtil.sliceAndMaskEmail(member.getEmailAsString()),
                                originParentTechComment4.getContents().getCommentContents(),
                                originParentTechComment4.getReplyTotalCount().getCount(),
                                originParentTechComment4.getRecommendTotalCount().getCount(),
                                false,
                                false,
                                false,
                                false
                        ),
                        Tuple.tuple(originParentTechComment5.getId(),
                                member.getId(),
                                member.getNickname().getNickname(),
                                CommonResponseUtil.sliceAndMaskEmail(member.getEmailAsString()),
                                originParentTechComment5.getContents().getCommentContents(),
                                originParentTechComment5.getReplyTotalCount().getCount(),
                                originParentTechComment5.getRecommendTotalCount().getCount(),
                                false,
                                false,
                                false,
                                false
                        )
                );

        TechCommentsResponse techCommentsResponse1 = response.getContent().get(0);
        List<TechRepliedCommentsResponse> replies1 = techCommentsResponse1.getReplies();
        assertThat(replies1).hasSize(4)
                .extracting(
                        "techCommentId",
                        "memberId",
                        "techParentCommentId",
                        "techParentCommentMemberId",
                        "techParentCommentAuthor",
                        "techOriginParentCommentId",
                        "author",
                        "maskedEmail",
                        "contents",
                        "recommendTotalCount",
                        "isCommentAuthor",
                        "isRecommended",
                        "isModified",
                        "isDeleted"
                )
                .containsExactly(
                        Tuple.tuple(parentTechComment1.getId(),
                                member.getId(),
                                originParentTechComment1.getId(),
                                originParentTechComment1.getCreatedBy().getId(),
                                member.getNicknameAsString(),
                                originParentTechComment1.getId(),
                                member.getNicknameAsString(),
                                CommonResponseUtil.sliceAndMaskEmail(member.getEmailAsString()),
                                parentTechComment1.getContents().getCommentContents(),
                                parentTechComment1.getRecommendTotalCount().getCount(),
                                false,
                                false,
                                false,
                                false
                        ),
                        Tuple.tuple(parentTechComment2.getId(),
                                member.getId(),
                                originParentTechComment1.getId(),
                                originParentTechComment1.getCreatedBy().getId(),
                                member.getNicknameAsString(),
                                originParentTechComment1.getId(),
                                member.getNicknameAsString(),
                                CommonResponseUtil.sliceAndMaskEmail(member.getEmailAsString()),
                                parentTechComment2.getContents().getCommentContents(),
                                parentTechComment2.getRecommendTotalCount().getCount(),
                                false,
                                false,
                                false,
                                false
                        ),
                        Tuple.tuple(techcomment1.getId(),
                                member.getId(),
                                parentTechComment1.getId(),
                                parentTechComment1.getCreatedBy().getId(),
                                member.getNicknameAsString(),
                                originParentTechComment1.getId(),
                                member.getNicknameAsString(),
                                CommonResponseUtil.sliceAndMaskEmail(member.getEmailAsString()),
                                techcomment1.getContents().getCommentContents(),
                                techcomment1.getRecommendTotalCount().getCount(),
                                false,
                                false,
                                false,
                                false
                        ),
                        Tuple.tuple(techcomment2.getId(),
                                member.getId(),
                                parentTechComment2.getId(),
                                parentTechComment2.getCreatedBy().getId(),
                                member.getNicknameAsString(),
                                originParentTechComment1.getId(),
                                member.getNicknameAsString(),
                                CommonResponseUtil.sliceAndMaskEmail(member.getEmailAsString()),
                                techcomment2.getContents().getCommentContents(),
                                techcomment2.getRecommendTotalCount().getCount(),
                                false,
                                false,
                                false,
                                false
                        )
                );

        TechCommentsResponse techCommentsResponse2 = response.getContent().get(1);
        List<TechRepliedCommentsResponse> replies2 = techCommentsResponse2.getReplies();
        assertThat(replies2).hasSize(2)
                .extracting(
                        "techCommentId",
                        "memberId",
                        "techParentCommentId",
                        "techParentCommentMemberId",
                        "techParentCommentAuthor",
                        "techOriginParentCommentId",
                        "author",
                        "maskedEmail",
                        "contents",
                        "recommendTotalCount",
                        "isCommentAuthor",
                        "isRecommended",
                        "isModified",
                        "isDeleted"
                )
                .containsExactly(
                        Tuple.tuple(parentTechComment3.getId(),
                                member.getId(),
                                originParentTechComment2.getId(),
                                originParentTechComment2.getCreatedBy().getId(),
                                member.getNicknameAsString(),
                                originParentTechComment2.getId(),
                                member.getNicknameAsString(),
                                CommonResponseUtil.sliceAndMaskEmail(member.getEmailAsString()),
                                parentTechComment3.getContents().getCommentContents(),
                                parentTechComment3.getRecommendTotalCount().getCount(),
                                false,
                                false,
                                false,
                                false
                        ),
                        Tuple.tuple(parentTechComment4.getId(),
                                member.getId(),
                                originParentTechComment2.getId(),
                                originParentTechComment2.getCreatedBy().getId(),
                                member.getNicknameAsString(),
                                originParentTechComment2.getId(),
                                member.getNicknameAsString(),
                                CommonResponseUtil.sliceAndMaskEmail(member.getEmailAsString()),
                                parentTechComment4.getContents().getCommentContents(),
                                parentTechComment4.getRecommendTotalCount().getCount(),
                                false,
                                false,
                                false,
                                false
                        )
                );

        TechCommentsResponse techCommentsResponse3 = response.getContent().get(2);
        List<TechRepliedCommentsResponse> replies3 = techCommentsResponse3.getReplies();
        assertThat(replies3).hasSize(0);

        TechCommentsResponse techCommentsResponse4 = response.getContent().get(3);
        List<TechRepliedCommentsResponse> replies4 = techCommentsResponse4.getReplies();
        assertThat(replies4).hasSize(0);

        TechCommentsResponse techCommentsResponse5 = response.getContent().get(4);
        List<TechRepliedCommentsResponse> replies5 = techCommentsResponse5.getReplies();
        assertThat(replies5).hasSize(0);
    }

    @Test
    @DisplayName("익명 회원은 커서 방식으로 기술블로그 댓글/답글을 조회할 수 있다. (기본 정렬은 최신순)")
    void getTechCommentsSortByLATEST() {
        // given
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(AuthenticationMemberUtils.ANONYMOUS_USER);

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

        TechComment techcomment1 = createRepliedTechComment(new CommentContents("최상위 댓글1의 답글1의 답글"), member,
                techArticle, originParentTechComment1, parentTechComment1, new Count(0L), new Count(0L), new Count(0L));
        TechComment techcomment2 = createRepliedTechComment(new CommentContents("최상위 댓글1의 답글2의 답글"), member,
                techArticle, originParentTechComment1, parentTechComment2, new Count(0L), new Count(0L), new Count(0L));

        techCommentRepository.saveAll(List.of(
                originParentTechComment1, originParentTechComment2, originParentTechComment3,
                originParentTechComment4, originParentTechComment5, originParentTechComment6,
                parentTechComment1, parentTechComment2, parentTechComment3, parentTechComment4,
                techcomment1, techcomment2
        ));

        Pageable pageable = PageRequest.of(0, 5);

        em.flush();
        em.clear();

        // when
        SliceCommentCustom<TechCommentsResponse> response = guestTechCommentService.getTechComments(techArticleId,
                null, TechCommentSort.LATEST, pageable, authentication);

        // then
        assertThat(response.getTotalOriginParentComments()).isEqualTo(6L);
        assertThat(response).hasSizeLessThanOrEqualTo(pageable.getPageSize())
                .extracting(
                        "techCommentId",
                        "memberId",
                        "author",
                        "maskedEmail",
                        "contents",
                        "replyTotalCount",
                        "recommendTotalCount",
                        "isCommentAuthor",
                        "isRecommended",
                        "isModified",
                        "isDeleted"
                )
                .containsExactly(
                        Tuple.tuple(originParentTechComment6.getId(),
                                member.getId(),
                                member.getNickname().getNickname(),
                                CommonResponseUtil.sliceAndMaskEmail(member.getEmailAsString()),
                                originParentTechComment6.getContents().getCommentContents(),
                                originParentTechComment6.getReplyTotalCount().getCount(),
                                originParentTechComment6.getRecommendTotalCount().getCount(),
                                false,
                                false,
                                false,
                                false
                        ),
                        Tuple.tuple(originParentTechComment5.getId(),
                                member.getId(),
                                member.getNickname().getNickname(),
                                CommonResponseUtil.sliceAndMaskEmail(member.getEmailAsString()),
                                originParentTechComment5.getContents().getCommentContents(),
                                originParentTechComment5.getReplyTotalCount().getCount(),
                                originParentTechComment5.getRecommendTotalCount().getCount(),
                                false,
                                false,
                                false,
                                false
                        ),
                        Tuple.tuple(originParentTechComment4.getId(),
                                member.getId(),
                                member.getNickname().getNickname(),
                                CommonResponseUtil.sliceAndMaskEmail(member.getEmailAsString()),
                                originParentTechComment4.getContents().getCommentContents(),
                                originParentTechComment4.getReplyTotalCount().getCount(),
                                originParentTechComment4.getRecommendTotalCount().getCount(),
                                false,
                                false,
                                false,
                                false
                        ),
                        Tuple.tuple(originParentTechComment3.getId(),
                                member.getId(),
                                member.getNickname().getNickname(),
                                CommonResponseUtil.sliceAndMaskEmail(member.getEmailAsString()),
                                originParentTechComment3.getContents().getCommentContents(),
                                originParentTechComment3.getReplyTotalCount().getCount(),
                                originParentTechComment3.getRecommendTotalCount().getCount(),
                                false,
                                false,
                                false,
                                false
                        ),
                        Tuple.tuple(originParentTechComment2.getId(),
                                member.getId(),
                                member.getNickname().getNickname(),
                                CommonResponseUtil.sliceAndMaskEmail(member.getEmailAsString()),
                                originParentTechComment2.getContents().getCommentContents(),
                                originParentTechComment2.getReplyTotalCount().getCount(),
                                originParentTechComment2.getRecommendTotalCount().getCount(),
                                false,
                                false,
                                false,
                                false)
                );

        TechCommentsResponse techCommentsResponse6 = response.getContent().get(0);
        List<TechRepliedCommentsResponse> replies6 = techCommentsResponse6.getReplies();
        assertThat(replies6).hasSize(0);

        TechCommentsResponse techCommentsResponse5 = response.getContent().get(1);
        List<TechRepliedCommentsResponse> replies5 = techCommentsResponse5.getReplies();
        assertThat(replies5).hasSize(0);

        TechCommentsResponse techCommentsResponse4 = response.getContent().get(2);
        List<TechRepliedCommentsResponse> replies4 = techCommentsResponse4.getReplies();
        assertThat(replies4).hasSize(0);

        TechCommentsResponse techCommentsResponse3 = response.getContent().get(3);
        List<TechRepliedCommentsResponse> replies3 = techCommentsResponse3.getReplies();
        assertThat(replies3).hasSize(0);

        TechCommentsResponse techCommentsResponse2 = response.getContent().get(4);
        List<TechRepliedCommentsResponse> replies2 = techCommentsResponse2.getReplies();
        assertThat(replies2).hasSize(2)
                .extracting("techCommentId")
                .containsExactly(parentTechComment3.getId(), parentTechComment4.getId());
    }

    @Test
    @DisplayName("익명 회원은 커서 방식으로 기술블로그 댓글/답글을 조회할 수 있다. (댓글 많은 순)")
    void getTechCommentsSortByMostCommented() {
        // given
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(AuthenticationMemberUtils.ANONYMOUS_USER);

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
                techArticle, new Count(0L), new Count(0L), new Count(4L));
        TechComment originParentTechComment3 = createMainTechComment(new CommentContents("최상위 댓글3"), member,
                techArticle, new Count(0L), new Count(0L), new Count(0L));
        TechComment originParentTechComment4 = createMainTechComment(new CommentContents("최상위 댓글4"), member,
                techArticle, new Count(0L), new Count(0L), new Count(2L));
        TechComment originParentTechComment5 = createMainTechComment(new CommentContents("최상위 댓글5"), member,
                techArticle, new Count(0L), new Count(0L), new Count(0L));
        TechComment originParentTechComment6 = createMainTechComment(new CommentContents("최상위 댓글6"), member,
                techArticle, new Count(0L), new Count(0L), new Count(0L));

        TechComment parentTechComment1 = createRepliedTechComment(new CommentContents("최상위 댓글2의 답글1"), member,
                techArticle, originParentTechComment2, originParentTechComment2, new Count(0L), new Count(0L),
                new Count(0L));
        TechComment parentTechComment2 = createRepliedTechComment(new CommentContents("최상위 댓글2의 답글2"), member,
                techArticle, originParentTechComment2, originParentTechComment2, new Count(0L), new Count(0L),
                new Count(0L));
        TechComment parentTechComment3 = createRepliedTechComment(new CommentContents("최상위 댓글4의 답글1"), member,
                techArticle, originParentTechComment4, originParentTechComment4, new Count(0L), new Count(0L),
                new Count(0L));
        TechComment parentTechComment4 = createRepliedTechComment(new CommentContents("최상위 댓글4의 답글2"), member,
                techArticle, originParentTechComment4, originParentTechComment4, new Count(0L), new Count(0L),
                new Count(0L));

        TechComment techcomment1 = createRepliedTechComment(new CommentContents("최상위 댓글2의 답글1의 답글"), member,
                techArticle, originParentTechComment2, parentTechComment1, new Count(0L), new Count(0L), new Count(0L));
        TechComment techcomment2 = createRepliedTechComment(new CommentContents("최상위 댓글2의 답글2의 답글"), member,
                techArticle, originParentTechComment2, parentTechComment2, new Count(0L), new Count(0L), new Count(0L));

        techCommentRepository.saveAll(List.of(
                originParentTechComment1, originParentTechComment2, originParentTechComment3,
                originParentTechComment4, originParentTechComment5, originParentTechComment6,
                parentTechComment1, parentTechComment2, parentTechComment3, parentTechComment4,
                techcomment1, techcomment2
        ));

        Pageable pageable = PageRequest.of(0, 5);

        em.flush();
        em.clear();

        // when
        SliceCommentCustom<TechCommentsResponse> response = guestTechCommentService.getTechComments(techArticleId,
                null, TechCommentSort.MOST_COMMENTED, pageable, authentication);

        // then
        assertThat(response.getTotalOriginParentComments()).isEqualTo(6L);
        assertThat(response).hasSizeLessThanOrEqualTo(pageable.getPageSize())
                .extracting(
                        "techCommentId",
                        "memberId",
                        "author",
                        "maskedEmail",
                        "contents",
                        "replyTotalCount",
                        "recommendTotalCount",
                        "isCommentAuthor",
                        "isRecommended",
                        "isModified",
                        "isDeleted"
                )
                .containsExactly(
                        Tuple.tuple(originParentTechComment2.getId(),
                                member.getId(),
                                member.getNickname().getNickname(),
                                CommonResponseUtil.sliceAndMaskEmail(member.getEmailAsString()),
                                originParentTechComment2.getContents().getCommentContents(),
                                originParentTechComment2.getReplyTotalCount().getCount(),
                                originParentTechComment2.getRecommendTotalCount().getCount(),
                                false,
                                false,
                                false,
                                false
                        ),
                        Tuple.tuple(originParentTechComment4.getId(),
                                member.getId(),
                                member.getNickname().getNickname(),
                                CommonResponseUtil.sliceAndMaskEmail(member.getEmailAsString()),
                                originParentTechComment4.getContents().getCommentContents(),
                                originParentTechComment4.getReplyTotalCount().getCount(),
                                originParentTechComment4.getRecommendTotalCount().getCount(),
                                false,
                                false,
                                false,
                                false
                        ),
                        Tuple.tuple(originParentTechComment6.getId(),
                                member.getId(),
                                member.getNickname().getNickname(),
                                CommonResponseUtil.sliceAndMaskEmail(member.getEmailAsString()),
                                originParentTechComment6.getContents().getCommentContents(),
                                originParentTechComment6.getReplyTotalCount().getCount(),
                                originParentTechComment6.getRecommendTotalCount().getCount(),
                                false,
                                false,
                                false,
                                false
                        ),
                        Tuple.tuple(originParentTechComment5.getId(),
                                member.getId(),
                                member.getNickname().getNickname(),
                                CommonResponseUtil.sliceAndMaskEmail(member.getEmailAsString()),
                                originParentTechComment5.getContents().getCommentContents(),
                                originParentTechComment5.getReplyTotalCount().getCount(),
                                originParentTechComment5.getRecommendTotalCount().getCount(),
                                false,
                                false,
                                false,
                                false
                        ),
                        Tuple.tuple(originParentTechComment3.getId(),
                                member.getId(),
                                member.getNickname().getNickname(),
                                CommonResponseUtil.sliceAndMaskEmail(member.getEmailAsString()),
                                originParentTechComment3.getContents().getCommentContents(),
                                originParentTechComment3.getReplyTotalCount().getCount(),
                                originParentTechComment3.getRecommendTotalCount().getCount(),
                                false,
                                false,
                                false,
                                false
                        )
                );

        TechCommentsResponse techCommentsResponse1 = response.getContent().get(0);
        List<TechRepliedCommentsResponse> replies1 = techCommentsResponse1.getReplies();
        assertThat(replies1).hasSize(4)
                .extracting(
                        "techCommentId",
                        "memberId",
                        "techParentCommentId",
                        "techParentCommentMemberId",
                        "techParentCommentAuthor",
                        "techOriginParentCommentId",
                        "author",
                        "maskedEmail",
                        "contents",
                        "recommendTotalCount",
                        "isCommentAuthor",
                        "isRecommended",
                        "isModified",
                        "isDeleted"
                )
                .containsExactly(
                        Tuple.tuple(parentTechComment1.getId(),
                                member.getId(),
                                originParentTechComment2.getId(),
                                originParentTechComment2.getCreatedBy().getId(),
                                member.getNicknameAsString(),
                                originParentTechComment2.getId(),
                                member.getNicknameAsString(),
                                CommonResponseUtil.sliceAndMaskEmail(member.getEmailAsString()),
                                parentTechComment1.getContents().getCommentContents(),
                                parentTechComment1.getRecommendTotalCount().getCount(),
                                false,
                                false,
                                false,
                                false
                        ),
                        Tuple.tuple(parentTechComment2.getId(),
                                member.getId(),
                                originParentTechComment2.getId(),
                                originParentTechComment2.getCreatedBy().getId(),
                                member.getNicknameAsString(),
                                originParentTechComment2.getId(),
                                member.getNicknameAsString(),
                                CommonResponseUtil.sliceAndMaskEmail(member.getEmailAsString()),
                                parentTechComment2.getContents().getCommentContents(),
                                parentTechComment2.getRecommendTotalCount().getCount(),
                                false,
                                false,
                                false,
                                false
                        ),
                        Tuple.tuple(techcomment1.getId(),
                                member.getId(),
                                parentTechComment1.getId(),
                                parentTechComment1.getCreatedBy().getId(),
                                member.getNicknameAsString(),
                                originParentTechComment2.getId(),
                                member.getNicknameAsString(),
                                CommonResponseUtil.sliceAndMaskEmail(member.getEmailAsString()),
                                techcomment1.getContents().getCommentContents(),
                                techcomment1.getRecommendTotalCount().getCount(),
                                false,
                                false,
                                false,
                                false
                        ),
                        Tuple.tuple(techcomment2.getId(),
                                member.getId(),
                                parentTechComment2.getId(),
                                parentTechComment2.getCreatedBy().getId(),
                                member.getNicknameAsString(),
                                originParentTechComment2.getId(),
                                member.getNicknameAsString(),
                                CommonResponseUtil.sliceAndMaskEmail(member.getEmailAsString()),
                                techcomment2.getContents().getCommentContents(),
                                techcomment2.getRecommendTotalCount().getCount(),
                                false,
                                false,
                                false,
                                false
                        )
                );

        TechCommentsResponse techCommentsResponse2 = response.getContent().get(1);
        List<TechRepliedCommentsResponse> replies2 = techCommentsResponse2.getReplies();
        assertThat(replies2).hasSize(2)
                .extracting(
                        "techCommentId",
                        "memberId",
                        "techParentCommentId",
                        "techParentCommentMemberId",
                        "techParentCommentAuthor",
                        "techOriginParentCommentId",
                        "author",
                        "maskedEmail",
                        "contents",
                        "recommendTotalCount",
                        "isCommentAuthor",
                        "isRecommended",
                        "isModified",
                        "isDeleted"
                )
                .containsExactly(
                        Tuple.tuple(parentTechComment3.getId(),
                                member.getId(),
                                originParentTechComment4.getId(),
                                originParentTechComment4.getCreatedBy().getId(),
                                member.getNicknameAsString(),
                                originParentTechComment4.getId(),
                                member.getNicknameAsString(),
                                CommonResponseUtil.sliceAndMaskEmail(member.getEmailAsString()),
                                parentTechComment3.getContents().getCommentContents(),
                                parentTechComment3.getRecommendTotalCount().getCount(),
                                false,
                                false,
                                false,
                                false
                        ),
                        Tuple.tuple(parentTechComment4.getId(),
                                member.getId(),
                                originParentTechComment4.getId(),
                                originParentTechComment4.getCreatedBy().getId(),
                                member.getNicknameAsString(),
                                originParentTechComment4.getId(),
                                member.getNicknameAsString(),
                                CommonResponseUtil.sliceAndMaskEmail(member.getEmailAsString()),
                                parentTechComment4.getContents().getCommentContents(),
                                parentTechComment4.getRecommendTotalCount().getCount(),
                                false,
                                false,
                                false,
                                false
                        )
                );

        TechCommentsResponse techCommentsResponse3 = response.getContent().get(2);
        List<TechRepliedCommentsResponse> replies3 = techCommentsResponse3.getReplies();
        assertThat(replies3).hasSize(0);

        TechCommentsResponse techCommentsResponse4 = response.getContent().get(3);
        List<TechRepliedCommentsResponse> replies4 = techCommentsResponse4.getReplies();
        assertThat(replies4).hasSize(0);

        TechCommentsResponse techCommentsResponse5 = response.getContent().get(4);
        List<TechRepliedCommentsResponse> replies5 = techCommentsResponse5.getReplies();
        assertThat(replies5).hasSize(0);
    }

    @Test
    @DisplayName("익명 회원은 커서 방식으로 기술블로그 댓글/답글을 조회할 수 있다. (추천 많은 순)")
    void getTechCommentsSortByMostRecommended() {
        // given
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(AuthenticationMemberUtils.ANONYMOUS_USER);

        Company company = createCompany("꿈빛 파티시엘", "https://example.png", "https://example.com", "https://example.com");
        companyRepository.save(company);

        TechArticle techArticle = TechArticle.createTechArticle(new Title("기술블로그 제목"), new Url("https://example.com"),
                new Count(1L),
                new Count(1L), new Count(12L), new Count(1L), null, company);
        techArticleRepository.save(techArticle);
        Long techArticleId = techArticle.getId();

        TechComment originParentTechComment1 = createMainTechComment(new CommentContents("최상위 댓글1"), member,
                techArticle, new Count(0L), new Count(3L), new Count(0L));
        TechComment originParentTechComment2 = createMainTechComment(new CommentContents("최상위 댓글2"), member,
                techArticle, new Count(0L), new Count(1L), new Count(0L));
        TechComment originParentTechComment3 = createMainTechComment(new CommentContents("최상위 댓글3"), member,
                techArticle, new Count(0L), new Count(5L), new Count(0L));
        TechComment originParentTechComment4 = createMainTechComment(new CommentContents("최상위 댓글4"), member,
                techArticle, new Count(0L), new Count(4L), new Count(0L));
        TechComment originParentTechComment5 = createMainTechComment(new CommentContents("최상위 댓글5"), member,
                techArticle, new Count(0L), new Count(2L), new Count(0L));
        TechComment originParentTechComment6 = createMainTechComment(new CommentContents("최상위 댓글6"), member,
                techArticle, new Count(0L), new Count(6L), new Count(0L));

        techCommentRepository.saveAll(List.of(
                originParentTechComment1, originParentTechComment2, originParentTechComment3,
                originParentTechComment4, originParentTechComment5, originParentTechComment6
        ));

        Pageable pageable = PageRequest.of(0, 5);

        em.flush();
        em.clear();

        // when
        SliceCommentCustom<TechCommentsResponse> response = guestTechCommentService.getTechComments(techArticleId,
                null, TechCommentSort.MOST_LIKED, pageable, authentication);

        // then
        assertThat(response.getTotalOriginParentComments()).isEqualTo(6L);
        assertThat(response).hasSizeLessThanOrEqualTo(pageable.getPageSize())
                .extracting(
                        "techCommentId",
                        "memberId",
                        "author",
                        "maskedEmail",
                        "contents",
                        "replyTotalCount",
                        "recommendTotalCount",
                        "isCommentAuthor",
                        "isRecommended",
                        "isModified",
                        "isDeleted"
                )
                .containsExactly(
                        Tuple.tuple(originParentTechComment6.getId(),
                                member.getId(),
                                member.getNickname().getNickname(),
                                CommonResponseUtil.sliceAndMaskEmail(member.getEmailAsString()),
                                originParentTechComment6.getContents().getCommentContents(),
                                originParentTechComment6.getReplyTotalCount().getCount(),
                                originParentTechComment6.getRecommendTotalCount().getCount(),
                                false,
                                false,
                                false,
                                false
                        ),
                        Tuple.tuple(originParentTechComment3.getId(),
                                member.getId(),
                                member.getNickname().getNickname(),
                                CommonResponseUtil.sliceAndMaskEmail(member.getEmailAsString()),
                                originParentTechComment3.getContents().getCommentContents(),
                                originParentTechComment3.getReplyTotalCount().getCount(),
                                originParentTechComment3.getRecommendTotalCount().getCount(),
                                false,
                                false,
                                false,
                                false
                        ),
                        Tuple.tuple(originParentTechComment4.getId(),
                                member.getId(),
                                member.getNickname().getNickname(),
                                CommonResponseUtil.sliceAndMaskEmail(member.getEmailAsString()),
                                originParentTechComment4.getContents().getCommentContents(),
                                originParentTechComment4.getReplyTotalCount().getCount(),
                                originParentTechComment4.getRecommendTotalCount().getCount(),
                                false,
                                false,
                                false,
                                false
                        ),
                        Tuple.tuple(originParentTechComment1.getId(),
                                member.getId(),
                                member.getNickname().getNickname(),
                                CommonResponseUtil.sliceAndMaskEmail(member.getEmailAsString()),
                                originParentTechComment1.getContents().getCommentContents(),
                                originParentTechComment1.getReplyTotalCount().getCount(),
                                originParentTechComment1.getRecommendTotalCount().getCount(),
                                false,
                                false,
                                false,
                                false
                        ),
                        Tuple.tuple(originParentTechComment5.getId(),
                                member.getId(),
                                member.getNickname().getNickname(),
                                CommonResponseUtil.sliceAndMaskEmail(member.getEmailAsString()),
                                originParentTechComment5.getContents().getCommentContents(),
                                originParentTechComment5.getReplyTotalCount().getCount(),
                                originParentTechComment5.getRecommendTotalCount().getCount(),
                                false,
                                false,
                                false,
                                false
                        )
                );

        TechCommentsResponse techCommentsResponse6 = response.getContent().get(0);
        List<TechRepliedCommentsResponse> replies6 = techCommentsResponse6.getReplies();
        assertThat(replies6).hasSize(0);

        TechCommentsResponse techCommentsResponse3 = response.getContent().get(1);
        List<TechRepliedCommentsResponse> replies3 = techCommentsResponse3.getReplies();
        assertThat(replies3).hasSize(0);

        TechCommentsResponse techCommentsResponse4 = response.getContent().get(2);
        List<TechRepliedCommentsResponse> replies4 = techCommentsResponse4.getReplies();
        assertThat(replies4).hasSize(0);

        TechCommentsResponse techCommentsResponse1 = response.getContent().get(3);
        List<TechRepliedCommentsResponse> replies1 = techCommentsResponse1.getReplies();
        assertThat(replies1).hasSize(0);

        TechCommentsResponse techCommentsResponse5 = response.getContent().get(4);
        List<TechRepliedCommentsResponse> replies5 = techCommentsResponse5.getReplies();
        assertThat(replies5).hasSize(0);
    }

    @Test
    @DisplayName("익명 회원은 커서 방식으로 커서 다음의 기술블로그 댓글/답글을 조회할 수 있다.")
    void getTechCommentsByCursor() {
        // given
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(AuthenticationMemberUtils.ANONYMOUS_USER);

        Company company = createCompany("꿈빛 파티시엘", "https://example.png", "https://example.com", "https://example.com");
        companyRepository.save(company);

        TechArticle techArticle = TechArticle.createTechArticle(new Title("기술블로그 제목"), new Url("https://example.com"),
                new Count(1L), new Count(1L), new Count(12L), new Count(1L), null, company);
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

        originParentTechComment6.changeDeletedAt(LocalDateTime.now(), member);

        techCommentRepository.saveAll(List.of(
                originParentTechComment1, originParentTechComment2, originParentTechComment3,
                originParentTechComment4, originParentTechComment5, originParentTechComment6
        ));

        Pageable pageable = PageRequest.of(0, 5);

        em.flush();
        em.clear();

        // when
        SliceCommentCustom<TechCommentsResponse> response = guestTechCommentService.getTechComments(techArticleId,
                originParentTechComment6.getId(), null, pageable, authentication);

        // then
        assertThat(response.getTotalOriginParentComments()).isEqualTo(5L); // 삭제된 댓글은 카운트하지 않는다
        assertThat(response).hasSizeLessThanOrEqualTo(pageable.getPageSize())
                .extracting(
                        "techCommentId",
                        "memberId",
                        "author",
                        "maskedEmail",
                        "contents",
                        "replyTotalCount",
                        "recommendTotalCount",
                        "isCommentAuthor",
                        "isRecommended",
                        "isModified",
                        "isDeleted"
                )
                .containsExactly(
                        Tuple.tuple(originParentTechComment5.getId(),
                                member.getId(),
                                member.getNickname().getNickname(),
                                CommonResponseUtil.sliceAndMaskEmail(member.getEmailAsString()),
                                originParentTechComment5.getContents().getCommentContents(),
                                originParentTechComment5.getReplyTotalCount().getCount(),
                                originParentTechComment5.getRecommendTotalCount().getCount(),
                                false,
                                false,
                                false,
                                false
                        ),
                        Tuple.tuple(originParentTechComment4.getId(),
                                member.getId(),
                                member.getNickname().getNickname(),
                                CommonResponseUtil.sliceAndMaskEmail(member.getEmailAsString()),
                                originParentTechComment4.getContents().getCommentContents(),
                                originParentTechComment4.getReplyTotalCount().getCount(),
                                originParentTechComment4.getRecommendTotalCount().getCount(),
                                false,
                                false,
                                false,
                                false
                        ),
                        Tuple.tuple(originParentTechComment3.getId(),
                                member.getId(),
                                member.getNickname().getNickname(),
                                CommonResponseUtil.sliceAndMaskEmail(member.getEmailAsString()),
                                originParentTechComment3.getContents().getCommentContents(),
                                originParentTechComment3.getReplyTotalCount().getCount(),
                                originParentTechComment3.getRecommendTotalCount().getCount(),
                                false,
                                false,
                                false,
                                false
                        ),
                        Tuple.tuple(originParentTechComment2.getId(),
                                member.getId(),
                                member.getNickname().getNickname(),
                                CommonResponseUtil.sliceAndMaskEmail(member.getEmailAsString()),
                                originParentTechComment2.getContents().getCommentContents(),
                                originParentTechComment2.getReplyTotalCount().getCount(),
                                originParentTechComment2.getRecommendTotalCount().getCount(),
                                false,
                                false,
                                false,
                                false
                        ),
                        Tuple.tuple(originParentTechComment1.getId(),
                                member.getId(),
                                member.getNickname().getNickname(),
                                CommonResponseUtil.sliceAndMaskEmail(member.getEmailAsString()),
                                originParentTechComment1.getContents().getCommentContents(),
                                originParentTechComment1.getReplyTotalCount().getCount(),
                                originParentTechComment1.getRecommendTotalCount().getCount(),
                                false,
                                false,
                                false,
                                false
                        )
                );

        TechCommentsResponse techCommentsResponse6 = response.getContent().get(0);
        List<TechRepliedCommentsResponse> replies6 = techCommentsResponse6.getReplies();
        assertThat(replies6).hasSize(0);

        TechCommentsResponse techCommentsResponse3 = response.getContent().get(1);
        List<TechRepliedCommentsResponse> replies3 = techCommentsResponse3.getReplies();
        assertThat(replies3).hasSize(0);

        TechCommentsResponse techCommentsResponse4 = response.getContent().get(2);
        List<TechRepliedCommentsResponse> replies4 = techCommentsResponse4.getReplies();
        assertThat(replies4).hasSize(0);

        TechCommentsResponse techCommentsResponse1 = response.getContent().get(3);
        List<TechRepliedCommentsResponse> replies1 = techCommentsResponse1.getReplies();
        assertThat(replies1).hasSize(0);

        TechCommentsResponse techCommentsResponse5 = response.getContent().get(4);
        List<TechRepliedCommentsResponse> replies5 = techCommentsResponse5.getReplies();
        assertThat(replies5).hasSize(0);
    }

    @Test
    @DisplayName("익명 회원이 아닌 경우 익명회원 전용 기술블로그 베스트 댓글 조회 메소드를 호출하면 예외가 발생한다.")
    void findTechBestCommentsNotAnonymousMember() {
        // given
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // when // then
        assertThatThrownBy(() -> guestTechCommentService.findTechBestComments(3, 1L, authentication))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(INVALID_METHODS_CALL_MESSAGE);
    }

    @Test
    @DisplayName("익명 회원이 offset에 정책에 맞게 기술블로그 베스트 댓글을 조회한다.")
    void findTechBestComments() {
        // given
        // 회원 생성
        SocialMemberDto socialMemberDto1 = createSocialDto("user1", name, "nickname1", password, "user1@gmail.com",
                socialType, Role.ROLE_ADMIN.name());
        SocialMemberDto socialMemberDto2 = createSocialDto("user2", name, "nickname2", password, "user2@gmail.com",
                socialType, role);
        SocialMemberDto socialMemberDto3 = createSocialDto("user3", name, "nickname3", password, "user3@gmail.com",
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
                new Count(1L), new Count(1L), new Count(12L), new Count(1L), null, company);
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
        TechCommentRecommend techCommentRecommend = createTechCommentRecommend(true, originParentTechComment1, member2);
        techCommentRecommendRepository.save(techCommentRecommend);

        // 답글 생성
        TechComment repliedTechComment = createRepliedTechComment(new CommentContents("최상위 댓글1의 답글1"), member3,
                techArticle, originParentTechComment1, originParentTechComment1, new Count(0L), new Count(0L),
                new Count(0L));
        techCommentRepository.save(repliedTechComment);

        // when
        // 익명회원 목킹
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(AuthenticationMemberUtils.ANONYMOUS_USER);

        List<TechCommentsResponse> response = guestTechCommentService.findTechBestComments(3, techArticle.getId(),
                authentication);

        // then
        assertThat(response).hasSize(3)
                .extracting(
                        "techCommentId",
                        "memberId",
                        "author",
                        "maskedEmail",
                        "contents",
                        "replyTotalCount",
                        "recommendTotalCount",
                        "isCommentAuthor",
                        "isRecommended",
                        "isModified",
                        "isDeleted"
                )
                .containsExactly(
                        Tuple.tuple(originParentTechComment1.getId(),
                                member1.getId(),
                                member1.getNickname().getNickname(),
                                CommonResponseUtil.sliceAndMaskEmail(member1.getEmailAsString()),
                                originParentTechComment1.getContents().getCommentContents(),
                                originParentTechComment1.getReplyTotalCount().getCount(),
                                originParentTechComment1.getRecommendTotalCount().getCount(),
                                false,
                                false,
                                true,
                                false
                        ),
                        Tuple.tuple(originParentTechComment2.getId(),
                                member2.getId(),
                                member2.getNickname().getNickname(),
                                CommonResponseUtil.sliceAndMaskEmail(member2.getEmailAsString()),
                                originParentTechComment2.getContents().getCommentContents(),
                                originParentTechComment2.getReplyTotalCount().getCount(),
                                originParentTechComment2.getRecommendTotalCount().getCount(),
                                false,
                                false,
                                false,
                                false
                        ),
                        Tuple.tuple(originParentTechComment3.getId(),
                                member3.getId(),
                                member3.getNickname().getNickname(),
                                CommonResponseUtil.sliceAndMaskEmail(member3.getEmailAsString()),
                                originParentTechComment3.getContents().getCommentContents(),
                                originParentTechComment3.getReplyTotalCount().getCount(),
                                originParentTechComment3.getRecommendTotalCount().getCount(),
                                false,
                                false,
                                false,
                                false
                        )
                );

        TechCommentsResponse techCommentsResponse = response.get(0);
        List<TechRepliedCommentsResponse> replies = techCommentsResponse.getReplies();
        assertThat(replies).hasSize(1)
                .extracting(
                        "techCommentId",
                        "memberId",
                        "techParentCommentMemberId",
                        "techParentCommentId",
                        "techOriginParentCommentId",
                        "techParentCommentAuthor",
                        "author",
                        "maskedEmail",
                        "contents",
                        "recommendTotalCount",
                        "isCommentAuthor",
                        "isRecommended",
                        "isModified",
                        "isDeleted"
                ).containsExactly(
                        Tuple.tuple(repliedTechComment.getId(),
                                member3.getId(),
                                repliedTechComment.getParent().getCreatedBy().getId(),
                                repliedTechComment.getParent().getId(),
                                repliedTechComment.getOriginParent().getId(),
                                repliedTechComment.getOriginParent().getCreatedBy().getNicknameAsString(),
                                member3.getNickname().getNickname(),
                                CommonResponseUtil.sliceAndMaskEmail(member3.getEmailAsString()),
                                repliedTechComment.getContents().getCommentContents(),
                                repliedTechComment.getRecommendTotalCount().getCount(),
                                false,
                                false,
                                false,
                                false
                        )
                );
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
                .officialUrl(new Url(officialUrl))
                .careerUrl(new Url(careerUrl))
                .officialImageUrl(new Url(officialImageUrl))
                .build();
    }
}
