package com.dreamypatisiel.devdevdev.domain.service.blame;

import static com.dreamypatisiel.devdevdev.domain.exception.CommonExceptionMessage.INVALID_NOT_FOUND_BLAME_TYPE_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.TechArticleExceptionMessage.INVALID_CAN_NOT_ACTION_DELETED_TECH_COMMENT_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.TechArticleExceptionMessage.INVALID_NOT_FOUND_TECH_COMMENT_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.service.blame.MemberBlameService.BLAME_TYPE_ETC;
import static com.dreamypatisiel.devdevdev.domain.service.blame.MemberTechBlameService.BLAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.dreamypatisiel.devdevdev.domain.entity.Blame;
import com.dreamypatisiel.devdevdev.domain.entity.BlameType;
import com.dreamypatisiel.devdevdev.domain.entity.Company;
import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.TechArticle;
import com.dreamypatisiel.devdevdev.domain.entity.TechComment;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.CompanyName;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Count;
import com.dreamypatisiel.devdevdev.domain.entity.enums.Role;
import com.dreamypatisiel.devdevdev.domain.entity.enums.SocialType;
import com.dreamypatisiel.devdevdev.domain.repository.BlameTypeRepository;
import com.dreamypatisiel.devdevdev.domain.repository.CompanyRepository;
import com.dreamypatisiel.devdevdev.domain.repository.blame.BlameRepository;
import com.dreamypatisiel.devdevdev.domain.repository.member.MemberRepository;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechArticleRepository;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechCommentRepository;
import com.dreamypatisiel.devdevdev.domain.service.blame.dto.BlameTechArticleDto;
import com.dreamypatisiel.devdevdev.exception.NotFoundException;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.SocialMemberDto;
import com.dreamypatisiel.devdevdev.web.dto.response.common.BlameResponse;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class MemberTechBlameServiceTest {

    @Autowired
    MemberTechBlameService memberTechBlameService;

    @Autowired
    TechArticleRepository techArticleRepository;
    @Autowired
    TechCommentRepository techCommentRepository;
    @Autowired
    BlameTypeRepository blameTypeRepository;
    @Autowired
    BlameRepository blameRepository;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    CompanyRepository companyRepository;

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
    @DisplayName("회원이 삭제 상태가 아닌 기술 블로그 댓글을 신고한다.")
    void blameTechArticleComment() {
        // given
        // 회원 생성
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        // 기술블록 회사 생성
        Company company = createCompany("기업1");
        companyRepository.save(company);

        // 기술블로그 생성
        TechArticle techArticle = createTechArticle(company);
        techArticleRepository.save(techArticle);

        // 기술블로그 댓글 생성
        TechComment techComment = createTechComment(new Count(0), techArticle, member);
        techCommentRepository.save(techComment);

        // 신고 종류 생성
        BlameType blameType = new BlameType("욕설1", 0);
        blameTypeRepository.save(blameType);

        BlameTechArticleDto blameTechArticleDto = new BlameTechArticleDto(techArticle.getId(), techComment.getId(),
                blameType.getId(), null);

        // when
        BlameResponse blameResponse = memberTechBlameService.blameTechArticleComment(blameTechArticleDto, member);

        // then
        assertThat(blameResponse.getBlameId()).isNotNull();

        // 신고 이력 검증
        Blame findBlame = blameRepository.findById(blameResponse.getBlameId()).get();
        assertAll(
                () -> assertThat(findBlame.getTechArticle().getId()).isEqualTo(techArticle.getId()),
                () -> assertThat(findBlame.getTechComment().getId()).isEqualTo(techComment.getId()),
                () -> assertThat(findBlame.getMember().getId()).isEqualTo(member.getId()),
                () -> assertThat(findBlame.getBlameType().getId()).isEqualTo(blameType.getId()),
                () -> assertThat(findBlame.getPick()).isNull(),
                () -> assertThat(findBlame.getPickComment()).isNull(),
                () -> assertThat(findBlame.getCustomReason()).isNull()
        );

        // 기술 블로그 댓글 신고 횟수 검증
        TechComment findTechComment = techCommentRepository.findById(techComment.getId()).get();
        assertThat(findTechComment.getBlameTotalCount().getCount()).isEqualTo(1L);
    }

    @Test
    @DisplayName("회원이 삭제 상태가 아닌 기술 블로그 신고(기타)를 하면 신고사유를 직접 입력할 수 있다.")
    void blameTechArticleCommentEtc() {
        // given
        // 회원 생성
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        // 기술블록 회사 생성
        Company company = createCompany("기업1");
        companyRepository.save(company);

        // 기술블로그 생성
        TechArticle techArticle = createTechArticle(company);
        techArticleRepository.save(techArticle);

        // 기술블로그 댓글 생성
        TechComment techComment = createTechComment(new Count(0), techArticle, member);
        techCommentRepository.save(techComment);

        // 신고 종류 생성
        BlameType blameType = new BlameType(BLAME_TYPE_ETC, 0);
        blameTypeRepository.save(blameType);

        String customReason = "기타 신고 사유";
        BlameTechArticleDto blameTechArticleDto = new BlameTechArticleDto(techArticle.getId(), techComment.getId(),
                blameType.getId(), customReason);

        // when
        BlameResponse blameResponse = memberTechBlameService.blameTechArticleComment(blameTechArticleDto, member);

        // then
        assertThat(blameResponse.getBlameId()).isNotNull();

        // 신고 이력 검증
        Blame findBlame = blameRepository.findById(blameResponse.getBlameId()).get();
        assertAll(
                () -> assertThat(findBlame.getTechArticle().getId()).isEqualTo(techArticle.getId()),
                () -> assertThat(findBlame.getTechComment().getId()).isEqualTo(techComment.getId()),
                () -> assertThat(findBlame.getMember().getId()).isEqualTo(member.getId()),
                () -> assertThat(findBlame.getBlameType().getId()).isEqualTo(blameType.getId()),
                () -> assertThat(findBlame.getPick()).isNull(),
                () -> assertThat(findBlame.getPickComment()).isNull(),
                () -> assertThat(findBlame.getCustomReason()).isEqualTo(customReason)
        );

        // 기술 블로그 댓글 신고 횟수 검증
        TechComment findTechComment = techCommentRepository.findById(techComment.getId()).get();
        assertThat(findTechComment.getBlameTotalCount().getCount()).isEqualTo(1L);
    }

    @Test
    @DisplayName("기술 블로그 댓글을 신고할 때 댓글이 존재하지 않으면 예외가 발생한다.")
    void blameTechArticleCommentNotFoundTechComment() {
        // given
        // 회원 생성
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        // 기술블로그 회사 생성
        Company company = createCompany("기업1");
        companyRepository.save(company);

        // 기술블로그 생성
        TechArticle techArticle = createTechArticle(company);
        techArticleRepository.save(techArticle);

        BlameTechArticleDto blameTechArticleDto = new BlameTechArticleDto(techArticle.getId(), 0L,
                0L, null);

        // when // then
        assertThatThrownBy(() -> memberTechBlameService.blameTechArticleComment(blameTechArticleDto, member))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(INVALID_NOT_FOUND_TECH_COMMENT_MESSAGE);
    }

    @Test
    @DisplayName("기술 블로그 댓글을 신고할 때 댓글이 삭제 상태이면 예외가 발생한다.")
    void blameTechArticleCommentIsDeleted() {
        // given
        // 회원 생성
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        // 기술블로그 회사 생성
        Company company = createCompany("기업1");
        companyRepository.save(company);

        // 기술블로그 생성
        TechArticle techArticle = createTechArticle(company);
        techArticleRepository.save(techArticle);

        // 삭제 상태의 기술블로그 댓글 생성
        TechComment techComment = createTechComment(new Count(0), techArticle, member);
        techComment.changeDeletedAt(LocalDateTime.now(), member);
        techCommentRepository.save(techComment);

        BlameTechArticleDto blameTechArticleDto = new BlameTechArticleDto(techArticle.getId(), techComment.getId(),
                0L, null);

        // when // then
        assertThatThrownBy(() -> memberTechBlameService.blameTechArticleComment(blameTechArticleDto, member))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(INVALID_CAN_NOT_ACTION_DELETED_TECH_COMMENT_MESSAGE, BLAME);
    }

    @Test
    @DisplayName("기술 블로그 댓글을 신고할 때 신고 종류가 존재하지 않으면 예외가 발생한다.")
    void blameTechArticleCommentNotFoundBlameType() {
        // given
        // 회원 생성
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        // 기술블록 회사 생성
        Company company = createCompany("기업1");
        companyRepository.save(company);

        // 기술블로그 생성
        TechArticle techArticle = createTechArticle(company);
        techArticleRepository.save(techArticle);

        // 기술블로그 댓글 생성
        TechComment techComment = createTechComment(new Count(0), techArticle, member);
        techCommentRepository.save(techComment);

        BlameTechArticleDto blameTechArticleDto = new BlameTechArticleDto(techArticle.getId(), techComment.getId(),
                0L, null);

        // when // then
        assertThatThrownBy(() -> memberTechBlameService.blameTechArticleComment(blameTechArticleDto, member))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(INVALID_NOT_FOUND_BLAME_TYPE_MESSAGE);
    }

    private TechComment createTechComment(Count blameTotalCount, TechArticle techArticle, Member createdBy) {
        return TechComment.builder()
                .blameTotalCount(blameTotalCount)
                .techArticle(techArticle)
                .createdBy(createdBy)
                .build();
    }

    private Company createCompany(String name) {
        return Company.builder()
                .name(new CompanyName(name))
                .build();
    }

    private TechArticle createTechArticle(Company company) {
        return TechArticle.builder()
                .company(company)
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