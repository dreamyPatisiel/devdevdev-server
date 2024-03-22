package com.dreamypatisiel.devdevdev.domain.service.pick;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

import com.dreamypatisiel.devdevdev.aws.s3.AwsS3Uploader;
import com.dreamypatisiel.devdevdev.aws.s3.properties.AwsS3Properties;
import com.dreamypatisiel.devdevdev.aws.s3.properties.S3;
import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.Pick;
import com.dreamypatisiel.devdevdev.domain.entity.PickOption;
import com.dreamypatisiel.devdevdev.domain.entity.PickOptionImage;
import com.dreamypatisiel.devdevdev.domain.entity.PickVote;
import com.dreamypatisiel.devdevdev.domain.entity.Role;
import com.dreamypatisiel.devdevdev.domain.entity.SocialType;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Count;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.PickOptionContents;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Title;
import com.dreamypatisiel.devdevdev.domain.policy.PickPopularScorePolicy;
import com.dreamypatisiel.devdevdev.domain.repository.MemberRepository;
import com.dreamypatisiel.devdevdev.domain.repository.PickOptionImageRepository;
import com.dreamypatisiel.devdevdev.domain.repository.PickOptionRepository;
import com.dreamypatisiel.devdevdev.domain.repository.PickVoteRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickSort;
import com.dreamypatisiel.devdevdev.domain.service.response.PickUploadImageResponse;
import com.dreamypatisiel.devdevdev.domain.service.response.PicksResponse;
import com.dreamypatisiel.devdevdev.exception.MemberException;
import com.dreamypatisiel.devdevdev.exception.PickOptionImageNameException;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.SocialMemberDto;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.UserPrincipal;
import jakarta.persistence.EntityManager;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.transaction.annotation.Transactional;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class MemberPickServiceTest {

    @Autowired
    MemberPickService memberPickService;
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
    EntityManager em;
    @Autowired
    AwsS3Uploader awsS3Uploader;
    @Autowired
    AwsS3Properties awsS3Properties;

    String userId = "dreamy5patisiel";
    String name = "꿈빛파티시엘";
    String nickname = "행복한 꿈빛파티시엘";
    String email = "dreamy5patisiel@kakao.com";
    String password = "password";
    String socialType = SocialType.KAKAO.name();
    String role = Role.ROLE_USER.name();

    Title pickOptionTitle1 = new Title("pickOptionTitle1");
    PickOptionContents pickContents1 = new PickOptionContents("hello1");
    Count pickOptionVoteCount1 = new Count(10);
    Title pickOptionTitle2 = new Title("pickOptionTitle2");
    PickOptionContents pickContents2 = new PickOptionContents("hello2");
    Count pickOptionVoteCount2 = new Count(90);
    Title pickTitle = new Title("픽픽픽 제목");
    String thumbnailUrl = "섬네일 이미지 url";
    String author = "운영자";

    @Test
    @DisplayName("회원이 커서 방식으로 픽픽픽 메인을 조회한다.")
    void findPicksMain() {
        // given
        PickOption pickOption1 = createPickOption(pickOptionTitle1, pickContents1, pickOptionVoteCount1);
        PickOption pickOption2 = createPickOption(pickOptionTitle2, pickContents2, pickOptionVoteCount2);

        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        PickVote pickVote = PickVote.create(member, pickOption1);
        pickVoteRepository.save(pickVote);

        Count pickVoteTotalCount = new Count(pickOptionVoteCount1.getCount() + pickOptionVoteCount2.getCount());
        Count pickViewTotalCount = new Count(1);
        Count pickCommentTotalCount = new Count(0);
        Count pickPopularScore = new Count(0);
        String thumbnailUrl = "섬네일 이미지 url";
        String author = "운영자";
        Pick pick = createPick(pickTitle, pickVoteTotalCount, pickViewTotalCount, pickCommentTotalCount,
                pickPopularScore, thumbnailUrl, author, List.of(pickOption1, pickOption2), List.of(pickVote));
        pick.changePopularScore(pickPopularScorePolicy);

        pickRepository.save(pick);
        pickOptionRepository.saveAll(List.of(pickOption1, pickOption2));

        Pageable pageable = PageRequest.of(0, 10);

        // when
        Slice<PicksResponse> picks = memberPickService.findPicksMain(pageable, Long.MAX_VALUE, null, authentication);

        // then
        Pick findPick = pickRepository.findById(pick.getId()).get();
        assertThat(picks).hasSize(1)
                .extracting("id", "title",
                        "voteTotalCount", "commentTotalCount",
                        "viewTotalCount", "popularScore","isVoted")
                .containsExactly(
                        tuple(findPick.getId(), findPick.getTitle().getTitle(),
                                findPick.getVoteTotalCount().getCount(), findPick.getCommentTotalCount().getCount(),
                                findPick.getViewTotalCount().getCount(), findPick.getPopularScore().getCount(), true)
                );

        List<PickOption> pickOptions = findPick.getPickOptions();
        assertThat(picks.getContent().get(0).getPickOptions()).hasSize(2)
                .extracting("id", "title", "percent", "isPicked")
                .containsExactly(
                        tuple(pickOptions.get(0).getId(), pickOptions.get(0).getTitle().getTitle(), 10, true),
                        tuple(pickOptions.get(1).getId(), pickOptions.get(1).getTitle().getTitle(), 90, false)
                );
    }

    @Test
    @DisplayName("화원이 커서 방식으로 회원 전용 조회수 내림차순으로 픽픽픽 메인을 조회한다.")
    void findPicksMainMOST_VIEWED() {
        // given
        PickOption pickOption1 = PickOption.create(new Title("픽옵션1"), new PickOptionContents("픽콘텐츠1"));
        PickOption pickOption2 = PickOption.create(new Title("픽옵션2"), new PickOptionContents("픽콘텐츠2"));
        pickOption1.changePickVoteCount(new Count(1));
        pickOption2.changePickVoteCount(new Count(2));

        Title title1 = new Title("픽1타이틀");
        Title title2 = new Title("픽2타이틀");
        Title title3 = new Title("픽3타이틀");

        Count pick1ViewTotalCount = new Count(1);
        Count pick2ViewTotalCount = new Count(2);
        Count pick3ViewTotalCount = new Count(3);

        Count count = new Count(1);
        Pick pick1 = createPick(title1, count, pick1ViewTotalCount, count, count,
                thumbnailUrl, author, List.of(pickOption1, pickOption2), List.of());
        Pick pick2 = createPick(title2, count, pick2ViewTotalCount, count, count,
                thumbnailUrl, author, List.of(pickOption1, pickOption2), List.of());
        Pick pick3 = createPick(title3, count, pick3ViewTotalCount, count, count,
                thumbnailUrl, author, List.of(pickOption1, pickOption2), List.of());

        pickRepository.saveAll(List.of(pick1, pick2, pick3));
        pickOptionRepository.saveAll(List.of(pickOption1, pickOption2));

        Pageable pageable = PageRequest.of(0, 10);

        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // when
        Slice<PicksResponse> picksMain = memberPickService.findPicksMain(pageable, null, PickSort.MOST_VIEWED, authentication);

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
    @DisplayName("회원이 커서 방식으로 익명 사용자 전용 생성시간 내림차순으로 픽픽픽 메인을 조회한다.")
    void findPicksMainLATEST() {
        // given
        PickOption pickOption1 = PickOption.create(new Title("픽옵션1"), new PickOptionContents("픽콘텐츠1"));
        PickOption pickOption2 = PickOption.create(new Title("픽옵션2"), new PickOptionContents("픽콘텐츠2"));
        pickOption1.changePickVoteCount(new Count(1));
        pickOption2.changePickVoteCount(new Count(2));

        Title title1 = new Title("픽1타이틀");
        Title title2 = new Title("픽2타이틀");
        Title title3 = new Title("픽3타이틀");
        Count count = new Count(1);
        Pick pick1 = createPick(title1, count, count, count, count,
                thumbnailUrl, author, List.of(pickOption1, pickOption2), List.of());
        Pick pick2 = createPick(title2, count, count, count, count,
                thumbnailUrl, author, List.of(pickOption1, pickOption2), List.of());
        Pick pick3 = createPick(title3, count, count, count, count,
                thumbnailUrl, author, List.of(pickOption1, pickOption2), List.of());

        pickRepository.saveAll(List.of(pick1, pick2, pick3));
        pickOptionRepository.saveAll(List.of(pickOption1, pickOption2));

        Pageable pageable = PageRequest.of(0, 10);

        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // when
        Slice<PicksResponse> picksMain = memberPickService.findPicksMain(pageable, Long.MAX_VALUE, PickSort.LATEST,
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
    @DisplayName("회원이 커서 방식으로 익명 사용자 전용 댓글수 내림차순으로 픽픽픽 메인을 조회한다.")
    void findPicksMainMOST_COMMENTED() {
        // given
        PickOption pickOption1 = PickOption.create(new Title("픽옵션1"), new PickOptionContents("픽콘텐츠1"));
        PickOption pickOption2 = PickOption.create(new Title("픽옵션2"), new PickOptionContents("픽콘텐츠2"));
        pickOption1.changePickVoteCount(new Count(1));
        pickOption2.changePickVoteCount(new Count(2));

        Title title1 = new Title("픽1타이틀");
        Title title2 = new Title("픽2타이틀");
        Title title3 = new Title("픽3타이틀");
        Count pick1commentTotalCount = new Count(1);
        Count pick2commentTotalCount = new Count(2);
        Count pick3commentTotalCount = new Count(3);
        Count count = new Count(1);
        Pick pick1 = createPick(title1, count, count, pick1commentTotalCount, count,
                thumbnailUrl, author, List.of(pickOption1, pickOption2), List.of());
        Pick pick2 = createPick(title2, count, count, pick2commentTotalCount, count,
                thumbnailUrl, author, List.of(pickOption1, pickOption2), List.of());
        Pick pick3 = createPick(title3, count, count, pick3commentTotalCount, count,
                thumbnailUrl, author, List.of(pickOption1, pickOption2), List.of());

        pickRepository.saveAll(List.of(pick1, pick2, pick3));
        pickOptionRepository.saveAll(List.of(pickOption1, pickOption2));

        Pageable pageable = PageRequest.of(0, 10);

        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // when
        Slice<PicksResponse> picksMain = memberPickService.findPicksMain(pageable, Long.MAX_VALUE, PickSort.MOST_COMMENTED,
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
    @DisplayName("회원이 커서 방식으로 익명 사용자 전용 인기순으로 픽픽픽 메인을 조회한다."
            + "(현재 가중치 = 댓글수:"+ PickPopularScorePolicy.COMMENT_WEIGHT+", 투표수:"+PickPopularScorePolicy.VOTE_WEIGHT+", 조회수:"+PickPopularScorePolicy.VIEW_WEIGHT+")")
    void findPicksMainPOPULAR() {
        // given
        PickOption pickOption1 = PickOption.create(new Title("픽옵션1"), new PickOptionContents("픽콘텐츠1"));
        PickOption pickOption2 = PickOption.create(new Title("픽옵션2"), new PickOptionContents("픽콘텐츠2"));
        pickOption1.changePickVoteCount(new Count(1));
        pickOption2.changePickVoteCount(new Count(2));

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

        Pick pick1 = createPick(new Title("픽1타이틀"), pick1VoteTotalCount, pick1ViewTotalCount, pick1commentTotalCount,
                thumbnailUrl, author, List.of(pickOption1, pickOption2), List.of());
        pick1.changePopularScore(pickPopularScorePolicy);

        Pick pick2 = createPick(new Title("픽2타이틀"), pick2VoteTotalCount, pick2ViewTotalCount, pick2commentTotalCount,
                thumbnailUrl, author, List.of(pickOption1, pickOption2), List.of());
        pick2.changePopularScore(pickPopularScorePolicy);

        Pick pick3 = createPick(new Title("픽3타이틀"), pick3VoteTotalCount, pick3ViewTotalCount, pick3commentTotalCount,
                thumbnailUrl, author, List.of(pickOption1, pickOption2), List.of());
        pick3.changePopularScore(pickPopularScorePolicy);

        pickRepository.saveAll(List.of(pick1, pick2, pick3));
        pickOptionRepository.saveAll(List.of(pickOption1, pickOption2));

        Pageable pageable = PageRequest.of(0, 10);

        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // when
        Slice<PicksResponse> picksMain = memberPickService.findPicksMain(pageable, Long.MAX_VALUE, PickSort.POPULAR,
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
    @DisplayName("커서 방식으로 픽픽픽 메인을 조회할 때 회원이 없으면 예외가 발생한다.")
    void findPicksMainException() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        UserPrincipal userPrincipal = UserPrincipal.createByEmailAndRoleAndSocialType(email, role, socialType);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // when // then
        assertThatThrownBy(() -> memberPickService.findPicksMain(pageable, Long.MAX_VALUE, null, authentication))
                .isInstanceOf(MemberException.class)
                .hasMessage(MemberException.INVALID_MEMBER_NOT_FOUND_MESSAGE);
    }

    @ParameterizedTest
    @ValueSource(strings = {MemberPickService.FIRST_PICK_OPTION_IMAGE, MemberPickService.SECOND_PICK_OPTION_IMAGE})
    @DisplayName("픽픽픽 이미지를 업로드 하고 DB에 픽픽픽 이미지 정보를 저장한다.")
    void uploadImages(String name) throws IOException {

        // given
        MockMultipartFile mockMultipartFile = createMockMultipartFile("testImage", "tesImage.png");

        // when
        PickUploadImageResponse pickUploadImageResponse = memberPickService.uploadImages(name, List.of(mockMultipartFile));

        // then
        PickOptionImage pickOptionImage = pickOptionImageRepository.findById(
                pickUploadImageResponse.getPickOptionImages().get(0).getPickOptionImageId()).get();
        assertThat(pickUploadImageResponse.getPickOptionImages()).hasSize(1)
                .extracting("name", "pickOptionImageId", "imageUrl", "imageKey")
                .containsExactly(
                        tuple(pickOptionImage.getName(), pickOptionImage.getId(), pickOptionImage.getImageUrl(), pickOptionImage.getImageKey())
                );

        // s3 이미지 삭제
        S3 s3 = awsS3Properties.getS3();
        awsS3Uploader.deletePickOptionImage(s3.bucket(), pickOptionImage.getImageKey());
    }

    @ParameterizedTest
    @ValueSource(strings = {"thirdPickOptionImage", "firstPickOptionImages", "secondPickOptionImages"})
    @DisplayName("픽픽픽 이미지를 업로드 할때 픽픽픽 옵션에 알맞은 형식의 이름이 아니면 예외가 발생한다.")
    void uploadImagesNameException(String name) {
        // given
        MockMultipartFile mockMultipartFile = createMockMultipartFile("testImage", "tesImage.png");

        // when // then
        assertThatThrownBy(() -> memberPickService.uploadImages(name, List.of(mockMultipartFile)))
                .isInstanceOf(PickOptionImageNameException.class)
                .hasMessage(MemberPickService.INVALID_PICK_IMAGE_NAME_MESSAGE);
    }

    private MockMultipartFile createMockMultipartFile(String name, String originalFilename) {
        return new MockMultipartFile(
                name,
                originalFilename,
                MediaType.IMAGE_PNG_VALUE,
                name.getBytes()
        );
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

    private PickOption createPickOption(Title title, PickOptionContents pickOptionContents, Count pickOptionVoteCount) {
        return PickOption.builder()
                .title(title)
                .contents(pickOptionContents)
                .voteTotalCount(pickOptionVoteCount)
                .build();
    }
}