package com.dreamypatisiel.devdevdev.domain.service.pick;

import com.dreamypatisiel.devdevdev.domain.entity.*;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Count;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.PickOptionContents;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Title;
import com.dreamypatisiel.devdevdev.domain.entity.enums.ContentStatus;
import com.dreamypatisiel.devdevdev.domain.entity.enums.PickOptionType;
import com.dreamypatisiel.devdevdev.domain.entity.enums.Role;
import com.dreamypatisiel.devdevdev.domain.entity.enums.SocialType;
import com.dreamypatisiel.devdevdev.domain.repository.member.MemberRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickOptionImageRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickOptionRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickSort;
import com.dreamypatisiel.devdevdev.domain.service.member.AnonymousMemberService;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.SocialMemberDto;
import com.dreamypatisiel.devdevdev.global.utils.AuthenticationMemberUtils;
import com.dreamypatisiel.devdevdev.openai.embeddings.EmbeddingsService;
import com.dreamypatisiel.devdevdev.web.dto.response.pick.PickMainResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.pick.PickMainResponseV2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.dreamypatisiel.devdevdev.domain.entity.enums.PickOptionType.firstPickOption;
import static com.dreamypatisiel.devdevdev.domain.entity.enums.PickOptionType.secondPickOption;
import static com.dreamypatisiel.devdevdev.domain.exception.PickExceptionMessage.INVALID_NOT_APPROVAL_STATUS_PICK_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.PickExceptionMessage.INVALID_NOT_FOUND_PICK_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.service.pick.PickTestUtils.createPickOptionImage;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
class GuestPickServiceV2Test {

    @Autowired
    GuestPickServiceV2 guestPickServiceV2;
    @Autowired
    PickRepository pickRepository;
    @Autowired
    PickOptionRepository pickOptionRepository;
    @Autowired
    PickOptionImageRepository pickOptionImageRepository;
    @Autowired
    MemberRepository memberRepository;
    @MockBean
    AnonymousMemberService anonymousMemberService;
    @MockBean
    EmbeddingsService embeddingsService;

    String userId = "dreamy5patisiel";
    String name = "꿈빛파티시엘";
    String nickname = "행복한 꿈빛파티시엘";
    String email = "dreamy5patisiel@kakao.com";
    String password = "password";
    String socialType = SocialType.KAKAO.name();
    String role = Role.ROLE_USER.name();

    @Test
    @DisplayName("익명 회원이 픽픽픽 메인을 조회한다.")
    void findPicksMain() {
        // given
        String anonymousMemberId = "GA1.1.276672604.1715872960";
        AnonymousMember anonymousMember = AnonymousMember.builder()
                .anonymousMemberId(anonymousMemberId)
                .build();
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(AuthenticationMemberUtils.ANONYMOUS_USER);

        // given
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

        Pageable pageable = PageRequest.of(0, 10);

        // when
        Slice<PickMainResponseV2> picksMain = guestPickServiceV2.findPicksMain(pageable, null, null, anonymousMemberId, authentication);

        // then
        Pick findPick = pickRepository.findById(pick.getId()).get();
        assertThat(picksMain).hasSize(1)
                .extracting("id", "title", "voteTotalCount", "commentTotalCount", "isNew")
                .containsExactly(
                        tuple(findPick.getId(),
                                findPick.getTitle().getTitle(),
                                findPick.getVoteTotalCount().getCount(),
                                findPick.getCommentTotalCount().getCount(),
                                true)
                );

        List<PickOption> pickOptions = findPick.getPickOptions();
        assertThat(picksMain.getContent().get(0).getPickOptions()).hasSize(2)
                .extracting("id", "title", "percent", "isPicked", "content", "thumbnailImageUrl")
                .containsExactly(
                        tuple(pickOptions.get(0).getId(), pickOptions.get(0).getTitle().getTitle(), 100,
                                false, "픽픽픽 옵션1 내용", "http://iamge1.png"),
                        tuple(pickOptions.get(1).getId(), pickOptions.get(1).getTitle().getTitle(), 0,
                                false, "픽픽픽 옵션2 내용", "http://iamge2.png")
                );
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

    private Pick createPick(Member member, Title title, Count pickVoteTotalCount, Count pickViewTotalCount,
                            Count pickcommentTotalCount, Count pickPopularScore, String thumbnailUrl,
                            String author, List<PickVote> pickVotes
    ) {

        Pick pick = Pick.builder()
                .member(member)
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

    private Pick createPick(Member member, Title title, Count pickVoteTotalCount, Count pickViewTotalCount,
                            Count pickcommentTotalCount, String thumbnailUrl, String author,
                            List<PickVote> pickVotes
    ) {

        Pick pick = Pick.builder()
                .member(member)
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

