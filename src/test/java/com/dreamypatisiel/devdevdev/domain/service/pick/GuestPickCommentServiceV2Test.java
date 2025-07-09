package com.dreamypatisiel.devdevdev.domain.service.pick;

import static com.dreamypatisiel.devdevdev.domain.exception.PickExceptionMessage.INVALID_CAN_NOT_REPLY_DELETED_PICK_COMMENT_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.PickExceptionMessage.INVALID_NOT_APPROVAL_STATUS_PICK_COMMENT_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.PickExceptionMessage.INVALID_NOT_APPROVAL_STATUS_PICK_REPLY_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.PickExceptionMessage.INVALID_NOT_FOUND_PICK_COMMENT_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.PickExceptionMessage.INVALID_NOT_FOUND_PICK_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.PickExceptionMessage.INVALID_NOT_FOUND_PICK_VOTE_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.service.pick.PickCommentService.DELETE;
import static com.dreamypatisiel.devdevdev.domain.service.pick.PickCommentService.MODIFY;
import static com.dreamypatisiel.devdevdev.domain.service.pick.PickCommentService.REGISTER;
import static com.dreamypatisiel.devdevdev.domain.service.pick.PickTestUtils.createPick;
import static com.dreamypatisiel.devdevdev.domain.service.pick.PickTestUtils.createPickComment;
import static com.dreamypatisiel.devdevdev.domain.service.pick.PickTestUtils.createPickCommentRecommend;
import static com.dreamypatisiel.devdevdev.domain.service.pick.PickTestUtils.createPickOption;
import static com.dreamypatisiel.devdevdev.domain.service.pick.PickTestUtils.createPickOptionImage;
import static com.dreamypatisiel.devdevdev.domain.service.pick.PickTestUtils.createPickVote;
import static com.dreamypatisiel.devdevdev.domain.service.pick.PickTestUtils.createReplidPickComment;
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
import com.dreamypatisiel.devdevdev.domain.entity.PickCommentRecommend;
import com.dreamypatisiel.devdevdev.domain.entity.PickOption;
import com.dreamypatisiel.devdevdev.domain.entity.PickOptionImage;
import com.dreamypatisiel.devdevdev.domain.entity.PickVote;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.CommentContents;
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
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickCommentSort;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickOptionImageRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickOptionRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickVoteRepository;
import com.dreamypatisiel.devdevdev.domain.service.pick.dto.PickCommentDto;
import com.dreamypatisiel.devdevdev.exception.NotFoundException;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.SocialMemberDto;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.UserPrincipal;
import com.dreamypatisiel.devdevdev.global.utils.AuthenticationMemberUtils;
import com.dreamypatisiel.devdevdev.web.dto.SliceCustom;
import com.dreamypatisiel.devdevdev.web.dto.request.pick.ModifyPickCommentRequest;
import com.dreamypatisiel.devdevdev.web.dto.request.pick.RegisterPickRepliedCommentRequest;
import com.dreamypatisiel.devdevdev.web.dto.response.pick.PickCommentResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.pick.PickCommentsResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.pick.PickRepliedCommentsResponse;
import com.dreamypatisiel.devdevdev.web.dto.util.CommentResponseUtil;
import com.dreamypatisiel.devdevdev.web.dto.util.CommonResponseUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;
import org.junit.jupiter.params.provider.ValueSource;
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

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @DisplayName("익명회원이 승인상태의 픽픽픽의 삭제상태가 아닌 댓글에 답글을 작성한다.")
    void registerPickRepliedComment(Boolean isPublic) {
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

        // 픽픽픽 댓글 생성
        PickComment pickComment = createPickComment(new CommentContents("댓글1"), isPublic, author, pick);
        pickCommentRepository.save(pickComment);

        // 픽픽픽 답글 생성
        PickComment replidPickComment = createReplidPickComment(new CommentContents("댓글1의 답글1"), anonymousMember, pick,
                pickComment, pickComment);
        pickCommentRepository.save(replidPickComment);

        em.flush();
        em.clear();

        RegisterPickRepliedCommentRequest request = new RegisterPickRepliedCommentRequest("댓글1의 답글1의 답글");
        PickCommentDto repliedCommentDto = PickCommentDto.createRepliedCommentDto(request, "anonymousMemberId");

        // when
        PickCommentResponse response = guestPickCommentServiceV2.registerPickRepliedComment(
                replidPickComment.getId(), pickComment.getId(), pick.getId(), repliedCommentDto, authentication);

        em.flush();
        em.clear();

        // then
        assertThat(response.getPickCommentId()).isNotNull();

        PickComment findPickComment = pickCommentRepository.findById(response.getPickCommentId()).get();
        assertAll(
                () -> assertThat(findPickComment.getContents().getCommentContents()).isEqualTo("댓글1의 답글1의 답글"),
                () -> assertThat(findPickComment.getIsPublic()).isFalse(),
                () -> assertThat(findPickComment.getDeletedAt()).isNull(),
                () -> assertThat(findPickComment.getBlameTotalCount().getCount()).isEqualTo(0L),
                () -> assertThat(findPickComment.getRecommendTotalCount().getCount()).isEqualTo(0L),
                () -> assertThat(findPickComment.getPick().getId()).isEqualTo(pick.getId()),
                () -> assertThat(findPickComment.getCreatedAnonymousBy().getId()).isEqualTo(anonymousMember.getId()),
                () -> assertThat(findPickComment.getParent().getId()).isEqualTo(replidPickComment.getId()),
                () -> assertThat(findPickComment.getOriginParent().getId()).isEqualTo(pickComment.getId()),
                () -> assertThat(findPickComment.getOriginParent().getReplyTotalCount().getCount()).isEqualTo(1L)
        );
    }

    @Test
    @DisplayName("회원이 익명회원 전용 픽픽픽 답글을 작성할 때 예외가 발생한다.")
    void registerPickRepliedCommentMemberException() {
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

        RegisterPickRepliedCommentRequest request = new RegisterPickRepliedCommentRequest("댓글1의 답글1의 답글");
        PickCommentDto repliedCommentDto = PickCommentDto.createRepliedCommentDto(request, "anonymousMemberId");

        // when // then
        assertThatThrownBy(
                () -> guestPickCommentServiceV2.registerPickRepliedComment(0L, 0L, 0L, repliedCommentDto, authentication))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(INVALID_METHODS_CALL_MESSAGE);
    }

    @Test
    @DisplayName("익명회원이 픽픽픽 답글을 작성할 때, 답글 대상의 댓글이 존재하지 않으면 예외가 발생한다.")
    void registerPickRepliedCommentNotFoundExceptionParent() {
        // given
        // 익명회원 생성
        AnonymousMember anonymousMember = AnonymousMember.create("anonymousMemberId", "익명으로 개발하는 댑댑이");
        anonymousMemberRepository.save(anonymousMember);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(AuthenticationMemberUtils.ANONYMOUS_USER);

        // 회원 생성
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        // 픽픽픽 생성
        Pick pick = createPick(new Title("픽픽픽 타이틀"), ContentStatus.APPROVAL, member);
        pickRepository.save(pick);

        RegisterPickRepliedCommentRequest request = new RegisterPickRepliedCommentRequest("댓글1의 답글1의 답글");
        PickCommentDto repliedCommentDto = PickCommentDto.createRepliedCommentDto(request, "anonymousMemberId");

        // when // then
        assertThatThrownBy(
                () -> guestPickCommentServiceV2.registerPickRepliedComment(0L, 0L, 0L, repliedCommentDto, authentication))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(INVALID_NOT_FOUND_PICK_COMMENT_MESSAGE);
    }

    @ParameterizedTest
    @EnumSource(value = ContentStatus.class, mode = Mode.EXCLUDE, names = {"APPROVAL"})
    @DisplayName("익명회원이 픽픽픽 답글을 작성할 때, 픽픽픽이 승인상태가 아니면 예외가 발생한다.")
    void registerPickRepliedCommentPickIsNotApproval(ContentStatus contentStatus) {
        // given
        // 익명회원 생성
        AnonymousMember anonymousMember = AnonymousMember.create("anonymousMemberId", "익명으로 개발하는 댑댑이");
        anonymousMemberRepository.save(anonymousMember);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(AuthenticationMemberUtils.ANONYMOUS_USER);

        // 회원 생성
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        // 픽픽픽 생성
        Pick pick = createPick(new Title("픽픽픽 타이틀"), contentStatus, member);
        pickRepository.save(pick);

        // 픽픽픽 댓글 생성
        PickComment pickComment = createPickComment(new CommentContents("댓글1"), false, member, pick);
        pickCommentRepository.save(pickComment);

        RegisterPickRepliedCommentRequest request = new RegisterPickRepliedCommentRequest("댓글1의 답글1의 답글");
        PickCommentDto repliedCommentDto = PickCommentDto.createRepliedCommentDto(request, "anonymousMemberId");

        // when // then
        assertThatThrownBy(
                () -> guestPickCommentServiceV2.registerPickRepliedComment(
                        pickComment.getId(), pickComment.getId(), pick.getId(), repliedCommentDto, authentication))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(INVALID_NOT_APPROVAL_STATUS_PICK_REPLY_MESSAGE, REGISTER);
    }

    @Test
    @DisplayName("익명회원이 픽픽픽 답글을 작성할 때 답글 대상의 댓글이 삭제 상태 이면 예외가 발생한다."
            + "(최초 댓글이 삭제상태이고 해당 댓글에 답글을 작성하는 경우)")
    void registerPickRepliedCommentDeleted() {
        // given
        // 익명회원 생성
        AnonymousMember anonymousMember = AnonymousMember.create("anonymousMemberId", "익명으로 개발하는 댑댑이");
        anonymousMemberRepository.save(anonymousMember);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(AuthenticationMemberUtils.ANONYMOUS_USER);

        // 회원 생성
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        // 픽픽픽 생성
        Pick pick = createPick(new Title("픽픽픽 타이틀"), ContentStatus.APPROVAL, new Count(0L), new Count(0L),
                new Count(0L), new Count(0L), member);
        pickRepository.save(pick);

        // 삭제상태의 픽픽픽 댓글 생성
        PickComment pickComment = createPickComment(new CommentContents("댓글1"), false, member, pick);
        pickComment.changeDeletedAtByMember(LocalDateTime.now(), member);
        pickCommentRepository.save(pickComment);

        RegisterPickRepliedCommentRequest request = new RegisterPickRepliedCommentRequest("댓글1의 답글1의 답글");
        PickCommentDto repliedCommentDto = PickCommentDto.createRepliedCommentDto(request, "anonymousMemberId");

        // when // then
        assertThatThrownBy(
                () -> guestPickCommentServiceV2.registerPickRepliedComment(
                        pickComment.getId(), pickComment.getId(), pick.getId(), repliedCommentDto, authentication))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(INVALID_CAN_NOT_REPLY_DELETED_PICK_COMMENT_MESSAGE, REGISTER);
    }

    @Test
    @DisplayName("익명회원이 픽픽픽 답글을 작성할 때 답글 대상의 댓글이 삭제 상태 이면 예외가 발생한다."
            + "(최초 댓글의 답글이 삭제상태이고 그 답글에 답글을 작성하는 경우)")
    void registerPickRepliedCommentRepliedDeleted() {
        // given
        // 익명회원 생성
        AnonymousMember anonymousMember = AnonymousMember.create("anonymousMemberId", "익명으로 개발하는 댑댑이");
        anonymousMemberRepository.save(anonymousMember);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(AuthenticationMemberUtils.ANONYMOUS_USER);

        // 회원 생성
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        // 픽픽픽 생성
        Pick pick = createPick(new Title("픽픽픽 타이틀"), ContentStatus.APPROVAL, new Count(0L), new Count(0L),
                new Count(0L), new Count(0L), member);
        pickRepository.save(pick);

        // 픽픽픽 댓글 생성
        PickComment pickComment = createPickComment(new CommentContents("댓글1"), false, member, pick);
        pickCommentRepository.save(pickComment);

        // 삭제상태의 픽픽픽 댓글의 답글 생성
        PickComment replidPickComment = createReplidPickComment(new CommentContents("댓글1의 답글"), member, pick,
                pickComment, pickComment);
        replidPickComment.changeDeletedAtByMember(LocalDateTime.now(), member);
        pickCommentRepository.save(replidPickComment);

        RegisterPickRepliedCommentRequest request = new RegisterPickRepliedCommentRequest("댓글1의 답글1의 답글");
        PickCommentDto repliedCommentDto = PickCommentDto.createRepliedCommentDto(request, "anonymousMemberId");

        // when // then
        assertThatThrownBy(
                () -> guestPickCommentServiceV2.registerPickRepliedComment(
                        replidPickComment.getId(), pickComment.getId(), pick.getId(), repliedCommentDto, authentication))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(INVALID_CAN_NOT_REPLY_DELETED_PICK_COMMENT_MESSAGE, REGISTER);
    }

    @Test
    @DisplayName("익명회원이 픽픽픽 답글을 작성할 때 답글 대상의 댓글이 존재하지 않으면 예외가 발생한다."
            + "(최초 댓글의 답글이 존재하지 않고 그 답글에 답글을 작성하는 경우)")
    void registerPickRepliedCommentRepliedNotFoundException() {
        // given
        // 익명회원 생성
        AnonymousMember anonymousMember = AnonymousMember.create("anonymousMemberId", "익명으로 개발하는 댑댑이");
        anonymousMemberRepository.save(anonymousMember);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(AuthenticationMemberUtils.ANONYMOUS_USER);

        // 회원 생성
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        // 픽픽픽 생성
        Pick pick = createPick(new Title("픽픽픽 타이틀"), ContentStatus.APPROVAL, member);
        pickRepository.save(pick);

        // 픽픽픽 댓글 생성
        PickComment pickComment = createPickComment(new CommentContents("댓글1"), false, member, pick);
        pickCommentRepository.save(pickComment);

        // 삭제상태의 픽픽픽 댓글의 답글(삭제 상태)
        PickComment replidPickComment = createReplidPickComment(new CommentContents("댓글1의 답글"), member, pick,
                pickComment, pickComment);
        pickCommentRepository.save(replidPickComment);
        replidPickComment.changeDeletedAtByMember(LocalDateTime.now(), member);

        RegisterPickRepliedCommentRequest request = new RegisterPickRepliedCommentRequest("댓글1의 답글1의 답글");
        PickCommentDto repliedCommentDto = PickCommentDto.createRepliedCommentDto(request, "anonymousMemberId");

        // when // then
        assertThatThrownBy(
                () -> guestPickCommentServiceV2.registerPickRepliedComment(
                        0L, pickComment.getId(), pick.getId(), repliedCommentDto, authentication))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(INVALID_NOT_FOUND_PICK_COMMENT_MESSAGE);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @DisplayName("승인 상태이고 익명회원 본인이 작성한 삭제되지 않은 픽픽픽 댓글을 수정한다.")
    void modifyPickComment(boolean isPublic) {
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
        Pick pick = createPick(new Title("픽픽픽 타이틀"), ContentStatus.APPROVAL, author);
        pickRepository.save(pick);

        // 픽픽픽 댓글 생성
        PickComment pickComment = createPickComment(new CommentContents("안녕하세웅"), isPublic, anonymousMember, pick);
        pickCommentRepository.save(pickComment);

        em.flush();
        em.clear();

        ModifyPickCommentRequest request = new ModifyPickCommentRequest("주무세웅");
        PickCommentDto modifyCommentDto = PickCommentDto.createModifyCommentDto(request, anonymousMember.getAnonymousMemberId());

        // when
        PickCommentResponse response = guestPickCommentServiceV2.modifyPickComment(pickComment.getId(),
                pick.getId(), modifyCommentDto, authentication);

        // then
        PickComment findPickComment = pickCommentRepository.findById(pickComment.getId()).get();
        assertAll(
                () -> assertThat(response.getPickCommentId()).isEqualTo(pickComment.getId()),
                () -> assertThat(findPickComment.getContents().getCommentContents()).isEqualTo(request.getContents()),
                () -> assertThat(findPickComment.getContentsLastModifiedAt()).isNotNull()
        );
    }

    @Test
    @DisplayName("익명회원이 픽픽픽 댓글을 수정할 때 익명회원 전용 메소드를 호출하지 않으면 예외가 발생한다.")
    void modifyPickCommentMemberException() {
        // given
        // 회원 생성
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        em.flush();
        em.clear();

        ModifyPickCommentRequest request = new ModifyPickCommentRequest("주무세웅");
        PickCommentDto modifyCommentDto = PickCommentDto.createModifyCommentDto(request, "anonymousMemberId");

        // when // then
        assertThatThrownBy(() -> guestPickCommentServiceV2.modifyPickComment(0L, 0L, modifyCommentDto,
                authentication))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(INVALID_METHODS_CALL_MESSAGE);
    }

    @Test
    @DisplayName("익명회원이 픽픽픽 댓글을 수정할 때 댓글이 존재하지 않으면 예외가 발생한다.")
    void modifyPickCommentNotFoundPickComment() {
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

        // 승인 상태 픽픽픽 생성
        Pick pick = createPick(new Title("픽픽픽 타이틀"), ContentStatus.APPROVAL, author);
        pickRepository.save(pick);

        em.flush();
        em.clear();

        ModifyPickCommentRequest request = new ModifyPickCommentRequest("주무세웅");
        PickCommentDto modifyCommentDto = PickCommentDto.createModifyCommentDto(request, anonymousMember.getAnonymousMemberId());

        // when // then
        assertThatThrownBy(() -> guestPickCommentServiceV2.modifyPickComment(0L, pick.getId(), modifyCommentDto,
                authentication))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(INVALID_NOT_FOUND_PICK_COMMENT_MESSAGE);
    }

    @Test
    @DisplayName("익명회원이 픽픽픽 댓글을 수정할 때 본인이 작성한 댓글이 존재하지 않으면 예외가 발생한다.")
    void modifyPickCommentNotFoundPickCommentOtherMember() {
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

        // 승인 상태 픽픽픽 생성
        Pick pick = createPick(new Title("픽픽픽 타이틀"), ContentStatus.APPROVAL, author);
        pickRepository.save(pick);

        // 픽픽픽 댓글 생성(다른 사람이 작성)
        PickComment pickComment = createPickComment(new CommentContents("안녕하세웅"), false, author, pick);
        pickCommentRepository.save(pickComment);

        em.flush();
        em.clear();

        ModifyPickCommentRequest request = new ModifyPickCommentRequest("주무세웅");
        PickCommentDto modifyCommentDto = PickCommentDto.createModifyCommentDto(request, anonymousMember.getAnonymousMemberId());

        // when // then
        assertThatThrownBy(() -> guestPickCommentServiceV2.modifyPickComment(pickComment.getId(), pick.getId(), modifyCommentDto,
                authentication))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(INVALID_NOT_FOUND_PICK_COMMENT_MESSAGE);
    }

    @Test
    @DisplayName("익명회원이 픽픽픽 댓글을 수정할 때 댓글이 삭제 상태이면 예외가 발생한다.")
    void modifyPickCommentNotFoundPickCommentIsDeletedAt() {
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

        // 승인 상태 픽픽픽 생성
        Pick pick = createPick(new Title("픽픽픽 타이틀"), ContentStatus.APPROVAL, author);
        pickRepository.save(pick);

        // 삭제 상태의 픽픽픽 댓글 생성
        PickComment pickComment = createPickComment(new CommentContents("안녕하세웅"), false, anonymousMember, pick);
        pickComment.changeDeletedAtByAnonymousMember(LocalDateTime.now(), anonymousMember);
        pickCommentRepository.save(pickComment);

        em.flush();
        em.clear();

        ModifyPickCommentRequest request = new ModifyPickCommentRequest("주무세웅");
        PickCommentDto modifyCommentDto = PickCommentDto.createModifyCommentDto(request, anonymousMember.getAnonymousMemberId());

        // when // then
        assertThatThrownBy(() -> guestPickCommentServiceV2.modifyPickComment(pickComment.getId(), pick.getId(), modifyCommentDto,
                authentication))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(INVALID_NOT_FOUND_PICK_COMMENT_MESSAGE);
    }

    @ParameterizedTest
    @EnumSource(value = ContentStatus.class, mode = Mode.EXCLUDE, names = {"APPROVAL"})
    @DisplayName("익명회원이 승인 상태가 아닌 픽픽픽 댓글을 수정할 때 예외가 발생한다.")
    void modifyPickCommentNotApproval(ContentStatus contentStatus) {
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

        // 승인 상태가 아닌 픽픽픽 생성
        Pick pick = createPick(new Title("픽픽픽 타이틀"), contentStatus, author);
        pickRepository.save(pick);

        // 픽픽픽 댓글 생성
        PickComment pickComment = createPickComment(new CommentContents("안녕하세웅"), false, anonymousMember, pick);
        pickCommentRepository.save(pickComment);

        em.flush();
        em.clear();

        ModifyPickCommentRequest request = new ModifyPickCommentRequest("주무세웅");
        PickCommentDto modifyCommentDto = PickCommentDto.createModifyCommentDto(request, anonymousMember.getAnonymousMemberId());

        // when // then
        assertThatThrownBy(() -> guestPickCommentServiceV2.modifyPickComment(pickComment.getId(), pick.getId(), modifyCommentDto,
                authentication))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(INVALID_NOT_APPROVAL_STATUS_PICK_COMMENT_MESSAGE, MODIFY);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @DisplayName("승인 상태의 픽픽픽에 포함되어 있는 삭제 상태가 아닌 댓글을 익명회원 본인이 삭제한다.")
    void deletePickComment(boolean isPublic) {
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
        Pick pick = createPick(new Title("픽픽픽 타이틀"), ContentStatus.APPROVAL, author);
        pickRepository.save(pick);

        // 픽픽픽 댓글 생성
        PickComment pickComment = createPickComment(new CommentContents("안녕하세웅"), isPublic, anonymousMember, pick);
        pickCommentRepository.save(pickComment);

        em.flush();
        em.clear();

        // when
        PickCommentResponse response = guestPickCommentServiceV2.deletePickComment(pickComment.getId(),
                pick.getId(), anonymousMember.getAnonymousMemberId(), authentication);

        // then
        PickComment findPickComment = pickCommentRepository.findById(pickComment.getId()).get();
        assertAll(
                () -> assertThat(response.getPickCommentId()).isEqualTo(pickComment.getId()),
                () -> assertThat(findPickComment.getDeletedAt()).isNotNull(),
                () -> assertThat(findPickComment.getDeletedAnonymousBy().getId()).isEqualTo(anonymousMember.getId())
        );
    }

    @Test
    @DisplayName("익명회원 전용 댓글을 삭제할 때 익명회원이 아니면 예외가 발생한다.")
    void deletePickComment() {
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

        em.flush();
        em.clear();

        // when // then
        assertThatThrownBy(() -> guestPickCommentServiceV2.deletePickComment(
                0L, 0L, "anonymousMemberId", authentication))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(INVALID_METHODS_CALL_MESSAGE);
    }

    @Test
    @DisplayName("익명회원이 픽픽픽 댓글을 삭제할 때 픽픽픽 댓글이 존재하지 않으면 예외가 발생한다.")
    void deletePickCommentNotFoundPickComment() {
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
        Pick pick = createPick(new Title("픽픽픽 타이틀"), ContentStatus.APPROVAL, author);
        pickRepository.save(pick);

        em.flush();
        em.clear();

        // when // then
        assertThatThrownBy(() -> guestPickCommentServiceV2.deletePickComment(
                0L, pick.getId(), anonymousMember.getAnonymousMemberId(), authentication))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(INVALID_NOT_FOUND_PICK_COMMENT_MESSAGE);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @DisplayName("익명회원이 픽픽픽 댓글을 삭제할 때 본인이 작성한 픽픽픽 댓글이 아니면 예외가 발생한다.")
    void deletePickCommentNotFoundPickCommentByMember() {
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
        Pick pick = createPick(new Title("픽픽픽 타이틀"), ContentStatus.APPROVAL, author);
        pickRepository.save(pick);

        // 다른 회원이 직성한 픽픽픽 댓글 생성
        PickComment pickComment = createPickComment(new CommentContents("안녕하세웅"), false, author, pick);
        pickCommentRepository.save(pickComment);

        em.flush();
        em.clear();

        // when // then
        assertThatThrownBy(() -> guestPickCommentServiceV2.deletePickComment(
                pickComment.getId(), pick.getId(), anonymousMember.getAnonymousMemberId(), authentication))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(INVALID_NOT_FOUND_PICK_COMMENT_MESSAGE);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @DisplayName("익명회원이 픽픽픽 댓글을 삭제할 때 픽픽픽이 존재하지 않으면 예외가 발생한다.")
    void deletePickCommentNotFoundPick(boolean isPublic) {
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
        Pick pick = createPick(new Title("픽픽픽 타이틀"), ContentStatus.APPROVAL, author);
        pickRepository.save(pick);

        // 다른 회원이 직성한 픽픽픽 댓글 생성
        PickComment pickComment = createPickComment(new CommentContents("안녕하세웅"), isPublic, anonymousMember, pick);
        pickCommentRepository.save(pickComment);

        em.flush();
        em.clear();

        // when // then
        assertThatThrownBy(() -> guestPickCommentServiceV2.deletePickComment(
                pickComment.getId(), 0L, anonymousMember.getAnonymousMemberId(), authentication))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(INVALID_NOT_FOUND_PICK_COMMENT_MESSAGE);
    }

    @ParameterizedTest
    @EnumSource(value = ContentStatus.class, mode = Mode.EXCLUDE, names = {"APPROVAL"})
    @DisplayName("익명회원이 픽픽픽 댓글을 삭제할 때 승인상태의 픽픽픽이 존재하지 않으면 예외가 발생한다.")
    void deletePickCommentNotFoundApprovalPick(ContentStatus contentStatus) {
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
        Pick pick = createPick(new Title("픽픽픽 타이틀"), contentStatus, author);
        pickRepository.save(pick);

        // 픽픽픽 댓글 생성
        PickComment pickComment = createPickComment(new CommentContents("안녕하세웅"), false, anonymousMember, pick);
        pickCommentRepository.save(pickComment);

        em.flush();
        em.clear();

        // when // then
        assertThatThrownBy(() -> guestPickCommentServiceV2.deletePickComment(
                pickComment.getId(), pick.getId(), anonymousMember.getAnonymousMemberId(), authentication))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(INVALID_NOT_APPROVAL_STATUS_PICK_COMMENT_MESSAGE, DELETE);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @DisplayName("익명회원이 픽픽픽 댓글을 삭제할 때 삭제 상태인 픽픽픽 댓글을 삭제하려고 하면 예외가 발생한다.")
    void deletePickCommentNotFoundPickCommentByDeletedAtIsNull(boolean isPublic) {
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
        Pick pick = createPick(new Title("픽픽픽 타이틀"), ContentStatus.APPROVAL, author);
        pickRepository.save(pick);

        // 삭제 상태의 픽픽픽 댓글 생성
        PickComment pickComment = createPickComment(new CommentContents("안녕하세웅"), isPublic, anonymousMember, pick);
        pickComment.changeDeletedAtByAnonymousMember(LocalDateTime.now(), anonymousMember);
        pickCommentRepository.save(pickComment);

        em.flush();
        em.clear();

        // when // then
        assertThatThrownBy(() -> guestPickCommentServiceV2.deletePickComment(
                pickComment.getId(), pick.getId(), anonymousMember.getAnonymousMemberId(), authentication))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(INVALID_NOT_FOUND_PICK_COMMENT_MESSAGE);
    }

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
        Member member1 = Member.createMemberBy(socialMemberDto1);
        Member member2 = Member.createMemberBy(socialMemberDto2);
        Member member3 = Member.createMemberBy(socialMemberDto3);
        Member member4 = Member.createMemberBy(socialMemberDto4);
        Member member5 = Member.createMemberBy(socialMemberDto5);
        memberRepository.saveAll(List.of(member1, member2, member3, member4, member5));

        // 익명회원 생성
        AnonymousMember anonymousMember = AnonymousMember.create("anonymousMemberId", "익명으로 개발하는 댑댑이");
        anonymousMemberRepository.save(anonymousMember);

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
        PickComment originParentPickComment5 = createPickComment(new CommentContents("익명회원이 작성한 댓글5"), false, new Count(0),
                new Count(0), anonymousMember, pick, null);
        PickComment originParentPickComment6 = createPickComment(new CommentContents("익명회원이 작성한 댓글6"), false, new Count(0),
                new Count(0), anonymousMember, pick, null);
        pickCommentRepository.saveAll(
                List.of(originParentPickComment6, originParentPickComment5, originParentPickComment4,
                        originParentPickComment3, originParentPickComment2, originParentPickComment1));

        // 픽픽픽 답글 생성
        PickComment pickReply1 = createReplidPickComment(new CommentContents("댓글1 답글1"), member1, pick,
                originParentPickComment1, originParentPickComment1);
        PickComment pickReply2 = createReplidPickComment(new CommentContents("익명회원이 작성한 답글1 답글1"), anonymousMember, pick,
                originParentPickComment1, pickReply1);
        pickReply2.changeDeletedAtByMember(LocalDateTime.now(), member1); // 삭제 상태로 변경
        PickComment pickReply3 = createReplidPickComment(new CommentContents("익명회원이 작성한 댓글2 답글1"), anonymousMember, pick,
                originParentPickComment2, originParentPickComment2);
        pickCommentRepository.saveAll(List.of(pickReply1, pickReply2, pickReply3));

        em.flush();
        em.clear();

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(AuthenticationMemberUtils.ANONYMOUS_USER);

        // when
        Pageable pageable = PageRequest.of(0, 5);
        SliceCustom<PickCommentsResponse> response = guestPickCommentServiceV2.findPickComments(pageable,
                pick.getId(), Long.MAX_VALUE, pickCommentSort, null, anonymousMember.getAnonymousMemberId(), authentication);

        // then
        // 최상위 댓글 검증
        assertThat(response).hasSize(5)
                .extracting(
                        "pickCommentId",
                        "memberId",
                        "anonymousMemberId",
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
                                null,
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
                                null,
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
                                null,
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
                                null,
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
                                null,
                                originParentPickComment5.getCreatedAnonymousBy().getId(),
                                originParentPickComment5.getCreatedAnonymousBy().getNickname(),
                                false,
                                true,
                                false,
                                null,
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
                        "anonymousMemberId",
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
                        "pickParentCommentAnonymousMemberId",
                        "pickParentCommentAuthor")
                .containsExactly(
                        Tuple.tuple(pickReply1.getId(),
                                pickReply1.getCreatedBy().getId(),
                                null,
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
                                null,
                                pickReply1.getParent().getCreatedBy().getNickname().getNickname()),

                        Tuple.tuple(pickReply2.getId(),
                                null,
                                pickReply2.getCreatedAnonymousBy().getId(),
                                pickReply2.getParent().getId(),
                                pickReply2.getOriginParent().getId(),
                                false,
                                true,
                                false,
                                pickReply2.getCreatedAnonymousBy().getNickname(),
                                null,
                                CommentResponseUtil.getCommentByPickCommentStatus(pickReply2),
                                pickReply2.getRecommendTotalCount().getCount(),
                                true,
                                false,
                                null,
                                pickReply2.getParent().getCreatedBy().getId(),
                                pickReply2.getParent().getCreatedBy().getNickname().getNickname())
                );

        // 두 번째 최상위 댓글의 답글 검증
        PickCommentsResponse pickCommentsResponse2 = response.getContent().get(1);
        List<PickRepliedCommentsResponse> replies2 = pickCommentsResponse2.getReplies();
        assertThat(replies2).hasSize(1)
                .extracting("pickCommentId",
                        "memberId",
                        "anonymousMemberId",
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
                        "pickParentCommentAnonymousMemberId",
                        "pickParentCommentAuthor")
                .containsExactly(
                        Tuple.tuple(pickReply3.getId(),
                                null,
                                pickReply3.getCreatedAnonymousBy().getId(),
                                pickReply3.getParent().getId(),
                                pickReply3.getOriginParent().getId(),
                                false,
                                true,
                                false,
                                pickReply3.getCreatedAnonymousBy().getNickname(),
                                null,
                                pickReply3.getContents().getCommentContents(),
                                pickReply3.getRecommendTotalCount().getCount(),
                                false,
                                false,
                                null,
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

        // 익명회원 생성
        AnonymousMember anonymousMember = AnonymousMember.create("anonymousMemberId", "익명으로 개발하는 댑댑이");
        anonymousMemberRepository.save(anonymousMember);

        // 픽픽픽 생성
        Pick pick = createPick(new Title("픽픽픽 타이틀"), ContentStatus.APPROVAL, new Count(9), member1);
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
                new Count(2), anonymousMember, pick, member1PickVote);
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
        PickComment pickReply1 = createReplidPickComment(new CommentContents("댓글1 답글1"), anonymousMember, pick,
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
        SliceCustom<PickCommentsResponse> response = guestPickCommentServiceV2.findPickComments(pageable,
                pick.getId(), Long.MAX_VALUE, pickCommentSort, EnumSet.of(PickOptionType.firstPickOption),
                anonymousMember.getAnonymousMemberId(), authentication);

        // then
        // 최상위 댓글 검증
        assertThat(response).hasSize(2)
                .extracting(
                        "pickCommentId",
                        "memberId",
                        "anonymousMemberId",
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
                                null,
                                originParentPickComment1.getCreatedAnonymousBy().getId(),
                                originParentPickComment1.getCreatedAnonymousBy().getNickname(),
                                false,
                                true,
                                false,
                                null,
                                originParentPickComment1.getPickVote().getPickOption().getPickOptionType(),
                                originParentPickComment1.getPickVote().getPickOption().getTitle().getTitle(),
                                originParentPickComment1.getContents().getCommentContents(),
                                originParentPickComment1.getReplyTotalCount().getCount(),
                                originParentPickComment1.getRecommendTotalCount().getCount(),
                                false,
                                true),

                        Tuple.tuple(originParentPickComment2.getId(),
                                originParentPickComment2.getCreatedBy().getId(),
                                null,
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
                        "anonymousMemberId",
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
                        "pickParentCommentAnonymousMemberId",
                        "pickParentCommentAuthor")
                .containsExactly(
                        Tuple.tuple(pickReply1.getId(),
                                null,
                                pickReply1.getCreatedAnonymousBy().getId(),
                                pickReply1.getParent().getId(),
                                pickReply1.getOriginParent().getId(),
                                false,
                                true,
                                false,
                                pickReply1.getCreatedAnonymousBy().getNickname(),
                                null,
                                pickReply1.getContents().getCommentContents(),
                                pickReply1.getRecommendTotalCount().getCount(),
                                false,
                                false,
                                null,
                                pickReply1.getParent().getCreatedAnonymousBy().getId(),
                                pickReply1.getParent().getCreatedAnonymousBy().getNickname()),

                        Tuple.tuple(pickReply2.getId(),
                                pickReply2.getCreatedBy().getId(),
                                null,
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
                                null,
                                pickReply2.getParent().getCreatedAnonymousBy().getId(),
                                pickReply2.getParent().getCreatedAnonymousBy().getNickname())
                );

        // 두 번째 최상위 댓글의 답글 검증
        PickCommentsResponse pickCommentsResponse2 = response.getContent().get(1);
        List<PickRepliedCommentsResponse> replies2 = pickCommentsResponse2.getReplies();
        assertThat(replies2).hasSize(1)
                .extracting("pickCommentId",
                        "memberId",
                        "anonymousMemberId",
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
                        "pickParentCommentAnonymousMemberId",
                        "pickParentCommentAuthor")
                .containsExactly(
                        Tuple.tuple(pickReply3.getId(),
                                pickReply3.getCreatedBy().getId(),
                                null,
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
                                null,
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

        // 익명회원 생성
        AnonymousMember anonymousMember = AnonymousMember.create("anonymousMemberId", "익명으로 개발하는 댑댑이");
        anonymousMemberRepository.save(anonymousMember);

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
                new Count(2), anonymousMember, pick, member1PickVote);
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
        SliceCustom<PickCommentsResponse> response = guestPickCommentServiceV2.findPickComments(pageable,
                pick.getId(), Long.MAX_VALUE, pickCommentSort, EnumSet.of(PickOptionType.secondPickOption),
                anonymousMember.getAnonymousMemberId(), authentication);

        // then
        // 최상위 댓글 검증
        assertThat(response).hasSize(1)
                .extracting(
                        "pickCommentId",
                        "memberId",
                        "anonymousMemberId",
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
                                null,
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
        assertThatThrownBy(() -> guestPickCommentServiceV2.findPickComments(pageable,
                1L, Long.MAX_VALUE, pickCommentSort, EnumSet.of(PickOptionType.secondPickOption), null, authentication))
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
        assertThatThrownBy(() -> guestPickCommentServiceV2.findPickBestComments(3, 1L, "anonymousMemberId", authentication))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(INVALID_METHODS_CALL_MESSAGE);
    }

    @Test
    @DisplayName("익명 회원이 offset에 정책에 맞게 픽픽픽 베스트 댓글을 조회한다.(추천수가 1개 이상인 댓글 부터 최대 3개가 조회된다.)")
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

        // 익명회원 생성
        AnonymousMember anonymousMember = AnonymousMember.create("anonymousMemberId", "익명으로 개발하는 댑댑이");
        anonymousMemberRepository.save(anonymousMember);

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
                new Count(3), anonymousMember, pick, member1PickVote);
        originParentPickComment1.modifyCommentContents(new CommentContents("수정된 댓글1"), LocalDateTime.now());
        PickComment originParentPickComment2 = createPickComment(new CommentContents("댓글2"), true, new Count(1),
                new Count(2), member2, pick, member2PickVote);
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
        PickComment pickReply1 = createReplidPickComment(new CommentContents("댓글1 답글1"), anonymousMember, pick,
                originParentPickComment1, originParentPickComment1);
        PickComment pickReply2 = createReplidPickComment(new CommentContents("답글1 답글1"), member6, pick,
                originParentPickComment1, pickReply1);
        pickReply2.changeDeletedAtByMember(LocalDateTime.now(), member1);
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

        List<PickCommentsResponse> response = guestPickCommentServiceV2.findPickBestComments(3, pick.getId(),
                anonymousMember.getAnonymousMemberId(), authentication);

        // then
        // 최상위 댓글 검증
        assertThat(response).hasSize(2)
                .extracting(
                        "pickCommentId",
                        "memberId",
                        "anonymousMemberId",
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
                                null,
                                originParentPickComment1.getCreatedAnonymousBy().getId(),
                                originParentPickComment1.getCreatedAnonymousBy().getNickname(),
                                false,
                                true,
                                false,
                                null,
                                originParentPickComment1.getPickVote().getPickOption().getPickOptionType(),
                                originParentPickComment1.getPickVote().getPickOption().getTitle().getTitle(),
                                originParentPickComment1.getContents().getCommentContents(),
                                originParentPickComment1.getReplyTotalCount().getCount(),
                                originParentPickComment1.getRecommendTotalCount().getCount(),
                                false,
                                true),

                        Tuple.tuple(originParentPickComment2.getId(),
                                originParentPickComment2.getCreatedBy().getId(),
                                null,
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
        PickCommentsResponse pickCommentsResponse1 = response.get(0);
        List<PickRepliedCommentsResponse> replies1 = pickCommentsResponse1.getReplies();
        assertThat(replies1).hasSize(2)
                .extracting("pickCommentId",
                        "memberId",
                        "anonymousMemberId",
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
                        "pickParentCommentAnonymousMemberId",
                        "pickParentCommentAuthor")
                .containsExactly(
                        Tuple.tuple(pickReply1.getId(),
                                null,
                                pickReply1.getCreatedAnonymousBy().getId(),
                                pickReply1.getParent().getId(),
                                pickReply1.getOriginParent().getId(),
                                false,
                                true,
                                false,
                                pickReply1.getCreatedAnonymousBy().getNickname(),
                                null,
                                pickReply1.getContents().getCommentContents(),
                                pickReply1.getRecommendTotalCount().getCount(),
                                false,
                                false,
                                null,
                                pickReply1.getParent().getCreatedAnonymousBy().getId(),
                                pickReply1.getParent().getCreatedAnonymousBy().getNickname()),

                        Tuple.tuple(pickReply2.getId(),
                                pickReply2.getCreatedBy().getId(),
                                null,
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
                                null,
                                pickReply1.getParent().getCreatedAnonymousBy().getId(),
                                pickReply1.getParent().getCreatedAnonymousBy().getNickname())
                );

        // 두 번째 최상위 댓글의 답글 검증
        PickCommentsResponse pickCommentsResponse2 = response.get(1);
        List<PickRepliedCommentsResponse> replies2 = pickCommentsResponse2.getReplies();
        assertThat(replies2).hasSize(1)
                .extracting("pickCommentId",
                        "memberId",
                        "anonymousMemberId",
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
                        "pickParentCommentAnonymousMemberId",
                        "pickParentCommentAuthor")
                .containsExactly(
                        Tuple.tuple(pickReply3.getId(),
                                pickReply3.getCreatedBy().getId(),
                                null,
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
                                null,
                                pickReply3.getParent().getCreatedBy().getNicknameAsString())
                );
    }
}