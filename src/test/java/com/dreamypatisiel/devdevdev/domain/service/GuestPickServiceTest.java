package com.dreamypatisiel.devdevdev.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.Pick;
import com.dreamypatisiel.devdevdev.domain.entity.PickOption;
import com.dreamypatisiel.devdevdev.domain.entity.PickVote;
import com.dreamypatisiel.devdevdev.domain.entity.Role;
import com.dreamypatisiel.devdevdev.domain.entity.SocialType;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Count;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.PickContents;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Title;
import com.dreamypatisiel.devdevdev.domain.repository.MemberRepository;
import com.dreamypatisiel.devdevdev.domain.repository.PickOptionRepository;
import com.dreamypatisiel.devdevdev.domain.repository.PickRepository;
import com.dreamypatisiel.devdevdev.domain.repository.PickVoteRepository;
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
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
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
    EntityManager em;

    String userId = "dreamy5patisiel";
    String name = "꿈빛파티시엘";
    String nickname = "행복한 꿈빛파티시엘";
    String email = "dreamy5patisiel@kakao.com";
    String password = "password";
    String socialType = SocialType.KAKAO.name();
    String role = Role.ROLE_USER.name();

    Title pickOptionTitle1 = new Title("pickOptionTitle1");
    PickContents pickContents1 = new PickContents("hello1");
    Count pickOptionVoteCount1 = new Count(10);
    Title pickOptionTitle2 = new Title("pickOptionTitle2");
    PickContents pickContents2 = new PickContents("hello2");
    Count pickOptionVoteCount2 = new Count(90);
    Title pickTitle = new Title("픽픽픽 제목");

    @Test
    @DisplayName("익명 사용자가 커서 방식으로 익명 사용자 전용 픽픽픽 메인을 조회한다.")
    void findPicks() {
        // given
        PickOption pickOption1 = PickOption.create(pickOptionTitle1, pickContents1, pickOptionVoteCount1);
        PickOption pickOption2 = PickOption.create(pickOptionTitle2, pickContents2, pickOptionVoteCount2);

        Count pickVoteTotalCount = new Count(pickOptionVoteCount1.getCount() + pickOptionVoteCount2.getCount());
        Count pickViewTotalCount = new Count(1);
        Count pickCommentTotalCount = new Count(0);
        String thumbnailUrl = "섬네일 이미지 url";
        String author = "운영자";
        Pick pick = Pick.create(pickTitle, pickVoteTotalCount, pickViewTotalCount, pickCommentTotalCount,
                thumbnailUrl, author, List.of(pickOption1, pickOption2), List.of());
        pickRepository.save(pick);
        pickOptionRepository.saveAll(List.of(pickOption1, pickOption2));

        Pageable pageable = PageRequest.of(0, 10);

        Long cursor = Long.MAX_VALUE;

        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn(AuthenticationMemberUtils.ANONYMOUS_USER);

        em.flush();
        em.clear();

        System.out.println("====================================");

        // when
        Slice<PicksResponse> picks = guestPickService.findPicks(pageable, cursor, userDetails);

        // then
        Pick findPick = pickRepository.findById(pick.getId()).get();
        assertThat(picks).hasSize(1)
                .extracting("id", "title", "voteTotalCount",
                        "commentTotalCount", "nextCursor")
                .containsExactly(
                        tuple(findPick.getId(), findPick.getTitle().getTitle(), findPick.getVoteTotalCount().getCount(),
                                findPick.getCommentTotalCount().getCount(), false)
                );

        List<PickOption> pickOptions = findPick.getPickOptions();
        assertThat(picks.getContent().get(0).getPickOptionsResponse()).hasSize(2)
                .extracting("id", "title", "percent")
                .containsExactly(
                        tuple(pickOptions.get(0).getId(), pickOptions.get(0).getTitle().getTitle(), 10),
                        tuple(pickOptions.get(1).getId(), pickOptions.get(1).getTitle().getTitle(), 90)
                );
    }

    @Test
    @DisplayName("커서 방식으로 익명 사용자 전용 픽픽픽 메인을 조회할 때 익명 사용자가 아니면 예외가 발생한다.")
    void findPicksException() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        Long cursor = Long.MAX_VALUE;

        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname,
                password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);

        // when // then
        assertThatThrownBy(() -> guestPickService.findPicks(pageable, cursor, userPrincipal))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(GuestPickService.INVALID_FIND_PICKS_METHODS_CALL_MESSAGE);
    }

    private SocialMemberDto createSocialDto(String userId, String name, String nickName, String password, String email, String socialType, String role) {
        return SocialMemberDto.builder()
                .userId(userId)
                .name(name)
                .nickName(nickName)
                .password(password)
                .email(email)
                .socialType(SocialType.valueOf(socialType))
                .role(Role.valueOf(role))
                .build();
    }
}