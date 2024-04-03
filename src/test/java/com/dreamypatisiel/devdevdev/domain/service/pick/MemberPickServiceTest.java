package com.dreamypatisiel.devdevdev.domain.service.pick;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import static com.dreamypatisiel.devdevdev.domain.service.pick.MemberPickService.*;
import static com.dreamypatisiel.devdevdev.web.controller.request.PickOptionName.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;

import com.dreamypatisiel.devdevdev.aws.s3.AwsS3Uploader;
import com.dreamypatisiel.devdevdev.aws.s3.S3ImageObject;
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
import com.dreamypatisiel.devdevdev.domain.service.response.PickRegisterResponse;
import com.dreamypatisiel.devdevdev.domain.service.response.PickUploadImageResponse;
import com.dreamypatisiel.devdevdev.domain.service.response.PicksResponse;
import com.dreamypatisiel.devdevdev.exception.ImageFileException;
import com.dreamypatisiel.devdevdev.exception.MemberException;
import com.dreamypatisiel.devdevdev.exception.PickOptionImageNameException;
import com.dreamypatisiel.devdevdev.exception.PickOptionNameException;
import com.dreamypatisiel.devdevdev.global.common.MemberProvider;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.SocialMemberDto;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.UserPrincipal;
import com.dreamypatisiel.devdevdev.web.controller.request.PickOptionName;
import com.dreamypatisiel.devdevdev.web.controller.request.PickOptionRequest;
import com.dreamypatisiel.devdevdev.web.controller.request.PickRegisterRequest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
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

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

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
                pickPopularScore, thumbnailUrl, author);
        pick.changePopularScore(pickPopularScorePolicy);
        pickRepository.save(pick);

        PickOption pickOption1 = createPickOption(pick, pickOptionTitle1, pickContents1, pickOptionVoteCount1);
        PickOption pickOption2 = createPickOption(pick, pickOptionTitle2, pickContents2, pickOptionVoteCount2);
        pickOptionRepository.saveAll(List.of(pickOption1, pickOption2));

        PickVote pickVote = createPickVote(member, pickOption1, pick);
        pickVoteRepository.save(pickVote);

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

        Count pick1ViewTotalCount = new Count(1);
        Count pick2ViewTotalCount = new Count(2);
        Count pick3ViewTotalCount = new Count(3);

        Count count = new Count(1);
        Pick pick1 = createPick(title1, count, pick1ViewTotalCount, count, count, thumbnailUrl, author);
        Pick pick2 = createPick(title2, count, pick2ViewTotalCount, count, count, thumbnailUrl, author);
        Pick pick3 = createPick(title3, count, pick3ViewTotalCount, count, count, thumbnailUrl, author);

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
        PickOption pickOption1 = createPickOption(new Title("픽옵션1"), new PickOptionContents("픽콘텐츠1"));
        PickOption pickOption2 = createPickOption(new Title("픽옵션2"), new PickOptionContents("픽콘텐츠2"));
        pickOption1.changePickVoteCount(new Count(1));
        pickOption2.changePickVoteCount(new Count(2));

        Title title1 = new Title("픽1타이틀");
        Title title2 = new Title("픽2타이틀");
        Title title3 = new Title("픽3타이틀");
        Count count = new Count(1);
        Pick pick1 = createPick(title1, count, count, count, count, thumbnailUrl, author);
        Pick pick2 = createPick(title2, count, count, count, count, thumbnailUrl, author);
        Pick pick3 = createPick(title3, count, count, count, count, thumbnailUrl, author);

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
        Count count = new Count(1);
        Pick pick1 = createPick(title1, count, count, pick1commentTotalCount, count, thumbnailUrl, author);
        Pick pick2 = createPick(title2, count, count, pick2commentTotalCount, count, thumbnailUrl, author);
        Pick pick3 = createPick(title3, count, count, pick3commentTotalCount, count, thumbnailUrl, author);

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
    @ValueSource(strings = {FIRST_PICK_OPTION_IMAGE, SECOND_PICK_OPTION_IMAGE})
    @DisplayName("픽픽픽 이미지를 업로드 하고 DB에 픽픽픽 이미지 정보를 저장한다.")
    void uploadImages(String name) {

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
        awsS3Uploader.deleteSingleImage(s3.bucket(), pickOptionImage.getImageKey());
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
        PickOptionImage secondPickOptionImage = createPickOptionImage(SECOND_PICK_OPTION_IMAGE, secondImageUrl, imageKey);
        pickOptionImageRepository.saveAll(List.of(firstPickOptionImage, secondPickOptionImage));

        PickOptionRequest firstPickOptionRequest = createPickOptionRequest("픽옵션1", "픽옵션1블라블라",
                List.of(firstPickOptionImage.getId()));
        PickOptionRequest secondPickOptionRequest = createPickOptionRequest("픽옵션2", "픽옵션2블라블라",
                List.of(secondPickOptionImage.getId()));

        Map<String, PickOptionRequest> pickOptions = new HashMap<>();
        pickOptions.put(FIRST_PICK_OPTION.getDescription(), firstPickOptionRequest);
        pickOptions.put(SECOND_PICK_OPTION.getDescription(), secondPickOptionRequest);

        PickRegisterRequest pickRegisterRequest = createPickRegisterRequest("나의 픽픽픽", pickOptions);

        // when
        PickRegisterResponse pickRegisterResponse = memberPickService.registerPick(pickRegisterRequest, authentication);

        // then
        Pick findPick = pickRepository.findById(pickRegisterResponse.getPickId()).get();
        assertAll(
                () -> assertThat(findPick.getTitle()).isEqualTo(new Title("나의 픽픽픽")),
                () -> assertThat(findPick.getAuthor()).isEqualTo(member.getName()),
                () -> assertThat(findPick.getPickOptions()).hasSize(2)
                        .extracting("title", "contents")
                        .containsExactly(
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

        PickRegisterRequest pickRegisterRequest = createPickRegisterRequest("나의 픽픽픽", new HashMap<>());

        // when // then
        assertThatThrownBy(() -> memberPickService.registerPick(pickRegisterRequest, authentication))
                .isInstanceOf(MemberException.class)
                .hasMessage(MemberException.INVALID_MEMBER_NOT_FOUND_MESSAGE);
    }

    @Test
    @DisplayName("잘못된 형식의 픽픽픽 옵션 필드 이름 이면 예외가 발생한다."
            + "(firstPickOption 또는 secondPickOption가 아닐경우)")
    void registerPickPickOptionNameException() {
        // given
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        PickOptionRequest thirdPickOptionRequest = createPickOptionRequest("픽옵션1", "픽옵션1블라블라", List.of(1L));

        Map<String, PickOptionRequest> pickOptions = new HashMap<>();
        pickOptions.put("thirdPickOption", thirdPickOptionRequest);
        PickRegisterRequest pickRegisterRequest = createPickRegisterRequest("나의 픽픽픽", pickOptions);

        // when // then
        assertThatThrownBy(() -> memberPickService.registerPick(pickRegisterRequest, authentication))
                .isInstanceOf(PickOptionNameException.class)
                .hasMessage(INVALID_PICK_OPTION_NAME_MESSAGE);
    }

    private PickVote createPickVote(Member member, PickOption pickOption, Pick pick) {
        PickVote pickVote = PickVote.builder()
                .member(member)
                .pickOption(pickOption)
                .pick(pick)
                .build();

        pickVote.changePick(pick);
        pickVote.changePickOption(pickOption);

        return pickVote;
    }

    private PickOptionImage createPickOptionImage(String name, String imageUrl, String imageKey) {
        return PickOptionImage.builder()
                .name(name)
                .imageUrl(imageUrl)
                .imageKey(imageKey)
                .build();
    }

    private PickRegisterRequest createPickRegisterRequest(String pickTitle, Map<String, PickOptionRequest> pickOptions) {
        return PickRegisterRequest.builder()
                .pickTitle(pickTitle)
                .pickOptions(pickOptions)
                .build();
    }

    private PickOptionRequest createPickOptionRequest(String pickOptionTitle, String pickOptionContent, List<Long> pickOptionImageIds) {
        return PickOptionRequest.builder()
                .pickOptionTitle(pickOptionTitle)
                .pickOptionContent(pickOptionContent)
                .pickOptionImageIds(pickOptionImageIds)
                .build();
    }

    @Test
    @DisplayName("픽픽픽 S3에 업로드 된 이미지를 삭제하고 DB에 저장된 이미지 정보도 삭제한다.")
    void deleteImage() {
        // given
        MockMultipartFile mockMultipartFile = createMockMultipartFile("testImage", "tesImage.png");
        S3 s3 = awsS3Properties.getS3();
        S3ImageObject s3ImageObject = awsS3Uploader.uploadSingleImage(mockMultipartFile, s3.bucket(), s3.createPickPickPickDirectory());

        PickOptionImage pickOptionImage = createPickOptionImage(s3ImageObject.getImageUrl(), s3ImageObject.getKey());
        pickOptionImageRepository.save(pickOptionImage);

        // when
        memberPickService.deleteImage(pickOptionImage.getId());

        // then
        assertThatThrownBy(() -> pickOptionImageRepository.findById(pickOptionImage.getId()).get())
                .isInstanceOf(NoSuchElementException.class);

        assertThatThrownBy(() -> amazonS3Client.getObject(s3.bucket(), s3ImageObject.getKey()))
                .isInstanceOf(SdkClientException.class);
    }

    @Test
    @DisplayName("픽픽픽 이미지를 삭제할 때 DB에 이미지 정보가 없으면 예외가 발생한다.")
    void deleteImageException() {
        // given // when // then
        assertThatThrownBy(() -> memberPickService.deleteImage(1L))
                .isInstanceOf(ImageFileException.class)
                .hasMessage(MemberPickService.INVALID_NOT_FOUND_PICK_OPTION_IMAGE_MESSAGE);
    }

    private PickOptionImage createPickOptionImage(String imageUrl, String imageKey) {
        return PickOptionImage.builder()
                .imageUrl(imageUrl)
                .imageKey(imageKey)
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
                            String author
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

        pick.changePickVote(pickVotes);

        return pick;
    }

    private PickOption createPickOption(Pick pick, Title title, PickOptionContents pickOptionContents, Count voteTotalCount) {
        PickOption pickOption = PickOption.builder()
                .title(title)
                .contents(pickOptionContents)
                .voteTotalCount(voteTotalCount)
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
}