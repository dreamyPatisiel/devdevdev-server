package com.dreamypatisiel.devdevdev.domain.service.pick;

import static com.dreamypatisiel.devdevdev.domain.entity.enums.PickOptionType.firstPickOption;
import static com.dreamypatisiel.devdevdev.domain.entity.enums.PickOptionType.secondPickOption;
import static com.dreamypatisiel.devdevdev.domain.exception.PickExceptionMessage.INVALID_CAN_NOT_VOTE_SAME_PICK_OPTION_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.PickExceptionMessage.INVALID_MODIFY_MEMBER_PICK_ONLY_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.PickExceptionMessage.INVALID_NOT_FOUND_CAN_MODIFY_PICK_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.PickExceptionMessage.INVALID_NOT_FOUND_PICK_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.PickExceptionMessage.INVALID_NOT_FOUND_PICK_OPTION_IMAGE_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.PickExceptionMessage.INVALID_PICK_IMAGE_NAME_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.service.pick.MemberPickService.FIRST_PICK_OPTION_IMAGE;
import static com.dreamypatisiel.devdevdev.domain.service.pick.MemberPickService.SECOND_PICK_OPTION_IMAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;

import com.amazonaws.services.s3.AmazonS3;
import com.dreamypatisiel.devdevdev.aws.s3.AwsS3Uploader;
import com.dreamypatisiel.devdevdev.aws.s3.properties.AwsS3Properties;
import com.dreamypatisiel.devdevdev.aws.s3.properties.S3;
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
import com.dreamypatisiel.devdevdev.domain.exception.PickExceptionMessage;
import com.dreamypatisiel.devdevdev.domain.policy.PickPopularScorePolicy;
import com.dreamypatisiel.devdevdev.domain.repository.member.MemberRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickOptionImageRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickOptionRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickSort;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickVoteRepository;
import com.dreamypatisiel.devdevdev.domain.service.response.PickDetailOptionImage;
import com.dreamypatisiel.devdevdev.domain.service.response.PickDetailOptionResponse;
import com.dreamypatisiel.devdevdev.domain.service.response.PickDetailResponse;
import com.dreamypatisiel.devdevdev.domain.service.response.PickMainResponse;
import com.dreamypatisiel.devdevdev.domain.service.response.PickModifyResponse;
import com.dreamypatisiel.devdevdev.domain.service.response.PickRegisterResponse;
import com.dreamypatisiel.devdevdev.domain.service.response.PickUploadImageResponse;
import com.dreamypatisiel.devdevdev.domain.service.response.VotePickOptionResponse;
import com.dreamypatisiel.devdevdev.domain.service.response.VotePickResponse;
import com.dreamypatisiel.devdevdev.exception.MemberException;
import com.dreamypatisiel.devdevdev.exception.NotFoundException;
import com.dreamypatisiel.devdevdev.exception.PickOptionImageNameException;
import com.dreamypatisiel.devdevdev.exception.VotePickOptionException;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.SocialMemberDto;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.UserPrincipal;
import com.dreamypatisiel.devdevdev.web.controller.request.ModifyPickOptionRequest;
import com.dreamypatisiel.devdevdev.web.controller.request.ModifyPickRequest;
import com.dreamypatisiel.devdevdev.web.controller.request.RegisterPickOptionRequest;
import com.dreamypatisiel.devdevdev.web.controller.request.RegisterPickRequest;
import com.dreamypatisiel.devdevdev.web.controller.request.VotePickOptionRequest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.transaction.annotation.Transactional;

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
    @PersistenceContext
    EntityManager em;
    @Autowired
    AwsS3Uploader awsS3Uploader;
    @Autowired
    AwsS3Properties awsS3Properties;
    @Autowired
    AmazonS3 amazonS3Client;

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
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Count pickVoteTotalCount = new Count(pickOptionVoteCount1.getCount() + pickOptionVoteCount2.getCount());
        Count pickViewTotalCount = new Count(1);
        Count pickCommentTotalCount = new Count(0);
        Count pickPopularScore = new Count(0);
        String thumbnailUrl = "섬네일 이미지 url";
        String author = "운영자";
        Pick pick = createPick(pickTitle, pickVoteTotalCount, pickViewTotalCount, pickCommentTotalCount,
                pickPopularScore, thumbnailUrl, author, ContentStatus.APPROVAL);
        pick.changePopularScore(pickPopularScorePolicy);
        pickRepository.save(pick);

        PickOption pickOption1 = createPickOption(pick, pickOptionTitle1, pickContents1, pickOptionVoteCount1);
        PickOption pickOption2 = createPickOption(pick, pickOptionTitle2, pickContents2, pickOptionVoteCount2);
        pickOptionRepository.saveAll(List.of(pickOption1, pickOption2));

        PickVote pickVote = createPickVote(member, pickOption1, pick);
        pickVoteRepository.save(pickVote);

        Pageable pageable = PageRequest.of(0, 10);

        // when
        Slice<PickMainResponse> picks = memberPickService.findPicksMain(pageable, Long.MAX_VALUE, null, authentication);

        // then
        Pick findPick = pickRepository.findById(pick.getId()).get();
        assertThat(picks).hasSize(1)
                .extracting("id", "title",
                        "voteTotalCount", "commentTotalCount",
                        "viewTotalCount", "popularScore", "isVoted")
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
    @DisplayName("회원이 커서 방식으로 회원 전용 조회수 내림차순으로 픽픽픽 메인을 조회한다.")
    void findPicksMainMOST_VIEWED() {
        // given
        PickOption pickOption1 = createPickOption(new Title("픽옵션1"), new PickOptionContents("픽콘텐츠1"));
        PickOption pickOption2 = createPickOption(new Title("픽옵션2"), new PickOptionContents("픽콘텐츠2"));
        pickOption1.changePickVoteCount(new Count(1));
        pickOption2.changePickVoteCount(new Count(2));

        Title title1 = new Title("픽1타이틀");
        Title title2 = new Title("픽2타이틀");
        Title title3 = new Title("픽3타이틀");
        Title title4 = new Title("픽4타이틀");

        Count pick1ViewTotalCount = new Count(1);
        Count pick2ViewTotalCount = new Count(2);
        Count pick3ViewTotalCount = new Count(3);
        Count pick4ViewTotalCount = new Count(4);

        Count count = new Count(1);
        Pick pick1 = createPick(title1, count, pick1ViewTotalCount, count, count, thumbnailUrl, author,
                ContentStatus.APPROVAL);
        Pick pick2 = createPick(title2, count, pick2ViewTotalCount, count, count, thumbnailUrl, author,
                ContentStatus.APPROVAL);
        Pick pick3 = createPick(title3, count, pick3ViewTotalCount, count, count, thumbnailUrl, author,
                ContentStatus.REJECT);
        Pick pick4 = createPick(title4, count, pick4ViewTotalCount, count, count, thumbnailUrl, author,
                ContentStatus.READY);

        pickRepository.saveAll(List.of(pick1, pick2, pick3, pick4));
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
        Slice<PickMainResponse> picksMain = memberPickService.findPicksMain(pageable, null, PickSort.MOST_VIEWED,
                authentication);

        // then
        assertThat(picksMain).hasSize(2)
                .extracting("title")
                .containsExactly(
                        title2.getTitle(),
                        title1.getTitle()
                );
    }

    @Test
    @DisplayName("회원이 커서 방식으로 익명 사용자 전용 생성시간 내림차순으로 픽픽픽 메인을 조회한다.")
    void findPicksMainLATEST() {
        // given
        PickOption pickOption1 = createPickOption(new Title("픽옵션1"), new PickOptionContents("픽콘텐츠1"));
        PickOption pickOption2 = createPickOption(new Title("픽옵션2"), new PickOptionContents("픽콘텐츠2"));
        pickOption1.changePickVoteCount(new Count(1));
        pickOption2.changePickVoteCount(new Count(2));

        Title title1 = new Title("픽1타이틀");
        Title title2 = new Title("픽2타이틀");
        Title title3 = new Title("픽3타이틀");
        Title title4 = new Title("픽4타이틀");
        Count count = new Count(1);
        Pick pick1 = createPick(title1, count, count, count, count, thumbnailUrl, author, ContentStatus.APPROVAL);
        Pick pick2 = createPick(title2, count, count, count, count, thumbnailUrl, author, ContentStatus.APPROVAL);
        Pick pick3 = createPick(title3, count, count, count, count, thumbnailUrl, author, ContentStatus.APPROVAL);
        Pick pick4 = createPick(title4, count, count, count, count, thumbnailUrl, author, ContentStatus.READY);
        Pick pick5 = createPick(title4, count, count, count, count, thumbnailUrl, author, ContentStatus.REJECT);

        pickRepository.saveAll(List.of(pick1, pick2, pick3, pick4, pick5));
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
        Slice<PickMainResponse> picksMain = memberPickService.findPicksMain(pageable, Long.MAX_VALUE, PickSort.LATEST,
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
        PickOption pickOption1 = createPickOption(new Title("픽옵션1"), new PickOptionContents("픽콘텐츠1"));
        PickOption pickOption2 = createPickOption(new Title("픽옵션2"), new PickOptionContents("픽콘텐츠2"));
        pickOption1.changePickVoteCount(new Count(1));
        pickOption2.changePickVoteCount(new Count(2));

        Title title1 = new Title("픽1타이틀");
        Title title2 = new Title("픽2타이틀");
        Title title3 = new Title("픽3타이틀");
        Count pick1commentTotalCount = new Count(1);
        Count pick2commentTotalCount = new Count(2);
        Count pick3commentTotalCount = new Count(3);
        Count pick4commentTotalCount = new Count(4);
        Count pick5commentTotalCount = new Count(5);
        Count count = new Count(1);
        Pick pick1 = createPick(title1, count, count, pick1commentTotalCount, count, thumbnailUrl, author,
                ContentStatus.APPROVAL);
        Pick pick2 = createPick(title2, count, count, pick2commentTotalCount, count, thumbnailUrl, author,
                ContentStatus.APPROVAL);
        Pick pick3 = createPick(title3, count, count, pick3commentTotalCount, count, thumbnailUrl, author,
                ContentStatus.APPROVAL);
        Pick pick4 = createPick(title3, count, count, pick4commentTotalCount, count, thumbnailUrl, author,
                ContentStatus.READY);
        Pick pick5 = createPick(title3, count, count, pick5commentTotalCount, count, thumbnailUrl, author,
                ContentStatus.REJECT);

        pickRepository.saveAll(List.of(pick1, pick2, pick3, pick4, pick5));
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
        Slice<PickMainResponse> picksMain = memberPickService.findPicksMain(pageable, Long.MAX_VALUE,
                PickSort.MOST_COMMENTED,
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
            + "(현재 가중치 = 댓글수:" + PickPopularScorePolicy.COMMENT_WEIGHT + ", 투표수:" + PickPopularScorePolicy.VOTE_WEIGHT
            + ", 조회수:" + PickPopularScorePolicy.VIEW_WEIGHT + ")")
    void findPicksMainPOPULAR() {
        // given
        PickOption pickOption1 = createPickOption(new Title("픽옵션1"), new PickOptionContents("픽콘텐츠1"));
        PickOption pickOption2 = createPickOption(new Title("픽옵션2"), new PickOptionContents("픽콘텐츠2"));
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
                thumbnailUrl, author, ContentStatus.APPROVAL, List.of());
        pick1.changePopularScore(pickPopularScorePolicy);

        Pick pick2 = createPick(new Title("픽2타이틀"), pick2VoteTotalCount, pick2ViewTotalCount, pick2commentTotalCount,
                thumbnailUrl, author, ContentStatus.APPROVAL, List.of());
        pick2.changePopularScore(pickPopularScorePolicy);

        Pick pick3 = createPick(new Title("픽3타이틀"), pick3VoteTotalCount, pick3ViewTotalCount, pick3commentTotalCount,
                thumbnailUrl, author, ContentStatus.APPROVAL, List.of());
        pick3.changePopularScore(pickPopularScorePolicy);

        Pick pick4 = createPick(new Title("픽4타이틀"), pick3VoteTotalCount, pick3ViewTotalCount, pick3commentTotalCount,
                thumbnailUrl, author, ContentStatus.READY, List.of());
        pick4.changePopularScore(pickPopularScorePolicy);

        Pick pick5 = createPick(new Title("픽5타이틀"), pick3VoteTotalCount, pick3ViewTotalCount, pick3commentTotalCount,
                thumbnailUrl, author, ContentStatus.REJECT, List.of());
        pick5.changePopularScore(pickPopularScorePolicy);

        pickRepository.saveAll(List.of(pick1, pick2, pick3, pick4, pick5));
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
        Slice<PickMainResponse> picksMain = memberPickService.findPicksMain(pageable, Long.MAX_VALUE, PickSort.POPULAR,
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
    @ValueSource(strings = {FIRST_PICK_OPTION_IMAGE, SECOND_PICK_OPTION_IMAGE})
    @DisplayName("픽픽픽 이미지를 업로드 하고 DB에 픽픽픽 이미지 정보를 저장한다.")
    void uploadImages(String name) {

        // given
        MockMultipartFile mockMultipartFile = createMockMultipartFile("testImage", "tesImage.png");

        // when
        PickUploadImageResponse pickUploadImageResponse = memberPickService.uploadImages(name,
                List.of(mockMultipartFile));

        // then
        PickOptionImage pickOptionImage = pickOptionImageRepository.findById(
                pickUploadImageResponse.getPickOptionImages().get(0).getPickOptionImageId()).get();
        assertThat(pickUploadImageResponse.getPickOptionImages()).hasSize(1)
                .extracting("name", "pickOptionImageId", "imageUrl", "imageKey")
                .containsExactly(
                        tuple(pickOptionImage.getName(), pickOptionImage.getId(), pickOptionImage.getImageUrl(),
                                pickOptionImage.getImageKey())
                );

        // s3 이미지 삭제
        S3 s3 = awsS3Properties.getS3();
        awsS3Uploader.deleteSingleImage(s3.bucket(), pickOptionImage.getImageKey());
    }

    @ParameterizedTest
    @ValueSource(strings = {"thirdPickOptionImage", "firstPickOptionImages", "firstPickOptionImages"})
    @DisplayName("픽픽픽 이미지를 업로드 할때 픽픽픽 옵션에 알맞은 형식의 이름이 아니면 예외가 발생한다.")
    void uploadImagesNameException(String name) {
        // given
        MockMultipartFile mockMultipartFile = createMockMultipartFile("testImage", "tesImage.png");

        // when // then
        assertThatThrownBy(() -> memberPickService.uploadImages(name, List.of(mockMultipartFile)))
                .isInstanceOf(PickOptionImageNameException.class)
                .hasMessage(INVALID_PICK_IMAGE_NAME_MESSAGE);
    }

    @Test
    @DisplayName("회원이 픽픽픽을 작성한다.")
    void registerPick() {
        // given
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String firstImageUrl = "http://devdevdev.co.kr/pickpickpick/fist.jpg";
        String secondImageUrl = "http://devdevdev.co.kr/pickpickpick/second.jpg";
        String imageKey = "/pickpickpick/xxx.jpg";

        PickOptionImage firstPickOptionImage = createPickOptionImage(FIRST_PICK_OPTION_IMAGE, firstImageUrl, imageKey);
        PickOptionImage secondPickOptionImage = createPickOptionImage(SECOND_PICK_OPTION_IMAGE, secondImageUrl,
                imageKey);
        pickOptionImageRepository.saveAll(List.of(firstPickOptionImage, secondPickOptionImage));

        RegisterPickOptionRequest firstPickOptionRequest = createPickOptionRequest("픽옵션1", "픽옵션1블라블라",
                List.of(firstPickOptionImage.getId()));
        RegisterPickOptionRequest secondPickOptionRequest = createPickOptionRequest("픽옵션2", "픽옵션2블라블라",
                List.of(secondPickOptionImage.getId()));

        Map<PickOptionType, RegisterPickOptionRequest> pickOptions = new HashMap<>();
        pickOptions.put(firstPickOption, firstPickOptionRequest);
        pickOptions.put(secondPickOption, secondPickOptionRequest);

        RegisterPickRequest pickRegisterRequest = createPickRegisterRequest("나의 픽픽픽", pickOptions);

        // when
        PickRegisterResponse pickRegisterResponse = memberPickService.registerPick(pickRegisterRequest, authentication);

        // then
        Pick findPick = pickRepository.findById(pickRegisterResponse.getPickId()).get();
        assertAll(
                () -> assertThat(findPick.getTitle()).isEqualTo(new Title("나의 픽픽픽")),
                () -> assertThat(findPick.getAuthor()).isEqualTo(member.getName()),
                () -> assertThat(findPick.getPickOptions()).hasSize(2)
                        .extracting("title", "contents")
                        .containsAnyOf(
                                tuple(new Title("픽옵션1"), new PickOptionContents("픽옵션1블라블라")),
                                tuple(new Title("픽옵션2"), new PickOptionContents("픽옵션2블라블라"))
                        )
        );
    }

    @Test
    @DisplayName("회원이 이미지가 없는 픽픽픽을 작성한다.")
    void registerPickNoImages() {
        // given
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        RegisterPickOptionRequest firstPickOptionRequest = createPickOptionRequest("픽옵션1", "픽옵션1블라블라", null);
        RegisterPickOptionRequest secondPickOptionRequest = createPickOptionRequest("픽옵션2", "픽옵션2블라블라", null);

        Map<PickOptionType, RegisterPickOptionRequest> pickOptions = new HashMap<>();
        pickOptions.put(firstPickOption, firstPickOptionRequest);
        pickOptions.put(secondPickOption, secondPickOptionRequest);

        RegisterPickRequest pickRegisterRequest = createPickRegisterRequest("나의 픽픽픽", pickOptions);

        // when
        PickRegisterResponse pickRegisterResponse = memberPickService.registerPick(pickRegisterRequest, authentication);

        // then
        Pick findPick = pickRepository.findById(pickRegisterResponse.getPickId()).get();
        assertAll(
                () -> assertThat(findPick.getTitle()).isEqualTo(new Title("나의 픽픽픽")),
                () -> assertThat(findPick.getAuthor()).isEqualTo(member.getName()),
                () -> assertThat(findPick.getPickOptions()).hasSize(2)
                        .extracting("title", "contents")
                        .containsAnyOf(
                                tuple(new Title("픽옵션1"), new PickOptionContents("픽옵션1블라블라")),
                                tuple(new Title("픽옵션2"), new PickOptionContents("픽옵션2블라블라"))
                        )
        );
    }

    @Test
    @DisplayName("픽픽픽을 작성할 때 회원이 없는 경우 예외가 발생한다.")
    void registerPickMemberException() {
        // given
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        RegisterPickRequest registerPickRequest = createPickRegisterRequest("나의 픽픽픽", new HashMap<>());

        // when // then
        assertThatThrownBy(() -> memberPickService.registerPick(registerPickRequest, authentication))
                .isInstanceOf(MemberException.class)
                .hasMessage(MemberException.INVALID_MEMBER_NOT_FOUND_MESSAGE);
    }

    @Test
    @DisplayName("회원이 존재하지 않는 이미지를 작성할 경우 예외가 발생한다.")
    void registerPickOptionImageNotSaveException() {
        // given
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String firstImageUrl = "http://devdevdev.co.kr/pickpickpick/fist.jpg";
        String secondImageUrl = "http://devdevdev.co.kr/pickpickpick/second.jpg";
        String imageKey = "/pickpickpick/xxx.jpg";

        PickOptionImage firstPickOptionImage = createPickOptionImage(FIRST_PICK_OPTION_IMAGE, firstImageUrl, imageKey);
        PickOptionImage secondPickOptionImage = createPickOptionImage(SECOND_PICK_OPTION_IMAGE, secondImageUrl,
                imageKey);
        pickOptionImageRepository.saveAll(List.of(firstPickOptionImage, secondPickOptionImage));

        RegisterPickOptionRequest firstPickOptionRequest = createPickOptionRequest("픽옵션1", "픽옵션1블라블라",
                List.of(firstPickOptionImage.getId() + 1_000L));
        RegisterPickOptionRequest secondPickOptionRequest = createPickOptionRequest("픽옵션2", "픽옵션2블라블라",
                List.of(secondPickOptionImage.getId() + 1_000L));

        Map<PickOptionType, RegisterPickOptionRequest> pickOptions = new HashMap<>();
        pickOptions.put(firstPickOption, firstPickOptionRequest);
        pickOptions.put(secondPickOption, secondPickOptionRequest);

        RegisterPickRequest pickRegisterRequest = createPickRegisterRequest("나의 픽픽픽", pickOptions);

        // when // then
        assertThatThrownBy(() -> memberPickService.registerPick(pickRegisterRequest, authentication))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(INVALID_NOT_FOUND_PICK_OPTION_IMAGE_MESSAGE);
    }

    @Test
    @DisplayName("픽픽픽을 수정한다.")
    void modifyPick() {
        // given
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // == 픽픽픽 작성 환경 == //
        Pick pick = createPick(pickTitle, member);
        pickRepository.save(pick);

        PickOption pickOption1 = createPickOption(pick, pickOptionTitle1, new PickOptionContents("픽옵션1콘텐츠"));
        PickOption pickOption2 = createPickOption(pick, pickOptionTitle2, new PickOptionContents("픽옵션2콘텐츠"));
        pickOptionRepository.saveAll(List.of(pickOption1, pickOption2));

        PickOptionImage pickOption1Image1 = createPickOptionImage("픽옵션1사진1", pickOption1);
        PickOptionImage pickOption1Image2 = createPickOptionImage("픽옵션1사진2", pickOption1);
        PickOptionImage pickOption2Image1 = createPickOptionImage("픽옵션2사진1", pickOption2);
        pickOptionImageRepository.saveAll(List.of(pickOption1Image1, pickOption1Image2, pickOption2Image1));
        // == 픽픽픽 작성 환경 == //

        // == 픽픽픽 새로운 사진 업로드 환경 == //
        pickOptionImageRepository.deleteAllById(List.of(pickOption1Image1.getId(), pickOption1Image2.getId(),
                pickOption2Image1.getId()));

        PickOptionImage newPickOption1Image1 = createPickOptionImage("픽옵션1사진1수정");
        PickOptionImage newPickOption2Image1 = createPickOptionImage("픽옵션2사진1수정");
        pickOptionImageRepository.saveAll(List.of(newPickOption1Image1, newPickOption2Image1));
        // == 픽픽픽 새로운 사진 업로드 환경 == //

        ModifyPickOptionRequest modifyPickOptionRequest1 = new ModifyPickOptionRequest(pickOption1.getId(), "픽옵션1제목수정",
                "픽옵션1콘텐츠수정", List.of(newPickOption1Image1.getId()));
        ModifyPickOptionRequest modifyPickOptionRequest2 = new ModifyPickOptionRequest(pickOption2.getId(), "픽옵션2제목수정",
                "픽옵션2콘텐츠수정", List.of(newPickOption2Image1.getId()));

        Map<PickOptionType, ModifyPickOptionRequest> modifyPickOptionRequests = new HashMap<>();
        modifyPickOptionRequests.put(firstPickOption, modifyPickOptionRequest1);
        modifyPickOptionRequests.put(secondPickOption, modifyPickOptionRequest2);

        ModifyPickRequest modifyPickRequest = createModifyPickRequest("픽타이틀수정", modifyPickOptionRequests);

        em.flush();
        em.clear();

        // when
        PickModifyResponse pickModifyResponse = memberPickService.modifyPick(pick.getId(), modifyPickRequest,
                authentication);

        em.flush();
        em.clear();

        // then
        assertThat(pickModifyResponse.getPickId()).isEqualTo(pick.getId());

        Pick findPick = pickRepository.findById(pick.getId()).get();
        assertThat(findPick.getTitle().getTitle()).isEqualTo("픽타이틀수정");

        List<PickOption> findPickOptions = findPick.getPickOptions();
        assertThat(findPickOptions).hasSize(2)
                .extracting("title", "contents")
                .containsExactly(
                        tuple(new Title("픽옵션1제목수정"), new PickOptionContents("픽옵션1콘텐츠수정")),
                        tuple(new Title("픽옵션2제목수정"), new PickOptionContents("픽옵션2콘텐츠수정"))
                );

        List<PickOptionImage> pickOption1Images = findPickOptions.get(0).getPickOptionImages();
        assertThat(pickOption1Images).hasSize(1)
                .extracting("name")
                .containsExactly("픽옵션1사진1수정");

        List<PickOptionImage> pickOption2Images = findPickOptions.get(1).getPickOptionImages();
        assertThat(pickOption2Images).hasSize(1)
                .extracting("name")
                .containsExactly("픽옵션2사진1수정");
    }

    @Test
    @DisplayName("픽픽픽을 수정할 때 회원이 조회되지 않으면 예외가 발생한다.")
    void modifyPickMemberException() {
        // given
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        ModifyPickRequest modifyPickRequest = mock(ModifyPickRequest.class);

        // when // then
        assertThatThrownBy(() -> memberPickService.modifyPick(1L, modifyPickRequest, authentication))
                .isInstanceOf(MemberException.class)
                .hasMessage(MemberException.INVALID_MEMBER_NOT_FOUND_MESSAGE);
    }

    @Test
    @DisplayName("픽픽픽을 수정할 때 수정할 픽픽픽이 없으면 예외가 발생한다.")
    void modifyPickNotFoundPickException() {
        // given
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        ModifyPickRequest modifyPickRequest = mock(ModifyPickRequest.class);

        // when // then
        assertThatThrownBy(() -> memberPickService.modifyPick(1L, modifyPickRequest, authentication))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(INVALID_NOT_FOUND_CAN_MODIFY_PICK_MESSAGE);
    }

    @Test
    @DisplayName("픽픽픽을 수정할 때 회원 본인이 작성한 픽픽픽이 아닐경우 예외가 발생한다.")
    void modifyPickAccessDeniedException() {
        // given
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // == 픽픽픽 작성 환경 == //
        SocialMemberDto socialOtherMemberDto = createSocialDto("otherUserId", "otherName",
                nickname, password, "otherDreamy5patisiel@kakao.com", socialType, role);
        Member otherMember = Member.createMemberBy(socialOtherMemberDto);
        memberRepository.save(otherMember);

        Pick pick = createPick(pickTitle, otherMember); // 다른회윈이 작성한 픽픽픽
        pickRepository.save(pick);

        PickOption pickOption1 = createPickOption(pick, pickOptionTitle1, new PickOptionContents("픽옵션1콘텐츠"));
        PickOption pickOption2 = createPickOption(pick, pickOptionTitle2, new PickOptionContents("픽옵션2콘텐츠"));
        pickOptionRepository.saveAll(List.of(pickOption1, pickOption2));

        PickOptionImage pickOption1Image1 = createPickOptionImage("픽옵션1사진1", pickOption1);
        PickOptionImage pickOption1Image2 = createPickOptionImage("픽옵션1사진2", pickOption1);
        PickOptionImage pickOption2Image1 = createPickOptionImage("픽옵션2사진1", pickOption2);
        pickOptionImageRepository.saveAll(List.of(pickOption1Image1, pickOption1Image2, pickOption2Image1));
        // == 픽픽픽 작성 환경 == //

        // == 픽픽픽 새로운 사진 업로드 환경 == //
        pickOptionImageRepository.deleteAllById(List.of(pickOption1Image1.getId(), pickOption1Image2.getId(),
                pickOption2Image1.getId()));

        PickOptionImage newPickOption1Image1 = createPickOptionImage("픽옵션1사진1수정");
        PickOptionImage newPickOption2Image1 = createPickOptionImage("픽옵션2사진1수정");
        pickOptionImageRepository.saveAll(List.of(newPickOption1Image1, newPickOption2Image1));
        // == 픽픽픽 새로운 사진 업로드 환경 == //

        ModifyPickOptionRequest modifyPickOptionRequest1 = new ModifyPickOptionRequest(pickOption1.getId(), "픽옵션1제목수정",
                "픽옵션1콘텐츠수정", List.of(newPickOption1Image1.getId()));
        ModifyPickOptionRequest modifyPickOptionRequest2 = new ModifyPickOptionRequest(pickOption2.getId(), "픽옵션2제목수정",
                "픽옵션2콘텐츠수정", List.of(newPickOption2Image1.getId()));

        Map<PickOptionType, ModifyPickOptionRequest> modifyPickOptionRequests = new HashMap<>();
        modifyPickOptionRequests.put(firstPickOption, modifyPickOptionRequest1);
        modifyPickOptionRequests.put(secondPickOption, modifyPickOptionRequest2);

        ModifyPickRequest modifyPickRequest = createModifyPickRequest("픽타이틀수정", modifyPickOptionRequests);

        // when // then
        assertThatThrownBy(() -> memberPickService.modifyPick(pick.getId(), modifyPickRequest, authentication))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage(INVALID_MODIFY_MEMBER_PICK_ONLY_MESSAGE);
    }

    @Test
    @DisplayName("회원이 자신이 작성한 픽픽픽 상세를 조회한다.")
    void findPickDetail() {
        // given
        // 회원 생성
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

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
        PickVote pickVote = createPickVote(member, firstPickOption, pick);
        pickVoteRepository.save(pickVote);

        em.flush();
        em.clear();

        // when
        PickDetailResponse pickDetail = memberPickService.findPickDetail(pick.getId(), authentication);

        // then
        assertThat(pickDetail).isNotNull();
        assertAll(
                () -> assertThat(pickDetail.getUserId()).isEqualTo(member.getName()),
                () -> assertThat(pickDetail.getNickname()).isEqualTo(member.getNickname().getNickname()),
                () -> assertThat(pickDetail.getPickTitle()).isEqualTo("픽픽픽 제목"),
                () -> assertThat(pickDetail.getIsMemberPick()).isEqualTo(true)
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

        List<PickDetailOptionImage> findFirstPickOptionPickOptionImagesResponse = findFirstPickOptionResponse.getPickDetailOptionImages();
        PickOptionImage findfirstPickOptionImage = findFirstPickOption.getPickOptionImages().get(0);
        assertThat(findFirstPickOptionPickOptionImagesResponse).hasSize(1)
                .extracting("id", "imageUrl")
                .containsExactly(
                        tuple(findfirstPickOptionImage.getId(), "http://iamge1.png")
                );

        PickOption findSecondPickOption = pickOptionRepository.findById(findSecondPickOptionResponse.getId()).get();
        assertThat(findSecondPickOptionResponse).isNotNull();
        assertAll(
                () -> assertThat(findSecondPickOptionResponse.getId()).isEqualTo(findSecondPickOption.getId()),
                () -> assertThat(findSecondPickOptionResponse.getTitle()).isEqualTo("픽픽픽 옵션2"),
                () -> assertThat(findSecondPickOptionResponse.getIsPicked()).isEqualTo(false),
                () -> assertThat(findSecondPickOptionResponse.getPercent()).isEqualTo(0),
                () -> assertThat(findSecondPickOptionResponse.getContent()).isEqualTo("픽픽픽 옵션2 내용"),
                () -> assertThat(findSecondPickOptionResponse.getVoteTotalCount()).isEqualTo(0)
        );

        List<PickDetailOptionImage> findSecondPickOptionPickOptionImagesResponse = findSecondPickOptionResponse.getPickDetailOptionImages();
        PickOptionImage findSecondPickOptionImage = secondPickOption.getPickOptionImages().get(0);
        assertThat(findSecondPickOptionPickOptionImagesResponse).hasSize(1)
                .extracting("id", "imageUrl")
                .containsExactly(
                        tuple(findSecondPickOptionImage.getId(), "http://iamge2.png")
                );
    }

    @Test
    @DisplayName("회원이 다른 회원이 작성한 픽픽픽 상세를 조회한다.")
    void findPickDetailByOtherMember() {
        // given

        // 회원 생성
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 픽픽픽을 작성한 회원 생성
        SocialMemberDto ohterSocialMemberDto = createSocialDto("howisitgoing", "유소영", "쏘영쏘", password,
                "merooongg@naver.com", socialType, role);
        Member otherMember = Member.createMemberBy(ohterSocialMemberDto);
        memberRepository.save(otherMember);

        // 픽픽픽 생성
        Pick pick = createPick(new Title("픽픽픽 제목"), new Count(0), new Count(0), new Count(1), new Count(0),
                otherMember, ContentStatus.APPROVAL);
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
        PickVote pickVote = createPickVote(member, firstPickOption, pick);
        pickVoteRepository.save(pickVote);

        em.flush();
        em.clear();

        // when
        PickDetailResponse pickDetail = memberPickService.findPickDetail(pick.getId(), authentication);

        // then
        assertThat(pickDetail).isNotNull();
        assertAll(
                () -> assertThat(pickDetail.getUserId()).isEqualTo(otherMember.getName()),
                () -> assertThat(pickDetail.getNickname()).isEqualTo(otherMember.getNickname().getNickname()),
                () -> assertThat(pickDetail.getPickTitle()).isEqualTo("픽픽픽 제목"),
                () -> assertThat(pickDetail.getIsMemberPick()).isEqualTo(false)
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

        List<PickDetailOptionImage> findFirstPickOptionPickOptionImagesResponse = findFirstPickOptionResponse.getPickDetailOptionImages();
        PickOptionImage findfirstPickOptionImage = findFirstPickOption.getPickOptionImages().get(0);
        assertThat(findFirstPickOptionPickOptionImagesResponse).hasSize(1)
                .extracting("id", "imageUrl")
                .containsExactly(
                        tuple(findfirstPickOptionImage.getId(), "http://iamge1.png")
                );

        PickOption findSecondPickOption = pickOptionRepository.findById(findSecondPickOptionResponse.getId()).get();
        assertThat(findSecondPickOptionResponse).isNotNull();
        assertAll(
                () -> assertThat(findSecondPickOptionResponse.getId()).isEqualTo(findSecondPickOption.getId()),
                () -> assertThat(findSecondPickOptionResponse.getTitle()).isEqualTo("픽픽픽 옵션2"),
                () -> assertThat(findSecondPickOptionResponse.getIsPicked()).isEqualTo(false),
                () -> assertThat(findSecondPickOptionResponse.getPercent()).isEqualTo(0),
                () -> assertThat(findSecondPickOptionResponse.getContent()).isEqualTo("픽픽픽 옵션2 내용"),
                () -> assertThat(findSecondPickOptionResponse.getVoteTotalCount()).isEqualTo(0)
        );

        List<PickDetailOptionImage> findSecondPickOptionPickOptionImagesResponse = findSecondPickOptionResponse.getPickDetailOptionImages();
        PickOptionImage findSecondPickOptionImage = secondPickOption.getPickOptionImages().get(0);
        assertThat(findSecondPickOptionPickOptionImagesResponse).hasSize(1)
                .extracting("id", "imageUrl")
                .containsExactly(
                        tuple(findSecondPickOptionImage.getId(), "http://iamge2.png")
                );
    }

    @Test
    @DisplayName("회원이 픽픽픽 상세 조회할 때 픽픽픽이 없으면 예외가 발생한다.")
    void findPickDetailNotFoundPickDetail() {
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
        assertThatThrownBy(() -> memberPickService.findPickDetail(0L, authentication))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(INVALID_NOT_FOUND_PICK_MESSAGE);
    }

    @ParameterizedTest
    @EnumSource(ContentStatus.class)
    @DisplayName("픽픽픽 상세를 조회할 때 픽픽픽이 승인상태가 아니면 예외가 발생한다.")
    void findPickDetail_INVALID_NOT_APPROVAL_STATUS_PICK_MESSAGE(ContentStatus contentStatus) {

        if (contentStatus.equals(ContentStatus.APPROVAL)) {
            return;
        }

        // 회원 생성
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 픽픽픽을 작성한 회원 생성
        SocialMemberDto ohterSocialMemberDto = createSocialDto("howisitgoing", "유소영", "쏘영쏘", password,
                "merooongg@naver.com", socialType, role);
        Member otherMember = Member.createMemberBy(ohterSocialMemberDto);
        memberRepository.save(otherMember);

        // 픽픽픽 생성
        Pick pick = createPick(new Title("픽픽픽 제목"), new Count(0), new Count(0), new Count(1), new Count(0),
                otherMember, contentStatus);
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
        PickVote pickVote = createPickVote(member, firstPickOption, pick);
        pickVoteRepository.save(pickVote);

        em.flush();
        em.clear();

        // when // then
        assertThatThrownBy(() -> memberPickService.findPickDetail(pick.getId(), authentication))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(PickExceptionMessage.INVALID_NOT_APPROVAL_STATUS_PICK_MESSAGE);
    }

    @Test
    @DisplayName("픽픽픽 옵션에 투표한 이력이 없는 회원이 픽픽픽 옵션 중 하나에 투표한다.")
    void votePickOptionNewCreate() {
        // given
        // 회원 생성
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

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

        VotePickOptionRequest request = VotePickOptionRequest.builder()
                .pickId(pick.getId())
                .pickOptionId(firstPickOption.getId())
                .build();

        // when
        VotePickResponse votePickResponse = memberPickService.votePickOption(request, authentication);

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
    @DisplayName("픽픽픽 옵션에 투표한 이력이 있는 회원이 다른 픽옵션에 투표 할 경우 기존 투표 이력은 삭제되고, 새로운 투표 이력이 생성된다.")
    void votePickOptionDeleteAndCreateNew() {
        // given
        // 회원 생성
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

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
        VotePickOptionRequest request = VotePickOptionRequest.builder()
                .pickId(pick.getId())
                .pickOptionId(secondPickOption.getId())
                .build();

        // when
        VotePickResponse votePickResponse = memberPickService.votePickOption(request, authentication);

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
    @DisplayName("회원이 픽픽픽 옵션을 투표할 때 이미 투표한 픽픽픽 옵션에 투표를 하면 예외가 발생한다.")
    void votePickOption_INVALID_CAN_NOT_VOTE_SAME_PICK_OPTION_MESSAGE() {
        // given
        // 회원 생성
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

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
        PickVote pickVote = createPickVote(member, firstPickOption, pick);
        pickVoteRepository.save(pickVote);

        VotePickOptionRequest request = VotePickOptionRequest.builder()
                .pickId(pick.getId())
                .pickOptionId(firstPickOption.getId())
                .build();

        // when // then
        assertThatThrownBy(() -> memberPickService.votePickOption(request, authentication))
                .isInstanceOf(VotePickOptionException.class)
                .hasMessage(INVALID_CAN_NOT_VOTE_SAME_PICK_OPTION_MESSAGE);
    }

    @Test
    @DisplayName("회원이 픽픽픽 옵션을 투표할 때 픽픽픽이 없으면 예외가 발생한다.")
    void votePickOption_INVALID_NOT_FOUND_PICK_MESSAGE() {
        // given
        // 회원 생성
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        VotePickOptionRequest request = VotePickOptionRequest.builder()
                .pickId(0L)
                .pickOptionId(0L)
                .build();

        // when // then
        assertThatThrownBy(() -> memberPickService.votePickOption(request, authentication))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(INVALID_NOT_FOUND_PICK_MESSAGE);
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

    private ModifyPickRequest createModifyPickRequest(String pickTitle,
                                                      Map<PickOptionType, ModifyPickOptionRequest> modifyPickOptionRequests) {
        return ModifyPickRequest.builder()
                .pickTitle(pickTitle)
                .pickOptions(modifyPickOptionRequests)
                .build();
    }

    private PickOptionImage createPickOptionImage(String name, String imageUrl, String imageKey) {
        return PickOptionImage.builder()
                .name(name)
                .imageUrl(imageUrl)
                .imageKey(imageKey)
                .build();
    }

    private PickOptionImage createPickOptionImage(String name) {
        return PickOptionImage.builder()
                .name(name)
                .build();
    }

    private PickOptionImage createPickOptionImage(String name, String imageUrl, PickOption pickOption) {
        PickOptionImage pickOptionImage = PickOptionImage.builder()
                .name(name)
                .imageUrl(imageUrl)
                .build();

        pickOptionImage.changePickOption(pickOption);

        return pickOptionImage;
    }

    private PickOptionImage createPickOptionImage(String name, PickOption pickOption) {
        PickOptionImage pickOptionImage = PickOptionImage.builder()
                .name(name)
                .build();

        pickOptionImage.changePickOption(pickOption);

        return pickOptionImage;
    }

    private RegisterPickRequest createPickRegisterRequest(String pickTitle,
                                                          Map<PickOptionType, RegisterPickOptionRequest> pickOptions) {
        return RegisterPickRequest.builder()
                .pickTitle(pickTitle)
                .pickOptions(pickOptions)
                .build();
    }

    private RegisterPickOptionRequest createPickOptionRequest(String pickOptionTitle, String pickOptionContent,
                                                              List<Long> pickOptionImageIds) {
        return RegisterPickOptionRequest.builder()
                .pickOptionTitle(pickOptionTitle)
                .pickOptionContent(pickOptionContent)
                .pickOptionImageIds(pickOptionImageIds)
                .build();
    }

    private MockMultipartFile createMockMultipartFile(String name, String originalFilename) {
        return new MockMultipartFile(
                name,
                originalFilename,
                MediaType.IMAGE_PNG_VALUE,
                name.getBytes()
        );
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

    private Pick createPick(Title title, Member member) {
        return Pick.builder()
                .title(title)
                .member(member)
                .build();
    }

    private Pick createPick(Title title, Count pickVoteTotalCount, Count pickViewTotalCount,
                            Count pickcommentTotalCount, Count pickPopularScore, String thumbnailUrl,
                            String author, ContentStatus contentStatus
    ) {

        return Pick.builder()
                .title(title)
                .voteTotalCount(pickVoteTotalCount)
                .viewTotalCount(pickViewTotalCount)
                .commentTotalCount(pickcommentTotalCount)
                .popularScore(pickPopularScore)
                .thumbnailUrl(thumbnailUrl)
                .author(author)
                .contentStatus(contentStatus)
                .build();
    }

    private Pick createPick(Title title, Count pickVoteTotalCount, Count pickViewTotalCount,
                            Count pickcommentTotalCount, String thumbnailUrl, String author,
                            ContentStatus contentStatus,
                            List<PickVote> pickVotes
    ) {

        Pick pick = Pick.builder()
                .title(title)
                .voteTotalCount(pickVoteTotalCount)
                .viewTotalCount(pickViewTotalCount)
                .commentTotalCount(pickcommentTotalCount)
                .thumbnailUrl(thumbnailUrl)
                .author(author)
                .contentStatus(contentStatus)
                .build();

        pick.changePickVote(pickVotes);

        return pick;
    }

    private PickOption createPickOption(Pick pick, Title title, PickOptionContents pickOptionContents,
                                        Count voteTotalCount) {
        PickOption pickOption = PickOption.builder()
                .title(title)
                .contents(pickOptionContents)
                .voteTotalCount(voteTotalCount)
                .build();

        pickOption.changePick(pick);

        return pickOption;
    }

    private PickOption createPickOption(Pick pick, Title title, PickOptionContents pickOptionContents) {
        PickOption pickOption = PickOption.builder()
                .title(title)
                .contents(pickOptionContents)
                .build();

        pickOption.changePick(pick);

        return pickOption;
    }

    private PickOption createPickOption(Pick pick, Title title, PickOptionContents pickOptionContents,
                                        Count pickOptionVoteCount,
                                        PickOptionType pickOptionType) {
        PickOption pickOption = PickOption.builder()
                .title(title)
                .contents(pickOptionContents)
                .voteTotalCount(pickOptionVoteCount)
                .pickOptionType(pickOptionType)
                .build();

        pickOption.changePick(pick);

        return pickOption;
    }

    private PickOption createPickOption(Title title, PickOptionContents pickOptionContents) {
        return PickOption.builder()
                .title(title)
                .contents(pickOptionContents)
                .build();
    }

    private PickVote createPickVote(Member member, PickOption pickOption, Pick pick) {
        PickVote pickVote = PickVote.builder()
                .member(member)
                .build();

        pickVote.changePickOption(pickOption);
        pickVote.changePick(pick);

        return pickVote;
    }
}