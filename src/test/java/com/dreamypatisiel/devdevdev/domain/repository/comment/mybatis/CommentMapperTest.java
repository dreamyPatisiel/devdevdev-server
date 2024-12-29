package com.dreamypatisiel.devdevdev.domain.repository.comment.mybatis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.dreamypatisiel.devdevdev.domain.entity.Company;
import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.Pick;
import com.dreamypatisiel.devdevdev.domain.entity.PickComment;
import com.dreamypatisiel.devdevdev.domain.entity.PickOption;
import com.dreamypatisiel.devdevdev.domain.entity.PickVote;
import com.dreamypatisiel.devdevdev.domain.entity.TechArticle;
import com.dreamypatisiel.devdevdev.domain.entity.TechComment;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.CommentContents;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.CompanyName;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Title;
import com.dreamypatisiel.devdevdev.domain.entity.enums.PickOptionType;
import com.dreamypatisiel.devdevdev.domain.entity.enums.Role;
import com.dreamypatisiel.devdevdev.domain.entity.enums.SocialType;
import com.dreamypatisiel.devdevdev.domain.repository.CompanyRepository;
import com.dreamypatisiel.devdevdev.domain.repository.comment.MyWrittenComment;
import com.dreamypatisiel.devdevdev.domain.repository.member.MemberRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickCommentRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickOptionRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickVoteRepository;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechArticleRepository;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechCommentRepository;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.SocialMemberDto;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.auditing.AuditingHandler;
import org.springframework.data.auditing.DateTimeProvider;

@SpringBootTest
@Transactional
class CommentMapperTest {

    @Autowired
    CommentMapper commentMapper;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    PickRepository pickRepository;
    @Autowired
    PickCommentRepository pickCommentRepository;
    @Autowired
    PickVoteRepository pickVoteRepository;
    @Autowired
    PickOptionRepository pickOptionRepository;
    @Autowired
    CompanyRepository companyRepository;
    @Autowired
    TechArticleRepository techArticleRepository;
    @Autowired
    TechCommentRepository techCommentRepository;

    @MockBean
    DateTimeProvider dateTimeProvider;

