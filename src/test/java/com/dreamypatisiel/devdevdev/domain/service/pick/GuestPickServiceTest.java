package com.dreamypatisiel.devdevdev.domain.service.pick;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.dreamypatisiel.devdevdev.domain.entity.Pick;
import com.dreamypatisiel.devdevdev.domain.entity.PickOption;
import com.dreamypatisiel.devdevdev.domain.entity.PickVote;
import com.dreamypatisiel.devdevdev.domain.entity.Role;
import com.dreamypatisiel.devdevdev.domain.entity.SocialType;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Count;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.PickOptionContents;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Title;
import com.dreamypatisiel.devdevdev.domain.policy.PickPopularScorePolicy;
import com.dreamypatisiel.devdevdev.domain.repository.MemberRepository;
import com.dreamypatisiel.devdevdev.domain.repository.PickOptionRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickRepository;
import com.dreamypatisiel.devdevdev.domain.repository.PickVoteRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickSort;
import com.dreamypatisiel.devdevdev.domain.service.response.PicksResponse;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.SocialMemberDto;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.UserPrincipal;
import com.dreamypatisiel.devdevdev.global.utils.AuthenticationMemberUtils;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
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
    @DisplayName("익명 사용자가 커서 방식으로 익명 사용자 전용 픽픽픽 메인을 조회한다.")
    void findPicksMain() {
        // given
        PickOption pickOption1 = createPickOption(pickOptionTitle1, pickOptionContents1, pickOptionVoteCount1);
        PickOption pickOption2 = createPickOption(pickOptionTitle2, pickOptionContents2, pickOptionVoteCount2);

        Count pickVoteTotalCount = new Count(pickOptionVoteCount1.getCount() + pickOptionVoteCount2.getCount());
        Count pickViewTotalCount = new Count(1);
        Count pickCommentTotalCount = new Count(0);
        Count pickPopularScore = new Count(0);
        Pick pick = createPick(pickTitle, pickVoteTotalCount, pickViewTotalCount, pickCommentTotalCount, pickPopularScore,
                thumbnailUrl, author, List.of(pickOption1, pickOption2), List.of());
        pickRepository.save(pick);
        pickOptionRepository.saveAll(List.of(pickOption1, pickOption2));

        Pageable pageable = PageRequest.of(0, 10);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(AuthenticationMemberUtils.ANONYMOUS_USER);

        // when
        Slice<PicksResponse> picksMain = guestPickService.findPicksMain(pageable, Long.MAX_VALUE, null, authentication);

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
    @DisplayName("익명 사용자가 커서 방식으로 익명 사용자 전용 조회수 내림차순으로 픽픽픽 메인을 조회한다.")
    void findPicksMainMOST_VIEWED() {
        // given
        PickOption pickOption1 = createPickOption(new Title("픽옵션1"), new PickOptionContents("픽콘텐츠1"), new Count(1));
        PickOption pickOption2 = createPickOption(new Title("픽옵션2"), new PickOptionContents("픽콘텐츠2"), new Count(2));

        Title title1 = new Title("픽1타이틀");
        Title title2 = new Title("픽2타이틀");
        Title title3 = new Title("픽3타이틀");

        Count pick1ViewTotalCount = new Count(1);
        Count pick2ViewTotalCount = new Count(2);
        Count pick3ViewTotalCount = new Count(3);

        Count count = new Count(0);
        Pick pick1 = createPick(title1, count, pick1ViewTotalCount, count, count,
                thumbnailUrl, author, List.of(pickOption1, pickOption2), List.of());
        Pick pick2 = createPick(title2, count, pick2ViewTotalCount, count, count,
                thumbnailUrl, author, List.of(pickOption1, pickOption2), List.of());
        Pick pick3 = createPick(title3, count, pick3ViewTotalCount, count, count,
                thumbnailUrl, author, List.of(pickOption1, pickOption2), List.of());

        pickRepository.saveAll(List.of(pick1, pick2, pick3));
        pickOptionRepository.saveAll(List.of(pickOption1, pickOption2));

        Pageable pageable = PageRequest.of(0, 10);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(AuthenticationMemberUtils.ANONYMOUS_USER);

        // when
        Slice<PicksResponse> picksMain = guestPickService.findPicksMain(pageable, Long.MAX_VALUE, PickSort.MOST_VIEWED,
                authentication);

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
    @DisplayName("익명 사용자가 커서 방식으로 익명 사용자 전용 생성시간 내림차순으로 픽픽픽 메인을 조회한다.")
    void findPicksMainLATEST() {
        // given
        PickOption pickOption1 = createPickOption(new Title("픽옵션1"), new PickOptionContents("픽콘텐츠1"), new Count(1));
        PickOption pickOption2 = createPickOption(new Title("픽옵션2"), new PickOptionContents("픽콘텐츠2"), new Count(2));

        Title title1 = new Title("픽1타이틀");
        Title title2 = new Title("픽2타이틀");
        Title title3 = new Title("픽3타이틀");
        Count count = new Count(0);
        Pick pick1 = createPick(title1, count, count, count, count,
                thumbnailUrl, author, List.of(pickOption1, pickOption2), List.of());
        Pick pick2 = createPick(title2, count, count, count, count,
                thumbnailUrl, author, List.of(pickOption1, pickOption2), List.of());
        Pick pick3 = createPick(title3, count, count, count, count,
                thumbnailUrl, author, List.of(pickOption1, pickOption2), List.of());

        pickRepository.saveAll(List.of(pick1, pick2, pick3));
        pickOptionRepository.saveAll(List.of(pickOption1, pickOption2));

        Pageable pageable = PageRequest.of(0, 10);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(AuthenticationMemberUtils.ANONYMOUS_USER);

        // when
        Slice<PicksResponse> picksMain = guestPickService.findPicksMain(pageable, Long.MAX_VALUE, PickSort.LATEST,
                authentication);

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
    @DisplayName("익명 사용자가 커서 방식으로 익명 사용자 전용 댓글수 내림차순으로 픽픽픽 메인을 조회한다.")
    void findPicksMainMOST_COMMENTED() {
        // given
        PickOption pickOption1 = createPickOption(new Title("픽옵션1"), new PickOptionContents("픽콘텐츠1"), new Count(1));
        PickOption pickOption2 = createPickOption(new Title("픽옵션2"), new PickOptionContents("픽콘텐츠2"), new Count(2));

        Title title1 = new Title("픽1타이틀");
        Title title2 = new Title("픽2타이틀");
        Title title3 = new Title("픽3타이틀");
        Count pick1commentTotalCount = new Count(1);
        Count pick2commentTotalCount = new Count(2);
        Count pick3commentTotalCount = new Count(3);
        Count count = new Count(0);
        Pick pick1 = createPick(title1, count, count, pick1commentTotalCount, count,
                thumbnailUrl, author, List.of(pickOption1, pickOption2), List.of());
        Pick pick2 = createPick(title2, count, count, pick2commentTotalCount, count,
                thumbnailUrl, author, List.of(pickOption1, pickOption2), List.of());
        Pick pick3 = createPick(title3, count, count, pick3commentTotalCount, count,
                thumbnailUrl, author, List.of(pickOption1, pickOption2), List.of());

        pickRepository.saveAll(List.of(pick1, pick2, pick3));
        pickOptionRepository.saveAll(List.of(pickOption1, pickOption2));

        Pageable pageable = PageRequest.of(0, 10);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(AuthenticationMemberUtils.ANONYMOUS_USER);

        // when
        Slice<PicksResponse> picksMain = guestPickService.findPicksMain(pageable, Long.MAX_VALUE, PickSort.MOST_COMMENTED,
                authentication);

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
    @DisplayName("익명 사용자가 커서 방식으로 익명 사용자 전용 인기순으로 픽픽픽 메인을 조회한다."
            + "(현재 가중치 = 댓글수:"+ PickPopularScorePolicy.COMMENT_WEIGHT+", 투표수:"+PickPopularScorePolicy.VOTE_WEIGHT+", 조회수:"+PickPopularScorePolicy.VIEW_WEIGHT+")")
    void findPicksMainPOPULAR() {
        // given
        PickOption pickOption1 = createPickOption(new Title("픽옵션1"), new PickOptionContents("픽콘텐츠1"), new Count(1));
        PickOption pickOption2 = createPickOption(new Title("픽옵션2"), new PickOptionContents("픽콘텐츠2"), new Count(2));

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
                thumbnailUrl, author, List.of(pickOption1, pickOption2), List.of());
        pick1.changePopularScore(pickPopularScorePolicy);

        Pick pick2 = createPick(title2, pick2VoteTotalCount, pick2ViewTotalCount, pick2commentTotalCount,
                thumbnailUrl, author, List.of(pickOption1, pickOption2), List.of());
        pick2.changePopularScore(pickPopularScorePolicy);

        Pick pick3 = createPick(title3, pick3VoteTotalCount, pick3ViewTotalCount, pick3commentTotalCount,
                thumbnailUrl, author, List.of(pickOption1, pickOption2), List.of());
        pick3.changePopularScore(pickPopularScorePolicy);

        pickRepository.saveAll(List.of(pick1, pick2, pick3));
        pickOptionRepository.saveAll(List.of(pickOption1, pickOption2));

        Pageable pageable = PageRequest.of(0, 10);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(AuthenticationMemberUtils.ANONYMOUS_USER);

        // when
        Slice<PicksResponse> picksMain = guestPickService.findPicksMain(pageable, Long.MAX_VALUE, PickSort.POPULAR,
                authentication);

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
    @DisplayName("커서 방식으로 익명 사용자 전용 픽픽픽 메인을 조회할 때 익명 사용자가 아니면 예외가 발생한다.")
    void findPicksMainException() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        UserPrincipal userPrincipal = UserPrincipal.createByEmailAndRoleAndSocialType(email, role, socialType);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // when // then
        assertThatThrownBy(() -> guestPickService.findPicksMain(pageable, Long.MAX_VALUE, null, authentication))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(AuthenticationMemberUtils.INVALID_METHODS_CALL_MESSAGE);
    }

    private SocialMemberDto createSocialDto(String userId, String name, String nickName, String password, String email, String socialType, String role) {
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
                            Count pickcommentTotalCount, Count pickPopularScore, String thumbnailUrl,
                            String author, List<PickOption> pickOptions, List<PickVote> pickVotes
    ) {

        Pick pick = Pick.builder()
                .title(title)
                .voteTotalCount(pickVoteTotalCount)
                .viewTotalCount(pickViewTotalCount)
                .commentTotalCount(pickcommentTotalCount)
                .popularScore(pickPopularScore)
                .thumbnailUrl(thumbnailUrl)
                .author(author)
                .build();

        pick.changePickOptions(pickOptions);
        pick.changePickVote(pickVotes);

        return pick;
    }

    private Pick createPick(Title title, Count pickVoteTotalCount, Count pickViewTotalCount,
                            Count pickcommentTotalCount, String thumbnailUrl, String author,
                            List<PickOption> pickOptions, List<PickVote> pickVotes
    ) {

        Pick pick = Pick.builder()
                .title(title)
                .voteTotalCount(pickVoteTotalCount)
                .viewTotalCount(pickViewTotalCount)
                .commentTotalCount(pickcommentTotalCount)
                .thumbnailUrl(thumbnailUrl)
                .author(author)
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
}