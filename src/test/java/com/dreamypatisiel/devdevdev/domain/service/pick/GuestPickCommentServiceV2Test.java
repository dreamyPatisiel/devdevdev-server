package com.dreamypatisiel.devdevdev.domain.service.pick;

import static com.dreamypatisiel.devdevdev.domain.exception.PickExceptionMessage.INVALID_NOT_APPROVAL_STATUS_PICK_COMMENT_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.PickExceptionMessage.INVALID_NOT_FOUND_PICK_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.PickExceptionMessage.INVALID_NOT_FOUND_PICK_VOTE_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.service.pick.PickCommentService.REGISTER;
import static com.dreamypatisiel.devdevdev.domain.service.pick.PickTestUtils.createPick;
import static com.dreamypatisiel.devdevdev.domain.service.pick.PickTestUtils.createPickOption;
import static com.dreamypatisiel.devdevdev.domain.service.pick.PickTestUtils.createPickOptionImage;
import static com.dreamypatisiel.devdevdev.domain.service.pick.PickTestUtils.createPickVote;
import static com.dreamypatisiel.devdevdev.domain.service.pick.PickTestUtils.createSocialDto;
import static com.dreamypatisiel.devdevdev.global.utils.AuthenticationMemberUtils.INVALID_METHODS_CALL_MESSAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amazonaws.services.s3.AmazonS3;
import com.dreamypatisiel.devdevdev.aws.s3.AwsS3Uploader;
import com.dreamypatisiel.devdevdev.aws.s3.properties.AwsS3Properties;
import com.dreamypatisiel.devdevdev.domain.entity.AnonymousMember;
import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.Pick;
import com.dreamypatisiel.devdevdev.domain.entity.PickComment;
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
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickCommentRecommendRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickCommentRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickOptionImageRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickOptionRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickVoteRepository;
import com.dreamypatisiel.devdevdev.domain.service.pick.dto.PickCommentDto;
import com.dreamypatisiel.devdevdev.exception.NotFoundException;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.SocialMemberDto;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.UserPrincipal;
import com.dreamypatisiel.devdevdev.global.utils.AuthenticationMemberUtils;
import com.dreamypatisiel.devdevdev.web.dto.response.pick.PickCommentResponse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class GuestPickCommentServiceV2Test {

    @Autowired
    GuestPickCommentServiceV2 guestPickCommentServiceV2;
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
    @Autowired
    AnonymousMemberRepository anonymousMemberRepository;

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
    String author = "운영자";

    @Test
    @DisplayName("익명회원이 승인상태의 픽픽픽에 선택지 투표 공개 댓글을 작성한다.")
    void registerPickCommentWithPickMainVote() {
        // given
        // 익명회원 생성
        AnonymousMember anonymousMember = AnonymousMember.create("anonymousMemberId", "익명으로 개발하는 댑댑이");
        anonymousMemberRepository.save(anonymousMember);

        // 픽픽픽 작성자 생성
        SocialMemberDto authorSocialMemberDto = createSocialDto("authorId", "author",
                nickname, password, "authorDreamy5patisiel@kakao.com", socialType, role);
        Member author = Member.createMemberBy(authorSocialMemberDto);
        memberRepository.save(author);

        // 픽픽픽 생성
        Pick pick = createPick(new Title("픽픽픽 타이틀"), ContentStatus.APPROVAL, new Count(0L), new Count(0L),
                new Count(0L), new Count(0L), author);
        pickRepository.save(pick);

        // 픽픽픽 옵션 생성
        PickOption firstPickOption = createPickOption(pick, new Title("픽픽픽 옵션1 타이틀"),
                new PickOptionContents("픽픽픽 옵션1 컨텐츠"), PickOptionType.firstPickOption);
        PickOption secondPickOption = createPickOption(pick, new Title("픽픽픽 옵션2 타이틀"),
                new PickOptionContents("픽픽픽 옵션2 컨텐츠"), PickOptionType.secondPickOption);
        pickOptionRepository.saveAll(List.of(firstPickOption, secondPickOption));

        // 픽픽픽 이미지 생성
        PickOptionImage firstPickOptionImage = createPickOptionImage("firstPickOptionImage", firstPickOption);
        PickOptionImage secondPickOptionImage = createPickOptionImage("secondPickOptionImage", firstPickOption);
        pickOptionImageRepository.saveAll(List.of(firstPickOptionImage, secondPickOptionImage));

        // 픽픽픽 투표 생성
        PickVote pickVote = createPickVote(anonymousMember, firstPickOption, pick);
        pickVoteRepository.save(pickVote);

        em.flush();
        em.clear();

        PickCommentDto pickCommentDto = new PickCommentDto("안녕하세웅", true, "anonymousMemberId");

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(AuthenticationMemberUtils.ANONYMOUS_USER);

        // when
        PickCommentResponse pickCommentResponse = guestPickCommentServiceV2.registerPickComment(pick.getId(), pickCommentDto,
                authentication);

        // then
        assertThat(pickCommentResponse.getPickCommentId()).isNotNull();

        PickComment findPickComment = pickCommentRepository.findById(pickCommentResponse.getPickCommentId()).get();
        assertAll(
                () -> assertThat(findPickComment.getContents().getCommentContents()).isEqualTo("안녕하세웅"),
                () -> assertThat(findPickComment.getIsPublic()).isEqualTo(true),
                () -> assertThat(findPickComment.getDeletedAt()).isNull(),
                () -> assertThat(findPickComment.getBlameTotalCount().getCount()).isEqualTo(0L),
                () -> assertThat(findPickComment.getRecommendTotalCount().getCount()).isEqualTo(0L),
                () -> assertThat(findPickComment.getPick().getId()).isEqualTo(pick.getId()),
                () -> assertThat(findPickComment.getCreatedAnonymousBy().getId()).isEqualTo(anonymousMember.getId()),
                () -> assertThat(findPickComment.getPickVote().getId()).isEqualTo(pickVote.getId())
        );
    }

    @Test
    @DisplayName("익명회원이 승인상태의 픽픽픽에 선택지 투표 비공개 댓글을 작성한다.")
    void registerPickCommentWithOutPickMainVote() {
        // given
        // 익명회원 생성
        AnonymousMember anonymousMember = AnonymousMember.create("anonymousMemberId", "익명으로 개발하는 댑댑이");
        anonymousMemberRepository.save(anonymousMember);

        // 픽픽픽 작성자 생성
        SocialMemberDto authorSocialMemberDto = createSocialDto("authorId", "author",
                nickname, password, "authorDreamy5patisiel@kakao.com", socialType, role);
        Member author = Member.createMemberBy(authorSocialMemberDto);
        memberRepository.save(author);

        // 픽픽픽 생성
        Pick pick = createPick(new Title("픽픽픽 타이틀"), ContentStatus.APPROVAL, new Count(0L), new Count(0L),
                new Count(0L), new Count(0L), author);
        pickRepository.save(pick);

        // 픽픽픽 옵션 생성
        PickOption firstPickOption = createPickOption(pick, new Title("픽픽픽 옵션1 타이틀"),
                new PickOptionContents("픽픽픽 옵션1 컨텐츠"), PickOptionType.firstPickOption);
        PickOption secondPickOption = createPickOption(pick, new Title("픽픽픽 옵션2 타이틀"),
                new PickOptionContents("픽픽픽 옵션2 컨텐츠"), PickOptionType.secondPickOption);
        pickOptionRepository.saveAll(List.of(firstPickOption, secondPickOption));

        // 픽픽픽 이미지 생성
        PickOptionImage firstPickOptionImage = createPickOptionImage("firstPickOptionImage", firstPickOption);
        PickOptionImage secondPickOptionImage = createPickOptionImage("secondPickOptionImage", firstPickOption);
        pickOptionImageRepository.saveAll(List.of(firstPickOptionImage, secondPickOptionImage));

        // 픽픽픽 투표 생성
        PickVote pickVote = createPickVote(anonymousMember, firstPickOption, pick);
        pickVoteRepository.save(pickVote);

        em.flush();
        em.clear();

        PickCommentDto pickCommentDto = new PickCommentDto("안녕하세웅", false, "anonymousMemberId");

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(AuthenticationMemberUtils.ANONYMOUS_USER);

        // when
        PickCommentResponse pickCommentResponse = guestPickCommentServiceV2.registerPickComment(pick.getId(), pickCommentDto,
                authentication);

        // then
        assertThat(pickCommentResponse.getPickCommentId()).isNotNull();

        PickComment findPickComment = pickCommentRepository.findById(pickCommentResponse.getPickCommentId()).get();
        assertAll(
                () -> assertThat(findPickComment.getContents().getCommentContents()).isEqualTo("안녕하세웅"),
                () -> assertThat(findPickComment.getIsPublic()).isEqualTo(false),
                () -> assertThat(findPickComment.getDeletedAt()).isNull(),
                () -> assertThat(findPickComment.getBlameTotalCount().getCount()).isEqualTo(0L),
                () -> assertThat(findPickComment.getRecommendTotalCount().getCount()).isEqualTo(0L),
                () -> assertThat(findPickComment.getPick().getId()).isEqualTo(pick.getId()),
                () -> assertThat(findPickComment.getCreatedAnonymousBy().getId()).isEqualTo(anonymousMember.getId()),
                () -> assertThat(findPickComment.getPickVote()).isNull()
        );
    }

    @Test
    @DisplayName("픽픽픽 익명회원 댓글을 작성할 때 익명회원이 아니면 예외가 발생한다.")
    void registerPickCommentIllegalStateException() {
        // given
        // 익명회원 생성
        AnonymousMember anonymousMember = AnonymousMember.create("anonymousMemberId", "익명으로 개발하는 댑댑이");
        anonymousMemberRepository.save(anonymousMember);

        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // when
        PickCommentDto pickCommentDto = new PickCommentDto("안녕하세웅", true, "anonymousMemberId");

        // then
        assertThatThrownBy(() -> guestPickCommentServiceV2.registerPickComment(0L, pickCommentDto, authentication))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(INVALID_METHODS_CALL_MESSAGE);
    }

    @Test
    @DisplayName("익명회원이 픽픽픽 댓글을 작성할 때 픽픽픽이 존재하지 않으면 예외가 발생한다.")
    void registerPickCommentPickMainNotFoundException() {
        // given
        // 익명회원 생성
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(AuthenticationMemberUtils.ANONYMOUS_USER);

        // when
        PickCommentDto pickCommentDto = new PickCommentDto("안녕하세웅", true, "anonymousMemberId");

        // then
        assertThatThrownBy(
                () -> guestPickCommentServiceV2.registerPickComment(1L, pickCommentDto, authentication))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(INVALID_NOT_FOUND_PICK_MESSAGE);
    }

    @ParameterizedTest
    @EnumSource(value = ContentStatus.class, mode = Mode.EXCLUDE, names = {"APPROVAL"})
    @DisplayName("익명회원이 픽픽픽 댓글을 작성할 때 픽픽픽이 승인상태가 아니면 예외가 발생한다.")
    void registerPickCommentNotApproval(ContentStatus contentStatus) {
        // given
        // 익명회원 생성
        AnonymousMember anonymousMember = AnonymousMember.create("anonymousMemberId", "익명으로 개발하는 댑댑이");
        anonymousMemberRepository.save(anonymousMember);

        // 픽픽픽 작성자 생성
        SocialMemberDto authorSocialMemberDto = createSocialDto("authorId", "author",
                nickname, password, "authorDreamy5patisiel@kakao.com", socialType, role);
        Member author = Member.createMemberBy(authorSocialMemberDto);
        memberRepository.save(author);

        // 픽픽픽 생성
        Pick pick = createPick(new Title("픽픽픽 타이틀"), contentStatus, new Count(0L), new Count(0L), new Count(0L),
                new Count(0L), author);
        pickRepository.save(pick);

        // 픽픽픽 옵션 생성
        PickOption firstPickOption = createPickOption(pick, new Title("픽픽픽 옵션1 타이틀"),
                new PickOptionContents("픽픽픽 옵션1 컨텐츠"), PickOptionType.firstPickOption);
        PickOption secondPickOption = createPickOption(pick, new Title("픽픽픽 옵션2 타이틀"),
                new PickOptionContents("픽픽픽 옵션2 컨텐츠"), PickOptionType.secondPickOption);
        pickOptionRepository.saveAll(List.of(firstPickOption, secondPickOption));

        em.flush();
        em.clear();

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(AuthenticationMemberUtils.ANONYMOUS_USER);

        PickCommentDto pickCommentDto = new PickCommentDto("안녕하세웅", true, "anonymousMemberId");

        // when // then
        assertThatThrownBy(
                () -> guestPickCommentServiceV2.registerPickComment(pick.getId(), pickCommentDto, authentication))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(INVALID_NOT_APPROVAL_STATUS_PICK_COMMENT_MESSAGE, REGISTER);
    }

    @Test
    @DisplayName("익명회원이 승인상태의 픽픽픽에 선택지 투표 공개 댓글을 작성할 때 픽픽픽 선택지 투표 이력이 없으면 예외가 발생한다.")
    void registerPickCommentNotFoundPickMainVote() {
        // given
        // 익명회원 생성
        AnonymousMember anonymousMember = AnonymousMember.create("anonymousMemberId", "익명으로 개발하는 댑댑이");
        anonymousMemberRepository.save(anonymousMember);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(AuthenticationMemberUtils.ANONYMOUS_USER);

        // 픽픽픽 작성자 생성
        SocialMemberDto authorSocialMemberDto = createSocialDto("authorId", "author",
                nickname, password, "authorDreamy5patisiel@kakao.com", socialType, role);
        Member author = Member.createMemberBy(authorSocialMemberDto);
        memberRepository.save(author);

        // 픽픽픽 생성
        Pick pick = createPick(new Title("픽픽픽 타이틀"), ContentStatus.APPROVAL, new Count(0L), new Count(0L),
                new Count(0L), new Count(0L), author);
        pickRepository.save(pick);

        // 픽픽픽 옵션 생성
        PickOption firstPickOption = createPickOption(pick, new Title("픽픽픽 옵션1 타이틀"),
                new PickOptionContents("픽픽픽 옵션1 컨텐츠"), PickOptionType.firstPickOption);
        PickOption secondPickOption = createPickOption(pick, new Title("픽픽픽 옵션2 타이틀"),
                new PickOptionContents("픽픽픽 옵션2 컨텐츠"), PickOptionType.secondPickOption);
        pickOptionRepository.saveAll(List.of(firstPickOption, secondPickOption));

        em.flush();
        em.clear();

        PickCommentDto pickCommentDto = new PickCommentDto("안녕하세웅", true, "anonymousMemberId");

        // when // then
        assertThatThrownBy(
                () -> guestPickCommentServiceV2.registerPickComment(pick.getId(), pickCommentDto, authentication))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(INVALID_NOT_FOUND_PICK_VOTE_MESSAGE);
    }
}