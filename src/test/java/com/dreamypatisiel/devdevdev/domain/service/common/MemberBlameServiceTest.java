package com.dreamypatisiel.devdevdev.domain.service.common;

import static com.dreamypatisiel.devdevdev.domain.exception.CommonExceptionMessage.INVALID_ALREADY_EXIST_BLAME;
import static com.dreamypatisiel.devdevdev.domain.exception.CommonExceptionMessage.INVALID_BLAME_PATH_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.MemberExceptionMessage.INVALID_MEMBER_NOT_FOUND_MESSAGE;
import static com.dreamypatisiel.devdevdev.web.dto.request.common.BlamePathType.PICK;
import static com.dreamypatisiel.devdevdev.web.dto.request.common.BlamePathType.TECH_ARTICLE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.dreamypatisiel.devdevdev.domain.entity.Blame;
import com.dreamypatisiel.devdevdev.domain.entity.BlameType;
import com.dreamypatisiel.devdevdev.domain.entity.Company;
import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.Pick;
import com.dreamypatisiel.devdevdev.domain.entity.PickComment;
import com.dreamypatisiel.devdevdev.domain.entity.TechArticle;
import com.dreamypatisiel.devdevdev.domain.entity.TechComment;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.CommentContents;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.CompanyName;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Count;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Title;
import com.dreamypatisiel.devdevdev.domain.entity.enums.ContentStatus;
import com.dreamypatisiel.devdevdev.domain.entity.enums.Role;
import com.dreamypatisiel.devdevdev.domain.entity.enums.SocialType;
import com.dreamypatisiel.devdevdev.domain.repository.BlameTypeRepository;
import com.dreamypatisiel.devdevdev.domain.repository.CompanyRepository;
import com.dreamypatisiel.devdevdev.domain.repository.blame.BlameRepository;
import com.dreamypatisiel.devdevdev.domain.repository.member.MemberRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickCommentRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickRepository;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechArticleRepository;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechCommentRepository;
import com.dreamypatisiel.devdevdev.domain.service.common.dto.BlamePickDto;
import com.dreamypatisiel.devdevdev.exception.MemberException;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.SocialMemberDto;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.UserPrincipal;
import com.dreamypatisiel.devdevdev.web.dto.request.common.BlameRequest;
import com.dreamypatisiel.devdevdev.web.dto.response.common.BlameResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.common.BlameTypeResponse;
import java.util.List;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class MemberBlameServiceTest {

    @Autowired
    MemberBlameService memberBlameService;
    @Autowired
    CompanyRepository companyRepository;
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
    TechCommentRepository techCommentRepository;
    @Autowired
    TechArticleRepository techArticleRepository;

    String userId = "dreamy5patisiel";
    String name = "꿈빛파티시엘";
    String nickname = "행복한 꿈빛파티시엘";
    String email = "dreamy5patisiel@kakao.com";
    String password = "password";
    String socialType = SocialType.KAKAO.name();
    String role = Role.ROLE_USER.name();
    String author = "운영자";

    @Test
    @DisplayName("모든 신고 사유를 조회합니다.")
    void findCreateBlameType() {
        // given
        BlameType blameType1 = createBlameType("욕설1", 0);
        BlameType blameType2 = createBlameType("욕설2", 1);
        BlameType blameType3 = createBlameType("욕설3", 2);
        BlameType blameType4 = createBlameType("욕설4", 3);

        blameTypeRepository.saveAll(List.of(blameType1, blameType2, blameType3, blameType4));

        // when
        List<BlameTypeResponse> blameTypes = memberBlameService.findBlameType();

        // then
        assertThat(blameTypes).hasSize(4)
                .extracting("id", "reason", "sortOrder")
                .containsExactly(
                        tuple(blameType1.getId(), "욕설1", 0),
                        tuple(blameType2.getId(), "욕설2", 1),
                        tuple(blameType3.getId(), "욕설3", 2),
                        tuple(blameType4.getId(), "욕설4", 3)
                );
    }

    @Test
    @DisplayName("댑댑댑 서비스에 신고할 때 회원이 존재하지 않으면 예외가 발생한다.")
    void blameMemberException() {
        // given
        // 회원 생성
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // when // then
        assertThatThrownBy(() -> memberBlameService.blame(null, null, authentication))
                .isInstanceOf(MemberException.class)
                .hasMessage(INVALID_MEMBER_NOT_FOUND_MESSAGE);
    }

    @Test
    @DisplayName("픽픽픽에 신고할 때 이미 신고 이력이 존재하면 예외가 발생한다.")
    void blameAlreadyExistBlamePick() {
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
        Pick pick = createPick("픽픽픽", ContentStatus.APPROVAL, new Count(0L));
        pickRepository.save(pick);

        // 신고 종류 생성
        BlameType blameType = new BlameType("욕설1", 0);
        blameTypeRepository.save(blameType);

        // 신고 이력 생성
        Blame blame = createBlame(pick, null, null, null, blameType, member);
        blameRepository.save(blame);

        BlameRequest blameRequest = createBlameRequestBy(pick.getId(), null, null, null, null);

        // when // then
        assertThatThrownBy(() -> memberBlameService.blame(null, blameRequest, authentication))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(INVALID_ALREADY_EXIST_BLAME);
    }

    @Test
    @DisplayName("픽픽픽 댓글에 신고할 때 이미 신고 이력이 존재하면 예외가 발생한다.")
    void blameAlreadyExistBlamePickComment() {
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
        Pick pick = createPick("픽픽픽", ContentStatus.APPROVAL, new Count(0L));
        pickRepository.save(pick);

        // 픽픽픽 댓글 생성
        PickComment pickComment = createPickComment(pick, member, "픽픽픽 댓글1");
        pickCommentRepository.save(pickComment);

        // 신고 종류 생성
        BlameType blameType = new BlameType("욕설1", 0);
        blameTypeRepository.save(blameType);

        // 신고 이력 생성
        Blame blame = createBlame(pick, pickComment, null, null, blameType, member);
        blameRepository.save(blame);

        BlameRequest blameRequest = createBlameRequestBy(pick.getId(), pickComment.getId(), null, null, null);

        // when // then
        assertThatThrownBy(() -> memberBlameService.blame(null, blameRequest, authentication))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(INVALID_ALREADY_EXIST_BLAME);
    }

    @Test
    @DisplayName("픽픽픽 댓글에 신고할 때 이미 신고 이력이 존재하면 예외가 발생한다.")
    void blameAlreadyExistBlameTechComment() {
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

        // 기술블로그 회사 생성
        Company company = createCompany("기업1");
        companyRepository.save(company);

        // 기술블로그 생성
        TechArticle techArticle = createTechArticle(company);
        techArticleRepository.save(techArticle);

        // 기술 블로그 댓글 생성
        TechComment techComment = createTechComment(new Count(0), techArticle, member);
        techCommentRepository.save(techComment);

        // 신고 종류 생성
        BlameType blameType = new BlameType("욕설1", 0);
        blameTypeRepository.save(blameType);

        // 신고 이력 생성
        Blame blame = createBlame(null, null, techArticle, techComment, blameType, member);
        blameRepository.save(blame);

        BlameRequest blameRequest = createBlameRequestBy(null, null, techArticle.getId(), techComment.getId(), null);

        // when // then
        assertThatThrownBy(() -> memberBlameService.blame(null, blameRequest, authentication))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(INVALID_ALREADY_EXIST_BLAME);
    }

    @Test
    @DisplayName("픽픽픽 게시글을 신고한다.")
    void blamePick() {
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
        Pick pick = createPick("픽픽픽", ContentStatus.APPROVAL, new Count(0L));
        pickRepository.save(pick);

        // 신고 종류 생성
        BlameType blameType = new BlameType("욕설1", 0);
        blameTypeRepository.save(blameType);

        BlameRequest blameRequest = createBlameRequestBy(pick.getId(), null, null, null, blameType.getId());

        // when
        BlameResponse blameResponse = memberBlameService.blame(PICK, blameRequest, authentication);

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
    @DisplayName("픽픽픽 댓글을 신고한다.")
    void blamePickComment() {
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
        Pick pick = createPick("픽픽픽", ContentStatus.APPROVAL, new Count(0L));
        pickRepository.save(pick);

        // 픽픽픽 댓글 생성
        PickComment pickComment = createPickComment(pick, member, "픽픽픽 댓글1");
        pickCommentRepository.save(pickComment);

        // 신고 종류 생성
        BlameType blameType = new BlameType("욕설1", 0);
        blameTypeRepository.save(blameType);

        BlameRequest blameRequest = createBlameRequestBy(pick.getId(), pickComment.getId(), null, null,
                blameType.getId());

        // when
        BlameResponse blameResponse = memberBlameService.blame(PICK, blameRequest, authentication);

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
    @DisplayName("기술 블로그 댓글에 신고한다.")
    void blameTechComment() {
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

        // 기술 블로그 회사 생성
        Company company = createCompany("기업1");
        companyRepository.save(company);

        // 기술 블로그 생성
        TechArticle techArticle = createTechArticle(company);
        techArticleRepository.save(techArticle);

        // 기술블로그 댓글 생성
        TechComment techComment = createTechComment(new Count(0), techArticle, member);
        techCommentRepository.save(techComment);

        // 신고 종류 생성
        BlameType blameType = new BlameType("욕설1", 0);
        blameTypeRepository.save(blameType);

        BlameRequest blameRequest = createBlameRequestBy(null, null, techArticle.getId(), techComment.getId(),
                blameType.getId());

        // when
        BlameResponse blameResponse = memberBlameService.blame(TECH_ARTICLE, blameRequest, authentication);

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

    @Disabled
    @ParameterizedTest
    @ValueSource(strings = {"pick", "tech_article"})
    @DisplayName("유효하지 않은 BlamePathType로 신고할 경우 예외가 발생한다.")
    void test(String invalidBlamePathType) {
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

        // 기술 블로그 회사 생성
        Company company = createCompany("기업1");
        companyRepository.save(company);

        // 기술 블로그 생성
        TechArticle techArticle = createTechArticle(company);
        techArticleRepository.save(techArticle);

        // 기술블로그 댓글 생성
        TechComment techComment = createTechComment(new Count(0), techArticle, member);
        techCommentRepository.save(techComment);

        // 신고 종류 생성
        BlameType blameType = new BlameType("욕설1", 0);
        blameTypeRepository.save(blameType);

        BlameRequest blameRequest = createBlameRequestBy(null, null, techArticle.getId(), techComment.getId(),
                blameType.getId());

        // when // then
        assertThatThrownBy(() -> memberBlameService.blame(null, blameRequest,
                authentication))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(INVALID_BLAME_PATH_MESSAGE);
    }

    private BlamePickDto createBlameDto(BlameRequest blameRequest) {
        return BlamePickDto.builder()
                .pickId(blameRequest.getPickId())
                .pickCommentId(blameRequest.getPickCommentId())
                .build();
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

    private Blame createBlame(Pick pick, PickComment pickComment, TechArticle techArticle, TechComment techComment,
                              BlameType blameType, Member member) {
        return Blame.builder()
                .pick(pick)
                .pickComment(pickComment)
                .techArticle(techArticle)
                .techComment(techComment)
                .blameType(blameType)
                .member(member)
                .build();
    }

    private BlameRequest createBlameRequestBy(Long pickId, Long pickCommentId, Long techArticleId,
                                              Long techArticleCommentId, Long blameTypeId) {
        return BlameRequest.builder()
                .pickId(pickId)
                .pickCommentId(pickCommentId)
                .techArticleId(techArticleId)
                .techArticleCommentId(techArticleCommentId)
                .blameTypeId(blameTypeId)
                .build();
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

    private BlameType createBlameType(String reason, int sortOrder) {
        return new BlameType(reason, sortOrder);
    }
}