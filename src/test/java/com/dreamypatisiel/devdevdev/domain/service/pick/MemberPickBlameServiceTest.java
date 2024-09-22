package com.dreamypatisiel.devdevdev.domain.service.pick;

import static com.dreamypatisiel.devdevdev.domain.exception.CommonExceptionMessage.INVALID_NOT_FOUND_BLAME_TYPE_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.PickExceptionMessage.INVALID_CAN_NOT_ACTION_DELETED_PICK_COMMENT_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.PickExceptionMessage.INVALID_NOT_FOUND_PICK_COMMENT_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.PickExceptionMessage.INVALID_NOT_FOUND_PICK_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.PickExceptionMessage.INVALID_SERVICE_PATH_ACCESS;
import static com.dreamypatisiel.devdevdev.domain.service.blame.MemberBlameService.BLAME_TYPE_ETC;
import static com.dreamypatisiel.devdevdev.domain.service.blame.MemberPickBlameService.BLAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.dreamypatisiel.devdevdev.domain.entity.Blame;
import com.dreamypatisiel.devdevdev.domain.entity.BlameType;
import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.Pick;
import com.dreamypatisiel.devdevdev.domain.entity.PickComment;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.CommentContents;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Count;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Title;
import com.dreamypatisiel.devdevdev.domain.entity.enums.ContentStatus;
import com.dreamypatisiel.devdevdev.domain.entity.enums.Role;
import com.dreamypatisiel.devdevdev.domain.entity.enums.SocialType;
import com.dreamypatisiel.devdevdev.domain.repository.BlameTypeRepository;
import com.dreamypatisiel.devdevdev.domain.repository.blame.BlameRepository;
import com.dreamypatisiel.devdevdev.domain.repository.member.MemberRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickCommentRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickRepository;
import com.dreamypatisiel.devdevdev.domain.service.blame.MemberPickBlameService;
import com.dreamypatisiel.devdevdev.domain.service.blame.dto.BlamePickDto;
import com.dreamypatisiel.devdevdev.exception.NotFoundException;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.SocialMemberDto;
import com.dreamypatisiel.devdevdev.web.dto.response.common.BlameResponse;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class MemberPickBlameServiceTest {

    @Autowired
    MemberPickBlameService memberPickBlameService;

    @Autowired
    BlameTypeRepository blameTypeRepository;
    @Autowired
    BlameRepository blameRepository;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    PickRepository pickRepository;
    @Autowired
    PickCommentRepository pickCommentRepository;

    @Autowired
    EntityManager em;

    String userId = "dreamy5patisiel";
    String name = "꿈빛파티시엘";
    String nickname = "행복한 꿈빛파티시엘";
    String email = "dreamy5patisiel@kakao.com";
    String password = "password";
    String socialType = SocialType.KAKAO.name();
    String role = Role.ROLE_USER.name();
    String author = "운영자";

    @Test
    @DisplayName("회원이 승인 상태의 픽픽픽에 신고를 한다.")
    void blamePick() {
        // given
        // 회원 생성
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        // 픽픽픽 생성
        Pick pick = createPick("픽픽픽", ContentStatus.APPROVAL, new Count(0L));
        pickRepository.save(pick);

        // 신고 종류 생성
        BlameType blameType = new BlameType("욕설1", 0);
        blameTypeRepository.save(blameType);

        BlamePickDto blamePickDto = new BlamePickDto(pick.getId(), null, blameType.getId(), null);

        // when
        BlameResponse blameResponse = memberPickBlameService.blamePick(blamePickDto, member);

        // then
        assertThat(blameResponse.getBlameId()).isNotNull();

        // 신고 이력 검증
        Blame findBlame = blameRepository.findById(blameResponse.getBlameId()).get();
        assertAll(
                () -> assertThat(findBlame.getPick().getId()).isEqualTo(pick.getId()),
                () -> assertThat(findBlame.getMember().getId()).isEqualTo(member.getId()),
                () -> assertThat(findBlame.getBlameType().getId()).isEqualTo(blameType.getId()),
                () -> assertThat(findBlame.getPickComment()).isNull(),
                () -> assertThat(findBlame.getTechArticle()).isNull(),
                () -> assertThat(findBlame.getTechComment()).isNull(),
                () -> assertThat(findBlame.getCustomReason()).isNull()
        );

        // 픽픽픽 신고 횟수 검증
        Pick findPick = pickRepository.findById(pick.getId()).get();
        assertThat(findPick.getBlameTotalCount().getCount()).isEqualTo(1L);
    }

    @Test
    @DisplayName("회원이 승인 상태의 픽픽픽에 신고(기타)를 하면 신고사유를 직접 입력할 수 있다.")
    void blamePickEtc() {
        // given
        // 회원 생성
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        // 픽픽픽 생성
        Pick pick = createPick("픽픽픽", ContentStatus.APPROVAL, new Count(0L));
        pickRepository.save(pick);

        // 신고 종류 생성
        BlameType blameType = new BlameType(BLAME_TYPE_ETC, 0);
        blameTypeRepository.save(blameType);

        String customReason = "기타 사유 입니다.";
        BlamePickDto blamePickDto = new BlamePickDto(pick.getId(), null, blameType.getId(), customReason);

        // when
        BlameResponse blameResponse = memberPickBlameService.blamePick(blamePickDto, member);

        // then
        assertThat(blameResponse.getBlameId()).isNotNull();

        // 신고 이력 검증
        Blame findBlame = blameRepository.findById(blameResponse.getBlameId()).get();
        assertAll(
                () -> assertThat(findBlame.getPick().getId()).isEqualTo(pick.getId()),
                () -> assertThat(findBlame.getMember().getId()).isEqualTo(member.getId()),
                () -> assertThat(findBlame.getBlameType().getId()).isEqualTo(blameType.getId()),
                () -> assertThat(findBlame.getPickComment()).isNull(),
                () -> assertThat(findBlame.getTechArticle()).isNull(),
                () -> assertThat(findBlame.getTechComment()).isNull(),
                () -> assertThat(findBlame.getCustomReason()).isEqualTo(customReason)
        );

        // 픽픽픽 신고 횟수 검증
        Pick findPick = pickRepository.findById(pick.getId()).get();
        assertThat(findPick.getBlameTotalCount().getCount()).isEqualTo(1L);
    }

    @Test
    @DisplayName("픽픽픽을 신고할 때 픽픽픽이 존재하지 않으면 예외가 발생한다.")
    void blamePickNotFoundPick() {
        // given
        // 회원 생성
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        BlamePickDto blamePickDto = new BlamePickDto(0L, null, 0L, null);

        // when // then
        assertThatThrownBy(() -> memberPickBlameService.blamePick(blamePickDto, member))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(INVALID_NOT_FOUND_PICK_MESSAGE);
    }

    @ParameterizedTest
    @EnumSource(value = ContentStatus.class, mode = Mode.EXCLUDE, names = "APPROVAL")
    @DisplayName("픽픽픽을 신고할 때 픽픽픽이 승인상태가 아니면 예외가 발생한다.")
    void blamePickIsNotApproval(ContentStatus contentStatus) {
        // given
        // 회원 생성
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        // 픽픽픽 생성
        Pick pick = createPick("픽픽픽", contentStatus, new Count(0L));
        pickRepository.save(pick);

        BlamePickDto blamePickDto = new BlamePickDto(pick.getId(), null, 0L, null);

        // when // then
        assertThatThrownBy(() -> memberPickBlameService.blamePick(blamePickDto, member))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(INVALID_NOT_FOUND_PICK_MESSAGE);
    }

    @Test
    @DisplayName("픽픽픽을 신고할 때 알맞은 신고 사유가 없을 경우 예외가 발생한다.")
    void blamePickNotFoundBlameType() {
        // given
        // 회원 생성
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        // 픽픽픽 생성
        Pick pick = createPick("픽픽픽", ContentStatus.APPROVAL, new Count(0L));
        pickRepository.save(pick);

        BlamePickDto blamePickDto = new BlamePickDto(pick.getId(), null, 0L, null);

        // when // then
        assertThatThrownBy(() -> memberPickBlameService.blamePick(blamePickDto, member))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(INVALID_NOT_FOUND_BLAME_TYPE_MESSAGE);
    }

    @Test
    @DisplayName("회원이 승인 상태의 픽픽픽의 삭제 상태가 아닌 댓글에 신고한다.")
    void blamePickComment() {
        // given
        // 회원 생성
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        // 픽픽픽 생성
        Pick pick = createPick("픽픽픽", ContentStatus.APPROVAL, new Count(0L));
        pickRepository.save(pick);

        // 픽픽픽 댓글 생성
        PickComment pickComment = createPickComment(pick, member, "픽픽픽 댓글");
        pickCommentRepository.save(pickComment);

        // 신고 종류 생성
        BlameType blameType = new BlameType("욕설1", 0);
        blameTypeRepository.save(blameType);

        BlamePickDto blamePickDto = new BlamePickDto(pick.getId(), pickComment.getId(), blameType.getId(), null);

        // when
        BlameResponse blameResponse = memberPickBlameService.blamePickComment(blamePickDto, member);

        // then
        assertThat(blameResponse.getBlameId()).isNotNull();

        // 신고 이력 검증
        Blame findBlame = blameRepository.findById(blameResponse.getBlameId()).get();
        assertAll(
                () -> assertThat(findBlame.getPick().getId()).isEqualTo(pick.getId()),
                () -> assertThat(findBlame.getPickComment().getId()).isEqualTo(pickComment.getId()),
                () -> assertThat(findBlame.getMember().getId()).isEqualTo(member.getId()),
                () -> assertThat(findBlame.getBlameType().getId()).isEqualTo(blameType.getId()),
                () -> assertThat(findBlame.getTechArticle()).isNull(),
                () -> assertThat(findBlame.getTechComment()).isNull(),
                () -> assertThat(findBlame.getCustomReason()).isNull()
        );

        // 픽픽픽 댓글 신고 횟수 검증
        PickComment findPickComment = pickCommentRepository.findById(pickComment.getId()).get();
        assertThat(findPickComment.getBlameTotalCount().getCount()).isEqualTo(1L);
    }

    @Test
    @DisplayName("회원이 승인 상태의 픽픽픽의 삭제 상태가 아닌 댓글에 신고한다.")
    void blamePickCommentEtc() {
        // given
        // 회원 생성
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        // 픽픽픽 생성
        Pick pick = createPick("픽픽픽", ContentStatus.APPROVAL, new Count(0L));
        pickRepository.save(pick);

        // 픽픽픽 댓글 생성
        PickComment pickComment = createPickComment(pick, member, "픽픽픽 댓글");
        pickCommentRepository.save(pickComment);

        // 신고 종류 생성
        BlameType blameType = new BlameType(BLAME_TYPE_ETC, 0);
        blameTypeRepository.save(blameType);

        String customReason = "기타 사유 입니다.";
        BlamePickDto blamePickDto = new BlamePickDto(pick.getId(), pickComment.getId(), blameType.getId(),
                customReason);

        // when
        BlameResponse blameResponse = memberPickBlameService.blamePickComment(blamePickDto, member);

        // then
        assertThat(blameResponse.getBlameId()).isNotNull();

        // 신고 이력 검증
        Blame findBlame = blameRepository.findById(blameResponse.getBlameId()).get();
        assertAll(
                () -> assertThat(findBlame.getPick().getId()).isEqualTo(pick.getId()),
                () -> assertThat(findBlame.getPickComment().getId()).isEqualTo(pickComment.getId()),
                () -> assertThat(findBlame.getMember().getId()).isEqualTo(member.getId()),
                () -> assertThat(findBlame.getBlameType().getId()).isEqualTo(blameType.getId()),
                () -> assertThat(findBlame.getTechArticle()).isNull(),
                () -> assertThat(findBlame.getTechComment()).isNull(),
                () -> assertThat(findBlame.getCustomReason()).isEqualTo(customReason)
        );

        // 픽픽픽 댓글 신고 횟수 검증
        PickComment findPickComment = pickCommentRepository.findById(pickComment.getId()).get();
        assertThat(findPickComment.getBlameTotalCount().getCount()).isEqualTo(1L);
    }

    @Test
    @DisplayName("픽픽픽 댓글을 신고할 때 픽픽픽 댓글 아이디가 존재하지 않으면 예외가 발생한다.")
    void blamePickCommentNotFoundPickCommentId() {
        // given
        // 회원 생성
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        BlamePickDto blamePickDto = new BlamePickDto(1L, null, 0L, null);

        // when // then
        assertThatThrownBy(() -> memberPickBlameService.blamePickComment(blamePickDto, member))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(INVALID_SERVICE_PATH_ACCESS);
    }

    @Test
    @DisplayName("픽픽픽 댓글을 신고할 때 픽픽 댓글이 존재하지 않으면 예외가 발생한다.")
    void blamePickCommentNotFoundPickComment() {
        // given
        // 회원 생성
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        // 픽픽픽 생성
        Pick pick = createPick("픽픽픽", ContentStatus.APPROVAL, new Count(0L));
        pickRepository.save(pick);

        BlamePickDto blamePickDto = new BlamePickDto(pick.getId(), 0L, 0L, null);

        // when // then
        assertThatThrownBy(() -> memberPickBlameService.blamePickComment(blamePickDto, member))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(INVALID_NOT_FOUND_PICK_COMMENT_MESSAGE);
    }

    @ParameterizedTest
    @EnumSource(value = ContentStatus.class, mode = Mode.EXCLUDE, names = "APPROVAL")
    @DisplayName("픽픽픽 댓글을 신고할 때 픽픽픽이 승인 상태가 아니면 예외가 발생한다.")
    void blamePickCommentPickIsNotApproval(ContentStatus contentStatus) {
        // given
        // 회원 생성
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        // 픽픽픽 생성
        Pick pick = createPick("픽픽픽", contentStatus, new Count(0L));
        pickRepository.save(pick);

        // 픽픽픽 댓글 생성
        PickComment pickComment = createPickComment(pick, member, "픽픽픽 댓글");
        pickCommentRepository.save(pickComment);

        BlamePickDto blamePickDto = new BlamePickDto(pick.getId(), pickComment.getId(), 0L, null);

        // when // then
        assertThatThrownBy(() -> memberPickBlameService.blamePickComment(blamePickDto, member))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(INVALID_NOT_FOUND_PICK_MESSAGE);
    }

    @Test
    @DisplayName("픽픽픽 댓글을 신고할 때 픽픽픽 댓글이 삭제 상태이면 예외가 발생한다.")
    void blamePickCommentIsDeleted() {
        // given
        // 회원 생성
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        // 픽픽픽 생성
        Pick pick = createPick("픽픽픽", ContentStatus.APPROVAL, new Count(0L));
        pickRepository.save(pick);

        // 삭제 상태의 픽픽픽 댓글 생성
        PickComment pickComment = createPickComment(pick, member, "픽픽픽 댓글");
        pickComment.changeDeletedAt(LocalDateTime.now(), member);
        pickCommentRepository.save(pickComment);

        BlamePickDto blamePickDto = new BlamePickDto(pick.getId(), pickComment.getId(), 0L, null);

        // when // then
        assertThatThrownBy(() -> memberPickBlameService.blamePickComment(blamePickDto, member))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(INVALID_CAN_NOT_ACTION_DELETED_PICK_COMMENT_MESSAGE, BLAME);
    }

    @Test
    @DisplayName("픽픽픽 댓글을 신고할 때 신고 사유가 존재하지 않으면 예외가 발생한다.")
    void blamePickCommentNotFoundBlameType() {
        // given
        // 회원 생성
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        // 픽픽픽 생성
        Pick pick = createPick("픽픽픽", ContentStatus.APPROVAL, new Count(0L));
        pickRepository.save(pick);

        // 픽픽픽 댓글 생성
        PickComment pickComment = createPickComment(pick, member, "픽픽픽 댓글");
        pickCommentRepository.save(pickComment);

        BlamePickDto blamePickDto = new BlamePickDto(pick.getId(), pickComment.getId(), 0L, null);

        // when // then
        assertThatThrownBy(() -> memberPickBlameService.blamePickComment(blamePickDto, member))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(INVALID_NOT_FOUND_BLAME_TYPE_MESSAGE);
    }

    private PickComment createPickComment(Pick pick, Member createdBy, String contents) {
        PickComment pickComment = PickComment.builder()
                .pick(pick)
                .createdBy(createdBy)
                .isPublic(true)
                .contents(new CommentContents(contents))
                .build();

        pickComment.changePick(pick);

        return pickComment;
    }

    private Pick createPick(String title, ContentStatus contentStatus, Count commentTotalCount) {
        return Pick.builder()
                .title(new Title(title))
                .contentStatus(contentStatus)
                .commentTotalCount(commentTotalCount)
                .build();
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
}