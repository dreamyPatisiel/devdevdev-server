package com.dreamypatisiel.devdevdev.domain.service.pick;

import static com.dreamypatisiel.devdevdev.domain.exception.MemberExceptionMessage.INVALID_MEMBER_NOT_FOUND_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.PickExceptionMessage.INVALID_CAN_NOT_REPLY_DELETED_PICK_COMMENT_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.PickExceptionMessage.INVALID_NOT_APPROVAL_STATUS_PICK_COMMENT_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.PickExceptionMessage.INVALID_NOT_APPROVAL_STATUS_PICK_REPLY_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.PickExceptionMessage.INVALID_NOT_FOUND_PICK_COMMENT_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.PickExceptionMessage.INVALID_NOT_FOUND_PICK_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.PickExceptionMessage.INVALID_NOT_FOUND_PICK_REPLY_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.PickExceptionMessage.INVALID_NOT_FOUND_PICK_VOTE_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.service.pick.MemberPickCommentService.DELETE;
import static com.dreamypatisiel.devdevdev.domain.service.pick.MemberPickCommentService.MODIFY;
import static com.dreamypatisiel.devdevdev.domain.service.pick.MemberPickCommentService.REGISTER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amazonaws.services.s3.AmazonS3;
import com.dreamypatisiel.devdevdev.aws.s3.AwsS3Uploader;
import com.dreamypatisiel.devdevdev.aws.s3.properties.AwsS3Properties;
import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.Pick;
import com.dreamypatisiel.devdevdev.domain.entity.PickComment;
import com.dreamypatisiel.devdevdev.domain.entity.PickOption;
import com.dreamypatisiel.devdevdev.domain.entity.PickOptionImage;
import com.dreamypatisiel.devdevdev.domain.entity.PickReply;
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
import com.dreamypatisiel.devdevdev.domain.repository.member.MemberRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickCommentRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickOptionImageRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickOptionRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickReplyRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickVoteRepository;
import com.dreamypatisiel.devdevdev.domain.service.response.PickCommentResponse;
import com.dreamypatisiel.devdevdev.domain.service.response.PickReplyResponse;
import com.dreamypatisiel.devdevdev.exception.MemberException;
import com.dreamypatisiel.devdevdev.exception.NotFoundException;
import com.dreamypatisiel.devdevdev.global.common.TimeProvider;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.SocialMemberDto;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.UserPrincipal;
import com.dreamypatisiel.devdevdev.web.controller.pick.request.ModifyPickCommentRequest;
import com.dreamypatisiel.devdevdev.web.controller.pick.request.ModifyPickOptionRequest;
import com.dreamypatisiel.devdevdev.web.controller.pick.request.ModifyPickReplyRequest;
import com.dreamypatisiel.devdevdev.web.controller.pick.request.ModifyPickRequest;
import com.dreamypatisiel.devdevdev.web.controller.pick.request.RegisterPickCommentRequest;
import com.dreamypatisiel.devdevdev.web.controller.pick.request.RegisterPickOptionRequest;
import com.dreamypatisiel.devdevdev.web.controller.pick.request.RegisterPickRepliedCommentRequest;
import com.dreamypatisiel.devdevdev.web.controller.pick.request.RegisterPickReplyRequest;
import com.dreamypatisiel.devdevdev.web.controller.pick.request.RegisterPickRequest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class MemberPickCommentServiceTest {
    @Autowired
    MemberPickCommentService memberPickCommentService;
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
    PickReplyRepository pickReplyRepository;

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
    @Autowired
    private TimeProvider timeProvider;

    @Test
    @DisplayName("승인상태의 픽픽픽에 선택지 투표 공개 댓글을 작성한다.")
    void registerPickCommentWithPickMainVote() {
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

        // 픽픽픽 작성자 생성
        SocialMemberDto authorSocialMemberDto = createSocialDto("authorId", "author",
                nickname, password, "authorDreamy5patisiel@kakao.com", socialType, role);
        Member author = Member.createMemberBy(authorSocialMemberDto);
        memberRepository.save(author);

        // 픽픽픽 생성
        Pick pick = createPick(new Title("픽픽픽 타이틀"), ContentStatus.APPROVAL, author);
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
        PickVote pickVote = createPickVote(member, firstPickOption, pick);
        pickVoteRepository.save(pickVote);

        em.flush();
        em.clear();

        RegisterPickCommentRequest request = new RegisterPickCommentRequest("안녕하세웅", true);

        // when
        PickCommentResponse pickCommentResponse = memberPickCommentService.registerPickComment(pick.getId(),
                request,
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
                () -> assertThat(findPickComment.getCreatedBy().getId()).isEqualTo(member.getId()),
                () -> assertThat(findPickComment.getPickVote().getId()).isEqualTo(pickVote.getId())
        );
    }

    @Test
    @DisplayName("승인상태의 픽픽픽에 선택지 투표 비공개 댓글을 작성한다.")
    void registerPickCommentWithOutPickMainVote() {
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

        // 픽픽픽 작성자 생성
        SocialMemberDto authorSocialMemberDto = createSocialDto("authorId", "author",
                nickname, password, "authorDreamy5patisiel@kakao.com", socialType, role);
        Member author = Member.createMemberBy(authorSocialMemberDto);
        memberRepository.save(author);

        // 픽픽픽 생성
        Pick pick = createPick(new Title("픽픽픽 타이틀"), ContentStatus.APPROVAL, author);
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
        PickVote pickVote = createPickVote(member, firstPickOption, pick);
        pickVoteRepository.save(pickVote);

        em.flush();
        em.clear();

        RegisterPickCommentRequest registerPickCommentDto = new RegisterPickCommentRequest("안녕하세웅", false);

        // when
        PickCommentResponse pickCommentResponse = memberPickCommentService.registerPickComment(pick.getId(),
                registerPickCommentDto, authentication);

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
                () -> assertThat(findPickComment.getCreatedBy().getId()).isEqualTo(member.getId()),
                () -> assertThat(findPickComment.getPickVote()).isNull()
        );
    }

    @Test
    @DisplayName("픽픽픽 댓글을 작성할 때 회원이 존재하지 않으면 예외가 발생한다.")
    void registerPickCommentMemberException() {
        // given
        // 회원 생성
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // when
        RegisterPickCommentRequest request = new RegisterPickCommentRequest("안녕하세웅", true);
        // then
        assertThatThrownBy(() -> memberPickCommentService.registerPickComment(0L, request, authentication))
                .isInstanceOf(MemberException.class)
                .hasMessage(INVALID_MEMBER_NOT_FOUND_MESSAGE);
    }

    @Test
    @DisplayName("픽픽픽 댓글을 작성할 때 픽픽픽이 존재하지 않으면 예외가 발생한다.")
    void registerPickCommentPickMainNotFoundException() {
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

        // when
        RegisterPickCommentRequest registerPickCommentDto = new RegisterPickCommentRequest("안녕하세웅", true);
        // then
        assertThatThrownBy(
                () -> memberPickCommentService.registerPickComment(1L, registerPickCommentDto, authentication))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(INVALID_NOT_FOUND_PICK_MESSAGE);
    }

    @ParameterizedTest
    @EnumSource(value = ContentStatus.class, mode = Mode.EXCLUDE, names = {"APPROVAL"})
    @DisplayName("픽픽픽 댓글을 작성할 때 픽픽픽이 승인상태가 아니면 예외가 발생한다.")
    void registerPickCommentNotApproval(ContentStatus contentStatus) {
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

        // 픽픽픽 작성자 생성
        SocialMemberDto authorSocialMemberDto = createSocialDto("authorId", "author",
                nickname, password, "authorDreamy5patisiel@kakao.com", socialType, role);
        Member author = Member.createMemberBy(authorSocialMemberDto);
        memberRepository.save(author);

        // 픽픽픽 생성
        Pick pick = createPick(new Title("픽픽픽 타이틀"), contentStatus, author);
        pickRepository.save(pick);

        // 픽픽픽 옵션 생성
        PickOption firstPickOption = createPickOption(pick, new Title("픽픽픽 옵션1 타이틀"),
                new PickOptionContents("픽픽픽 옵션1 컨텐츠"), PickOptionType.firstPickOption);
        PickOption secondPickOption = createPickOption(pick, new Title("픽픽픽 옵션2 타이틀"),
                new PickOptionContents("픽픽픽 옵션2 컨텐츠"), PickOptionType.secondPickOption);
        pickOptionRepository.saveAll(List.of(firstPickOption, secondPickOption));

        em.flush();
        em.clear();

        RegisterPickCommentRequest request = new RegisterPickCommentRequest("안녕하세웅", true);

        // when // then
        assertThatThrownBy(
                () -> memberPickCommentService.registerPickComment(pick.getId(), request, authentication))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(INVALID_NOT_APPROVAL_STATUS_PICK_COMMENT_MESSAGE, REGISTER);
    }

    @Test
    @DisplayName("승인상태의 픽픽픽에 선택지 투표 공개 댓글을 작성할 때 픽픽픽 선택지 투표 이력이 없으면 예외가 발생한다.")
    void registerPickCommentNotFoundPickMainVote() {
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

        // 픽픽픽 작성자 생성
        SocialMemberDto authorSocialMemberDto = createSocialDto("authorId", "author",
                nickname, password, "authorDreamy5patisiel@kakao.com", socialType, role);
        Member author = Member.createMemberBy(authorSocialMemberDto);
        memberRepository.save(author);

        // 픽픽픽 생성
        Pick pick = createPick(new Title("픽픽픽 타이틀"), ContentStatus.APPROVAL, author);
        pickRepository.save(pick);

        // 픽픽픽 옵션 생성
        PickOption firstPickOption = createPickOption(pick, new Title("픽픽픽 옵션1 타이틀"),
                new PickOptionContents("픽픽픽 옵션1 컨텐츠"), PickOptionType.firstPickOption);
        PickOption secondPickOption = createPickOption(pick, new Title("픽픽픽 옵션2 타이틀"),
                new PickOptionContents("픽픽픽 옵션2 컨텐츠"), PickOptionType.secondPickOption);
        pickOptionRepository.saveAll(List.of(firstPickOption, secondPickOption));

        em.flush();
        em.clear();

        RegisterPickCommentRequest request = new RegisterPickCommentRequest("안녕하세웅", true);

        // when // then
        assertThatThrownBy(
                () -> memberPickCommentService.registerPickComment(pick.getId(), request, authentication))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(INVALID_NOT_FOUND_PICK_VOTE_MESSAGE);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @DisplayName("승인상태의 픽픽픽의 삭제상태가 아닌 댓글에 답글을 작성한다.")
    void registerPickRepliedComment(Boolean isPublic) {
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

        // 픽픽픽 작성자 생성
        SocialMemberDto authorSocialMemberDto = createSocialDto("authorId", "author",
                nickname, password, "authorDreamy5patisiel@kakao.com", socialType, role);
        Member author = Member.createMemberBy(authorSocialMemberDto);
        memberRepository.save(author);

        // 픽픽픽 생성
        Pick pick = createPick(new Title("픽픽픽 타이틀"), ContentStatus.APPROVAL, author);
        pickRepository.save(pick);

        // 픽픽픽 댓글 생성
        PickComment pickComment = createPickComment(new CommentContents("댓글1"), isPublic, member, pick);
        pickCommentRepository.save(pickComment);

        // 픽픽픽 답글 생성
        PickComment replidPickComment = createReplidPickComment(new CommentContents("댓글1의 답글1"), isPublic, member, pick,
                pickComment, pickComment);
        pickCommentRepository.save(replidPickComment);

        em.flush();
        em.clear();

        RegisterPickRepliedCommentRequest request = new RegisterPickRepliedCommentRequest("댓글1의 답글1의 답글");

        // when
        PickCommentResponse response = memberPickCommentService.registerPickRepliedComment(
                replidPickComment.getId(), pickComment.getId(), pick.getId(), request, authentication);

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
                () -> assertThat(findPickComment.getCreatedBy().getId()).isEqualTo(member.getId()),
                () -> assertThat(findPickComment.getParent().getId()).isEqualTo(replidPickComment.getId()),
                () -> assertThat(findPickComment.getOriginParent().getId()).isEqualTo(pickComment.getId()),
                () -> assertThat(findPickComment.getOriginParent().getReplyTotalCount().getCount()).isEqualTo(1L)
        );
    }

    @Test
    @DisplayName("픽픽픽 답글을 작성할 때 회원이 존재하지 않으면 예외가 발생한다.")
    void registerPickRepliedCommentMemberException() {
        // given
        // 회원 생성
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        RegisterPickRepliedCommentRequest request = new RegisterPickRepliedCommentRequest("댓글1의 답글1의 답글");

        // when // then
        assertThatThrownBy(
                () -> memberPickCommentService.registerPickRepliedComment(0L, 0L, 0L, request, authentication))
                .isInstanceOf(MemberException.class)
                .hasMessage(INVALID_MEMBER_NOT_FOUND_MESSAGE);
    }

    @Test
    @DisplayName("픽픽픽 답글을 작성할 때, 답글 대상의 댓글이 존재하지 않으면 예외가 발생한다.")
    void registerPickRepliedCommentNotFoundExceptionParent() {
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
        Pick pick = createPick(new Title("픽픽픽 타이틀"), ContentStatus.APPROVAL, member);
        pickRepository.save(pick);

        RegisterPickRepliedCommentRequest request = new RegisterPickRepliedCommentRequest("댓글1의 답글1의 답글");

        // when // then
        assertThatThrownBy(
                () -> memberPickCommentService.registerPickRepliedComment(0L, 0L, 0L, request, authentication))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(INVALID_NOT_FOUND_PICK_COMMENT_MESSAGE);
    }

    @ParameterizedTest
    @EnumSource(value = ContentStatus.class, mode = Mode.EXCLUDE, names = {"APPROVAL"})
    @DisplayName("픽픽픽 답글을 작성할 때, 픽픽픽이 승인상태가 아니면 예외가 발생한다.")
    void registerPickRepliedCommentPickIsNotApproval(ContentStatus contentStatus) {
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
        Pick pick = createPick(new Title("픽픽픽 타이틀"), contentStatus, member);
        pickRepository.save(pick);

        // 픽픽픽 댓글 생성
        PickComment pickComment = createPickComment(new CommentContents("댓글1"), false, member, pick);
        pickCommentRepository.save(pickComment);

        RegisterPickRepliedCommentRequest request = new RegisterPickRepliedCommentRequest("댓글1의 답글1의 답글");

        // when // then
        assertThatThrownBy(
                () -> memberPickCommentService.registerPickRepliedComment(
                        pickComment.getId(), pickComment.getId(), pick.getId(), request, authentication))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(INVALID_NOT_APPROVAL_STATUS_PICK_REPLY_MESSAGE, REGISTER);
    }

    @Test
    @DisplayName("픽픽픽 답글을 작성할 때 답글 대상의 댓글이 삭제상태이면 예외가 발생한다."
            + "(최초 댓글이 삭제상태이고 해당 댓글에 답글을 작성하는 경우)")
    void registerPickRepliedCommentDeleted() {
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
        Pick pick = createPick(new Title("픽픽픽 타이틀"), ContentStatus.APPROVAL, member);
        pickRepository.save(pick);

        // 삭제상태의 픽픽픽 댓글 생성
        PickComment pickComment = createPickComment(new CommentContents("댓글1"), false, member, pick);
        pickComment.changeDeletedAt(LocalDateTime.now(), member);
        pickCommentRepository.save(pickComment);

        RegisterPickRepliedCommentRequest request = new RegisterPickRepliedCommentRequest("댓글1의 답글1의 답글");

        // when // then
        assertThatThrownBy(
                () -> memberPickCommentService.registerPickRepliedComment(
                        pickComment.getId(), pickComment.getId(), pick.getId(), request, authentication))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(INVALID_CAN_NOT_REPLY_DELETED_PICK_COMMENT_MESSAGE, REGISTER);
    }

    @Test
    @DisplayName("픽픽픽 답글을 작성할 때 답글 대상의 댓글이 삭제상태이면 예외가 발생한다."
            + "(최초 댓글의 답글이 삭제상태이고 그 답글에 답글을 작성하는 경우)")
    void registerPickRepliedCommentRepliedDeleted() {
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
        Pick pick = createPick(new Title("픽픽픽 타이틀"), ContentStatus.APPROVAL, member);
        pickRepository.save(pick);

        // 픽픽픽 댓글 생성
        PickComment pickComment = createPickComment(new CommentContents("댓글1"), false, member, pick);
        pickCommentRepository.save(pickComment);

        // 삭제상태의 픽픽픽 댓글의 답글 생성
        PickComment replidPickComment = createReplidPickComment(new CommentContents("댓글1의 답글"), false, member, pick,
                pickComment, pickComment);
        replidPickComment.changeDeletedAt(LocalDateTime.now(), member);
        pickCommentRepository.save(replidPickComment);

        RegisterPickRepliedCommentRequest request = new RegisterPickRepliedCommentRequest("댓글1의 답글1의 답글");

        // when // then
        assertThatThrownBy(
                () -> memberPickCommentService.registerPickRepliedComment(
                        replidPickComment.getId(), pickComment.getId(), pick.getId(), request, authentication))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(INVALID_CAN_NOT_REPLY_DELETED_PICK_COMMENT_MESSAGE, REGISTER);
    }

    @Test
    @DisplayName("픽픽픽 답글을 작성할 때 답글 대상의 댓글이 존재하지 않으면 예외가 발생한다."
            + "(최초 댓글의 답글이 존재하지 않고 그 답글에 답글을 작성하는 경우)")
    void registerPickRepliedCommentRepliedNotFoundException() {
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
        Pick pick = createPick(new Title("픽픽픽 타이틀"), ContentStatus.APPROVAL, member);
        pickRepository.save(pick);

        // 픽픽픽 댓글 생성
        PickComment pickComment = createPickComment(new CommentContents("댓글1"), false, member, pick);
        pickCommentRepository.save(pickComment);

        // 삭제상태의 픽픽픽 댓글의 답글
        PickComment replidPickComment = createReplidPickComment(new CommentContents("댓글1의 답글"), false, member, pick,
                pickComment, pickComment);

        RegisterPickRepliedCommentRequest request = new RegisterPickRepliedCommentRequest("댓글1의 답글1의 답글");

        // when // then
        assertThatThrownBy(
                () -> memberPickCommentService.registerPickRepliedComment(
                        0L, pickComment.getId(), pick.getId(), request, authentication))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(INVALID_NOT_FOUND_PICK_COMMENT_MESSAGE);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @DisplayName("승인 상태이고 회원 본인이 작성한 삭제되지 않은 픽픽픽 댓글을 수정한다.")
    void modifyPickComment(boolean isPublic) {
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

        // 픽픽픽 작성자 생성
        SocialMemberDto authorSocialMemberDto = createSocialDto("authorId", "author",
                nickname, password, "authorDreamy5patisiel@kakao.com", socialType, role);
        Member author = Member.createMemberBy(authorSocialMemberDto);
        memberRepository.save(author);

        // 픽픽픽 생성
        Pick pick = createPick(new Title("픽픽픽 타이틀"), ContentStatus.APPROVAL, author);
        pickRepository.save(pick);

        // 픽픽픽 댓글 생성
        PickComment pickComment = createPickComment(new CommentContents("안녕하세웅"), isPublic, member, pick);
        pickCommentRepository.save(pickComment);

        em.flush();
        em.clear();

        ModifyPickCommentRequest request = new ModifyPickCommentRequest("주무세웅");

        // when
        PickCommentResponse response = memberPickCommentService.modifyPickComment(pickComment.getId(),
                pick.getId(), request, authentication);

        // then
        PickComment findPickComment = pickCommentRepository.findById(pickComment.getId()).get();
        assertAll(
                () -> assertThat(response.getPickCommentId()).isEqualTo(pickComment.getId()),
                () -> assertThat(findPickComment.getContents().getCommentContents()).isEqualTo(request.getContents())
        );
    }

    @Test
    @DisplayName("픽픽픽 댓글을 수정할 때 회원이 존재하지 않으면 예외가 발생한다.")
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

        // when // then
        assertThatThrownBy(() -> memberPickCommentService.modifyPickComment(0L, 0L, request,
                authentication))
                .isInstanceOf(MemberException.class)
                .hasMessage(INVALID_MEMBER_NOT_FOUND_MESSAGE);
    }

    @Test
    @DisplayName("픽픽픽 댓글을 수정할 때 댓글이 존재하지 않으면 예외가 발생한다.")
    void modifyPickCommentNotFoundPickComment() {
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

        // when // then
        assertThatThrownBy(() -> memberPickCommentService.modifyPickComment(0L, pick.getId(), request,
                authentication))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(INVALID_NOT_FOUND_PICK_COMMENT_MESSAGE);
    }

    @Test
    @DisplayName("픽픽픽 댓글을 수정할 때 본인이 작성한 댓글이 존재하지 않으면 예외가 발생한다.")
    void modifyPickCommentNotFoundPickCommentOtherMember() {
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

        // when // then
        assertThatThrownBy(() -> memberPickCommentService.modifyPickComment(pickComment.getId(), pick.getId(), request,
                authentication))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(INVALID_NOT_FOUND_PICK_COMMENT_MESSAGE);
    }

    @Test
    @DisplayName("픽픽픽 댓글을 수정할 때 댓글이 삭제 상태이면 예외가 발생한다.")
    void modifyPickCommentNotFoundPickCommentIsDeletedAt() {
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

        // 픽픽픽 작성자 생성
        SocialMemberDto authorSocialMemberDto = createSocialDto("authorId", "author",
                nickname, password, "authorDreamy5patisiel@kakao.com", socialType, role);
        Member author = Member.createMemberBy(authorSocialMemberDto);
        memberRepository.save(author);

        // 승인 상태 픽픽픽 생성
        Pick pick = createPick(new Title("픽픽픽 타이틀"), ContentStatus.APPROVAL, author);
        pickRepository.save(pick);

        // 삭제 상태의 픽픽픽 댓글 생성
        PickComment pickComment = createPickComment(new CommentContents("안녕하세웅"), false, member, pick);
        pickComment.changeDeletedAt(LocalDateTime.now(), member);
        pickCommentRepository.save(pickComment);

        em.flush();
        em.clear();

        ModifyPickCommentRequest request = new ModifyPickCommentRequest("주무세웅");

        // when // then
        assertThatThrownBy(() -> memberPickCommentService.modifyPickComment(pickComment.getId(), pick.getId(), request,
                authentication))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(INVALID_NOT_FOUND_PICK_COMMENT_MESSAGE);
    }

    @ParameterizedTest
    @EnumSource(value = ContentStatus.class, mode = Mode.EXCLUDE, names = {"APPROVAL"})
    @DisplayName("승인 상태가 아닌 픽픽픽 댓글을 수정할 때 예외가 발생한다.")
    void modifyPickCommentNotApproval(ContentStatus contentStatus) {
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

        // 픽픽픽 작성자 생성
        SocialMemberDto authorSocialMemberDto = createSocialDto("authorId", "author",
                nickname, password, "authorDreamy5patisiel@kakao.com", socialType, role);
        Member author = Member.createMemberBy(authorSocialMemberDto);
        memberRepository.save(author);

        // 승인 상태가 아닌 픽픽픽 생성
        Pick pick = createPick(new Title("픽픽픽 타이틀"), contentStatus, author);
        pickRepository.save(pick);

        // 픽픽픽 댓글 생성
        PickComment pickComment = createPickComment(new CommentContents("안녕하세웅"), false, member, pick);
        pickCommentRepository.save(pickComment);

        em.flush();
        em.clear();

        ModifyPickCommentRequest request = new ModifyPickCommentRequest("주무세웅");

        // when // then
        assertThatThrownBy(() -> memberPickCommentService.modifyPickComment(pickComment.getId(), pick.getId(), request,
                authentication))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(INVALID_NOT_APPROVAL_STATUS_PICK_COMMENT_MESSAGE, MODIFY);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @DisplayName("회원 본인이 작성한 승인 상태의 픽픽픽의 삭제상태가 아닌 댓글을 삭제한다.")
    void deletePickComment(boolean isPublic) {
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

        // 픽픽픽 작성자 생성
        SocialMemberDto authorSocialMemberDto = createSocialDto("authorId", "author",
                nickname, password, "authorDreamy5patisiel@kakao.com", socialType, role);
        Member author = Member.createMemberBy(authorSocialMemberDto);
        memberRepository.save(author);

        // 픽픽픽 생성
        Pick pick = createPick(new Title("픽픽픽 타이틀"), ContentStatus.APPROVAL, author);
        pickRepository.save(pick);

        // 픽픽픽 댓글 생성
        PickComment pickComment = createPickComment(new CommentContents("안녕하세웅"), isPublic, member, pick);
        pickCommentRepository.save(pickComment);

        em.flush();
        em.clear();

        // when
        PickCommentResponse response = memberPickCommentService.deletePickComment(pickComment.getId(),
                pick.getId(), authentication);

        // then
        PickComment findPickComment = pickCommentRepository.findById(pickComment.getId()).get();
        assertAll(
                () -> assertThat(response.getPickCommentId()).isEqualTo(pickComment.getId()),
                () -> assertThat(findPickComment.getDeletedAt()).isNotNull(),
                () -> assertThat(findPickComment.getDeletedBy().getId()).isEqualTo(member.getId())
        );
    }

    @ParameterizedTest
    @EnumSource(value = ContentStatus.class)
    @DisplayName("관리자는 모든 상태의 픽픽픽의 삭제되지 않은 댓글이 존재하면 삭제 가능하다.")
    void deletePickCommentAdmin(ContentStatus contentStatus) {
        // given
        // 관리자 회원 생성
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType,
                Role.ROLE_ADMIN.name());
        Member admin = Member.createMemberBy(socialMemberDto);
        memberRepository.save(admin);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(admin);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 픽픽픽 작성자 생성
        SocialMemberDto authorSocialMemberDto = createSocialDto("authorId", "author",
                nickname, password, "authorDreamy5patisiel@kakao.com", socialType, role);
        Member author = Member.createMemberBy(authorSocialMemberDto);
        memberRepository.save(author);

        // 픽픽픽 생성
        Pick pick = createPick(new Title("픽픽픽 타이틀"), contentStatus, author);
        pickRepository.save(pick);

        // 관리자가 작성하지 않은 픽픽픽 댓글 생성
        PickComment pickComment = createPickComment(new CommentContents("안녕하세웅"), false, author, pick);
        pickCommentRepository.save(pickComment);

        em.flush();
        em.clear();

        // when
        PickCommentResponse response = memberPickCommentService.deletePickComment(pickComment.getId(),
                pick.getId(), authentication);

        // then
        PickComment findPickComment = pickCommentRepository.findById(pickComment.getId()).get();
        assertAll(
                () -> assertThat(response.getPickCommentId()).isEqualTo(pickComment.getId()),
                () -> assertThat(findPickComment.getDeletedAt()).isNotNull(),
                () -> assertThat(findPickComment.getDeletedBy().getId()).isEqualTo(admin.getId())
        );
    }

    @Test
    @DisplayName("댓글을 삭제할 때 회원이 존재하지 않으면 예외가 발생한다.")
    void deletePickComment() {
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

        // when // then
        assertThatThrownBy(() -> memberPickCommentService.deletePickComment(0L, 0L, authentication))
                .isInstanceOf(MemberException.class)
                .hasMessage(INVALID_MEMBER_NOT_FOUND_MESSAGE);
    }

    @Test
    @DisplayName("일반회원이 픽픽픽 댓글을 삭제할 때 픽픽픽 댓글이 존재하지 않으면 예외가 발생한다.")
    void deletePickCommentNotFoundPickComment() {
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
        assertThatThrownBy(
                () -> memberPickCommentService.deletePickComment(0L, pick.getId(), authentication))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(INVALID_NOT_FOUND_PICK_COMMENT_MESSAGE);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @DisplayName("일반회원이 픽픽픽 댓글을 삭제할 때 본인이 작성한 픽픽픽 댓글이 아니면 예외가 발생한다.")
    void deletePickCommentNotFoundPickCommentByMember() {
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
        assertThatThrownBy(() -> memberPickCommentService.deletePickComment(pickComment.getId(), pick.getId(),
                authentication))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(INVALID_NOT_FOUND_PICK_COMMENT_MESSAGE);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @DisplayName("일반회원이 픽픽픽 댓글을 삭제할 때 픽픽픽이 존재하지 않으면 예외가 발생한다.")
    void deletePickCommentNotFoundPick(boolean isPublic) {
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

        // 픽픽픽 작성자 생성
        SocialMemberDto authorSocialMemberDto = createSocialDto("authorId", "author",
                nickname, password, "authorDreamy5patisiel@kakao.com", socialType, role);
        Member author = Member.createMemberBy(authorSocialMemberDto);
        memberRepository.save(author);

        // 픽픽픽 생성
        Pick pick = createPick(new Title("픽픽픽 타이틀"), ContentStatus.APPROVAL, author);
        pickRepository.save(pick);

        // 다른 회원이 직성한 픽픽픽 댓글 생성
        PickComment pickComment = createPickComment(new CommentContents("안녕하세웅"), isPublic, member, pick);
        pickCommentRepository.save(pickComment);

        em.flush();
        em.clear();

        // when // then
        assertThatThrownBy(() -> memberPickCommentService.deletePickComment(pickComment.getId(), 0L, authentication))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(INVALID_NOT_FOUND_PICK_COMMENT_MESSAGE);
    }

    @ParameterizedTest
    @EnumSource(value = ContentStatus.class, mode = Mode.EXCLUDE, names = {"APPROVAL"})
    @DisplayName("일반회원이 픽픽픽 댓글을 삭제할 때 승인상태의 픽픽픽이 존재하지 않으면 예외가 발생한다.")
    void deletePickCommentNotFoundApprovalPick(ContentStatus contentStatus) {
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

        // 픽픽픽 작성자 생성
        SocialMemberDto authorSocialMemberDto = createSocialDto("authorId", "author",
                nickname, password, "authorDreamy5patisiel@kakao.com", socialType, role);
        Member author = Member.createMemberBy(authorSocialMemberDto);
        memberRepository.save(author);

        // 픽픽픽 생성
        Pick pick = createPick(new Title("픽픽픽 타이틀"), contentStatus, author);
        pickRepository.save(pick);

        // 픽픽픽 댓글 생성
        PickComment pickComment = createPickComment(new CommentContents("안녕하세웅"), false, member, pick);
        pickCommentRepository.save(pickComment);

        em.flush();
        em.clear();

        // when // then
        assertThatThrownBy(
                () -> memberPickCommentService.deletePickComment(pickComment.getId(), pick.getId(), authentication))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(INVALID_NOT_APPROVAL_STATUS_PICK_COMMENT_MESSAGE, DELETE);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @DisplayName("일반회원이 픽픽픽 댓글을 삭제할 때 삭제 상태인 픽픽픽 댓글을 삭제하려고 하면 예외가 발생한다.")
    void deletePickCommentNotFoundPickCommentByDeletedAtIsNull(boolean isPublic) {
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

        // 픽픽픽 작성자 생성
        SocialMemberDto authorSocialMemberDto = createSocialDto("authorId", "author",
                nickname, password, "authorDreamy5patisiel@kakao.com", socialType, role);
        Member author = Member.createMemberBy(authorSocialMemberDto);
        memberRepository.save(author);

        // 픽픽픽 생성
        Pick pick = createPick(new Title("픽픽픽 타이틀"), ContentStatus.APPROVAL, author);
        pickRepository.save(pick);

        // 삭제 상태의 픽픽픽 댓글 생성
        PickComment pickComment = createPickComment(new CommentContents("안녕하세웅"), isPublic, member, pick);
        pickComment.changeDeletedAt(LocalDateTime.now(), member);
        pickCommentRepository.save(pickComment);

        em.flush();
        em.clear();

        // when // then
        assertThatThrownBy(
                () -> memberPickCommentService.deletePickComment(pickComment.getId(), pick.getId(), authentication))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(INVALID_NOT_FOUND_PICK_COMMENT_MESSAGE);
    }

    @Test
    @DisplayName("회원은 승인 상태의 픽픽픽의 삭제 상태가 아닌 댓글에 답글을 작성할 수 있다.")
    void registerPickReply() {
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

        // 픽픽픽 작성자 생성
        SocialMemberDto authorSocialMemberDto = createSocialDto("authorId", "author",
                nickname, password, "authorDreamy5patisiel@kakao.com", socialType, role);
        Member author = Member.createMemberBy(authorSocialMemberDto);
        memberRepository.save(author);

        // 픽픽픽 생성
        Pick pick = createPick(new Title("픽픽픽 타이틀"), ContentStatus.APPROVAL, author);
        pickRepository.save(pick);

        // 픽픽픽 댓글 생성
        PickComment pickComment = createPickComment(new CommentContents("안녕하세웅"), false, member, pick);
        pickCommentRepository.save(pickComment);

        RegisterPickReplyRequest request = new RegisterPickReplyRequest("안녕하세웅 답글");

        // when
        PickReplyResponse response = memberPickCommentService.registerPickReply(pickComment.getId(),
                pick.getId(), request, authentication);

        // then
        assertThat(response.getPickReplyId()).isNotNull();

        PickReply findPickReply = pickReplyRepository.findById(response.getPickReplyId()).get();
        assertAll(
                () -> assertThat(findPickReply.getContents().getCommentContents()).isEqualTo(request.getContents()),
                () -> assertThat(findPickReply.getDeletedAt()).isNull(),
                () -> assertThat(findPickReply.getBlameTotalCount().getCount()).isEqualTo(0L),
                () -> assertThat(findPickReply.getRecommendTotalCount().getCount()).isEqualTo(0L),
                () -> assertThat(findPickReply.getPickComment().getId()).isEqualTo(pickComment.getId()),
                () -> assertThat(findPickReply.getCreatedBy().getId()).isEqualTo(member.getId())
        );
    }

    @Test
    @DisplayName("픽픽픽 답글을 작성할 때 회원이 존재하지 않으면 예외가 발생한다.")
    void registerPickReplyMemberException() {
        // given
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        RegisterPickReplyRequest request = new RegisterPickReplyRequest("안녕하세웅 답글");

        PickComment mockPickComment = mock(PickComment.class);
        Pick mockPick = mock(Pick.class);

        // when
        when(mockPickComment.getId()).thenReturn(1L);
        when(mockPick.getId()).thenReturn(1L);

        // then
        assertThatThrownBy(() -> memberPickCommentService.registerPickReply(mockPickComment.getId(), mockPick.getId(),
                request, authentication))
                .isInstanceOf(MemberException.class)
                .hasMessage(INVALID_MEMBER_NOT_FOUND_MESSAGE);
    }

    @Test
    @DisplayName("픽픽픽 답글을 작성할 때 픽픽픽 댓글이 존재하지 않으면 예외가 발생한다.")
    void registerPickReplyNotFoundExceptionPickComment() {
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

        // 픽픽픽 작성자 생성
        SocialMemberDto authorSocialMemberDto = createSocialDto("authorId", "author",
                nickname, password, "authorDreamy5patisiel@kakao.com", socialType, role);
        Member author = Member.createMemberBy(authorSocialMemberDto);
        memberRepository.save(author);

        // 픽픽픽 생성
        Pick pick = createPick(new Title("픽픽픽 타이틀"), ContentStatus.APPROVAL, author);
        pickRepository.save(pick);

        RegisterPickReplyRequest request = new RegisterPickReplyRequest("안녕하세웅 답글");

        // when // then
        assertThatThrownBy(() -> memberPickCommentService.registerPickReply(0L, pick.getId(),
                request, authentication))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(INVALID_NOT_FOUND_PICK_COMMENT_MESSAGE);
    }

    @Test
    @DisplayName("픽픽픽 답글을 작성할 때 픽픽픽 댓글이 삭제 상태이면 예외가 발생한다.")
    void registerPickReplyPickCommentIsDeletedAtTrue() {
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

        // 픽픽픽 작성자 생성
        SocialMemberDto authorSocialMemberDto = createSocialDto("authorId", "author",
                nickname, password, "authorDreamy5patisiel@kakao.com", socialType, role);
        Member author = Member.createMemberBy(authorSocialMemberDto);
        memberRepository.save(author);

        // 픽픽픽 생성
        Pick pick = createPick(new Title("픽픽픽 타이틀"), ContentStatus.APPROVAL, author);
        pickRepository.save(pick);

        // 삭제된 픽픽픽 댓글 생성
        PickComment pickComment = createPickComment(new CommentContents("안녕하세웅"), false, member, pick);
        pickComment.changeDeletedAt(LocalDateTime.now(), member);
        pickCommentRepository.save(pickComment);

        RegisterPickReplyRequest request = new RegisterPickReplyRequest("안녕하세웅 답글");

        // when // then
        assertThatThrownBy(() -> memberPickCommentService.registerPickReply(pickComment.getId(), pick.getId(),
                request, authentication))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(INVALID_CAN_NOT_REPLY_DELETED_PICK_COMMENT_MESSAGE, REGISTER);
    }

    @ParameterizedTest
    @EnumSource(value = ContentStatus.class, mode = Mode.EXCLUDE, names = {"APPROVAL"})
    @DisplayName("픽픽픽 답글을 작성할 때 픽픽픽이 승인상태가 아니면 예외가 발생한다.")
    void registerPickReplyPickNotApproval(ContentStatus contentStatus) {
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

        // 픽픽픽 작성자 생성
        SocialMemberDto authorSocialMemberDto = createSocialDto("authorId", "author",
                nickname, password, "authorDreamy5patisiel@kakao.com", socialType, role);
        Member author = Member.createMemberBy(authorSocialMemberDto);
        memberRepository.save(author);

        // 승인 상태가 아닌 픽픽픽 생성
        Pick pick = createPick(new Title("픽픽픽 타이틀"), contentStatus, author);
        pickRepository.save(pick);

        // 픽픽픽 댓글 생성
        PickComment pickComment = createPickComment(new CommentContents("안녕하세웅"), false, member, pick);
        pickCommentRepository.save(pickComment);

        RegisterPickReplyRequest request = new RegisterPickReplyRequest("안녕하세웅 답글");

        // when // then
        assertThatThrownBy(() -> memberPickCommentService.registerPickReply(pickComment.getId(), pick.getId(),
                request, authentication))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(INVALID_NOT_APPROVAL_STATUS_PICK_REPLY_MESSAGE, REGISTER);
    }

    @Test
    @DisplayName("승인 상태의 픽픽픽 게시글의 댓글에 회원 본인이 작성한 삭제되지 않은 답글을 수정한다.")
    void modifyPickReply() {
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

        // 픽픽픽 작성자 생성
        SocialMemberDto authorSocialMemberDto = createSocialDto("authorId", "author",
                nickname, password, "authorDreamy5patisiel@kakao.com", socialType, role);
        Member author = Member.createMemberBy(authorSocialMemberDto);
        memberRepository.save(author);

        // 픽픽픽 생성
        Pick pick = createPick(new Title("픽픽픽 타이틀"), ContentStatus.APPROVAL, author);
        pickRepository.save(pick);

        // 픽픽픽 댓글 생성
        PickComment pickComment = createPickComment(new CommentContents("안녕하세웅 댓글"), false, member, pick);
        pickCommentRepository.save(pickComment);

        // 픽픽픽 답글 생성
        PickReply pickReply = createPickReply(new CommentContents("안녕하세웅 답글"), member, pickComment);
        pickReplyRepository.save(pickReply);

        em.flush();
        em.clear();

        ModifyPickReplyRequest request = new ModifyPickReplyRequest("안녕하세웅 수정 답글");

        // when
        PickReplyResponse response = memberPickCommentService.modifyPickReply(pickReply.getId(), pickComment.getId(),
                pick.getId(), request, authentication);

        // then
        assertThat(response.getPickReplyId()).isEqualTo(pickReply.getId());

        PickReply findPickReply = pickReplyRepository.findById(response.getPickReplyId()).get();
        assertAll(
                () -> assertThat(findPickReply.getContents().getCommentContents()).isEqualTo(request.getContents()),
                () -> assertThat(findPickReply.getPickComment().getId()).isEqualTo(pickComment.getId()),
                () -> assertThat(findPickReply.getPickComment().getPick().getId()).isEqualTo(pick.getId()),
                () -> assertThat(findPickReply.getCreatedBy().getId()).isEqualTo(member.getId())
        );
    }

    @Test
    @DisplayName("픽픽픽 답글을 수정할 때 회원이 존재하지 않으면 예외가 발생한다.")
    void modifyPickReplyMemberException() {
        // given
        // 회원 생성
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        ModifyPickReplyRequest request = new ModifyPickReplyRequest("안녕하세웅 수정 답글");

        // when
        Pick mockPick = mock(Pick.class);
        PickComment mockPickComment = mock(PickComment.class);
        PickReply mockPickReply = mock(PickReply.class);

        when(mockPick.getId()).thenReturn(1L);
        when(mockPickComment.getId()).thenReturn(1L);
        when(mockPickReply.getId()).thenReturn(1L);

        // then
        assertThatThrownBy(() -> memberPickCommentService.modifyPickReply(
                mockPickReply.getId(), mockPickComment.getId(), mockPick.getId(), request, authentication))
                .isInstanceOf(MemberException.class)
                .hasMessage(INVALID_MEMBER_NOT_FOUND_MESSAGE);
    }

    @Test
    @DisplayName("픽픽픽 답글을 수정할 때 픽픽픽이 존재하지 않으면 예외가 발생한다.")
    void modifyPickReplyNotFoundExceptionPick() {
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

        ModifyPickReplyRequest request = new ModifyPickReplyRequest("안녕하세웅 수정 답글");

        // when
        Pick mockPick = mock(Pick.class);
        PickComment mockPickComment = mock(PickComment.class);
        PickReply mockPickReply = mock(PickReply.class);

        when(mockPick.getId()).thenReturn(1L);
        when(mockPickComment.getId()).thenReturn(1L);
        when(mockPickReply.getId()).thenReturn(1L);

        // then
        assertThatThrownBy(() -> memberPickCommentService.modifyPickReply(
                mockPickReply.getId(), mockPickComment.getId(), mockPick.getId(), request, authentication))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(INVALID_NOT_FOUND_PICK_REPLY_MESSAGE);
    }

    @Test
    @DisplayName("픽픽픽 답글을 수정할 때 픽픽픽이 댓글이 존재하지 않으면 예외가 발생한다.")
    void modifyPickReplyNotFoundExceptionPickComment() {
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

        ModifyPickReplyRequest request = new ModifyPickReplyRequest("안녕하세웅 수정 답글");

        // when
        PickComment mockPickComment = mock(PickComment.class);
        PickReply mockPickReply = mock(PickReply.class);

        when(mockPickComment.getId()).thenReturn(1L);
        when(mockPickReply.getId()).thenReturn(1L);

        // then
        assertThatThrownBy(() -> memberPickCommentService.modifyPickReply(
                mockPickReply.getId(), mockPickComment.getId(), pick.getId(), request, authentication))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(INVALID_NOT_FOUND_PICK_REPLY_MESSAGE);
    }

    @Test
    @DisplayName("픽픽픽 답글을 수정할 때 픽픽픽이 답글이 존재하지 않으면 예외가 발생한다.")
    void modifyPickReplyNotFoundExceptionPickReply() {
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

        // 픽픽픽 작성자 생성
        SocialMemberDto authorSocialMemberDto = createSocialDto("authorId", "author",
                nickname, password, "authorDreamy5patisiel@kakao.com", socialType, role);
        Member author = Member.createMemberBy(authorSocialMemberDto);
        memberRepository.save(author);

        // 픽픽픽 생성
        Pick pick = createPick(new Title("픽픽픽 타이틀"), ContentStatus.APPROVAL, author);
        pickRepository.save(pick);

        // 픽픽픽 댓글 생성
        PickComment pickComment = createPickComment(new CommentContents("안녕하세웅 댓글"), false, member, pick);
        pickCommentRepository.save(pickComment);

        em.flush();
        em.clear();

        ModifyPickReplyRequest request = new ModifyPickReplyRequest("안녕하세웅 수정 답글");

        // when
        PickReply mockPickReply = mock(PickReply.class);

        when(mockPickReply.getId()).thenReturn(1L);

        // then
        assertThatThrownBy(() -> memberPickCommentService.modifyPickReply(
                mockPickReply.getId(), pickComment.getId(), pick.getId(), request, authentication))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(INVALID_NOT_FOUND_PICK_REPLY_MESSAGE);
    }

    @Test
    @DisplayName("픽픽픽 답글을 수정할 때 자신이 작성한 픽픽픽 답글이 아니면 예외가 발생한다.")
    void modifyPickReplyNotFoundExceptionCreatedBy() {
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

        // 픽픽픽 작성자 생성
        SocialMemberDto authorSocialMemberDto = createSocialDto("authorId", "author",
                nickname, password, "authorDreamy5patisiel@kakao.com", socialType, role);
        Member author = Member.createMemberBy(authorSocialMemberDto);
        memberRepository.save(author);

        // 픽픽픽 생성
        Pick pick = createPick(new Title("픽픽픽 타이틀"), ContentStatus.APPROVAL, author);
        pickRepository.save(pick);

        // 픽픽픽 댓글 생성
        PickComment pickComment = createPickComment(new CommentContents("안녕하세웅 댓글"), false, member, pick);
        pickCommentRepository.save(pickComment);

        // 픽픽픽 답글 작성 회원 생성
        SocialMemberDto otherSocialMemberDto = createSocialDto("otherMember", name, nickname, password,
                "otherMember@gmail.com", socialType, role);
        Member otherMember = Member.createMemberBy(otherSocialMemberDto);
        memberRepository.save(otherMember);

        // 픽픽픽 답글 생성
        PickReply pickReply = createPickReply(new CommentContents("안녕하세웅 답글"), otherMember, pickComment);
        pickReplyRepository.save(pickReply);

        em.flush();
        em.clear();

        ModifyPickReplyRequest request = new ModifyPickReplyRequest("안녕하세웅 수정 답글");

        // when // then
        assertThatThrownBy(() -> memberPickCommentService.modifyPickReply(
                pickReply.getId(), pickComment.getId(), pick.getId(), request, authentication))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(INVALID_NOT_FOUND_PICK_REPLY_MESSAGE);
    }

    @Test
    @DisplayName("픽픽픽 답글을 수정할 때 픽픽픽 답글이 삭제상태 이면 예외가 발생한다.")
    void modifyPickReplyNotFoundExceptionIsDeleted() {
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

        // 픽픽픽 작성자 생성
        SocialMemberDto authorSocialMemberDto = createSocialDto("authorId", "author",
                nickname, password, "authorDreamy5patisiel@kakao.com", socialType, role);
        Member author = Member.createMemberBy(authorSocialMemberDto);
        memberRepository.save(author);

        // 픽픽픽 생성
        Pick pick = createPick(new Title("픽픽픽 타이틀"), ContentStatus.APPROVAL, author);
        pickRepository.save(pick);

        // 픽픽픽 댓글 생성
        PickComment pickComment = createPickComment(new CommentContents("안녕하세웅 댓글"), false, member, pick);
        pickCommentRepository.save(pickComment);

        // 픽픽픽 답글 작성 회원 생성
        SocialMemberDto otherSocialMemberDto = createSocialDto("otherMember", name, nickname, password,
                "otherMember@gmail.com", socialType, role);
        Member otherMember = Member.createMemberBy(otherSocialMemberDto);
        memberRepository.save(otherMember);

        // 삭제 상태의 픽픽픽 답글 생성
        PickReply pickReply = createPickReply(new CommentContents("안녕하세웅 답글"), otherMember, pickComment);
        pickReply.changeDeletedAt(LocalDateTime.now(), otherMember);
        pickReplyRepository.save(pickReply);

        em.flush();
        em.clear();

        ModifyPickReplyRequest request = new ModifyPickReplyRequest("안녕하세웅 수정 답글");

        // when // then
        assertThatThrownBy(() -> memberPickCommentService.modifyPickReply(
                pickReply.getId(), pickComment.getId(), pick.getId(), request, authentication))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(INVALID_NOT_FOUND_PICK_REPLY_MESSAGE);
    }

    @ParameterizedTest
    @EnumSource(value = ContentStatus.class, mode = Mode.EXCLUDE, names = {"APPROVAL"})
    @DisplayName("픽픽픽 답글을 작성할 때 픽픽픽이 승인 상태가 아니면 예외가 발생한다.")
    void modifyPickReplyException(ContentStatus contentStatus) {
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

        // 픽픽픽 작성자 생성
        SocialMemberDto authorSocialMemberDto = createSocialDto("authorId", "author",
                nickname, password, "authorDreamy5patisiel@kakao.com", socialType, role);
        Member author = Member.createMemberBy(authorSocialMemberDto);
        memberRepository.save(author);

        // 픽픽픽 생성
        Pick pick = createPick(new Title("픽픽픽 타이틀"), contentStatus, author);
        pickRepository.save(pick);

        // 픽픽픽 댓글 생성
        PickComment pickComment = createPickComment(new CommentContents("안녕하세웅 댓글"), false, member, pick);
        pickCommentRepository.save(pickComment);

        // 픽픽픽 답글 생성
        PickReply pickReply = createPickReply(new CommentContents("안녕하세웅 답글"), member, pickComment);
        pickReplyRepository.save(pickReply);

        em.flush();
        em.clear();

        ModifyPickReplyRequest request = new ModifyPickReplyRequest("안녕하세웅 수정 답글");

        // when // then
        assertThatThrownBy(() -> memberPickCommentService.modifyPickReply(
                pickReply.getId(), pickComment.getId(), pick.getId(), request, authentication))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(INVALID_NOT_APPROVAL_STATUS_PICK_REPLY_MESSAGE, MODIFY);
    }

    @Test
    @DisplayName("회원이 승인 상태의 픽픽픽 댓글의 본인이 작성한 삭제되지 않은 답글을 삭제한다.")
    void deletePickReply() {
        // 회원 생성
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 픽픽픽 작성자 생성
        SocialMemberDto authorSocialMemberDto = createSocialDto("authorId", "author",
                nickname, password, "authorDreamy5patisiel@kakao.com", socialType, role);
        Member author = Member.createMemberBy(authorSocialMemberDto);
        memberRepository.save(author);

        // 픽픽픽 생성
        Pick pick = createPick(new Title("픽픽픽 타이틀"), ContentStatus.APPROVAL, author);
        pickRepository.save(pick);

        // 픽픽픽 댓글 생성
        PickComment pickComment = createPickComment(new CommentContents("안녕하세웅 댓글"), false, member, pick);
        pickCommentRepository.save(pickComment);

        // 픽픽픽 답글 생성
        PickReply pickReply = createPickReply(new CommentContents("안녕하세웅 답글"), member, pickComment);
        pickReplyRepository.save(pickReply);

        em.flush();
        em.clear();

        // when
        PickReplyResponse response = memberPickCommentService.deletePickReply(pickReply.getId(), pickComment.getId(),
                pick.getId(), authentication);

        // then
        assertThat(response.getPickReplyId()).isEqualTo(pickReply.getId());

        PickReply findPickReply = pickReplyRepository.findById(response.getPickReplyId()).get();
        assertAll(
                () -> assertThat(findPickReply.getDeletedAt()).isNotNull(),
                () -> assertThat(findPickReply.getDeletedBy().getId()).isEqualTo(member.getId())
        );
    }

    @ParameterizedTest
    @EnumSource(ContentStatus.class)
    @DisplayName("어드민 권한이 있는 회원은 다른 회원의 삭제상태가 아닌 픽픽픽 답글을 삭제할 수 있다.")
    void deletePickReplyByAdmin(ContentStatus contentStatus) {
        // 어드민 회원 생성
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType,
                Role.ROLE_ADMIN.name());
        Member admin = Member.createMemberBy(socialMemberDto);
        memberRepository.save(admin);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(admin);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 픽픽픽 작성자 생성
        SocialMemberDto authorSocialMemberDto = createSocialDto("authorId", "author",
                nickname, password, "authorDreamy5patisiel@kakao.com", socialType, role);
        Member author = Member.createMemberBy(authorSocialMemberDto);
        memberRepository.save(author);

        // 픽픽픽 생성
        Pick pick = createPick(new Title("픽픽픽 타이틀"), contentStatus, author);
        pickRepository.save(pick);

        // 픽픽픽 댓글 생성
        PickComment pickComment = createPickComment(new CommentContents("안녕하세웅 댓글"), false, author, pick);
        pickCommentRepository.save(pickComment);

        // 픽픽픽 답글 생성
        PickReply pickReply = createPickReply(new CommentContents("안녕하세웅 답글"), author, pickComment);
        pickReplyRepository.save(pickReply);

        em.flush();
        em.clear();

        // when
        PickReplyResponse response = memberPickCommentService.deletePickReply(pickReply.getId(), pickComment.getId(),
                pick.getId(), authentication);

        // then
        assertThat(response.getPickReplyId()).isEqualTo(pickReply.getId());

        PickReply findPickReply = pickReplyRepository.findById(response.getPickReplyId()).get();
        assertAll(
                () -> assertThat(findPickReply.getDeletedAt()).isNotNull(),
                () -> assertThat(findPickReply.getDeletedBy().getId()).isEqualTo(admin.getId())
        );
    }

    @ParameterizedTest
    @EnumSource(ContentStatus.class)
    @DisplayName("어드민 권한이 있는 회원이 픽픽픽 답글을 삭제할 때 픽픽픽 답글이 삭제상태 이면 예외가 발생한다.")
    void deletePickReplyByAdminNotFoundExceptionPickReplyIsDeleted(ContentStatus contentStatus) {
        // 어드민 회원 생성
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType,
                Role.ROLE_ADMIN.name());
        Member admin = Member.createMemberBy(socialMemberDto);
        memberRepository.save(admin);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(admin);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 픽픽픽 작성자 생성
        SocialMemberDto authorSocialMemberDto = createSocialDto("authorId", "author",
                nickname, password, "authorDreamy5patisiel@kakao.com", socialType, role);
        Member author = Member.createMemberBy(authorSocialMemberDto);
        memberRepository.save(author);

        // 픽픽픽 생성
        Pick pick = createPick(new Title("픽픽픽 타이틀"), contentStatus, author);
        pickRepository.save(pick);

        // 픽픽픽 댓글 생성
        PickComment pickComment = createPickComment(new CommentContents("안녕하세웅 댓글"), false, author, pick);
        pickCommentRepository.save(pickComment);

        // 픽픽픽 답글 생성
        PickReply pickReply = createPickReply(new CommentContents("안녕하세웅 답글"), author, pickComment);
        pickReply.changeDeletedAt(LocalDateTime.now(), author);
        pickReplyRepository.save(pickReply);

        em.flush();
        em.clear();

        // when // then
        assertThatThrownBy(() -> memberPickCommentService.deletePickReply(pickReply.getId(), pickComment.getId(),
                pick.getId(), authentication))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(INVALID_NOT_FOUND_PICK_REPLY_MESSAGE);
    }

    @ParameterizedTest
    @EnumSource(ContentStatus.class)
    @DisplayName("어드민 권한이 있는 회원이 픽픽픽 답글을 삭제할 때 픽픽픽 답글이 존재하지 않으면 예외가 발생한다.")
    void deletePickReplyByAdminNotFoundExceptionPickReply(ContentStatus contentStatus) {
        // 어드민 회원 생성
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType,
                Role.ROLE_ADMIN.name());
        Member admin = Member.createMemberBy(socialMemberDto);
        memberRepository.save(admin);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(admin);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 픽픽픽 작성자 생성
        SocialMemberDto authorSocialMemberDto = createSocialDto("authorId", "author",
                nickname, password, "authorDreamy5patisiel@kakao.com", socialType, role);
        Member author = Member.createMemberBy(authorSocialMemberDto);
        memberRepository.save(author);

        // 픽픽픽 생성
        Pick pick = createPick(new Title("픽픽픽 타이틀"), contentStatus, author);
        pickRepository.save(pick);

        // 픽픽픽 댓글 생성
        PickComment pickComment = createPickComment(new CommentContents("안녕하세웅 댓글"), false, author, pick);
        pickCommentRepository.save(pickComment);

        em.flush();
        em.clear();

        // when
        PickReply mockPickReply = mock(PickReply.class);
        when(mockPickReply.getId()).thenReturn(1L);

        // then
        assertThatThrownBy(() -> memberPickCommentService.deletePickReply(mockPickReply.getId(), pickComment.getId(),
                pick.getId(), authentication))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(INVALID_NOT_FOUND_PICK_REPLY_MESSAGE);
    }

    @Test
    @DisplayName("픽픽픽 답글을 삭제할 때 회원 본인이 작성하지 않은 답글이면 예외가 발생한다.")
    void deletePickReplyNoFoundExceptionCreatedBy() {
        // 회원 생성
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 다른 회원 생성
        SocialMemberDto authorSocialMemberDto = createSocialDto("authorId", "author",
                nickname, password, "authorDreamy5patisiel@kakao.com", socialType, role);
        Member author = Member.createMemberBy(authorSocialMemberDto);
        memberRepository.save(author);

        // 픽픽픽 생성
        Pick pick = createPick(new Title("픽픽픽 타이틀"), ContentStatus.APPROVAL, author);
        pickRepository.save(pick);

        // 픽픽픽 댓글 생성
        PickComment pickComment = createPickComment(new CommentContents("안녕하세웅 댓글"), false, author, pick);
        pickCommentRepository.save(pickComment);

        // 픽픽픽 답글 생성
        PickReply pickReply = createPickReply(new CommentContents("안녕하세웅 답글"), author, pickComment);
        pickReplyRepository.save(pickReply);

        em.flush();
        em.clear();

        // when // then
        assertThatThrownBy(() -> memberPickCommentService.deletePickReply(pickReply.getId(), pickComment.getId(),
                pick.getId(), authentication))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(INVALID_NOT_FOUND_PICK_REPLY_MESSAGE);
    }

    @Test
    @DisplayName("회원이 픽픽픽 답글을 삭제할 때 픽픽픽 답글이 삭제 상태이면 예외가 발생한다.")
    void deletePickReplyNotFoundExceptionPickReplyIsDeleted() {
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
        Pick pick = createPick(new Title("픽픽픽 타이틀"), ContentStatus.APPROVAL, member);
        pickRepository.save(pick);

        // 픽픽픽 댓글 생성
        PickComment pickComment = createPickComment(new CommentContents("안녕하세웅 댓글"), false, member, pick);
        pickCommentRepository.save(pickComment);

        // 픽픽픽 답글 생성
        PickReply pickReply = createPickReply(new CommentContents("안녕하세웅 답글"), member, pickComment);
        pickReply.changeDeletedAt(LocalDateTime.now(), member);
        pickReplyRepository.save(pickReply);

        em.flush();
        em.clear();

        // when // then
        assertThatThrownBy(() -> memberPickCommentService.deletePickReply(pickReply.getId(), pickComment.getId(),
                pick.getId(), authentication))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(INVALID_NOT_FOUND_PICK_REPLY_MESSAGE);
    }

    @Test
    @DisplayName("회원이 픽픽픽 답글을 삭제할 때 픽픽픽 답글이 존재하지 않으면 예외가 발생한다.")
    void deletePickReplyNotFoundPickReply() {
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
        Pick pick = createPick(new Title("픽픽픽 타이틀"), ContentStatus.APPROVAL, member);
        pickRepository.save(pick);

        // 픽픽픽 댓글 생성
        PickComment pickComment = createPickComment(new CommentContents("안녕하세웅 댓글"), false, member, pick);
        pickCommentRepository.save(pickComment);

        em.flush();
        em.clear();

        // when
        PickReply mockPickReply = mock(PickReply.class);
        when(mockPickReply.getId()).thenReturn(1L);

        // then
        assertThatThrownBy(() -> memberPickCommentService.deletePickReply(mockPickReply.getId(), pickComment.getId(),
                pick.getId(), authentication))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(INVALID_NOT_FOUND_PICK_REPLY_MESSAGE);
    }

    @ParameterizedTest
    @EnumSource(value = ContentStatus.class, mode = Mode.EXCLUDE, names = {"APPROVAL"})
    @DisplayName("회원이 픽픽픽 답글을 삭제할 때 픽픽픽이 승인상태가 아니면 예외가 발생한다.")
    void deletePickReplyPickIsNotApproval(ContentStatus contentStatus) {
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
        Pick pick = createPick(new Title("픽픽픽 타이틀"), contentStatus, member);
        pickRepository.save(pick);

        // 픽픽픽 최초 댓글 생성
        PickComment pickComment = createPickComment(new CommentContents("안녕하세웅 댓글"), false, member, pick);
        pickCommentRepository.save(pickComment);

        // 픽픽픽 답글 생성
        PickReply pickReply = createPickReply(new CommentContents("안녕하세웅 답글"), member, pickComment);
        pickReplyRepository.save(pickReply);

        em.flush();
        em.clear();

        // when // then
        assertThatThrownBy(() -> memberPickCommentService.deletePickReply(pickReply.getId(), pickComment.getId(),
                pick.getId(), authentication))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(INVALID_NOT_APPROVAL_STATUS_PICK_REPLY_MESSAGE, DELETE);
    }

    private PickComment createReplidPickComment(CommentContents contents, Boolean isPublic, Member member, Pick pick,
                                                PickComment originParent, PickComment parent) {
        PickComment pickComment = PickComment.builder()
                .contents(contents)
                .isPublic(isPublic)
                .createdBy(member)
                .pick(pick)
                .originParent(originParent)
                .parent(parent)
                .build();

        pickComment.changePick(pick);

        return pickComment;
    }

    private PickReply createPickReply(CommentContents commentContents, Member member, PickComment pickComment) {
        return PickReply.builder()
                .contents(commentContents)
                .createdBy(member)
                .pickComment(pickComment)
                .build();
    }

    private PickComment createPickComment(CommentContents contents, Boolean isPublic, Member member, Pick pick) {
        PickComment pickComment = PickComment.builder()
                .contents(contents)
                .isPublic(isPublic)
                .createdBy(member)
                .replyTotalCount(new Count(0))
                .pick(pick)
                .build();

        pickComment.changePick(pick);

        return pickComment;
    }

    private Pick createPick(Title title, ContentStatus contentStatus, Member member) {
        return Pick.builder()
                .title(title)
                .contentStatus(contentStatus)
                .member(member)
                .build();
    }

    private PickOption createPickOption(Title title, Count voteTotalCount, Pick pick, PickOptionType pickOptionType) {
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
                .imageUrl("imageUrl")
                .imageKey("imageKey")
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

    private PickOptionImage createPickOptionImage(String name, PickOption pickOption) {
        PickOptionImage pickOptionImage = PickOptionImage.builder()
                .name(name)
                .imageUrl("imageUrl")
                .imageKey("imageKey")
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

    private PickOption createPickOption(Pick pick, Title title, PickOptionContents pickOptionContents,
                                        PickOptionType pickOptionType) {
        PickOption pickOption = PickOption.builder()
                .title(title)
                .pickOptionType(pickOptionType)
                .contents(pickOptionContents)
                .pick(pick)
                .build();

        pickOption.changePick(pick);

        return pickOption;
    }

    private PickOption createPickOption(Pick pick, Title title, PickOptionContents pickOptionContents,
                                        Count pickOptionVoteCount) {
        PickOption pickOption = PickOption.builder()
                .title(title)
                .contents(pickOptionContents)
                .voteTotalCount(pickOptionVoteCount)
                .build();

        pickOption.changePick(pick);

        return pickOption;
    }

    private PickOption createPickOption(Title title, PickOptionContents pickOptionContents,
                                        PickOptionType pickOptionType) {
        return PickOption.builder()
                .title(title)
                .contents(pickOptionContents)
                .pickOptionType(pickOptionType)
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