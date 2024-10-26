package com.dreamypatisiel.devdevdev.domain.service.pick;

import static com.dreamypatisiel.devdevdev.global.utils.AuthenticationMemberUtils.INVALID_METHODS_CALL_MESSAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.Pick;
import com.dreamypatisiel.devdevdev.domain.entity.PickComment;
import com.dreamypatisiel.devdevdev.domain.entity.PickCommentRecommend;
import com.dreamypatisiel.devdevdev.domain.entity.PickOption;
import com.dreamypatisiel.devdevdev.domain.entity.PickVote;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.CommentContents;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Count;
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
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.SocialMemberDto;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.UserPrincipal;
import com.dreamypatisiel.devdevdev.global.utils.AuthenticationMemberUtils;
import com.dreamypatisiel.devdevdev.web.dto.SliceCustom;
import com.dreamypatisiel.devdevdev.web.dto.response.pick.PickCommentsResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.pick.PickRepliedCommentsResponse;
import com.dreamypatisiel.devdevdev.web.dto.util.CommentResponseUtil;
import com.dreamypatisiel.devdevdev.web.dto.util.CommonResponseUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.List;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class GuestPickCommentServiceTest {

    @Autowired
    GuestPickCommentService guestPickCommentService;
    @Autowired
    PickRepository pickRepository;
    @Autowired
    PickOptionRepository pickOptionRepository;
    @Autowired
    PickVoteRepository pickVoteRepository;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    PickOptionImageRepository pickOptionImageRepository;
    @Autowired
    PickPopularScorePolicy pickPopularScorePolicy;
    @Autowired
    PickCommentRepository pickCommentRepository;
    @Autowired
    PickCommentRecommendRepository pickCommentRecommendRepository;

    @PersistenceContext
    EntityManager em;

    String userId = "dreamy5patisiel";
    String name = "꿈빛파티시엘";
    String nickname = "행복한 꿈빛파티시엘";
    String email = "dreamy5patisiel@kakao.com";
    String password = "password";
    String socialType = SocialType.KAKAO.name();
    String role = Role.ROLE_USER.name();
    String author = "운영자";

    @ParameterizedTest
    @EnumSource(PickCommentSort.class)
    @DisplayName("익명회원이 픽픽픽 모든 댓글/답글을 알맞게 정렬하여 커서 방식으로 조회한다.")
    void findPickCommentsByPickCommentSort(PickCommentSort pickCommentSort) {
        // given
        // 회원 생성
        SocialMemberDto socialMemberDto1 = createSocialDto("user1", name, "nickname1", password, "user1@gmail.com",
                socialType, Role.ROLE_ADMIN.name());
        SocialMemberDto socialMemberDto2 = createSocialDto("user2", name, "nickname2", password, "user2@gmail.com",
                socialType, role);
        SocialMemberDto socialMemberDto3 = createSocialDto("user3", name, "nickname3", password, "user3@gmail.com",
                socialType, role);
        SocialMemberDto socialMemberDto4 = createSocialDto("user4", name, "nickname4", password, "user4@gmail.com",
                socialType, role);
        SocialMemberDto socialMemberDto5 = createSocialDto("user5", name, "nickname5", password, "user5@gmail.com",
                socialType, role);
        SocialMemberDto socialMemberDto6 = createSocialDto("user6", name, "nickname6", password, "user6@gmail.com",
                socialType, role);
        Member member1 = Member.createMemberBy(socialMemberDto1);
        Member member2 = Member.createMemberBy(socialMemberDto2);
        Member member3 = Member.createMemberBy(socialMemberDto3);
        Member member4 = Member.createMemberBy(socialMemberDto4);
        Member member5 = Member.createMemberBy(socialMemberDto5);
        Member member6 = Member.createMemberBy(socialMemberDto6);
        memberRepository.saveAll(List.of(member1, member2, member3, member4, member5, member6));

        // 픽픽픽 생성
        Pick pick = createPick(new Title("픽픽픽 타이틀"), ContentStatus.APPROVAL, new Count(6), member1);
        pickRepository.save(pick);

        // 픽픽픽 옵션 생성
        PickOption firstPickOption = createPickOption(new Title("픽픽픽 옵션1"), new Count(0), pick,
                PickOptionType.firstPickOption);
        PickOption secondPickOption = createPickOption(new Title("픽픽픽 옵션2"), new Count(0), pick,
                PickOptionType.secondPickOption);
        pickOptionRepository.saveAll(List.of(firstPickOption, secondPickOption));

        // 픽픽픽 투표 생성
        PickVote member1PickVote = createPickVote(member1, firstPickOption, pick);
        PickVote member2PickVote = createPickVote(member2, firstPickOption, pick);
        PickVote member3PickVote = createPickVote(member3, secondPickOption, pick);
        PickVote member4PickVote = createPickVote(member4, secondPickOption, pick);
        pickVoteRepository.saveAll(List.of(member1PickVote, member2PickVote, member3PickVote, member4PickVote));

        // 픽픽픽 최초 댓글 생성
        PickComment originParentPickComment1 = createPickComment(new CommentContents("댓글1"), true, new Count(2),
                new Count(2), member1, pick, member1PickVote);
        originParentPickComment1.modifyCommentContents(new CommentContents("댓글1 수정"), LocalDateTime.now());
        PickComment originParentPickComment2 = createPickComment(new CommentContents("댓글2"), true, new Count(1),
                new Count(1), member2, pick, member2PickVote);
        PickComment originParentPickComment3 = createPickComment(new CommentContents("댓글3"), true, new Count(0),
                new Count(0), member3, pick, member3PickVote);
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
        PickComment pickReply1 = createReplidPickComment(new CommentContents("댓글1 답글1"), member1, pick,
                originParentPickComment1, originParentPickComment1);
        PickComment pickReply2 = createReplidPickComment(new CommentContents("답글1 답글1"), member6, pick,
                originParentPickComment1, pickReply1);
        pickReply2.changeDeletedAt(LocalDateTime.now(), member1);
        PickComment pickReply3 = createReplidPickComment(new CommentContents("댓글2 답글1"), member6, pick,
                originParentPickComment2, originParentPickComment2);
        pickCommentRepository.saveAll(List.of(pickReply1, pickReply2, pickReply3));

        em.flush();
        em.clear();

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(AuthenticationMemberUtils.ANONYMOUS_USER);

        // when
        Pageable pageable = PageRequest.of(0, 5);
        SliceCustom<PickCommentsResponse> response = guestPickCommentService.findPickComments(pageable,
                pick.getId(), Long.MAX_VALUE, pickCommentSort, null, authentication);

        // then
        // 최상위 댓글 검증
        assertThat(response).hasSize(5)
                .extracting(
                        "pickCommentId",
                        "memberId",
                        "author",
                        "isCommentOfPickAuthor",
                        "isCommentAuthor",
                        "isRecommended",
                        "maskedEmail",
                        "votedPickOption",
                        "votedPickOptionTitle",
                        "contents",
                        "replyTotalCount",
                        "recommendTotalCount",
                        "isDeleted",
                        "isModified")
                .containsExactly(
                        Tuple.tuple(originParentPickComment1.getId(),
                                originParentPickComment1.getCreatedBy().getId(),
                                originParentPickComment1.getCreatedBy().getNickname().getNickname(),
                                true,
                                false,
                                false,
                                CommonResponseUtil.sliceAndMaskEmail(
                                        originParentPickComment1.getCreatedBy().getEmail().getEmail()),
                                originParentPickComment1.getPickVote().getPickOption().getPickOptionType(),
                                originParentPickComment1.getPickVote().getPickOption().getTitle().getTitle(),
                                originParentPickComment1.getContents().getCommentContents(),
                                originParentPickComment1.getReplyTotalCount().getCount(),
                                originParentPickComment1.getRecommendTotalCount().getCount(),
                                false,
                                true),

                        Tuple.tuple(originParentPickComment2.getId(),
                                originParentPickComment2.getCreatedBy().getId(),
                                originParentPickComment2.getCreatedBy().getNickname().getNickname(),
                                false,
                                false,
                                false,
                                CommonResponseUtil.sliceAndMaskEmail(
                                        originParentPickComment2.getCreatedBy().getEmail().getEmail()),
                                originParentPickComment2.getPickVote().getPickOption().getPickOptionType(),
                                originParentPickComment2.getPickVote().getPickOption().getTitle().getTitle(),
                                originParentPickComment2.getContents().getCommentContents(),
                                originParentPickComment2.getReplyTotalCount().getCount(),
                                originParentPickComment2.getRecommendTotalCount().getCount(),
                                false,
                                false),

                        Tuple.tuple(originParentPickComment3.getId(),
                                originParentPickComment3.getCreatedBy().getId(),
                                originParentPickComment3.getCreatedBy().getNickname().getNickname(),
                                false,
                                false,
                                false,
                                CommonResponseUtil.sliceAndMaskEmail(
                                        originParentPickComment3.getCreatedBy().getEmail().getEmail()),
                                originParentPickComment3.getPickVote().getPickOption().getPickOptionType(),
                                originParentPickComment3.getPickVote().getPickOption().getTitle().getTitle(),
                                originParentPickComment3.getContents().getCommentContents(),
                                originParentPickComment3.getReplyTotalCount().getCount(),
                                originParentPickComment3.getRecommendTotalCount().getCount(),
                                false,
                                false),

                        Tuple.tuple(originParentPickComment4.getId(),
                                originParentPickComment4.getCreatedBy().getId(),
                                originParentPickComment4.getCreatedBy().getNickname().getNickname(),
                                false,
                                false,
                                false,
                                CommonResponseUtil.sliceAndMaskEmail(
                                        originParentPickComment4.getCreatedBy().getEmail().getEmail()),
                                null,
                                null,
                                originParentPickComment4.getContents().getCommentContents(),
                                originParentPickComment4.getReplyTotalCount().getCount(),
                                originParentPickComment4.getRecommendTotalCount().getCount(),
                                false,
                                false),

                        Tuple.tuple(originParentPickComment5.getId(),
                                originParentPickComment5.getCreatedBy().getId(),
                                originParentPickComment5.getCreatedBy().getNickname().getNickname(),
                                false,
                                false,
                                false,
                                CommonResponseUtil.sliceAndMaskEmail(
                                        originParentPickComment5.getCreatedBy().getEmail().getEmail()),
                                null,
                                null,
                                originParentPickComment5.getContents().getCommentContents(),
                                originParentPickComment5.getReplyTotalCount().getCount(),
                                originParentPickComment5.getRecommendTotalCount().getCount(),
                                false,
                                false)
                );

        // 첫 번째 최상위 댓글의 답글 검증
        PickCommentsResponse pickCommentsResponse1 = response.getContent().get(0);
        List<PickRepliedCommentsResponse> replies1 = pickCommentsResponse1.getReplies();
        assertThat(replies1).hasSize(2)
                .extracting("pickCommentId",
                        "memberId",
                        "pickParentCommentId",
                        "pickOriginParentCommentId",
                        "isCommentOfPickAuthor",
                        "isCommentAuthor",
                        "isRecommended",
                        "author",
                        "maskedEmail",
                        "contents",
                        "recommendTotalCount",
                        "isDeleted",
                        "isModified",
                        "pickParentCommentMemberId",
                        "pickParentCommentAuthor")
                .containsExactly(
                        Tuple.tuple(pickReply1.getId(), pickReply1.getCreatedBy().getId(),
                                pickReply1.getParent().getId(),
                                pickReply1.getOriginParent().getId(),
                                true,
                                false,
                                false,
                                pickReply1.getCreatedBy().getNickname().getNickname(),
                                CommonResponseUtil.sliceAndMaskEmail(pickReply1.getCreatedBy().getEmail().getEmail()),
                                pickReply1.getContents().getCommentContents(),
                                pickReply1.getRecommendTotalCount().getCount(),
                                false,
                                false,
                                pickReply1.getParent().getCreatedBy().getId(),
                                pickReply1.getParent().getCreatedBy().getNickname().getNickname()),

                        Tuple.tuple(pickReply2.getId(), pickReply2.getCreatedBy().getId(),
                                pickReply2.getParent().getId(),
                                pickReply2.getOriginParent().getId(),
                                false,
                                false,
                                false,
                                pickReply2.getCreatedBy().getNickname().getNickname(),
                                CommonResponseUtil.sliceAndMaskEmail(pickReply2.getCreatedBy().getEmail().getEmail()),
                                CommentResponseUtil.getCommentByPickCommentStatus(pickReply2),
                                pickReply2.getRecommendTotalCount().getCount(),
                                true,
                                false,
                                pickReply2.getParent().getCreatedBy().getId(),
                                pickReply2.getParent().getCreatedBy().getNickname().getNickname())
                );

        // 두 번째 최상위 댓글의 답글 검증
        PickCommentsResponse pickCommentsResponse2 = response.getContent().get(1);
        List<PickRepliedCommentsResponse> replies2 = pickCommentsResponse2.getReplies();
        assertThat(replies2).hasSize(1)
                .extracting("pickCommentId",
                        "memberId",
                        "pickParentCommentId",
                        "pickOriginParentCommentId",
                        "isCommentOfPickAuthor",
                        "isCommentAuthor",
                        "isRecommended",
                        "author",
                        "maskedEmail",
                        "contents",
                        "recommendTotalCount",
                        "isDeleted",
                        "isModified",
                        "pickParentCommentMemberId",
                        "pickParentCommentAuthor")
                .containsExactly(
                        Tuple.tuple(pickReply3.getId(),
                                pickReply3.getCreatedBy().getId(),
                                pickReply3.getParent().getId(),
                                pickReply3.getOriginParent().getId(),
                                false,
                                false,
                                false,
                                pickReply3.getCreatedBy().getNickname().getNickname(),
                                CommonResponseUtil.sliceAndMaskEmail(pickReply3.getCreatedBy().getEmail().getEmail()),
                                pickReply3.getContents().getCommentContents(),
                                pickReply3.getRecommendTotalCount().getCount(),
                                false,
                                false,
                                pickReply3.getParent().getCreatedBy().getId(),
                                pickReply3.getParent().getCreatedBy().getNickname().getNickname())
                );

        // 세 번째 최상위 댓글의 답글 검증
        PickCommentsResponse pickCommentsResponse3 = response.getContent().get(2);
        List<PickRepliedCommentsResponse> replies3 = pickCommentsResponse3.getReplies();
        assertThat(replies3).hasSize(0);

        // 네 번째 최상위 댓글의 답글 검증
        PickCommentsResponse pickCommentsResponse4 = response.getContent().get(3);
        List<PickRepliedCommentsResponse> replies4 = pickCommentsResponse4.getReplies();
        assertThat(replies4).hasSize(0);

        // 다섯 번째 최상위 댓글의 답글 검증
        PickCommentsResponse pickCommentsResponse5 = response.getContent().get(4);
        List<PickRepliedCommentsResponse> replies5 = pickCommentsResponse5.getReplies();
        assertThat(replies5).hasSize(0);
    }

    @ParameterizedTest
    @EnumSource(PickCommentSort.class)
    @DisplayName("익명회원이 픽픽픽 모든 첫 번째 픽픽픽 옵션에 투표한 댓글/답글을 알맞게 정렬하여 커서 방식으로 조회한다.")
    void findPickCommentsByPickCommentSortAndFirstPickOption(PickCommentSort pickCommentSort) {
        // given
        // 회원 생성
        SocialMemberDto socialMemberDto1 = createSocialDto("user1", name, "nickname1", password, "user1@gmail.com",
                socialType, role);
        SocialMemberDto socialMemberDto2 = createSocialDto("user2", name, "nickname2", password, "user2@gmail.com",
                socialType, role);
        SocialMemberDto socialMemberDto3 = createSocialDto("user3", name, "nickname3", password, "user3@gmail.com",
                socialType, role);
        SocialMemberDto socialMemberDto4 = createSocialDto("user4", name, "nickname4", password, "user4@gmail.com",
                socialType, role);
        SocialMemberDto socialMemberDto5 = createSocialDto("user5", name, "nickname5", password, "user5@gmail.com",
                socialType, role);
        SocialMemberDto socialMemberDto6 = createSocialDto("user6", name, "nickname6", password, "user6@gmail.com",
                socialType, role);
        Member member1 = Member.createMemberBy(socialMemberDto1);
        Member member2 = Member.createMemberBy(socialMemberDto2);
        Member member3 = Member.createMemberBy(socialMemberDto3);
        Member member4 = Member.createMemberBy(socialMemberDto4);
        Member member5 = Member.createMemberBy(socialMemberDto5);
        Member member6 = Member.createMemberBy(socialMemberDto6);
        memberRepository.saveAll(List.of(member1, member2, member3, member4, member5, member6));

        // 픽픽픽 생성
        Pick pick = createPick(new Title("픽픽픽 타이틀"), ContentStatus.APPROVAL, new Count(6), member1);
        pickRepository.save(pick);

        // 픽픽픽 옵션 생성
        PickOption firstPickOption = createPickOption(new Title("픽픽픽 옵션1"), new Count(0), pick,
                PickOptionType.firstPickOption);
        PickOption secondPickOption = createPickOption(new Title("픽픽픽 옵션2"), new Count(0), pick,
                PickOptionType.secondPickOption);
        pickOptionRepository.saveAll(List.of(firstPickOption, secondPickOption));

        // 픽픽픽 투표 생성
        PickVote member1PickVote = createPickVote(member1, firstPickOption, pick);
        PickVote member2PickVote = createPickVote(member2, firstPickOption, pick);
        PickVote member3PickVote = createPickVote(member3, secondPickOption, pick);
        PickVote member4PickVote = createPickVote(member4, secondPickOption, pick);
        pickVoteRepository.saveAll(List.of(member1PickVote, member2PickVote, member3PickVote, member4PickVote));

        // 픽픽픽 최초 댓글 생성
        PickComment originParentPickComment1 = createPickComment(new CommentContents("댓글1"), true, new Count(2),
                new Count(2), member1, pick, member1PickVote);
        originParentPickComment1.modifyCommentContents(new CommentContents("댓글1 수정"), LocalDateTime.now());
        PickComment originParentPickComment2 = createPickComment(new CommentContents("댓글2"), true, new Count(1),
                new Count(1), member2, pick, member2PickVote);
        PickComment originParentPickComment3 = createPickComment(new CommentContents("댓글3"), true, new Count(0),
                new Count(0), member3, pick, member3PickVote);
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
        PickComment pickReply1 = createReplidPickComment(new CommentContents("댓글1 답글1"), member1, pick,
                originParentPickComment1, originParentPickComment1);
        PickComment pickReply2 = createReplidPickComment(new CommentContents("답글1 답글1"), member6, pick,
                originParentPickComment1, pickReply1);
        PickComment pickReply3 = createReplidPickComment(new CommentContents("댓글2 답글1"), member6, pick,
                originParentPickComment2, originParentPickComment2);
        pickCommentRepository.saveAll(List.of(pickReply1, pickReply2, pickReply3));

        em.flush();
        em.clear();

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(AuthenticationMemberUtils.ANONYMOUS_USER);

        // when
        Pageable pageable = PageRequest.of(0, 5);
        SliceCustom<PickCommentsResponse> response = guestPickCommentService.findPickComments(pageable,
                pick.getId(), Long.MAX_VALUE, pickCommentSort, PickOptionType.firstPickOption, authentication);

        // then
        // 최상위 댓글 검증
        assertThat(response).hasSize(2)
                .extracting(
                        "pickCommentId",
                        "memberId",
                        "author",
                        "isCommentOfPickAuthor",
                        "isCommentAuthor",
                        "isRecommended",
                        "maskedEmail",
                        "votedPickOption",
                        "votedPickOptionTitle",
                        "contents",
                        "replyTotalCount",
                        "recommendTotalCount",
                        "isDeleted",
                        "isModified")
                .containsExactly(
                        Tuple.tuple(originParentPickComment1.getId(),
                                originParentPickComment1.getCreatedBy().getId(),
                                originParentPickComment1.getCreatedBy().getNickname().getNickname(),
                                true,
                                false,
                                false,
                                CommonResponseUtil.sliceAndMaskEmail(
                                        originParentPickComment1.getCreatedBy().getEmail().getEmail()),
                                originParentPickComment1.getPickVote().getPickOption().getPickOptionType(),
                                originParentPickComment1.getPickVote().getPickOption().getTitle().getTitle(),
                                originParentPickComment1.getContents().getCommentContents(),
                                originParentPickComment1.getReplyTotalCount().getCount(),
                                originParentPickComment1.getRecommendTotalCount().getCount(),
                                false,
                                true),

                        Tuple.tuple(originParentPickComment2.getId(),
                                originParentPickComment2.getCreatedBy().getId(),
                                originParentPickComment2.getCreatedBy().getNickname().getNickname(),
                                false,
                                false,
                                false,
                                CommonResponseUtil.sliceAndMaskEmail(
                                        originParentPickComment2.getCreatedBy().getEmail().getEmail()),
                                originParentPickComment2.getPickVote().getPickOption().getPickOptionType(),
                                originParentPickComment2.getPickVote().getPickOption().getTitle().getTitle(),
                                originParentPickComment2.getContents().getCommentContents(),
                                originParentPickComment2.getReplyTotalCount().getCount(),
                                originParentPickComment2.getRecommendTotalCount().getCount(),
                                false,
                                false)
                );

        // 첫 번째 최상위 댓글의 답글 검증
        PickCommentsResponse pickCommentsResponse1 = response.getContent().get(0);
        List<PickRepliedCommentsResponse> replies1 = pickCommentsResponse1.getReplies();
        assertThat(replies1).hasSize(2)
                .extracting("pickCommentId",
                        "memberId",
                        "pickParentCommentId",
                        "pickOriginParentCommentId",
                        "isCommentOfPickAuthor",
                        "isCommentAuthor",
                        "isRecommended",
                        "author",
                        "maskedEmail",
                        "contents",
                        "recommendTotalCount",
                        "isDeleted",
                        "isModified",
                        "pickParentCommentMemberId",
                        "pickParentCommentAuthor")
                .containsExactly(
                        Tuple.tuple(pickReply1.getId(), pickReply1.getCreatedBy().getId(),
                                pickReply1.getParent().getId(),
                                pickReply1.getOriginParent().getId(),
                                true,
                                false,
                                false,
                                pickReply1.getCreatedBy().getNickname().getNickname(),
                                CommonResponseUtil.sliceAndMaskEmail(pickReply1.getCreatedBy().getEmail().getEmail()),
                                pickReply1.getContents().getCommentContents(),
                                pickReply1.getRecommendTotalCount().getCount(),
                                false,
                                false,
                                pickReply1.getParent().getCreatedBy().getId(),
                                pickReply1.getParent().getCreatedBy().getNickname().getNickname()),

                        Tuple.tuple(pickReply2.getId(), pickReply2.getCreatedBy().getId(),
                                pickReply2.getParent().getId(),
                                pickReply2.getOriginParent().getId(),
                                false,
                                false,
                                false,
                                pickReply2.getCreatedBy().getNickname().getNickname(),
                                CommonResponseUtil.sliceAndMaskEmail(pickReply2.getCreatedBy().getEmail().getEmail()),
                                pickReply2.getContents().getCommentContents(),
                                pickReply2.getRecommendTotalCount().getCount(),
                                false,
                                false,
                                pickReply2.getParent().getCreatedBy().getId(),
                                pickReply2.getParent().getCreatedBy().getNickname().getNickname())
                );

        // 두 번째 최상위 댓글의 답글 검증
        PickCommentsResponse pickCommentsResponse2 = response.getContent().get(1);
        List<PickRepliedCommentsResponse> replies2 = pickCommentsResponse2.getReplies();
        assertThat(replies2).hasSize(1)
                .extracting("pickCommentId",
                        "memberId",
                        "pickParentCommentId",
                        "pickOriginParentCommentId",
                        "isCommentOfPickAuthor",
                        "isCommentAuthor",
                        "isRecommended",
                        "author",
                        "maskedEmail",
                        "contents",
                        "recommendTotalCount",
                        "isDeleted",
                        "isModified",
                        "pickParentCommentMemberId",
                        "pickParentCommentAuthor")
                .containsExactly(
                        Tuple.tuple(pickReply3.getId(),
                                pickReply3.getCreatedBy().getId(),
                                pickReply3.getParent().getId(),
                                pickReply3.getOriginParent().getId(),
                                false,
                                false,
                                false,
                                pickReply3.getCreatedBy().getNickname().getNickname(),
                                CommonResponseUtil.sliceAndMaskEmail(pickReply3.getCreatedBy().getEmail().getEmail()),
                                pickReply3.getContents().getCommentContents(),
                                pickReply3.getRecommendTotalCount().getCount(),
                                false,
                                false,
                                pickReply3.getParent().getCreatedBy().getId(),
                                pickReply3.getParent().getCreatedBy().getNickname().getNickname())
                );
    }

    @ParameterizedTest
    @EnumSource(PickCommentSort.class)
    @DisplayName("익명회원이 픽픽픽 모든 두 번째 픽픽픽 옵션에 투표한 댓글/답글을 알맞게 정렬하여 커서 방식으로 조회한다.")
    void findPickCommentsByPickCommentSortAndSecondPickOption(PickCommentSort pickCommentSort) {
        // given
        // 회원 생성
        SocialMemberDto socialMemberDto1 = createSocialDto("user1", name, "nickname1", password, "user1@gmail.com",
                socialType, role);
        SocialMemberDto socialMemberDto2 = createSocialDto("user2", name, "nickname2", password, "user2@gmail.com",
                socialType, role);
        SocialMemberDto socialMemberDto3 = createSocialDto("user3", name, "nickname3", password, "user3@gmail.com",
                socialType, role);
        SocialMemberDto socialMemberDto4 = createSocialDto("user4", name, "nickname4", password, "user4@gmail.com",
                socialType, role);
        SocialMemberDto socialMemberDto5 = createSocialDto("user5", name, "nickname5", password, "user5@gmail.com",
                socialType, role);
        SocialMemberDto socialMemberDto6 = createSocialDto("user6", name, "nickname6", password, "user6@gmail.com",
                socialType, role);
        Member member1 = Member.createMemberBy(socialMemberDto1);
        Member member2 = Member.createMemberBy(socialMemberDto2);
        Member member3 = Member.createMemberBy(socialMemberDto3);
        Member member4 = Member.createMemberBy(socialMemberDto4);
        Member member5 = Member.createMemberBy(socialMemberDto5);
        Member member6 = Member.createMemberBy(socialMemberDto6);
        memberRepository.saveAll(List.of(member1, member2, member3, member4, member5, member6));

        // 픽픽픽 생성
        Pick pick = createPick(new Title("픽픽픽 타이틀"), ContentStatus.APPROVAL, new Count(6), member1);
        pickRepository.save(pick);

        // 픽픽픽 옵션 생성
        PickOption firstPickOption = createPickOption(new Title("픽픽픽 옵션1"), new Count(0), pick,
                PickOptionType.firstPickOption);
        PickOption secondPickOption = createPickOption(new Title("픽픽픽 옵션2"), new Count(0), pick,
                PickOptionType.secondPickOption);
        pickOptionRepository.saveAll(List.of(firstPickOption, secondPickOption));

        // 픽픽픽 투표 생성
        PickVote member1PickVote = createPickVote(member1, firstPickOption, pick);
        PickVote member2PickVote = createPickVote(member2, firstPickOption, pick);
        PickVote member3PickVote = createPickVote(member3, secondPickOption, pick);
        PickVote member4PickVote = createPickVote(member4, secondPickOption, pick);
        pickVoteRepository.saveAll(List.of(member1PickVote, member2PickVote, member3PickVote, member4PickVote));

        // 픽픽픽 최초 댓글 생성
        PickComment originParentPickComment1 = createPickComment(new CommentContents("댓글1"), true, new Count(2),
                new Count(2), member1, pick, member1PickVote);
        PickComment originParentPickComment2 = createPickComment(new CommentContents("댓글2"), true, new Count(1),
                new Count(1), member2, pick, member2PickVote);
        PickComment originParentPickComment3 = createPickComment(new CommentContents("댓글3"), true, new Count(0),
                new Count(0), member3, pick, member3PickVote);
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
        PickComment pickReply1 = createReplidPickComment(new CommentContents("댓글1 답글1"), member1, pick,
                originParentPickComment1, originParentPickComment1);
        PickComment pickReply2 = createReplidPickComment(new CommentContents("답글1 답글1"), member6, pick,
                originParentPickComment1, pickReply1);
        PickComment pickReply3 = createReplidPickComment(new CommentContents("댓글2 답글1"), member6, pick,
                originParentPickComment2, originParentPickComment2);
        pickCommentRepository.saveAll(List.of(pickReply3, pickReply2, pickReply1));

        em.flush();
        em.clear();

        // when
        // 익명회원 목킹
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(AuthenticationMemberUtils.ANONYMOUS_USER);

        Pageable pageable = PageRequest.of(0, 5);
        SliceCustom<PickCommentsResponse> response = guestPickCommentService.findPickComments(pageable,
                pick.getId(), Long.MAX_VALUE, pickCommentSort, PickOptionType.secondPickOption, authentication);

        // then
        // 최상위 댓글 검증
        assertThat(response).hasSize(1)
                .extracting(
                        "pickCommentId",
                        "memberId",
                        "author",
                        "isCommentOfPickAuthor",
                        "isCommentAuthor",
                        "isRecommended",
                        "maskedEmail",
                        "votedPickOption",
                        "votedPickOptionTitle",
                        "contents",
                        "replyTotalCount",
                        "recommendTotalCount",
                        "isDeleted",
                        "isModified")
                .containsExactly(
                        Tuple.tuple(originParentPickComment3.getId(),
                                originParentPickComment3.getCreatedBy().getId(),
                                originParentPickComment3.getCreatedBy().getNickname().getNickname(),
                                false,
                                false,
                                false,
                                CommonResponseUtil.sliceAndMaskEmail(
                                        originParentPickComment3.getCreatedBy().getEmail().getEmail()),
                                originParentPickComment3.getPickVote().getPickOption().getPickOptionType(),
                                originParentPickComment3.getPickVote().getPickOption().getTitle().getTitle(),
                                originParentPickComment3.getContents().getCommentContents(),
                                originParentPickComment3.getReplyTotalCount().getCount(),
                                originParentPickComment3.getRecommendTotalCount().getCount(),
                                false,
                                false)
                );

        // 첫 번째 최상위 댓글의 답글 검증
        PickCommentsResponse pickCommentsResponse1 = response.getContent().get(0);
        List<PickRepliedCommentsResponse> replies1 = pickCommentsResponse1.getReplies();
        assertThat(replies1).hasSize(0);
    }

    @ParameterizedTest
    @EnumSource(PickCommentSort.class)
    @DisplayName("익명회원이 아닌 경우 익명회원 전용 픽픽픽 댓글/답글 조회 메소드를 호출하면 예외가 발생한다.")
    void findPickCommentsNotAnonymousMember(PickCommentSort pickCommentSort) {
        // given
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Pageable pageable = PageRequest.of(0, 5);

        // when // then
        assertThatThrownBy(() -> guestPickCommentService.findPickComments(pageable,
                1L, Long.MAX_VALUE, pickCommentSort, PickOptionType.secondPickOption, authentication))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(INVALID_METHODS_CALL_MESSAGE);
    }

    @Test
    @DisplayName("익명 회원이 아닌 경우 익명회원 전용 픽픽픽 베스트 댓글 조회 메소드를 호출하면 예외가 발생한다.")
    void findPickBestCommentsNotAnonymousMember() {
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
        assertThatThrownBy(() -> guestPickCommentService.findPickBestComments(3, 1L, authentication))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(INVALID_METHODS_CALL_MESSAGE);
    }

    @Test
    @DisplayName("익명 회원이 offset에 정책에 맞게 픽픽픽 베스트 댓글을 조회한다.")
    void findPickBestComments() {
        // given
        // 회원 생성
        SocialMemberDto socialMemberDto1 = createSocialDto("user1", name, "nickname1", password, "user1@gmail.com",
                socialType, Role.ROLE_ADMIN.name());
        SocialMemberDto socialMemberDto2 = createSocialDto("user2", name, "nickname2", password, "user2@gmail.com",
                socialType, role);
        SocialMemberDto socialMemberDto3 = createSocialDto("user3", name, "nickname3", password, "user3@gmail.com",
                socialType, role);
        SocialMemberDto socialMemberDto4 = createSocialDto("user4", name, "nickname4", password, "user4@gmail.com",
                socialType, role);
        SocialMemberDto socialMemberDto5 = createSocialDto("user5", name, "nickname5", password, "user5@gmail.com",
                socialType, role);
        SocialMemberDto socialMemberDto6 = createSocialDto("user6", name, "nickname6", password, "user6@gmail.com",
                socialType, role);
        Member member1 = Member.createMemberBy(socialMemberDto1);
        Member member2 = Member.createMemberBy(socialMemberDto2);
        Member member3 = Member.createMemberBy(socialMemberDto3);
        Member member4 = Member.createMemberBy(socialMemberDto4);
        Member member5 = Member.createMemberBy(socialMemberDto5);
        Member member6 = Member.createMemberBy(socialMemberDto6);
        memberRepository.saveAll(List.of(member1, member2, member3, member4, member5, member6));

        // 픽픽픽 생성
        Pick pick = createPick(new Title("픽픽픽 타이틀"), ContentStatus.APPROVAL, new Count(6), member1);
        pickRepository.save(pick);

        // 픽픽픽 옵션 생성
        PickOption firstPickOption = createPickOption(new Title("픽픽픽 옵션1"), new Count(0), pick,
                PickOptionType.firstPickOption);
        PickOption secondPickOption = createPickOption(new Title("픽픽픽 옵션2"), new Count(0), pick,
                PickOptionType.secondPickOption);
        pickOptionRepository.saveAll(List.of(firstPickOption, secondPickOption));

        // 픽픽픽 투표 생성
        PickVote member1PickVote = createPickVote(member1, firstPickOption, pick);
        PickVote member2PickVote = createPickVote(member2, firstPickOption, pick);
        PickVote member3PickVote = createPickVote(member3, secondPickOption, pick);
        PickVote member4PickVote = createPickVote(member4, secondPickOption, pick);
        pickVoteRepository.saveAll(List.of(member1PickVote, member2PickVote, member3PickVote, member4PickVote));

        // 픽픽픽 최초 댓글 생성
        PickComment originParentPickComment1 = createPickComment(new CommentContents("댓글1"), true, new Count(2),
                new Count(3), member1, pick, member1PickVote);
        originParentPickComment1.modifyCommentContents(new CommentContents("수정된 댓글1"), LocalDateTime.now());
        PickComment originParentPickComment2 = createPickComment(new CommentContents("댓글2"), true, new Count(1),
                new Count(2), member2, pick, member2PickVote);
        PickComment originParentPickComment3 = createPickComment(new CommentContents("댓글3"), true, new Count(0),
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
        PickComment pickReply1 = createReplidPickComment(new CommentContents("댓글1 답글1"), member1, pick,
                originParentPickComment1, originParentPickComment1);
        PickComment pickReply2 = createReplidPickComment(new CommentContents("답글1 답글1"), member6, pick,
                originParentPickComment1, pickReply1);
        pickReply2.changeDeletedAt(LocalDateTime.now(), member1);
        PickComment pickReply3 = createReplidPickComment(new CommentContents("댓글2 답글1"), member6, pick,
                originParentPickComment2, originParentPickComment2);
        pickCommentRepository.saveAll(List.of(pickReply1, pickReply2, pickReply3));

        // 추천 생성
        PickCommentRecommend pickCommentRecommend = createPickCommentRecommend(originParentPickComment1, member1, true);
        pickCommentRecommendRepository.save(pickCommentRecommend);

        em.flush();
        em.clear();

        // when
        // 익명회원 목킹
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(AuthenticationMemberUtils.ANONYMOUS_USER);

        List<PickCommentsResponse> response = guestPickCommentService.findPickBestComments(3, pick.getId(),
                authentication);

        // then
        // 최상위 댓글 검증
        assertThat(response).hasSize(3)
                .extracting(
                        "pickCommentId",
                        "memberId",
                        "author",
                        "isCommentOfPickAuthor",
                        "isCommentAuthor",
                        "isRecommended",
                        "maskedEmail",
                        "votedPickOption",
                        "votedPickOptionTitle",
                        "contents",
                        "replyTotalCount",
                        "recommendTotalCount",
                        "isDeleted",
                        "isModified")
                .containsExactly(
                        Tuple.tuple(originParentPickComment1.getId(),
                                originParentPickComment1.getCreatedBy().getId(),
                                originParentPickComment1.getCreatedBy().getNickname().getNickname(),
                                true,
                                false,
                                false,
                                CommonResponseUtil.sliceAndMaskEmail(
                                        originParentPickComment1.getCreatedBy().getEmail().getEmail()),
                                originParentPickComment1.getPickVote().getPickOption().getPickOptionType(),
                                originParentPickComment1.getPickVote().getPickOption().getTitle().getTitle(),
                                originParentPickComment1.getContents().getCommentContents(),
                                originParentPickComment1.getReplyTotalCount().getCount(),
                                originParentPickComment1.getRecommendTotalCount().getCount(),
                                false,
                                true),

                        Tuple.tuple(originParentPickComment2.getId(),
                                originParentPickComment2.getCreatedBy().getId(),
                                originParentPickComment2.getCreatedBy().getNickname().getNickname(),
                                false,
                                false,
                                false,
                                CommonResponseUtil.sliceAndMaskEmail(
                                        originParentPickComment2.getCreatedBy().getEmail().getEmail()),
                                originParentPickComment2.getPickVote().getPickOption().getPickOptionType(),
                                originParentPickComment2.getPickVote().getPickOption().getTitle().getTitle(),
                                originParentPickComment2.getContents().getCommentContents(),
                                originParentPickComment2.getReplyTotalCount().getCount(),
                                originParentPickComment2.getRecommendTotalCount().getCount(),
                                false,
                                false),

                        Tuple.tuple(originParentPickComment3.getId(),
                                originParentPickComment3.getCreatedBy().getId(),
                                originParentPickComment3.getCreatedBy().getNickname().getNickname(),
                                false,
                                false,
                                false,
                                CommonResponseUtil.sliceAndMaskEmail(
                                        originParentPickComment3.getCreatedBy().getEmail().getEmail()),
                                originParentPickComment3.getPickVote().getPickOption().getPickOptionType(),
                                originParentPickComment3.getPickVote().getPickOption().getTitle().getTitle(),
                                originParentPickComment3.getContents().getCommentContents(),
                                originParentPickComment3.getReplyTotalCount().getCount(),
                                originParentPickComment3.getRecommendTotalCount().getCount(),
                                false,
                                false)
                );

        // 첫 번째 최상위 댓글의 답글 검증
        PickCommentsResponse pickCommentsResponse1 = response.get(0);
        List<PickRepliedCommentsResponse> replies1 = pickCommentsResponse1.getReplies();
        assertThat(replies1).hasSize(2)
                .extracting("pickCommentId",
                        "memberId",
                        "pickParentCommentId",
                        "pickOriginParentCommentId",
                        "isCommentOfPickAuthor",
                        "isCommentAuthor",
                        "isRecommended",
                        "author",
                        "maskedEmail",
                        "contents",
                        "recommendTotalCount",
                        "isDeleted",
                        "isModified",
                        "pickParentCommentMemberId",
                        "pickParentCommentAuthor")
                .containsExactly(
                        Tuple.tuple(pickReply1.getId(), pickReply1.getCreatedBy().getId(),
                                pickReply1.getParent().getId(),
                                pickReply1.getOriginParent().getId(),
                                true,
                                false,
                                false,
                                pickReply1.getCreatedBy().getNickname().getNickname(),
                                CommonResponseUtil.sliceAndMaskEmail(pickReply1.getCreatedBy().getEmail().getEmail()),
                                pickReply1.getContents().getCommentContents(),
                                pickReply1.getRecommendTotalCount().getCount(),
                                false,
                                false,
                                pickReply1.getParent().getCreatedBy().getId(),
                                pickReply1.getParent().getCreatedBy().getNicknameAsString()),

                        Tuple.tuple(pickReply2.getId(), pickReply2.getCreatedBy().getId(),
                                pickReply2.getParent().getId(),
                                pickReply2.getOriginParent().getId(),
                                false,
                                false,
                                false,
                                pickReply2.getCreatedBy().getNickname().getNickname(),
                                CommonResponseUtil.sliceAndMaskEmail(pickReply2.getCreatedBy().getEmail().getEmail()),
                                CommentResponseUtil.getCommentByPickCommentStatus(pickReply2),
                                pickReply2.getRecommendTotalCount().getCount(),
                                true,
                                false,
                                pickReply1.getParent().getCreatedBy().getId(),
                                pickReply1.getParent().getCreatedBy().getNicknameAsString())
                );

        // 두 번째 최상위 댓글의 답글 검증
        PickCommentsResponse pickCommentsResponse2 = response.get(1);
        List<PickRepliedCommentsResponse> replies2 = pickCommentsResponse2.getReplies();
        assertThat(replies2).hasSize(1)
                .extracting("pickCommentId",
                        "memberId",
                        "pickParentCommentId",
                        "pickOriginParentCommentId",
                        "isCommentOfPickAuthor",
                        "isCommentAuthor",
                        "isRecommended",
                        "author",
                        "maskedEmail",
                        "contents",
                        "recommendTotalCount",
                        "isDeleted",
                        "isModified",
                        "pickParentCommentMemberId",
                        "pickParentCommentAuthor")
                .containsExactly(
                        Tuple.tuple(pickReply3.getId(),
                                pickReply3.getCreatedBy().getId(),
                                pickReply3.getParent().getId(),
                                pickReply3.getOriginParent().getId(),
                                false,
                                false,
                                false,
                                pickReply3.getCreatedBy().getNickname().getNickname(),
                                CommonResponseUtil.sliceAndMaskEmail(pickReply3.getCreatedBy().getEmail().getEmail()),
                                pickReply3.getContents().getCommentContents(),
                                pickReply3.getRecommendTotalCount().getCount(),
                                false,
                                false,
                                pickReply3.getParent().getCreatedBy().getId(),
                                pickReply3.getParent().getCreatedBy().getNicknameAsString())
                );

        // 세 번째 최상위 댓글의 답글 검증
        PickCommentsResponse pickCommentsResponse3 = response.get(2);
        List<PickRepliedCommentsResponse> replies3 = pickCommentsResponse3.getReplies();
        assertThat(replies3).hasSize(0);
    }

    private PickVote createPickVote(Member member, PickOption pickOption, Pick pick) {
        PickVote pickVote = PickVote.builder()
                .member(member)
                .build();

        pickVote.changePickOption(pickOption);
        pickVote.changePick(pick);

        return pickVote;
    }

    private Pick createPick(Title title, ContentStatus contentStatus, Count viewTotalCount, Count voteTotalCount,
                            Count commentTotalCount, Count popularScore, Member member) {
        return Pick.builder()
                .title(title)
                .contentStatus(contentStatus)
                .viewTotalCount(viewTotalCount)
                .voteTotalCount(voteTotalCount)
                .commentTotalCount(commentTotalCount)
                .popularScore(popularScore)
                .member(member)
                .build();
    }

    private PickComment createPickComment(CommentContents contents, Boolean isPublic, Count recommendTotalCount,
                                          Member member, Pick pick) {
        PickComment pickComment = PickComment.builder()
                .contents(contents)
                .isPublic(isPublic)
                .createdBy(member)
                .recommendTotalCount(recommendTotalCount)
                .pick(pick)
                .build();

        pickComment.changePick(pick);

        return pickComment;
    }

    private PickCommentRecommend createPickCommentRecommend(PickComment pickComment, Member member,
                                                            Boolean recommendedStatus) {
        return PickCommentRecommend.builder()
                .pickComment(pickComment)
                .member(member)
                .recommendedStatus(recommendedStatus)
                .build();
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

    private PickOption createPickOption(Title title, Count voteTotalCount, Pick pick, PickOptionType pickOptionType) {
        PickOption pickOption = PickOption.builder()
                .title(title)
                .voteTotalCount(voteTotalCount)
                .pickOptionType(pickOptionType)
                .build();

        pickOption.changePick(pick);

        return pickOption;
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