    @SpyBean
    AuditingHandler auditingHandler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        auditingHandler.setDateTimeProvider(dateTimeProvider);
    }

    @Test
    @DisplayName("회원이 작성한 픽픽픽, 기술블로그 댓글을 커서 방식으로 작성일시 내림차순으로 조회한다.")
    void findByMemberIdAndPickCommentIdAndTechCommentIdOrderByCommentCreatedAtDesc() {
        // given
        when(dateTimeProvider.getNow()).thenReturn(Optional.of(LocalDateTime.of(2024, 1, 1, 0, 0)));

        // 회원 생성
        SocialMemberDto socialMemberDto = createSocialDto("user", "name", "nickname", "password", "user@gmail.com",
                SocialType.KAKAO.name(), Role.ROLE_USER.name());
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        // 픽픽픽 생성
        Pick pick = createPick("픽픽픽", member);
        pickRepository.save(pick);

        // 픽픽픽 옵션 생성
        PickOption pickOption = createPickOption(pick, "픽픽픽 A", PickOptionType.firstPickOption);
        pickOptionRepository.save(pickOption);

        // 픽픽픽 투표 생성
        PickVote pickVote = createPickVote(pick, pickOption, member);
        pickVoteRepository.save(pickVote);

        // 픽픽픽 댓글 생성
        PickComment pickComment1 = createPickComment(pick, member, pickVote, null, null, "픽픽픽 댓글1", true);
        PickComment pickComment2 = createPickComment(pick, member, null, pickComment1, pickComment1, "픽픽픽 댓글2", false);
        PickComment pickComment3 = createPickComment(pick, member, null, pickComment1, pickComment2, "픽픽픽 댓글3", false);

        when(dateTimeProvider.getNow()).thenReturn(Optional.of(LocalDateTime.of(2024, 1, 1, 0, 0)));
        pickCommentRepository.save(pickComment1);

        when(dateTimeProvider.getNow()).thenReturn(Optional.of(LocalDateTime.of(2024, 2, 1, 0, 0)));
        pickCommentRepository.save(pickComment2);

        when(dateTimeProvider.getNow()).thenReturn(Optional.of(LocalDateTime.of(2024, 3, 1, 0, 0)));
        pickCommentRepository.save(pickComment3);

        // 기술블로그 회사 생성
        Company company = createCompany("DreamyPatisiel");
        companyRepository.save(company);

        // 기술블로그 생성
        TechArticle techArticle = createTechArticle(company, "기술블로그 제목");
        techArticleRepository.save(techArticle);

        // 기술블로그 댓글 생성
        TechComment techComment1 = createTechComment(techArticle, member, null, null, "기술블로그 댓글1");
        TechComment techComment2 = createTechComment(techArticle, member, techComment1, techComment1, "기술블로그 댓글2");
        TechComment techComment3 = createTechComment(techArticle, member, techComment1, techComment2, "기술블로그 댓글3");

        when(dateTimeProvider.getNow()).thenReturn(Optional.of(LocalDateTime.of(2024, 4, 1, 0, 0)));
        techCommentRepository.save(techComment1);

        when(dateTimeProvider.getNow()).thenReturn(Optional.of(LocalDateTime.of(2024, 5, 1, 0, 0)));
        techCommentRepository.save(techComment2);

        when(dateTimeProvider.getNow()).thenReturn(Optional.of(LocalDateTime.of(2024, 6, 1, 0, 0)));
        techCommentRepository.save(techComment3);

        // when
        Long techCommentId = techComment3.getId() + 1L;
        Long pickCommentId = pickComment3.getId() + 1L;

        int limit = 6;

        List<MyWrittenComment> myWrittenComments = commentMapper.findByMemberIdAndPickCommentIdAndTechCommentIdOrderByCommentCreatedAtDesc(
                member.getId(), pickCommentId, techCommentId, limit);

        // then
        assertThat(myWrittenComments).hasSize(6)
                .extracting("postId", "postTitle", "commentId", "commentType", "commentContents",
                        "pickOptionTitle", "pickOptionType", "commentCreatedAt")
                .containsExactly(
                        Tuple.tuple(techArticle.getId(), techArticle.getTitle().getTitle(), techComment3.getId(),
                                "TECH_ARTICLE", techComment3.getContents().getCommentContents(),
                                null, null,
                                techComment3.getCreatedAt()),
                        Tuple.tuple(techArticle.getId(), techArticle.getTitle().getTitle(), techComment2.getId(),
                                "TECH_ARTICLE", techComment2.getContents().getCommentContents(),
                                null, null,
                                techComment2.getCreatedAt()),
                        Tuple.tuple(techArticle.getId(), techArticle.getTitle().getTitle(), techComment1.getId(),
                                "TECH_ARTICLE", techComment1.getContents().getCommentContents(),
                                null, null,
                                techComment1.getCreatedAt()),
                        Tuple.tuple(pick.getId(), pick.getTitle().getTitle(), pickComment3.getId(),
                                "PICK", pickComment3.getContents().getCommentContents(),
                                null, null,
                                pickComment3.getCreatedAt()),
                        Tuple.tuple(pick.getId(), pick.getTitle().getTitle(), pickComment2.getId(),
                                "PICK", pickComment2.getContents().getCommentContents(),
                                null, null,
                                pickComment2.getCreatedAt()),
                        Tuple.tuple(pick.getId(), pick.getTitle().getTitle(), pickComment1.getId(),
                                "PICK", pickComment1.getContents().getCommentContents(),
                                pickOption.getTitle().getTitle(), pickOption.getPickOptionType().name(),
                                pickComment1.getCreatedAt())
                );
    }

    private static TechComment createTechComment(TechArticle techArticle, Member member, TechComment originParent,
                                                 TechComment parent, String contents) {
        return TechComment.builder()
                .techArticle(techArticle)
                .createdBy(member)
                .originParent(originParent)
                .parent(parent)
                .contents(new CommentContents(contents))
                .build();
    }

    private static TechArticle createTechArticle(Company company, String title) {
        return TechArticle.builder()
                .company(company)
                .title(new Title(title))
                .build();
    }

    private static Company createCompany(String companyName) {
        return Company.builder()
                .name(new CompanyName(companyName))
                .build();
    }

    private static PickOption createPickOption(Pick pick, String title, PickOptionType pickOptionType) {
        return PickOption.builder()
                .pick(pick)
                .title(new Title(title))
                .pickOptionType(pickOptionType)
                .build();
    }

    private static PickVote createPickVote(Pick pick, PickOption pickOption, Member member) {
        return PickVote.builder()
                .pick(pick)
                .pickOption(pickOption)
                .member(member)
                .build();
    }

    private static PickComment createPickComment(Pick pick, Member member, PickVote pickVote, PickComment originParent,
                                                 PickComment parent, String contents, Boolean isPublic) {
        return PickComment.builder()
                .pick(pick)
                .createdBy(member)
                .pickVote(pickVote)
                .originParent(originParent)
                .parent(parent)
                .contents(new CommentContents(contents))
                .isPublic(isPublic)
                .build();
    }

    private static Pick createPick(String title, Member member) {
        return Pick.builder()
                .title(new Title(title))
                .member(member)
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