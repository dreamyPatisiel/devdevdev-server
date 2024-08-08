package com.dreamypatisiel.devdevdev.domain.service.pick;

import static com.dreamypatisiel.devdevdev.domain.exception.PickExceptionMessage.INVALID_ANONYMOUS_MEMBER_ID_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.PickExceptionMessage.INVALID_CAN_NOT_VOTE_SAME_PICK_OPTION_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.PickExceptionMessage.INVALID_NOT_FOUND_PICK_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.service.pick.GuestPickService.INVALID_ANONYMOUS_CAN_NOT_USE_THIS_FUNCTION_MESSAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.dreamypatisiel.devdevdev.domain.entity.AnonymousMember;
import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.Pick;
import com.dreamypatisiel.devdevdev.domain.entity.PickOption;
import com.dreamypatisiel.devdevdev.domain.entity.PickOptionImage;
import com.dreamypatisiel.devdevdev.domain.entity.PickVote;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Count;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.PickOptionContents;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Title;
import com.dreamypatisiel.devdevdev.domain.entity.enums.ContentStatus;
import com.dreamypatisiel.devdevdev.domain.entity.enums.PickOptionType;
import com.dreamypatisiel.devdevdev.domain.entity.enums.Role;
import com.dreamypatisiel.devdevdev.domain.entity.enums.SocialType;
import com.dreamypatisiel.devdevdev.domain.policy.PickPopularScorePolicy;
import com.dreamypatisiel.devdevdev.domain.repository.member.AnonymousMemberRepository;
import com.dreamypatisiel.devdevdev.domain.repository.member.MemberRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickOptionImageRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickOptionRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickSort;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickVoteRepository;
import com.dreamypatisiel.devdevdev.domain.service.pick.dto.RegisterPickCommentDto;
import com.dreamypatisiel.devdevdev.domain.service.pick.dto.VotePickOptionDto;
import com.dreamypatisiel.devdevdev.domain.service.response.PickDetailOptionImageResponse;
import com.dreamypatisiel.devdevdev.domain.service.response.PickDetailOptionResponse;
import com.dreamypatisiel.devdevdev.domain.service.response.PickDetailResponse;
import com.dreamypatisiel.devdevdev.domain.service.response.PickMainResponse;
import com.dreamypatisiel.devdevdev.domain.service.response.VotePickOptionResponse;
import com.dreamypatisiel.devdevdev.domain.service.response.VotePickResponse;
import com.dreamypatisiel.devdevdev.domain.service.response.util.PickResponseUtils;
import com.dreamypatisiel.devdevdev.exception.NotFoundException;
import com.dreamypatisiel.devdevdev.exception.VotePickOptionException;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.SocialMemberDto;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.UserPrincipal;
import com.dreamypatisiel.devdevdev.global.utils.AuthenticationMemberUtils;
import com.dreamypatisiel.devdevdev.web.controller.request.RegisterPickRequest;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class GuestPickServiceTest {

    @Autowired
    GuestPickService guestPickService;
    @Autowired
    PickRepository pickRepository;
    @Autowired
    PickOptionRepository pickOptionRepository;
    @Autowired
    PickVoteRepository pickVoteRepository;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    PickPopularScorePolicy pickPopularScorePolicy;
    @Autowired
    PickOptionImageRepository pickOptionImageRepository;
    @Autowired
    AnonymousMemberRepository anonymousMemberRepository;
    @Autowired
    EntityManager em;

    String userId = "dreamy5patisiel";
    String name = "꿈빛파티시엘";
    String nickname = "행복한 꿈빛파티시엘";
    String email = "dreamy5patisiel@kakao.com";
    String password = "password";
    String socialType = SocialType.KAKAO.name();
    String role = Role.ROLE_USER.name();

    Title pickOptionTitle1 = new Title("pickOptionTitle1");
    PickOptionContents pickOptionContents1 = new PickOptionContents("hello1");
    Count pickOptionVoteCount1 = new Count(10);
    Title pickOptionTitle2 = new Title("pickOptionTitle2");
    PickOptionContents pickOptionContents2 = new PickOptionContents("hello2");
    Count pickOptionVoteCount2 = new Count(90);
    Title pickTitle = new Title("픽픽픽 제목");
    String thumbnailUrl = "섬네일 이미지 url";
    String author = "운영자";

    @Test
    @DisplayName("익명 회원이 커서 방식으로 익명 회원 전용 픽픽픽 메인을 조회한다.")
    void findPicksMain() {
        // given
        Count pickVoteTotalCount = new Count(pickOptionVoteCount1.getCount() + pickOptionVoteCount2.getCount());
        Count pickViewTotalCount = new Count(1);
        Count pickCommentTotalCount = new Count(0);
        Count pickPopularScore = new Count(0);
        Pick pick = createPick(pickTitle, pickVoteTotalCount, pickViewTotalCount, pickCommentTotalCount,
                pickPopularScore,
                thumbnailUrl, author, List.of());
        pickRepository.save(pick);

        PickOption pickOption1 = createPickOption(pick, pickOptionTitle1, pickOptionContents1, pickOptionVoteCount1,
                PickOptionType.firstPickOption);
        PickOption pickOption2 = createPickOption(pick, pickOptionTitle2, pickOptionContents2, pickOptionVoteCount2,
                PickOptionType.secondPickOption);
        pickOptionRepository.saveAll(List.of(pickOption1, pickOption2));

        Pageable pageable = PageRequest.of(0, 10);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(AuthenticationMemberUtils.ANONYMOUS_USER);

        String anonymousMemberId = "GA1.1.276672604.1715872960";

        // when
        Slice<PickMainResponse> picksMain = guestPickService.findPicksMain(pageable, Long.MAX_VALUE, null,
                anonymousMemberId, authentication);

        // then
        Pick findPick = pickRepository.findById(pick.getId()).get();
        assertThat(picksMain).hasSize(1)
                .extracting("id", "title", "voteTotalCount",
                        "commentTotalCount")
                .containsExactly(
                        tuple(findPick.getId(), findPick.getTitle().getTitle(), findPick.getVoteTotalCount().getCount(),
                                findPick.getCommentTotalCount().getCount())
                );

        List<PickOption> pickOptions = findPick.getPickOptions();
        assertThat(picksMain.getContent().get(0).getPickOptions()).hasSize(2)
                .extracting("id", "title", "percent")
                .containsExactly(
                        tuple(pickOptions.get(0).getId(), pickOptions.get(0).getTitle().getTitle(), 10),
                        tuple(pickOptions.get(1).getId(), pickOptions.get(1).getTitle().getTitle(), 90)
                );
    }

    @Test
    @DisplayName("익명 회원이 커서 방식으로 익명 회원 전용 조회수 내림차순으로 픽픽픽 메인을 조회한다.")
    void findPicksMainMOST_VIEWED() {
        // given
        Title title1 = new Title("픽1타이틀");
        Title title2 = new Title("픽2타이틀");
        Title title3 = new Title("픽3타이틀");

        Count pick1ViewTotalCount = new Count(1);
        Count pick2ViewTotalCount = new Count(2);
        Count pick3ViewTotalCount = new Count(3);

        Count count = new Count(0);
        Pick pick1 = createPick(title1, count, pick1ViewTotalCount, count, count, thumbnailUrl, author, List.of());
        Pick pick2 = createPick(title2, count, pick2ViewTotalCount, count, count, thumbnailUrl, author, List.of());
        Pick pick3 = createPick(title3, count, pick3ViewTotalCount, count, count, thumbnailUrl, author, List.of());
        pickRepository.saveAll(List.of(pick1, pick2, pick3));

        PickOption pickOption1 = createPickOption(pick1, new Title("픽옵션1"), new PickOptionContents("픽콘텐츠1"),
                new Count(1), PickOptionType.firstPickOption);
        PickOption pickOption2 = createPickOption(pick1, new Title("픽옵션2"), new PickOptionContents("픽콘텐츠2"),
                new Count(2), PickOptionType.secondPickOption);
        PickOption pickOption3 = createPickOption(pick2, new Title("픽옵션3"), new PickOptionContents("픽콘텐츠3"),
                new Count(3), PickOptionType.firstPickOption);
        PickOption pickOption4 = createPickOption(pick2, new Title("픽옵션4"), new PickOptionContents("픽콘텐츠4"),
                new Count(4), PickOptionType.secondPickOption);
        PickOption pickOption5 = createPickOption(pick3, new Title("픽옵션5"), new PickOptionContents("픽콘텐츠5"),
                new Count(5), PickOptionType.firstPickOption);
        PickOption pickOption6 = createPickOption(pick3, new Title("픽옵션6"), new PickOptionContents("픽콘텐츠6"),
                new Count(6), PickOptionType.secondPickOption);
        pickOptionRepository.saveAll(
                List.of(pickOption1, pickOption2, pickOption3, pickOption4, pickOption5, pickOption6));

        Pageable pageable = PageRequest.of(0, 10);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(AuthenticationMemberUtils.ANONYMOUS_USER);

        String anonymousMemberId = "GA1.1.276672604.1715872960";

        // when
        Slice<PickMainResponse> picksMain = guestPickService.findPicksMain(pageable, Long.MAX_VALUE,
                PickSort.MOST_VIEWED, anonymousMemberId, authentication);

        // then
        assertThat(picksMain).hasSize(3)
                .extracting("title")
                .containsExactly(
                        title3.getTitle(),
                        title2.getTitle(),
                        title1.getTitle()
                );
    }

    @Test
    @DisplayName("익명 회원이 커서 방식으로 익명 회원 전용 생성시간 내림차순으로 픽픽픽 메인을 조회한다.")
    void findPicksMainLATEST() {
        // given
        Title title1 = new Title("픽1타이틀");
        Title title2 = new Title("픽2타이틀");
        Title title3 = new Title("픽3타이틀");
        Count count = new Count(0);

        Pick pick1 = createPick(title1, count, count, count, count, thumbnailUrl, author, List.of());
        Pick pick2 = createPick(title2, count, count, count, count, thumbnailUrl, author, List.of());
        Pick pick3 = createPick(title3, count, count, count, count, thumbnailUrl, author, List.of());
        pickRepository.saveAll(List.of(pick1, pick2, pick3));

        PickOption pickOption1 = createPickOption(pick1, new Title("픽옵션1"), new PickOptionContents("픽콘텐츠1"),
                new Count(1), PickOptionType.firstPickOption);
        PickOption pickOption2 = createPickOption(pick1, new Title("픽옵션2"), new PickOptionContents("픽콘텐츠2"),
                new Count(2), PickOptionType.secondPickOption);
        PickOption pickOption3 = createPickOption(pick2, new Title("픽옵션3"), new PickOptionContents("픽콘텐츠3"),
                new Count(3), PickOptionType.firstPickOption);
        PickOption pickOption4 = createPickOption(pick2, new Title("픽옵션4"), new PickOptionContents("픽콘텐츠4"),
                new Count(4), PickOptionType.secondPickOption);
        PickOption pickOption5 = createPickOption(pick3, new Title("픽옵션5"), new PickOptionContents("픽콘텐츠5"),
                new Count(5), PickOptionType.firstPickOption);
        PickOption pickOption6 = createPickOption(pick3, new Title("픽옵션6"), new PickOptionContents("픽콘텐츠6"),
                new Count(6), PickOptionType.secondPickOption);
        pickOptionRepository.saveAll(
                List.of(pickOption1, pickOption2, pickOption3, pickOption4, pickOption5, pickOption6));

        Pageable pageable = PageRequest.of(0, 10);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(AuthenticationMemberUtils.ANONYMOUS_USER);

        String anonymousMemberId = "GA1.1.276672604.1715872960";

        // when
        Slice<PickMainResponse> picksMain = guestPickService.findPicksMain(pageable, Long.MAX_VALUE, PickSort.LATEST,
                anonymousMemberId, authentication);

        // then
        assertThat(picksMain).hasSize(3)
                .extracting("title")
                .containsExactly(
                        title3.getTitle(),
                        title2.getTitle(),
                        title1.getTitle()
                );
    }

    @Test
    @DisplayName("익명 회원가 커서 방식으로 익명 회원 전용 댓글수 내림차순으로 픽픽픽 메인을 조회한다.")
    void findPicksMainMOST_COMMENTED() {
        // given

        Title title1 = new Title("픽1타이틀");
        Title title2 = new Title("픽2타이틀");
        Title title3 = new Title("픽3타이틀");
        Count pick1commentTotalCount = new Count(1);
        Count pick2commentTotalCount = new Count(2);
        Count pick3commentTotalCount = new Count(3);
        Count count = new Count(0);

        Pick pick1 = createPick(title1, count, count, pick1commentTotalCount, count, thumbnailUrl, author, List.of());
        Pick pick2 = createPick(title2, count, count, pick2commentTotalCount, count, thumbnailUrl, author, List.of());
        Pick pick3 = createPick(title3, count, count, pick3commentTotalCount, count, thumbnailUrl, author, List.of());
        pickRepository.saveAll(List.of(pick1, pick2, pick3));

        PickOption pickOption1 = createPickOption(pick1, new Title("픽옵션1"), new PickOptionContents("픽콘텐츠1"),
                new Count(1), PickOptionType.firstPickOption);
        PickOption pickOption2 = createPickOption(pick1, new Title("픽옵션2"), new PickOptionContents("픽콘텐츠2"),
                new Count(2), PickOptionType.secondPickOption);
        PickOption pickOption3 = createPickOption(pick2, new Title("픽옵션3"), new PickOptionContents("픽콘텐츠3"),
                new Count(3), PickOptionType.firstPickOption);
        PickOption pickOption4 = createPickOption(pick2, new Title("픽옵션4"), new PickOptionContents("픽콘텐츠4"),
                new Count(4), PickOptionType.secondPickOption);
        PickOption pickOption5 = createPickOption(pick3, new Title("픽옵션5"), new PickOptionContents("픽콘텐츠5"),
                new Count(5), PickOptionType.firstPickOption);
        PickOption pickOption6 = createPickOption(pick3, new Title("픽옵션6"), new PickOptionContents("픽콘텐츠6"),
                new Count(6), PickOptionType.secondPickOption);
        pickOptionRepository.saveAll(
                List.of(pickOption1, pickOption2, pickOption3, pickOption4, pickOption5, pickOption6));

        Pageable pageable = PageRequest.of(0, 10);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(AuthenticationMemberUtils.ANONYMOUS_USER);

        String anonymousMemberId = "GA1.1.276672604.1715872960";

        // when
        Slice<PickMainResponse> picksMain = guestPickService.findPicksMain(pageable, Long.MAX_VALUE,
                PickSort.MOST_COMMENTED, anonymousMemberId, authentication);

        // then
        assertThat(picksMain).hasSize(3)
                .extracting("title")
                .containsExactly(
                        title3.getTitle(),
                        title2.getTitle(),
                        title1.getTitle()
                );
    }

    @Test
    @DisplayName("익명 회원이 커서 방식으로 익명 회원 전용 인기순으로 픽픽픽 메인을 조회한다."
            + "(현재 가중치 = 댓글수:" + PickPopularScorePolicy.COMMENT_WEIGHT + ", 투표수:" + PickPopularScorePolicy.VOTE_WEIGHT
            + ", 조회수:" + PickPopularScorePolicy.VIEW_WEIGHT + ")")
    void findPicksMainPOPULAR() {
        // given
        Title title1 = new Title("픽1타이틀");
        Title title2 = new Title("픽2타이틀");
        Title title3 = new Title("픽3타이틀");

        Count pick1commentTotalCount = new Count(2);
        Count pick1VoteTotalCount = new Count(2);
        Count pick1ViewTotalCount = new Count(1);

        Count pick2commentTotalCount = new Count(1);
        Count pick2VoteTotalCount = new Count(2);
        Count pick2ViewTotalCount = new Count(2);

        Count pick3commentTotalCount = new Count(1);
        Count pick3VoteTotalCount = new Count(1);
        Count pick3ViewTotalCount = new Count(2);

        Pick pick1 = createPick(title1, pick1VoteTotalCount, pick1ViewTotalCount, pick1commentTotalCount,
                thumbnailUrl, author, List.of());
        pick1.changePopularScore(pickPopularScorePolicy);

        Pick pick2 = createPick(title2, pick2VoteTotalCount, pick2ViewTotalCount, pick2commentTotalCount,
                thumbnailUrl, author, List.of());
        pick2.changePopularScore(pickPopularScorePolicy);

        Pick pick3 = createPick(title3, pick3VoteTotalCount, pick3ViewTotalCount, pick3commentTotalCount,
                thumbnailUrl, author, List.of());
        pick3.changePopularScore(pickPopularScorePolicy);

        pickRepository.saveAll(List.of(pick1, pick2, pick3));

        PickOption pickOption1 = createPickOption(pick1, new Title("픽옵션1"), new PickOptionContents("픽콘텐츠1"),
                new Count(1), PickOptionType.firstPickOption);
        PickOption pickOption2 = createPickOption(pick1, new Title("픽옵션2"), new PickOptionContents("픽콘텐츠2"),
                new Count(2), PickOptionType.secondPickOption);
        PickOption pickOption3 = createPickOption(pick2, new Title("픽옵션3"), new PickOptionContents("픽콘텐츠3"),
                new Count(3), PickOptionType.firstPickOption);
        PickOption pickOption4 = createPickOption(pick2, new Title("픽옵션4"), new PickOptionContents("픽콘텐츠4"),
                new Count(4), PickOptionType.secondPickOption);
        PickOption pickOption5 = createPickOption(pick3, new Title("픽옵션5"), new PickOptionContents("픽콘텐츠5"),
                new Count(5), PickOptionType.firstPickOption);
        PickOption pickOption6 = createPickOption(pick3, new Title("픽옵션6"), new PickOptionContents("픽콘텐츠6"),
                new Count(6), PickOptionType.secondPickOption);
        pickOptionRepository.saveAll(
                List.of(pickOption1, pickOption2, pickOption3, pickOption4, pickOption5, pickOption6));

        Pageable pageable = PageRequest.of(0, 10);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(AuthenticationMemberUtils.ANONYMOUS_USER);

        String anonymousMemberId = "GA1.1.276672604.1715872960";

        // when
        Slice<PickMainResponse> picksMain = guestPickService.findPicksMain(pageable, Long.MAX_VALUE, PickSort.POPULAR,
                anonymousMemberId, authentication);

        // then
        assertThat(picksMain).hasSize(3)
                .extracting("title")
                .containsExactly(
                        title1.getTitle(),
                        title2.getTitle(),
                        title3.getTitle()
                );
    }

    @Test
    @DisplayName("커서 방식으로 익명 회원 전용 픽픽픽 메인을 조회할 때 익명 회원가 아니면 예외가 발생한다.")
    void findPicksMainException() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        UserPrincipal userPrincipal = UserPrincipal.createByEmailAndRoleAndSocialType(email, role, socialType);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String anonymousMemberId = "GA1.1.276672604.1715872960";

        // when // then
        assertThatThrownBy(
                () -> guestPickService.findPicksMain(pageable, Long.MAX_VALUE, null, anonymousMemberId, authentication))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(AuthenticationMemberUtils.INVALID_METHODS_CALL_MESSAGE);
    }

    @Test
    @DisplayName("익명 회원이 이미지를 업로드하면 예외가 발생한다.")
    void uploadImagesAccessDeniedException() {
        // given
        MockMultipartFile mockMultipartFile = mock(MockMultipartFile.class);

        // when // then
        assertThatThrownBy(() -> guestPickService.uploadImages("testImage", List.of(mockMultipartFile)))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage(INVALID_ANONYMOUS_CAN_NOT_USE_THIS_FUNCTION_MESSAGE);
    }

    @Test
    @DisplayName("익명 회원이 픽픽픽을 작성하면 예외가 발생한다.")
    void registerPickAccessDeniedException() {
        // given
        RegisterPickRequest registerPickRequest = mock(RegisterPickRequest.class);
        Authentication authentication = mock(Authentication.class);

        // then
        assertThatThrownBy(() -> guestPickService.registerPick(registerPickRequest, authentication))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage(INVALID_ANONYMOUS_CAN_NOT_USE_THIS_FUNCTION_MESSAGE);
    }

    @Test
    @DisplayName("익명 회원이 픽픽픽 상세를 조회한다.")
    void findPickDetail() {
        // given
        // 익명 회원 생성
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(AuthenticationMemberUtils.ANONYMOUS_USER);

        String anonymousMemberId = "GA1.1.276672604.1715872960";
        AnonymousMember anonymousMember = AnonymousMember.builder()
                .anonymousMemberId(anonymousMemberId)
                .build();
        anonymousMemberRepository.save(anonymousMember);

        // 픽픽픽 작성 회원 생성
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        // 픽픽픽 생성
        Pick pick = createPick(new Title("픽픽픽 제목"), new Count(0), new Count(0), new Count(1), new Count(0), member,
                ContentStatus.APPROVAL);
        pickRepository.save(pick);

        // 픽픽픽 옵션 생성
        PickOption firstPickOption = createPickOption(pick, new Title("픽픽픽 옵션1"), new PickOptionContents("픽픽픽 옵션1 내용"),
                new Count(1), PickOptionType.firstPickOption);
        PickOption secondPickOption = createPickOption(pick, new Title("픽픽픽 옵션2"), new PickOptionContents("픽픽픽 옵션2 내용"),
                new Count(0), PickOptionType.secondPickOption);
        pickOptionRepository.saveAll(List.of(firstPickOption, secondPickOption));

        // 픽픽픽 옵션 이미지 생성
        PickOptionImage firstPickOptionImage = createPickOptionImage("이미지1", "http://iamge1.png", firstPickOption);
        PickOptionImage secondPickOptionImage = createPickOptionImage("이미지2", "http://iamge2.png", secondPickOption);
        pickOptionImageRepository.saveAll(List.of(firstPickOptionImage, secondPickOptionImage));

        // 픽픽픽 옵션 투표 여부
        PickVote pickVote = createPickVote(anonymousMember, firstPickOption, pick);
        pickVoteRepository.save(pickVote);

        em.flush();
        em.clear();

        // when
        PickDetailResponse pickDetail = guestPickService.findPickDetail(pick.getId(), anonymousMemberId,
                authentication);

        // then
        assertThat(pickDetail).isNotNull();
        assertAll(
                () -> assertThat(pickDetail.getUserId()).isEqualTo(
                        PickResponseUtils.sliceAndMaskEmail(member.getEmail().getEmail())),
                () -> assertThat(pickDetail.getNickname()).isEqualTo(member.getNickname().getNickname()),
                () -> assertThat(pickDetail.getPickTitle()).isEqualTo("픽픽픽 제목"),
                () -> assertThat(pickDetail.getIsAuthor()).isEqualTo(false)
        );

        Map<PickOptionType, PickDetailOptionResponse> pickOptions = pickDetail.getPickOptions();
        PickDetailOptionResponse findFirstPickOptionResponse = pickOptions.get(PickOptionType.firstPickOption);
        PickDetailOptionResponse findSecondPickOptionResponse = pickOptions.get(PickOptionType.secondPickOption);

        PickOption findFirstPickOption = pickOptionRepository.findById(findFirstPickOptionResponse.getId()).get();
        assertThat(findFirstPickOptionResponse).isNotNull();
        assertAll(
                () -> assertThat(findFirstPickOptionResponse.getId()).isEqualTo(findFirstPickOption.getId()),
                () -> assertThat(findFirstPickOptionResponse.getTitle()).isEqualTo("픽픽픽 옵션1"),
                () -> assertThat(findFirstPickOptionResponse.getIsPicked()).isEqualTo(true),
                () -> assertThat(findFirstPickOptionResponse.getPercent()).isEqualTo(100),
                () -> assertThat(findFirstPickOptionResponse.getContent()).isEqualTo("픽픽픽 옵션1 내용"),
                () -> assertThat(findFirstPickOptionResponse.getVoteTotalCount()).isEqualTo(1)
        );

        List<PickDetailOptionImageResponse> findFirstPickOptionPickOptionImagesResponse = findFirstPickOptionResponse.getPickDetailOptionImages();
        PickOptionImage findFirstPickOptionImage = findFirstPickOption.getPickOptionImages().get(0);
        assertThat(findFirstPickOptionPickOptionImagesResponse).hasSize(1)
                .extracting("id", "imageUrl")
                .containsExactly(
                        tuple(findFirstPickOptionImage.getId(), "http://iamge1.png")
                );

        PickOption findfirstPickOption = pickOptionRepository.findById(findSecondPickOptionResponse.getId()).get();
        assertThat(findSecondPickOptionResponse).isNotNull();
        assertAll(
                () -> assertThat(findSecondPickOptionResponse.getId()).isEqualTo(findfirstPickOption.getId()),
                () -> assertThat(findSecondPickOptionResponse.getTitle()).isEqualTo("픽픽픽 옵션2"),
                () -> assertThat(findSecondPickOptionResponse.getIsPicked()).isEqualTo(false),
                () -> assertThat(findSecondPickOptionResponse.getPercent()).isEqualTo(0),
                () -> assertThat(findSecondPickOptionResponse.getContent()).isEqualTo("픽픽픽 옵션2 내용"),
                () -> assertThat(findSecondPickOptionResponse.getVoteTotalCount()).isEqualTo(0)
        );

        List<PickDetailOptionImageResponse> findfirstPickOptionPickOptionImagesResponse = findSecondPickOptionResponse.getPickDetailOptionImages();
        PickOptionImage findsecondPickOptionImage = secondPickOption.getPickOptionImages().get(0);
        assertThat(findfirstPickOptionPickOptionImagesResponse).hasSize(1)
                .extracting("id", "imageUrl")
                .containsExactly(
                        tuple(findsecondPickOptionImage.getId(), "http://iamge2.png")
                );
    }

    @Test
    @DisplayName("회원이 익명 회원 전용 픽픽픽 상세를 조회하면 예외가 발생한다.")
    void findPickDetailException() {
        // given
        UserPrincipal userPrincipal = UserPrincipal.createByEmailAndRoleAndSocialType(email, role, socialType);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        Pick pick = createPick(new Title("픽픽픽 제목"), new Count(1), member, ContentStatus.APPROVAL);
        pickRepository.save(pick);

        String anonymousMemberId = "GA1.1.276672604.1715872960";

        // when // then
        assertThatThrownBy(() -> guestPickService.findPickDetail(pick.getId(), anonymousMemberId, authentication))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(AuthenticationMemberUtils.INVALID_METHODS_CALL_MESSAGE);
    }

    @Test
    @DisplayName("익명 회원이 픽픽픽 상세 조회할 때 픽픽픽이 없으면 예외가 발생한다.")
    void findPickDetailNotFoundException() {
        // given
        // 익명 회원 생성
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(AuthenticationMemberUtils.ANONYMOUS_USER);

        String anonymousMemberId = "GA1.1.276672604.1715872960";

        // when // then
        assertThatThrownBy(() -> guestPickService.findPickDetail(0L, anonymousMemberId, authentication))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(INVALID_NOT_FOUND_PICK_MESSAGE);
    }

    @Test
    @DisplayName("픽픽픽 옵션에 투표한 이력이 없는 익명 회원이 픽픽픽 옵션 중 하나에 투표한다.")
    void votePickOptionNewCreate() {
        // given
        // 익명 회원 생성
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(AuthenticationMemberUtils.ANONYMOUS_USER);

        // 픽픽픽 게시글을 작성할 회원 생성
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        // 픽픽픽 생성
        Pick pick = createPick(new Title("픽픽픽 제목"), new Count(0), new Count(0), new Count(0), new Count(0), member,
                ContentStatus.APPROVAL);
        pickRepository.save(pick);

        // 픽픽픽 옵션 생성
        PickOption firstPickOption = createPickOption(new Title("첫번째 픽옵션 제목"), new Count(0),
                PickOptionType.firstPickOption, pick);
        PickOption secondPickOption = createPickOption(new Title("두번째 픽옵션 제목"), new Count(0),
                PickOptionType.secondPickOption, pick);
        pickOptionRepository.saveAll(List.of(firstPickOption, secondPickOption));

        String anonymousMemberId = "GA1.1.276672604.1715872960";
        VotePickOptionDto dto = VotePickOptionDto.builder()
                .pickId(pick.getId())
                .pickOptionId(firstPickOption.getId())
                .anonymousMemberId(anonymousMemberId)
                .build();

        // when
        VotePickResponse votePickResponse = guestPickService.votePickOption(dto, authentication);

        // then
        assertAll(
                () -> assertThat(votePickResponse.getPickId()).isEqualTo(pick.getId()),
                () -> assertThat(votePickResponse.getVotePickOptions()).hasSize(2)
        );

        VotePickOptionResponse votePickOptionResponseIndex1 = votePickResponse.getVotePickOptions().get(0);
        assertAll(
                () -> assertThat(votePickOptionResponseIndex1.getPickOptionId()).isEqualTo(firstPickOption.getId()),
                () -> assertThat(votePickOptionResponseIndex1.getPickVoteId()).isNotNull(),
                () -> assertThat(votePickOptionResponseIndex1.getPercent()).isEqualTo(100),
                () -> assertThat(votePickOptionResponseIndex1.getVoteTotalCount()).isEqualTo(1),
                () -> assertThat(votePickOptionResponseIndex1.getIsPicked()).isEqualTo(true)
        );

        VotePickOptionResponse votePickOptionResponseIndex2 = votePickResponse.getVotePickOptions().get(1);
        assertAll(
                () -> assertThat(votePickOptionResponseIndex2.getPickOptionId()).isEqualTo(secondPickOption.getId()),
                () -> assertThat(votePickOptionResponseIndex2.getPickVoteId()).isNull(),
                () -> assertThat(votePickOptionResponseIndex2.getPercent()).isEqualTo(0),
                () -> assertThat(votePickOptionResponseIndex2.getVoteTotalCount()).isEqualTo(0),
                () -> assertThat(votePickOptionResponseIndex2.getIsPicked()).isEqualTo(false)
        );
    }

    @Test
    @DisplayName("픽픽픽 옵션에 투표한 이력이 있는 익명 회원이 다른 픽옵션에 투표 할 경우 기존 투표 이력은 삭제되고, 새로운 투표 이력이 생성된다.")
    void votePickOptionDeleteAndCreateNew() {
        // given
        // 회원 생성
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        // 익명 회원 생성
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(AuthenticationMemberUtils.ANONYMOUS_USER);

        // 픽픽픽 생성
        Pick pick = createPick(new Title("픽픽픽 제목"), new Count(0), new Count(0), new Count(0), new Count(0), member,
                ContentStatus.APPROVAL);
        pickRepository.save(pick);

        // 픽픽픽 옵션 생성
        PickOption firstPickOption = createPickOption(new Title("첫번째 픽옵션 제목"), new Count(0),
                PickOptionType.firstPickOption, pick);
        PickOption secondPickOption = createPickOption(new Title("두번째 픽옵션 제목"), new Count(0),
                PickOptionType.secondPickOption, pick);
        pickOptionRepository.saveAll(List.of(firstPickOption, secondPickOption));

        // 첫 번째 픽픽픽 옵션에 투표이력 생성
        PickVote pickVote = createPickVote(member, firstPickOption, pick);
        pickVoteRepository.save(pickVote);

        // 두 번째 픽픽픽 옵션에 투표
        String anonymousMemberId = "GA1.1.276672604.1715872960";
        VotePickOptionDto dto = VotePickOptionDto.builder()
                .pickId(pick.getId())
                .pickOptionId(secondPickOption.getId())
                .anonymousMemberId(anonymousMemberId)
                .build();

        // when
        VotePickResponse votePickResponse = guestPickService.votePickOption(dto, authentication);

        // then
        assertAll(
                () -> assertThat(votePickResponse.getPickId()).isEqualTo(pick.getId()),
                () -> assertThat(votePickResponse.getVotePickOptions()).hasSize(2)
        );

        VotePickOptionResponse votePickOptionResponseIndex1 = votePickResponse.getVotePickOptions().get(0);
        assertAll(
                () -> assertThat(votePickOptionResponseIndex1.getPickOptionId()).isEqualTo(firstPickOption.getId()),
                () -> assertThat(votePickOptionResponseIndex1.getPickVoteId()).isNull(),
                () -> assertThat(votePickOptionResponseIndex1.getPercent()).isEqualTo(0),
                () -> assertThat(votePickOptionResponseIndex1.getVoteTotalCount()).isEqualTo(0),
                () -> assertThat(votePickOptionResponseIndex1.getIsPicked()).isEqualTo(false)
        );

        VotePickOptionResponse votePickOptionResponseIndex2 = votePickResponse.getVotePickOptions().get(1);
        assertAll(
                () -> assertThat(votePickOptionResponseIndex2.getPickOptionId()).isEqualTo(secondPickOption.getId()),
                () -> assertThat(votePickOptionResponseIndex2.getPickVoteId()).isNotNull(),
                () -> assertThat(votePickOptionResponseIndex2.getPercent()).isEqualTo(100),
                () -> assertThat(votePickOptionResponseIndex2.getVoteTotalCount()).isEqualTo(1),
                () -> assertThat(votePickOptionResponseIndex2.getIsPicked()).isEqualTo(true)
        );
    }

    @Test
    @DisplayName("익명 회원이 픽픽픽 옵션을 투표할 때 이미 투표한 픽픽픽 옵션에 투표를 하면 예외가 발생한다.")
    void votePickOption_INVALID_CAN_NOT_VOTE_SAME_PICK_OPTION_MESSAGE() {
        // given
        // 회원 생성
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        // 익명 회원 생성
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(AuthenticationMemberUtils.ANONYMOUS_USER);

        String anonymousMemberId = "GA1.1.276672604.1715872960";
        AnonymousMember anonymousMember = AnonymousMember.builder()
                .anonymousMemberId(anonymousMemberId)
                .build();
        anonymousMemberRepository.save(anonymousMember);

        // 픽픽픽 생성
        Pick pick = createPick(new Title("픽픽픽 제목"), new Count(0), new Count(0), new Count(0), new Count(0), member,
                ContentStatus.APPROVAL);
        pickRepository.save(pick);

        // 픽픽픽 옵션 생성
        PickOption firstPickOption = createPickOption(new Title("첫번째 픽옵션 제목"), new Count(0),
                PickOptionType.firstPickOption, pick);
        PickOption secondPickOption = createPickOption(new Title("두번째 픽옵션 제목"), new Count(0),
                PickOptionType.secondPickOption, pick);
        pickOptionRepository.saveAll(List.of(firstPickOption, secondPickOption));

        // 픽픽픽 투표 생성
        PickVote pickVote = createPickVote(anonymousMember, firstPickOption, pick);
        pickVoteRepository.save(pickVote);

        VotePickOptionDto dto = VotePickOptionDto.builder()
                .pickId(pick.getId())
                .pickOptionId(firstPickOption.getId())
                .anonymousMemberId(anonymousMemberId)
                .build();

        // when // then
        assertThatThrownBy(() -> guestPickService.votePickOption(dto, authentication))
                .isInstanceOf(VotePickOptionException.class)
                .hasMessage(INVALID_CAN_NOT_VOTE_SAME_PICK_OPTION_MESSAGE);
    }

    @Test
    @DisplayName("익명 회원이 픽픽픽 옵션을 투표할 때 픽픽픽이 없으면 예외가 발생한다.")
    void votePickOption_INVALID_NOT_FOUND_PICK_MESSAGE() {
        // given
        // 회원 생성
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        // 익명 회원 생성
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(AuthenticationMemberUtils.ANONYMOUS_USER);

        String anonymousMemberId = "GA1.1.276672604.1715872960";
        VotePickOptionDto dto = VotePickOptionDto.builder()
                .pickId(0L)
                .pickOptionId(0L)
                .anonymousMemberId(anonymousMemberId)
                .build();

        // when // then
        assertThatThrownBy(() -> guestPickService.votePickOption(dto, authentication))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(INVALID_NOT_FOUND_PICK_MESSAGE);
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    @DisplayName("익명 회원이 픽픽픽 옵션을 투표할 때 anonymousMemberId이 빈값이거나 null이면 예외가 발생한다.")
    void votePickOption_INVALID_ANONYMOUS_MEMBER_ID_MESSAGE(String anonymousMemberId) {
        // given
        // 익명 회원 생성
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(AuthenticationMemberUtils.ANONYMOUS_USER);

        // 픽픽픽 작성자 생성
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        // 픽픽픽 생성
        Pick pick = createPick(new Title("픽픽픽 제목"), new Count(0), new Count(0), new Count(0), new Count(0), member,
                ContentStatus.APPROVAL);
        pickRepository.save(pick);

        // 픽픽픽 옵션 생성
        PickOption firstPickOption = createPickOption(new Title("첫번째 픽옵션 제목"), new Count(0),
                PickOptionType.firstPickOption, pick);
        PickOption secondPickOption = createPickOption(new Title("두번째 픽옵션 제목"), new Count(0),
                PickOptionType.secondPickOption, pick);
        pickOptionRepository.saveAll(List.of(firstPickOption, secondPickOption));

        VotePickOptionDto dto = VotePickOptionDto.builder()
                .pickId(pick.getId())
                .pickOptionId(firstPickOption.getId())
                .anonymousMemberId(anonymousMemberId)
                .build();

        // when // then
        assertThatThrownBy(() -> guestPickService.votePickOption(dto, authentication))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(INVALID_ANONYMOUS_MEMBER_ID_MESSAGE);
    }

    @Test
    @DisplayName("익명 회원이 픽픽픽 삭제를 할 경우 예외가 발생한다.")
    void deletePickAccessDeniedException() {
        // given
        // 익명 회원 생성
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(AuthenticationMemberUtils.ANONYMOUS_USER);

        // 픽픽픽 작성자 생성
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        // 픽픽픽 생성
        Pick pick = createPick(new Title("픽픽픽 제목"), new Count(0), new Count(0), new Count(0), new Count(0), member,
                ContentStatus.APPROVAL);
        pickRepository.save(pick);

        // when // then
        assertThatThrownBy(() -> guestPickService.deletePick(pick.getId(), authentication))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage(INVALID_ANONYMOUS_CAN_NOT_USE_THIS_FUNCTION_MESSAGE);
    }

    @Test
    @DisplayName("비회원은 픽픽픽 댓글을 작성할 때 예외가 발생한다.")
    void registerPickCommentAccessDeniedException() {
        // given
        // 익명 회원 생성
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(AuthenticationMemberUtils.ANONYMOUS_USER);

        RegisterPickCommentDto registerPickCommentDto = new RegisterPickCommentDto(1L, "안녕하세웅",
                1L, true);

        // when // then
        assertThatThrownBy(() -> guestPickService.registerPickComment(registerPickCommentDto, authentication))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage(INVALID_ANONYMOUS_CAN_NOT_USE_THIS_FUNCTION_MESSAGE);
    }

    private Pick createPick(Title title, Count viewTotalCount, Count commentTotalCount, Count voteTotalCount,
                            Count poplarScore, Member member, ContentStatus contentStatus) {
        return Pick.builder()
                .title(title)
                .viewTotalCount(viewTotalCount)
                .voteTotalCount(voteTotalCount)
                .commentTotalCount(commentTotalCount)
                .popularScore(poplarScore)
                .member(member)
                .contentStatus(contentStatus)
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

    private PickVote createPickVote(Member member, PickOption pickOption, Pick pick) {
        PickVote pickVote = PickVote.builder()
                .member(member)
                .pickOption(pickOption)
                .pick(pick)
                .build();

        pickVote.changePick(pick);

        return pickVote;
    }

    private PickVote createPickVote(AnonymousMember anonymousMember, PickOption pickOption, Pick pick) {
        PickVote pickVote = PickVote.builder()
                .anonymousMember(anonymousMember)
                .pickOption(pickOption)
                .pick(pick)
                .build();

        pickVote.changePick(pick);

        return pickVote;
    }

    private Pick createPick(Title title, Count pickVoteCount, Member member, ContentStatus contentStatus) {
        return Pick.builder()
                .title(title)
                .voteTotalCount(pickVoteCount)
                .member(member)
                .contentStatus(contentStatus)
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

    private Pick createPick(Title title, Count pickVoteTotalCount, Count pickViewTotalCount,
                            Count pickcommentTotalCount, Count pickPopularScore, String thumbnailUrl,
                            String author, List<PickVote> pickVotes
    ) {

        Pick pick = Pick.builder()
                .title(title)
                .voteTotalCount(pickVoteTotalCount)
                .viewTotalCount(pickViewTotalCount)
                .commentTotalCount(pickcommentTotalCount)
                .popularScore(pickPopularScore)
                .thumbnailUrl(thumbnailUrl)
                .author(author)
                .contentStatus(ContentStatus.APPROVAL)
                .build();

        pick.changePickVote(pickVotes);

        return pick;
    }

    private Pick createPick(Title title, Count pickVoteTotalCount, Count pickViewTotalCount,
                            Count pickcommentTotalCount, String thumbnailUrl, String author,
                            List<PickVote> pickVotes
    ) {

        Pick pick = Pick.builder()
                .title(title)
                .voteTotalCount(pickVoteTotalCount)
                .viewTotalCount(pickViewTotalCount)
                .commentTotalCount(pickcommentTotalCount)
                .thumbnailUrl(thumbnailUrl)
                .author(author)
                .contentStatus(ContentStatus.APPROVAL)
                .build();

        pick.changePickVote(pickVotes);

        return pick;
    }

    private PickOption createPickOption(Pick pick, Title title, PickOptionContents pickOptionContents,
                                        Count voteTotalCount, PickOptionType pickOptionType) {
        PickOption pickOption = PickOption.builder()
                .title(title)
                .contents(pickOptionContents)
                .voteTotalCount(voteTotalCount)
                .pickOptionType(pickOptionType)
                .build();

        pickOption.changePick(pick);

        return pickOption;
    }


}