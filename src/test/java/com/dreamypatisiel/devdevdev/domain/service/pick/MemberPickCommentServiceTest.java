package com.dreamypatisiel.devdevdev.domain.service.pick;

import static com.dreamypatisiel.devdevdev.domain.exception.MemberExceptionMessage.INVALID_MEMBER_NOT_FOUND_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.PickExceptionMessage.INVALID_CAN_NOT_ACTION_DELETED_PICK_COMMENT_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.PickExceptionMessage.INVALID_CAN_NOT_REPLY_DELETED_PICK_COMMENT_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.PickExceptionMessage.INVALID_NOT_APPROVAL_STATUS_PICK_COMMENT_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.PickExceptionMessage.INVALID_NOT_APPROVAL_STATUS_PICK_REPLY_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.PickExceptionMessage.INVALID_NOT_FOUND_PICK_COMMENT_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.PickExceptionMessage.INVALID_NOT_FOUND_PICK_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.PickExceptionMessage.INVALID_NOT_FOUND_PICK_VOTE_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.service.pick.MemberPickCommentService.DELETE;
import static com.dreamypatisiel.devdevdev.domain.service.pick.MemberPickCommentService.MODIFY;
import static com.dreamypatisiel.devdevdev.domain.service.pick.MemberPickCommentService.RECOMMEND;
import static com.dreamypatisiel.devdevdev.domain.service.pick.MemberPickCommentService.REGISTER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.amazonaws.services.s3.AmazonS3;
import com.dreamypatisiel.devdevdev.aws.s3.AwsS3Uploader;
import com.dreamypatisiel.devdevdev.aws.s3.properties.AwsS3Properties;
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
import com.dreamypatisiel.devdevdev.domain.repository.member.MemberRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickCommentRecommendRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickCommentRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickCommentSort;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickOptionImageRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickOptionRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickVoteRepository;
import com.dreamypatisiel.devdevdev.exception.MemberException;
import com.dreamypatisiel.devdevdev.exception.NotFoundException;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.SocialMemberDto;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.UserPrincipal;
import com.dreamypatisiel.devdevdev.web.dto.SliceCustom;
import com.dreamypatisiel.devdevdev.web.dto.request.pick.ModifyPickCommentRequest;
import com.dreamypatisiel.devdevdev.web.dto.request.pick.ModifyPickOptionRequest;
import com.dreamypatisiel.devdevdev.web.dto.request.pick.ModifyPickRequest;
import com.dreamypatisiel.devdevdev.web.dto.request.pick.RegisterPickCommentRequest;
import com.dreamypatisiel.devdevdev.web.dto.request.pick.RegisterPickOptionRequest;
import com.dreamypatisiel.devdevdev.web.dto.request.pick.RegisterPickRepliedCommentRequest;
import com.dreamypatisiel.devdevdev.web.dto.request.pick.RegisterPickRequest;
import com.dreamypatisiel.devdevdev.web.dto.response.pick.PickCommentRecommendResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.pick.PickCommentResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.pick.PickCommentsResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.pick.PickRepliedCommentsResponse;
import com.dreamypatisiel.devdevdev.web.dto.util.CommentResponseUtil;
import com.dreamypatisiel.devdevdev.web.dto.util.CommonResponseUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
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
    PickCommentRecommendRepository pickCommentRecommendRepository;

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
        Pick pick = createPick(new Title("픽픽픽 타이틀"), ContentStatus.APPROVAL, new Count(0L), new Count(0L),
                new Count(0L), new Count(0L), author);
        pickRepository.save(pick);

        // 픽픽픽 댓글 생성
        PickComment pickComment = createPickComment(new CommentContents("댓글1"), isPublic, member, pick);
        pickCommentRepository.save(pickComment);

        // 픽픽픽 답글 생성
        PickComment replidPickComment = createReplidPickComment(new CommentContents("댓글1의 답글1"), member, pick,
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
        Pick pick = createPick(new Title("픽픽픽 타이틀"), ContentStatus.APPROVAL, new Count(0L), new Count(0L),
                new Count(0L), new Count(0L), member);
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
        Pick pick = createPick(new Title("픽픽픽 타이틀"), ContentStatus.APPROVAL, new Count(0L), new Count(0L),
                new Count(0L), new Count(0L), member);
        pickRepository.save(pick);

        // 픽픽픽 댓글 생성
        PickComment pickComment = createPickComment(new CommentContents("댓글1"), false, member, pick);
        pickCommentRepository.save(pickComment);

        // 삭제상태의 픽픽픽 댓글의 답글 생성
        PickComment replidPickComment = createReplidPickComment(new CommentContents("댓글1의 답글"), member, pick,
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
        PickComment replidPickComment = createReplidPickComment(new CommentContents("댓글1의 답글"), member, pick,
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
                () -> assertThat(findPickComment.getContents().getCommentContents()).isEqualTo(request.getContents()),
                () -> assertThat(findPickComment.getContentsLastModifiedAt()).isNotNull()
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

    @ParameterizedTest
    @EnumSource(PickCommentSort.class)
    @DisplayName("픽픽픽 모든 댓글/답글을 알맞게 정렬하여 커서 방식으로 조회한다.")
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
        SocialMemberDto socialMemberDto6 = createSocialDto("user6", name, "nickname6", password, "user6@gmail.com",
                socialType, role);
        Member member1 = Member.createMemberBy(socialMemberDto1);
        Member member2 = Member.createMemberBy(socialMemberDto2);
        Member member3 = Member.createMemberBy(socialMemberDto3);
        Member member4 = Member.createMemberBy(socialMemberDto4);
        Member member5 = Member.createMemberBy(socialMemberDto5);
        Member member6 = Member.createMemberBy(socialMemberDto6);
        memberRepository.saveAll(List.of(member1, member2, member3, member4, member5, member6));

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member1);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

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
        originParentPickComment1.modifyCommentContents(new CommentContents("수정된 댓글1"), LocalDateTime.now());
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
        pickReply2.changeDeletedAt(LocalDateTime.now(), member1);
        PickComment pickReply3 = createReplidPickComment(new CommentContents("댓글2 답글1"), member6, pick,
                originParentPickComment2, originParentPickComment2);
        pickCommentRepository.saveAll(List.of(pickReply1, pickReply2, pickReply3));

        // 추천 생성
        PickCommentRecommend pickCommentRecommend = createPickCommentRecommend(originParentPickComment1, member1, true);
        pickCommentRecommendRepository.save(pickCommentRecommend);

        em.flush();
        em.clear();

        // when
        Pageable pageable = PageRequest.of(0, 5);
        SliceCustom<PickCommentsResponse> response = memberPickCommentService.findPickComments(pageable,
                pick.getId(), Long.MAX_VALUE, pickCommentSort, null, authentication);

        // then
        // 최상위 댓글 검증
        assertThat(response).hasSize(5)
                .extracting(
                        "pickCommentId",
                        "memberId",
                        "author",
                        "isCommentOfPickAuthor",
                        "isCommentAuthor",
                        "isRecommended",
                        "maskedEmail",
                        "votedPickOption",
                        "votedPickOptionTitle",
                        "contents",
                        "replyTotalCount",
                        "likeTotalCount",
                        "isDeleted",
                        "isModified")
                .containsExactly(
                        Tuple.tuple(originParentPickComment1.getId(),
                                originParentPickComment1.getCreatedBy().getId(),
                                originParentPickComment1.getCreatedBy().getNickname().getNickname(),
                                true,
                                true,
                                true,
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
                                originParentPickComment5.getCreatedBy().getId(),
                                originParentPickComment5.getCreatedBy().getNickname().getNickname(),
                                false,
                                false,
                                false,
                                CommonResponseUtil.sliceAndMaskEmail(
                                        originParentPickComment5.getCreatedBy().getEmail().getEmail()),
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
                        "pickParentCommentId",
                        "pickOriginParentCommentId",
                        "isCommentOfPickAuthor",
                        "isCommentAuthor",
                        "isRecommended",
                        "author",
                        "maskedEmail",
                        "contents",
                        "likeTotalCount",
                        "isDeleted",
                        "isModified",
                        "parentCommentMemberId",
                        "parentCommentAuthor")
                .containsExactly(
                        Tuple.tuple(pickReply1.getId(), pickReply1.getCreatedBy().getId(),
                                pickReply1.getParent().getId(),
                                pickReply1.getOriginParent().getId(),
                                true,
                                true,
                                false,
                                pickReply1.getCreatedBy().getNickname().getNickname(),
                                CommonResponseUtil.sliceAndMaskEmail(pickReply1.getCreatedBy().getEmail().getEmail()),
                                pickReply1.getContents().getCommentContents(),
                                pickReply1.getRecommendTotalCount().getCount(),
                                false,
                                false,
                                pickReply1.getParent().getCreatedBy().getId(),
                                pickReply1.getParent().getCreatedBy().getNickname().getNickname()),

                        Tuple.tuple(pickReply2.getId(), pickReply2.getCreatedBy().getId(),
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
                                pickReply2.getParent().getCreatedBy().getId(),
                                pickReply2.getParent().getCreatedBy().getNickname().getNickname())
                );

        // 두 번째 최상위 댓글의 답글 검증
        PickCommentsResponse pickCommentsResponse2 = response.getContent().get(1);
        List<PickRepliedCommentsResponse> replies2 = pickCommentsResponse2.getReplies();
        assertThat(replies2).hasSize(1)
                .extracting("pickCommentId",
                        "memberId",
                        "pickParentCommentId",
                        "pickOriginParentCommentId",
                        "isCommentOfPickAuthor",
                        "isCommentAuthor",
                        "isRecommended",
                        "author",
                        "maskedEmail",
                        "contents",
                        "likeTotalCount",
                        "isDeleted",
                        "isModified",
                        "parentCommentMemberId",
                        "parentCommentAuthor")
                .containsExactly(
                        Tuple.tuple(pickReply3.getId(),
                                pickReply3.getCreatedBy().getId(),
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
    @DisplayName("픽픽픽 모든 첫 번째 픽픽픽 옵션에 투표한 댓글/답글을 알맞게 정렬하여 커서 방식으로 조회한다.")
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

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member1);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

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
        originParentPickComment1.modifyCommentContents(new CommentContents("수정된 댓글1"), LocalDateTime.now());
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
        pickCommentRepository.saveAll(List.of(pickReply1, pickReply2, pickReply3));

        // 추천 생성
        PickCommentRecommend pickCommentRecommend = createPickCommentRecommend(originParentPickComment1, member1, true);
        pickCommentRecommendRepository.save(pickCommentRecommend);

        em.flush();
        em.clear();

        // when
        Pageable pageable = PageRequest.of(0, 5);
        SliceCustom<PickCommentsResponse> response = memberPickCommentService.findPickComments(pageable,
                pick.getId(), Long.MAX_VALUE, pickCommentSort, PickOptionType.firstPickOption, authentication);

        // then
        // 최상위 댓글 검증
        assertThat(response).hasSize(2)
                .extracting(
                        "pickCommentId",
                        "memberId",
                        "author",
                        "isCommentOfPickAuthor",
                        "isCommentAuthor",
                        "isCommentAuthor",
                        "maskedEmail",
                        "votedPickOption",
                        "votedPickOptionTitle",
                        "contents",
                        "replyTotalCount",
                        "likeTotalCount",
                        "isDeleted",
                        "isModified")
                .containsExactly(
                        Tuple.tuple(originParentPickComment1.getId(),
                                originParentPickComment1.getCreatedBy().getId(),
                                originParentPickComment1.getCreatedBy().getNickname().getNickname(),
                                true,
                                true,
                                true,
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
                        "pickParentCommentId",
                        "pickOriginParentCommentId",
                        "isCommentOfPickAuthor",
                        "isCommentAuthor",
                        "isRecommended",
                        "author",
                        "maskedEmail",
                        "contents",
                        "likeTotalCount",
                        "isDeleted",
                        "isModified",
                        "parentCommentMemberId",
                        "parentCommentAuthor")
                .containsExactly(
                        Tuple.tuple(pickReply1.getId(), pickReply1.getCreatedBy().getId(),
                                pickReply1.getParent().getId(),
                                pickReply1.getOriginParent().getId(),
                                true,
                                true,
                                false,
                                pickReply1.getCreatedBy().getNickname().getNickname(),
                                CommonResponseUtil.sliceAndMaskEmail(pickReply1.getCreatedBy().getEmail().getEmail()),
                                pickReply1.getContents().getCommentContents(),
                                pickReply1.getRecommendTotalCount().getCount(),
                                false,
                                false,
                                pickReply1.getParent().getCreatedBy().getId(),
                                pickReply1.getParent().getCreatedBy().getNickname().getNickname()),

                        Tuple.tuple(pickReply2.getId(), pickReply2.getCreatedBy().getId(),
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
                                pickReply2.getParent().getCreatedBy().getId(),
                                pickReply2.getParent().getCreatedBy().getNickname().getNickname())
                );

        // 두 번째 최상위 댓글의 답글 검증
        PickCommentsResponse pickCommentsResponse2 = response.getContent().get(1);
        List<PickRepliedCommentsResponse> replies2 = pickCommentsResponse2.getReplies();
        assertThat(replies2).hasSize(1)
                .extracting("pickCommentId",
                        "memberId",
                        "pickParentCommentId",
                        "pickOriginParentCommentId",
                        "isCommentOfPickAuthor",
                        "isCommentAuthor",
                        "isRecommended",
                        "author",
                        "maskedEmail",
                        "contents",
                        "likeTotalCount",
                        "isDeleted",
                        "isModified",
                        "parentCommentMemberId",
                        "parentCommentAuthor")
                .containsExactly(
                        Tuple.tuple(pickReply3.getId(),
                                pickReply3.getCreatedBy().getId(),
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
                                pickReply3.getParent().getCreatedBy().getNickname().getNickname())
                );
    }

    @ParameterizedTest
    @EnumSource(PickCommentSort.class)
    @DisplayName("픽픽픽 모든 두 번째 픽픽픽 옵션에 투표한 댓글/답글을 알맞게 정렬하여 커서 방식으로 조회한다.")
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

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member1);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

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
        originParentPickComment1.modifyCommentContents(new CommentContents("수정된 댓글1"), LocalDateTime.now());
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

        // 추천 생성
        PickCommentRecommend pickCommentRecommend = createPickCommentRecommend(originParentPickComment1, member1, true);
        pickCommentRecommendRepository.save(pickCommentRecommend);

        em.flush();
        em.clear();

        // when
        Pageable pageable = PageRequest.of(0, 5);
        SliceCustom<PickCommentsResponse> response = memberPickCommentService.findPickComments(pageable,
                pick.getId(), Long.MAX_VALUE, pickCommentSort, PickOptionType.secondPickOption, authentication);

        // then
        // 최상위 댓글 검증
        assertThat(response).hasSize(1)
                .extracting(
                        "pickCommentId",
                        "memberId",
                        "author",
                        "isCommentOfPickAuthor",
                        "isCommentAuthor",
                        "isRecommended",
                        "maskedEmail",
                        "votedPickOption",
                        "votedPickOptionTitle",
                        "contents",
                        "replyTotalCount",
                        "likeTotalCount",
                        "isDeleted",
                        "isModified")
                .containsExactly(
                        Tuple.tuple(originParentPickComment3.getId(),
                                originParentPickComment3.getCreatedBy().getId(),
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

    @Test
    @DisplayName("회원이 승인상태 픽픽픽의 삭제되지 않은 댓글/답글에 추천한다.")
    void recommendPickCommentIsTrue() {
        // given
        // 회원 생성
        SocialMemberDto socialMemberDto = createSocialDto("user1", name, "nickname1", password, "user1@gmail.com",
                socialType, role);
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
        PickComment pickComment = createPickComment(new CommentContents("픽픽픽 댓글"), true, new Count(0), member, pick);
        pickCommentRepository.save(pickComment);

        em.flush();
        em.clear();

        // when
        PickCommentRecommendResponse response = memberPickCommentService.recommendPickComment(
                pick.getId(), pickComment.getId(), authentication);

        em.flush();
        em.clear();

        // then
        assertAll(
                () -> assertThat(response.getRecommendStatus()).isTrue(),
                () -> assertThat(response.getRecommendTotalCount()).isEqualTo(1L)
        );
    }

    @Test
    @DisplayName("회원이 이미 픽픽픽 댓글/답글을 추천한 상태일 때 추천하게 되면 추천을 취소한다.")
    void recommendPickCommentIsFalse() {
        // given
        // 회원 생성
        SocialMemberDto socialMemberDto = createSocialDto("user1", name, "nickname1", password, "user1@gmail.com",
                socialType, role);
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
        PickComment pickComment = createPickComment(new CommentContents("픽픽픽 댓글"), true, new Count(1), member, pick);
        pickCommentRepository.save(pickComment);

        // 픽픽픽 댓글 추천 생성
        PickCommentRecommend pickCommentRecommend = createPickCommentRecommend(pickComment, member, true);
        pickCommentRecommendRepository.save(pickCommentRecommend);

        em.flush();
        em.clear();

        // when
        PickCommentRecommendResponse response = memberPickCommentService.recommendPickComment(
                pick.getId(), pickComment.getId(), authentication);

        em.flush();
        em.clear();

        // then
        assertAll(
                () -> assertThat(response.getRecommendStatus()).isFalse(),
                () -> assertThat(response.getRecommendTotalCount()).isEqualTo(0L)
        );
    }

    @Test
    @DisplayName("회원이 이미 픽픽픽 댓글/답글을 추천 취소 상태일 때 추천하게 되면 추천한다.")
    void recommendPickCommentIsTrueAlreadyFalse() {
        // given
        // 회원 생성
        SocialMemberDto socialMemberDto = createSocialDto("user1", name, "nickname1", password, "user1@gmail.com",
                socialType, role);
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
        PickComment pickComment = createPickComment(new CommentContents("픽픽픽 댓글"), true, new Count(0), member, pick);
        pickCommentRepository.save(pickComment);

        // 픽픽픽 댓글 추천 생성
        PickCommentRecommend pickCommentRecommend = createPickCommentRecommend(pickComment, member, false);
        pickCommentRecommendRepository.save(pickCommentRecommend);

        em.flush();
        em.clear();

        // when
        PickCommentRecommendResponse response = memberPickCommentService.recommendPickComment(
                pick.getId(), pickComment.getId(), authentication);

        em.flush();
        em.clear();

        // then
        assertAll(
                () -> assertThat(response.getRecommendStatus()).isTrue(),
                () -> assertThat(response.getRecommendTotalCount()).isEqualTo(1L)
        );
    }

    @Test
    @DisplayName("픽픽픽 댓글을 추천할 때 회원이 존재하지 않으면 예외가 발생한다.")
    void recommendPickCommentMemberException() {
        // given
        SocialMemberDto socialMemberDto = createSocialDto("user1", name, "nickname1", password, "user1@gmail.com",
                socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // when // then
        assertThatThrownBy(() -> memberPickCommentService.recommendPickComment(1L, 1L, authentication))
                .isInstanceOf(MemberException.class)
                .hasMessage(INVALID_MEMBER_NOT_FOUND_MESSAGE);
    }

    @Test
    @DisplayName("픽픽픽 댓글을 추천할 때 댓글이 존재하지 않으면 예외가 발생한다.")
    void recommendPickCommentNotFoundException() {
        // given
        // 회원 생성
        SocialMemberDto socialMemberDto = createSocialDto("user1", name, "nickname1", password, "user1@gmail.com",
                socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // when // then
        assertThatThrownBy(() -> memberPickCommentService.recommendPickComment(0L, 0L, authentication))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(INVALID_NOT_FOUND_PICK_COMMENT_MESSAGE);
    }

    @ParameterizedTest
    @EnumSource(value = ContentStatus.class, mode = Mode.EXCLUDE, names = {"APPROVAL"})
    @DisplayName("픽픽픽 댓글을 추천할 때 픽픽픽이 승인상태가 아니면 예외가 발생한다.")
    void recommendPickCommentPickIsNotApproval(ContentStatus contentStatus) {
        // given
        // 회원 생성
        SocialMemberDto socialMemberDto = createSocialDto("user1", name, "nickname1", password, "user1@gmail.com",
                socialType, role);
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
        PickComment pickComment = createPickComment(new CommentContents("픽픽픽 댓글"), true, member, pick);
        pickCommentRepository.save(pickComment);

        // when // then
        assertThatThrownBy(
                () -> memberPickCommentService.recommendPickComment(pick.getId(), pickComment.getId(), authentication))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(INVALID_NOT_APPROVAL_STATUS_PICK_COMMENT_MESSAGE, RECOMMEND);
    }

    @Test
    @DisplayName("픽픽픽 댓글을 추천할 때 댓글이 삭제상태이면 예외가 발생한다.")
    void recommendPickCommentIsDeleted() {
        // given
        // 회원 생성
        SocialMemberDto socialMemberDto = createSocialDto("user1", name, "nickname1", password, "user1@gmail.com",
                socialType, role);
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
        PickComment pickComment = createPickComment(new CommentContents("픽픽픽 댓글"), true, member, pick);
        pickComment.changeDeletedAt(LocalDateTime.now(), member);
        pickCommentRepository.save(pickComment);

        // when // then
        assertThatThrownBy(
                () -> memberPickCommentService.recommendPickComment(pick.getId(), pickComment.getId(), authentication))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(INVALID_CAN_NOT_ACTION_DELETED_PICK_COMMENT_MESSAGE, RECOMMEND);
    }

    private Pick createPick(Title title, ContentStatus contentStatus, Count viewTotalCount, Count voteTotalCount,
                            Count commentTotalCount, Count popularScore, Member member) {
        return Pick.builder()
                .title(title)
                .contentStatus(contentStatus)
                .viewTotalCount(viewTotalCount)
                .voteTotalCount(voteTotalCount)
                .commentTotalCount(commentTotalCount)
                .popularScore(popularScore)
                .member(member)
                .build();
    }

    private PickComment createPickComment(CommentContents contents, Boolean isPublic, Count recommendTotalCount,
                                          Member member, Pick pick) {
        PickComment pickComment = PickComment.builder()
                .contents(contents)
                .isPublic(isPublic)
                .createdBy(member)
                .recommendTotalCount(recommendTotalCount)
                .pick(pick)
                .build();

        pickComment.changePick(pick);

        return pickComment;
    }

    private PickCommentRecommend createPickCommentRecommend(PickComment pickComment, Member member,
                                                            Boolean recommendedStatus) {
        PickCommentRecommend pickCommentRecommend = PickCommentRecommend.builder()
                .member(member)
                .recommendedStatus(recommendedStatus)
                .build();

        pickCommentRecommend.changePickComment(pickComment);

        return pickCommentRecommend;
    }

    private Pick createPick(Title title, ContentStatus contentStatus, Count commentTotalCount, Member member) {
        return Pick.builder()
                .title(title)
                .contentStatus(contentStatus)
                .commentTotalCount(commentTotalCount)
                .member(member)
                .build();
    }

    private PickComment createPickComment(CommentContents contents, Boolean isPublic, Count replyTotalCount,
                                          Count recommendTotalCount, Member member, Pick pick, PickVote pickVote) {
        PickComment pickComment = PickComment.builder()
                .contents(contents)
                .isPublic(isPublic)
                .createdBy(member)
                .replyTotalCount(replyTotalCount)
                .recommendTotalCount(recommendTotalCount)
                .pick(pick)
                .pickVote(pickVote)
                .build();

        pickComment.changePick(pick);

        return pickComment;
    }

    private PickComment createReplidPickComment(CommentContents contents, Member member, Pick pick,
                                                PickComment originParent, PickComment parent) {
        PickComment pickComment = PickComment.builder()
                .contents(contents)
                .createdBy(member)
                .pick(pick)
                .originParent(originParent)
                .isPublic(false)
                .parent(parent)
                .recommendTotalCount(new Count(0))
                .replyTotalCount(new Count(0))
                .build();

        pickComment.changePick(pick);

        return pickComment;
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