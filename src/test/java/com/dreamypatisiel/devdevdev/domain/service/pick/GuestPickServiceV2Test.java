package com.dreamypatisiel.devdevdev.domain.service.pick;

import com.dreamypatisiel.devdevdev.domain.entity.*;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Count;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.PickOptionContents;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Title;
import com.dreamypatisiel.devdevdev.domain.entity.enums.ContentStatus;
import com.dreamypatisiel.devdevdev.domain.entity.enums.PickOptionType;
import com.dreamypatisiel.devdevdev.domain.entity.enums.Role;
import com.dreamypatisiel.devdevdev.domain.entity.enums.SocialType;
import com.dreamypatisiel.devdevdev.domain.repository.member.AnonymousMemberRepository;
import com.dreamypatisiel.devdevdev.domain.repository.member.MemberRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickOptionImageRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickOptionRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickVoteRepository;
import com.dreamypatisiel.devdevdev.exception.NotFoundException;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.SocialMemberDto;
import com.dreamypatisiel.devdevdev.global.utils.AuthenticationMemberUtils;
import com.dreamypatisiel.devdevdev.openai.embeddings.EmbeddingsService;
import com.dreamypatisiel.devdevdev.web.dto.response.pick.PickDetailOptionImageResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.pick.PickDetailOptionResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.pick.PickDetailResponseV2;
import com.dreamypatisiel.devdevdev.web.dto.response.pick.PickMainResponseV2;
import com.dreamypatisiel.devdevdev.web.dto.util.CommonResponseUtil;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static com.dreamypatisiel.devdevdev.domain.exception.PickExceptionMessage.INVALID_NOT_FOUND_PICK_MESSAGE;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertAll;
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
    PickVoteRepository pickVoteRepository;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    AnonymousMemberRepository anonymousMemberRepository;
    @Autowired
    EntityManager em;
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
        Pick pick = createPick(new Title("픽픽픽 제목"), new Count(0), new Count(5), new Count(1), new Count(0), member,
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
        PickDetailResponseV2 pickDetail = guestPickServiceV2.findPickDetail(pick.getId(), anonymousMemberId,
                authentication);

        // then
        assertThat(pickDetail).isNotNull();
        assertAll(
                () -> assertThat(pickDetail.getUserId()).isEqualTo(
                        CommonResponseUtil.sliceAndMaskEmail(member.getEmail().getEmail())),
                () -> assertThat(pickDetail.getNickname()).isEqualTo(member.getNickname().getNickname()),
                () -> assertThat(pickDetail.getPickTitle()).isEqualTo("픽픽픽 제목"),
                () -> assertThat(pickDetail.getCommentTotalCount()).isEqualTo(5),
                () -> assertThat(pickDetail.getVoteTotalCount()).isEqualTo(1),
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
    @DisplayName("익명 회원이 픽픽픽 상세 조회할 때 픽픽픽이 없으면 예외가 발생한다. V2")
    void findPickDetailNotFoundPickDetail() {
        // given
        String anonymousMemberId = "GA1.1.276672604.1715872960";
        AnonymousMember anonymousMember = AnonymousMember.builder()
                .anonymousMemberId(anonymousMemberId)
                .build();
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(AuthenticationMemberUtils.ANONYMOUS_USER);

        // when // then
        assertThatThrownBy(() -> guestPickServiceV2.findPickDetail(0L, anonymousMemberId, authentication))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(INVALID_NOT_FOUND_PICK_MESSAGE);
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

