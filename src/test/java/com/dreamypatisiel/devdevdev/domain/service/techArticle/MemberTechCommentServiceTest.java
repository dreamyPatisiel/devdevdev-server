package com.dreamypatisiel.devdevdev.domain.service.techArticle;

import static com.dreamypatisiel.devdevdev.domain.exception.MemberExceptionMessage.INVALID_MEMBER_NOT_FOUND_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.TechArticleExceptionMessage.INVALID_CAN_NOT_RECOMMEND_DELETED_TECH_COMMENT_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.TechArticleExceptionMessage.INVALID_CAN_NOT_REPLY_DELETED_TECH_COMMENT_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.TechArticleExceptionMessage.INVALID_NOT_FOUND_TECH_COMMENT_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.TechArticleExceptionMessage.NOT_FOUND_TECH_ARTICLE_MESSAGE;
import static com.dreamypatisiel.devdevdev.global.common.MemberProvider.INVALID_ANONYMOUS_CAN_NOT_USE_THIS_FUNCTION_MESSAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.dreamypatisiel.devdevdev.domain.entity.Company;
import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.TechArticle;
import com.dreamypatisiel.devdevdev.domain.entity.TechComment;
import com.dreamypatisiel.devdevdev.domain.entity.TechCommentRecommend;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.CommentContents;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.CompanyName;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Count;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Title;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Url;
import com.dreamypatisiel.devdevdev.domain.entity.enums.Role;
import com.dreamypatisiel.devdevdev.domain.entity.enums.SocialType;
import com.dreamypatisiel.devdevdev.domain.repository.CompanyRepository;
import com.dreamypatisiel.devdevdev.domain.repository.member.MemberRepository;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechArticleRepository;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechCommentRecommendRepository;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechCommentRepository;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechCommentSort;
import com.dreamypatisiel.devdevdev.domain.service.techArticle.techComment.MemberTechCommentService;
import com.dreamypatisiel.devdevdev.exception.MemberException;
import com.dreamypatisiel.devdevdev.exception.NotFoundException;
import com.dreamypatisiel.devdevdev.global.common.TimeProvider;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.SocialMemberDto;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.UserPrincipal;
import com.dreamypatisiel.devdevdev.global.utils.AuthenticationMemberUtils;
import com.dreamypatisiel.devdevdev.web.dto.SliceCommentCustom;
import com.dreamypatisiel.devdevdev.web.dto.request.techArticle.ModifyTechCommentRequest;
import com.dreamypatisiel.devdevdev.web.dto.request.techArticle.RegisterTechCommentRequest;
import com.dreamypatisiel.devdevdev.web.dto.response.techArticle.TechCommentRecommendResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.techArticle.TechCommentResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.techArticle.TechCommentsResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.techArticle.TechRepliedCommentsResponse;
import com.dreamypatisiel.devdevdev.web.dto.util.CommonResponseUtil;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
public class MemberTechCommentServiceTest {

    @Autowired
    MemberTechCommentService memberTechCommentService;

    @Autowired
    TechArticleRepository techArticleRepository;

    @Autowired
    TechCommentRepository techCommentRepository;

    @Autowired
    CompanyRepository companyRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    TechCommentRecommendRepository techCommentRecommendRepository;

    @MockBean
    TimeProvider timeProvider;

    @Autowired
    EntityManager em;

    String userId = "dreamy5patisiel";
    String name = "꿈빛파티시엘";
    String nickname = "행복한 꿈빛파티시엘";
    String email = "dreamy5patisiel@kakao.com";
    String password = "password";
    String socialType = SocialType.KAKAO.name();
    String role = Role.ROLE_USER.name();
    String adminUserId = "dreamy5patisiel-admin";
    String adminName = "꿈빛파티시엘 관리자";
    String adminNickname = "행복한 꿈빛파티시엘 관리자";
    String adminEmail = "dreamy5patisiel-admin@kakao.com";
    String adminPassword = "password";
    String adminSocialType = SocialType.KAKAO.name();
    String adminRole = Role.ROLE_ADMIN.name();

    @Test
    @DisplayName("회원은 기술블로그 댓글을 작성할 수 있다.")
    void registerTechComment() {
        // given
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Company company = createCompany("꿈빛 파티시엘", "https://example.png", "https://example.com", "https://example.com");
        companyRepository.save(company);

        TechArticle techArticle = TechArticle.createTechArticle(new Title("기술블로그 제목"), new Url("https://example.com"),
                new Count(1L),
                new Count(1L), new Count(1L), new Count(1L), null, company);
        TechArticle savedTechArticle = techArticleRepository.save(techArticle);
        Long id = savedTechArticle.getId();

        RegisterTechCommentRequest registerTechCommentRequest = new RegisterTechCommentRequest("댓글입니다.");

        // when
        TechCommentResponse techCommentResponse = memberTechCommentService.registerMainTechComment(
                id, registerTechCommentRequest, authentication);
        em.flush();

        // then
        assertThat(techCommentResponse.getTechCommentId()).isNotNull();

        TechComment findTechComment = techCommentRepository.findById(techCommentResponse.getTechCommentId())
                .get();

        assertAll(
                // 댓글 생성 확인
                () -> assertThat(findTechComment.getContents().getCommentContents()).isEqualTo("댓글입니다."),
                () -> assertThat(findTechComment.getBlameTotalCount().getCount()).isEqualTo(0L),
                () -> assertThat(findTechComment.getRecommendTotalCount().getCount()).isEqualTo(0L),
                () -> assertThat(findTechComment.getReplyTotalCount().getCount()).isEqualTo(0L),
                () -> assertThat(findTechComment.getCreatedBy().getId()).isEqualTo(member.getId()),
                () -> assertThat(findTechComment.getTechArticle().getId()).isEqualTo(id),
                // 기술블로그 댓글 수 증가 확인
                () -> assertThat(findTechComment.getTechArticle().getCommentTotalCount().getCount()).isEqualTo(2L)
        );
    }

    @Test
    @DisplayName("회원이 기술블로그 댓글을 작성할 때 존재하지 않는 기술블로그에 댓글을 작성하면 예외가 발생한다.")
    void registerTechCommentNotFoundTechArticleException() {
        // given
        Company company = createCompany("꿈빛 파티시엘", "https://example.png", "https://example.com", "https://example.com");
        companyRepository.save(company);

        TechArticle techArticle = TechArticle.createTechArticle(new Title("기술블로그 제목"), new Url("https://example.com"),
                new Count(1L),
                new Count(1L), new Count(1L), new Count(1L), null, company);
        TechArticle savedTechArticle = techArticleRepository.save(techArticle);
        Long id = savedTechArticle.getId() + 1;

        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        RegisterTechCommentRequest registerTechCommentRequest = new RegisterTechCommentRequest("댓글입니다.");

        // when // then
        assertThatThrownBy(
                () -> memberTechCommentService.registerMainTechComment(id, registerTechCommentRequest, authentication))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(NOT_FOUND_TECH_ARTICLE_MESSAGE);
    }

    @Test
    @DisplayName("회원이 기술블로그 댓글을 작성할 때 회원이 없으면 예외가 발생한다.")
    void registerTechCommentNotFoundMemberException() {
        // given
        UserPrincipal userPrincipal = UserPrincipal.createByEmailAndRoleAndSocialType(email, role, socialType);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Long id = 1L;
        RegisterTechCommentRequest registerTechCommentRequest = new RegisterTechCommentRequest("댓글입니다.");

        // when // then
        assertThatThrownBy(
                () -> memberTechCommentService.registerMainTechComment(id, registerTechCommentRequest, authentication))
                .isInstanceOf(MemberException.class)
                .hasMessage(INVALID_MEMBER_NOT_FOUND_MESSAGE);
    }

    @Test
    @DisplayName("회원은 본인이 작성한 삭제되지 않은 댓글을 수정할 수 있다. 수정시 편집된 시각이 갱신된다.")
    void modifyTechComment() {
        // given
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Company company = createCompany("꿈빛 파티시엘", "https://example.png", "https://example.com", "https://example.com");
        companyRepository.save(company);

        TechArticle techArticle = TechArticle.createTechArticle(new Title("기술블로그 제목"), new Url("https://example.com"),
                new Count(1L),
                new Count(1L), new Count(1L), new Count(1L), null, company);
        techArticleRepository.save(techArticle);
        Long techArticleId = techArticle.getId();

        TechComment techComment = TechComment.createMainTechComment(new CommentContents("댓글입니다"), member, techArticle);
        techCommentRepository.save(techComment);
        Long techCommentId = techComment.getId();

        ModifyTechCommentRequest modifyTechCommentRequest = new ModifyTechCommentRequest("댓글 수정입니다.");

        LocalDateTime modifiedDateTime = LocalDateTime.of(2024, 10, 6, 0, 0, 0);
        when(timeProvider.getLocalDateTimeNow()).thenReturn(modifiedDateTime);

        // when
        TechCommentResponse techCommentResponse = memberTechCommentService.modifyTechComment(
                techArticleId, techCommentId, modifyTechCommentRequest, authentication);
        em.flush();

        // then
        assertThat(techCommentResponse.getTechCommentId()).isNotNull();

        TechComment findTechComment = techCommentRepository.findById(techCommentResponse.getTechCommentId())
                .get();

        assertAll(
                () -> assertThat(findTechComment.getContents().getCommentContents()).isEqualTo("댓글 수정입니다."),
                () -> assertThat(findTechComment.getBlameTotalCount().getCount()).isEqualTo(0L),
                () -> assertThat(findTechComment.getRecommendTotalCount().getCount()).isEqualTo(0L),
                () -> assertThat(findTechComment.getCreatedBy().getId()).isEqualTo(member.getId()),
                () -> assertThat(findTechComment.getTechArticle().getId()).isEqualTo(techArticleId),
                () -> assertThat(findTechComment.getId()).isEqualTo(techCommentId),
                () -> assertThat(findTechComment.getContentsLastModifiedAt()).isEqualTo(modifiedDateTime)
        );
    }

    @Test
    @DisplayName("회원이 기술블로그 댓글을 수정할 때 회원이 없으면 예외가 발생한다.")
    void modifyTechCommentNotFoundMemberException() {
        // given
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        ModifyTechCommentRequest modifyTechCommentRequest = new ModifyTechCommentRequest("댓글 수정입니다.");

        // when // then
        assertThatThrownBy(
                () -> memberTechCommentService.modifyTechComment(0L, 0L, modifyTechCommentRequest,
                        authentication))
                .isInstanceOf(MemberException.class)
                .hasMessage(INVALID_MEMBER_NOT_FOUND_MESSAGE);
    }

    @Test
    @DisplayName("회원이 기술블로그 댓글을 수정할 때 댓글이 존재하지 않으면 예외가 발생한다.")
    void modifyTechCommentNotFoundTechArticleCommentException() {
        // given
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Company company = createCompany("꿈빛 파티시엘", "https://example.png", "https://example.com", "https://example.com");
        companyRepository.save(company);

        TechArticle techArticle = TechArticle.createTechArticle(new Title("기술블로그 제목"), new Url("https://example.com"),
                new Count(1L),
                new Count(1L), new Count(1L), new Count(1L), null, company);
        techArticleRepository.save(techArticle);
        Long techArticleId = techArticle.getId();

        ModifyTechCommentRequest modifyTechCommentRequest = new ModifyTechCommentRequest("댓글 수정입니다.");

        // when // then
        assertThatThrownBy(
                () -> memberTechCommentService.modifyTechComment(techArticleId, 0L, modifyTechCommentRequest,
                        authentication))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(INVALID_NOT_FOUND_TECH_COMMENT_MESSAGE);
    }

    @Test
    @DisplayName("회원이 기술블로그 댓글을 수정할 때, 이미 삭제된 댓글이라면 예외가 발생한다.")
    void modifyTechCommentAlreadyDeletedException() {
        // given
        Company company = createCompany("꿈빛 파티시엘", "https://example.png", "https://example.com", "https://example.com");
        companyRepository.save(company);

        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        TechArticle techArticle = TechArticle.createTechArticle(new Title("기술블로그 제목"), new Url("https://example.com"),
                new Count(1L),
                new Count(1L), new Count(1L), new Count(1L), null, company);
        techArticleRepository.save(techArticle);
        Long techArticleId = techArticle.getId();

        TechComment techComment = TechComment.createMainTechComment(new CommentContents("댓글입니다"), member, techArticle);
        techCommentRepository.save(techComment);
        Long techCommentId = techComment.getId();

        LocalDateTime deletedAt = LocalDateTime.of(2024, 10, 6, 0, 0, 0);
        techComment.changeDeletedAt(deletedAt, member);
        em.flush();

        ModifyTechCommentRequest modifyTechCommentRequest = new ModifyTechCommentRequest("댓글 수정");

        // when // then
        assertThatThrownBy(
                () -> memberTechCommentService.modifyTechComment(techArticleId, techCommentId, modifyTechCommentRequest,
                        authentication))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(INVALID_NOT_FOUND_TECH_COMMENT_MESSAGE);
    }

    @Test
    @DisplayName("회원은 본인이 작성한, 아직 삭제되지 않은 댓글을 삭제할 수 있다.")
    void deleteTechComment() {
        // given
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Company company = createCompany("꿈빛 파티시엘", "https://example.png", "https://example.com", "https://example.com");
        companyRepository.save(company);

        TechArticle techArticle = TechArticle.createTechArticle(new Title("기술블로그 제목"), new Url("https://example.com"),
                new Count(1L),
                new Count(1L), new Count(1L), new Count(1L), null, company);
        techArticleRepository.save(techArticle);
        Long techArticleId = techArticle.getId();

        TechComment techComment = TechComment.createMainTechComment(new CommentContents("댓글입니다"), member, techArticle);
        techCommentRepository.save(techComment);
        Long techCommentId = techComment.getId();

        LocalDateTime deletedAt = LocalDateTime.of(2024, 10, 6, 0, 0, 0);
        when(timeProvider.getLocalDateTimeNow()).thenReturn(deletedAt);

        em.flush();

        // when
        memberTechCommentService.deleteTechComment(techArticleId, techCommentId, authentication);

        // then
        TechComment findTechComment = techCommentRepository.findById(techCommentId).get();

        assertAll(
                () -> assertThat(findTechComment.getDeletedAt()).isNotNull(),
                () -> assertThat(findTechComment.getDeletedBy().getId()).isEqualTo(member.getId()),
                () -> assertThat(findTechComment.getDeletedAt()).isEqualTo(deletedAt)
        );
    }

    @Test
    @DisplayName("회원이 댓글을 삭제할 때, 이미 삭제된 댓글이라면 예외가 발생한다.")
    void deleteTechCommentAlreadyDeletedException() {
        // given
        Company company = createCompany("꿈빛 파티시엘", "https://example.png", "https://example.com", "https://example.com");
        companyRepository.save(company);

        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        TechArticle techArticle = TechArticle.createTechArticle(new Title("기술블로그 제목"), new Url("https://example.com"),
                new Count(1L),
                new Count(1L), new Count(1L), new Count(1L), null, company);
        techArticleRepository.save(techArticle);
        Long techArticleId = techArticle.getId();

        TechComment techComment = TechComment.createMainTechComment(new CommentContents("댓글입니다"), member, techArticle);
        techCommentRepository.save(techComment);
        Long techCommentId = techComment.getId();

        LocalDateTime deletedAt = LocalDateTime.of(2024, 10, 6, 0, 0, 0);
        techComment.changeDeletedAt(deletedAt, member);
        em.flush();

        // when // then
        assertThatThrownBy(
                () -> memberTechCommentService.deleteTechComment(techArticleId, techCommentId, authentication))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(INVALID_NOT_FOUND_TECH_COMMENT_MESSAGE);
    }

    @Test
    @DisplayName("회원이 댓글을 삭제할 때, 댓글이 존재하지 않으면 예외가 발생한다.")
    void deleteTechCommentNotFoundException() {
        // given
        Company company = createCompany("꿈빛 파티시엘", "https://example.png", "https://example.com", "https://example.com");
        companyRepository.save(company);

        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        TechArticle techArticle = TechArticle.createTechArticle(new Title("기술블로그 제목"), new Url("https://example.com"),
                new Count(1L),
                new Count(1L), new Count(1L), new Count(1L), null, company);
        techArticleRepository.save(techArticle);
        Long techArticleId = techArticle.getId();

        // when // then
        assertThatThrownBy(
                () -> memberTechCommentService.deleteTechComment(techArticleId, 0L, authentication))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(INVALID_NOT_FOUND_TECH_COMMENT_MESSAGE);
    }

    @Test
    @DisplayName("어드민 권한의 회원은 본인이 작성하지 않은 댓글을 삭제할 수 있다.")
    void deleteTechCommentAdmin() {
        // given
        SocialMemberDto socialAdminDto = createSocialDto(adminUserId, adminName, adminNickname, adminPassword,
                adminEmail, adminSocialType, adminRole);
        Member admin = Member.createMemberBy(socialAdminDto);
        memberRepository.save(admin);

        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(admin);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Company company = createCompany("꿈빛 파티시엘", "https://example.png", "https://example.com", "https://example.com");
        companyRepository.save(company);

        TechArticle techArticle = TechArticle.createTechArticle(new Title("기술블로그 제목"), new Url("https://example.com"),
                new Count(1L),
                new Count(1L), new Count(1L), new Count(1L), null, company);
        techArticleRepository.save(techArticle);
        Long techArticleId = techArticle.getId();

        TechComment techComment = TechComment.createMainTechComment(new CommentContents("댓글입니다"), member, techArticle);
        techCommentRepository.save(techComment);
        Long techCommentId = techComment.getId();

        LocalDateTime deletedAt = LocalDateTime.of(2024, 10, 6, 0, 0, 0);
        when(timeProvider.getLocalDateTimeNow()).thenReturn(deletedAt);

        em.flush();

        // when
        memberTechCommentService.deleteTechComment(techArticleId, techCommentId, authentication);

        // then
        TechComment findTechComment = techCommentRepository.findById(techCommentId).get();

        assertAll(
                () -> assertThat(findTechComment.getDeletedAt()).isNotNull(),
                () -> assertThat(findTechComment.getDeletedAt()).isEqualTo(deletedAt),
                () -> assertThat(findTechComment.getDeletedBy().getId()).isEqualTo(admin.getId())
        );
    }

    @Test
    @DisplayName("회원이 본인이 작성하지 않은 댓글을 삭제하려고 하면 예외가 발생한다.")
    void deleteTechCommentNotByMemberException() {
        // given
        SocialMemberDto authorDto = createSocialDto(adminUserId, adminName, adminNickname, adminPassword, adminEmail,
                adminSocialType, adminRole);
        Member author = Member.createMemberBy(authorDto);
        memberRepository.save(author);

        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Company company = createCompany("꿈빛 파티시엘", "https://example.png", "https://example.com", "https://example.com");
        companyRepository.save(company);

        TechArticle techArticle = TechArticle.createTechArticle(new Title("기술블로그 제목"), new Url("https://example.com"),
                new Count(1L),
                new Count(1L), new Count(1L), new Count(1L), null, company);
        techArticleRepository.save(techArticle);
        Long techArticleId = techArticle.getId();

        TechComment techComment = TechComment.createMainTechComment(new CommentContents("댓글입니다"), author, techArticle);
        techCommentRepository.save(techComment);
        Long techCommentId = techComment.getId();

        // when // then
        assertThatThrownBy(
                () -> memberTechCommentService.deleteTechComment(techArticleId, techCommentId, authentication))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(INVALID_NOT_FOUND_TECH_COMMENT_MESSAGE);
    }

    @Test
    @DisplayName("댓글을 삭제할 때 회원이 존재하지 않으면 예외가 발생한다.")
    void deleteTechCommentNotFoundMemberException() {
        // given
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // when // then
        assertThatThrownBy(
                () -> memberTechCommentService.deleteTechComment(0L, 0L, authentication))
                .isInstanceOf(MemberException.class)
                .hasMessage(INVALID_MEMBER_NOT_FOUND_MESSAGE);
    }

    @Test
    @DisplayName("회원은 기술블로그 댓글에 답글을 작성할 수 있다.")
    void registerRepliedTechComment() {
        // given
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Company company = createCompany("꿈빛 파티시엘", "https://example.png", "https://example.com", "https://example.com");
        companyRepository.save(company);

        TechArticle techArticle = TechArticle.createTechArticle(new Title("기술블로그 제목"), new Url("https://example.com"),
                new Count(1L),
                new Count(1L), new Count(1L), new Count(1L), null, company);
        techArticleRepository.save(techArticle);
        Long techArticleId = techArticle.getId();

        TechComment parentTechComment = TechComment.createMainTechComment(new CommentContents("댓글입니다."), member,
                techArticle);
        techCommentRepository.save(parentTechComment);
        Long parentTechCommentId = parentTechComment.getId();

        RegisterTechCommentRequest registerRepliedTechComment = new RegisterTechCommentRequest("답글입니다.");

        // when
        TechCommentResponse techCommentResponse = memberTechCommentService.registerRepliedTechComment(
                techArticleId, parentTechCommentId, parentTechCommentId, registerRepliedTechComment, authentication);
        em.flush();

        // then
        assertThat(techCommentResponse.getTechCommentId()).isNotNull();

        TechComment findRepliedTechComment = techCommentRepository.findById(techCommentResponse.getTechCommentId())
                .get();

        assertAll(
                // 답글 생성 확인
                () -> assertThat(findRepliedTechComment.getContents().getCommentContents()).isEqualTo("답글입니다."),
                () -> assertThat(findRepliedTechComment.getBlameTotalCount().getCount()).isEqualTo(0L),
                () -> assertThat(findRepliedTechComment.getRecommendTotalCount().getCount()).isEqualTo(0L),
                () -> assertThat(findRepliedTechComment.getReplyTotalCount().getCount()).isEqualTo(0L),
                () -> assertThat(findRepliedTechComment.getCreatedBy().getId()).isEqualTo(member.getId()),
                () -> assertThat(findRepliedTechComment.getParent().getId()).isEqualTo(parentTechCommentId),
                () -> assertThat(findRepliedTechComment.getOriginParent().getId()).isEqualTo(parentTechCommentId),
                // 최상단 댓글의 답글 수 증가 확인
                () -> assertThat(findRepliedTechComment.getOriginParent().getReplyTotalCount().getCount()).isEqualTo(
                        1L),
                // 기술블로그 댓글 수 증가 확인
                () -> assertThat(findRepliedTechComment.getTechArticle().getCommentTotalCount().getCount()).isEqualTo(
                        2L)
        );
    }

    @Test
    @DisplayName("회원은 기술블로그 댓글의 답글에 답글을 작성할 수 있다.")
    void registerRepliedTechCommentToRepliedTechComment() {
        // given
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Company company = createCompany("꿈빛 파티시엘", "https://example.png", "https://example.com", "https://example.com");
        companyRepository.save(company);

        TechArticle techArticle = TechArticle.createTechArticle(new Title("기술블로그 제목"), new Url("https://example.com"),
                new Count(1L),
                new Count(1L), new Count(2L), new Count(1L), null, company);
        techArticleRepository.save(techArticle);
        Long techArticleId = techArticle.getId();

        TechComment originParentTechComment = TechComment.createMainTechComment(new CommentContents("댓글입니다."), member,
                techArticle);
        techCommentRepository.save(originParentTechComment);
        Long originParentTechCommentId = originParentTechComment.getId();

        TechComment parentTechComment = TechComment.createRepliedTechComment(new CommentContents("답글입니다."), member,
                techArticle, originParentTechComment, originParentTechComment);
        techCommentRepository.save(parentTechComment);
        Long parentTechCommentId = parentTechComment.getId();

        RegisterTechCommentRequest registerRepliedTechComment = new RegisterTechCommentRequest("답글입니다.");

        // when
        TechCommentResponse techCommentResponse = memberTechCommentService.registerRepliedTechComment(
                techArticleId, originParentTechCommentId, parentTechCommentId, registerRepliedTechComment,
                authentication);
        em.flush();

        // then
        assertThat(techCommentResponse.getTechCommentId()).isNotNull();

        TechComment findRepliedTechComment = techCommentRepository.findById(techCommentResponse.getTechCommentId())
                .get();

        assertAll(
                () -> assertThat(findRepliedTechComment.getContents().getCommentContents()).isEqualTo("답글입니다."),
                () -> assertThat(findRepliedTechComment.getBlameTotalCount().getCount()).isEqualTo(0L),
                () -> assertThat(findRepliedTechComment.getRecommendTotalCount().getCount()).isEqualTo(0L),
                () -> assertThat(findRepliedTechComment.getReplyTotalCount().getCount()).isEqualTo(0L),
                () -> assertThat(findRepliedTechComment.getCreatedBy().getId()).isEqualTo(member.getId()),
                () -> assertThat(findRepliedTechComment.getParent().getId()).isEqualTo(parentTechCommentId),
                () -> assertThat(findRepliedTechComment.getOriginParent().getId()).isEqualTo(originParentTechCommentId),
                // 최상단 댓글의 답글 수 증가 확인
                () -> assertThat(findRepliedTechComment.getOriginParent().getReplyTotalCount().getCount()).isEqualTo(
                        1L),
                // 기술블로그 댓글 수 증가 확인
                () -> assertThat(findRepliedTechComment.getTechArticle().getCommentTotalCount().getCount()).isEqualTo(
                        3L)
        );
    }

    @Test
    @DisplayName("회원이 기술블로그 댓글에 답글을 작성할 때 존재하지 않는 댓글에 답글을 작성하면 예외가 발생한다.")
    void registerRepliedTechCommentNotFoundTechCommentException() {
        // given
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Company company = createCompany("꿈빛 파티시엘", "https://example.png", "https://example.com", "https://example.com");
        companyRepository.save(company);

        TechArticle techArticle = TechArticle.createTechArticle(new Title("기술블로그 제목"), new Url("https://example.com"),
                new Count(1L),
                new Count(1L), new Count(1L), new Count(1L), null, company);
        techArticleRepository.save(techArticle);
        Long techArticleId = techArticle.getId();

        TechComment techComment = TechComment.createMainTechComment(new CommentContents("댓글입니다."), member, techArticle);
        techCommentRepository.save(techComment);
        Long techCommentId = techComment.getId() + 1;

        RegisterTechCommentRequest registerRepliedTechComment = new RegisterTechCommentRequest("답글입니다.");

        // when // then
        assertThatThrownBy(
                () -> memberTechCommentService.registerRepliedTechComment(techArticleId, techCommentId, techCommentId,
                        registerRepliedTechComment, authentication))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(INVALID_NOT_FOUND_TECH_COMMENT_MESSAGE);
    }

    @Test
    @DisplayName("회원이 기술블로그 댓글에 답글을 작성할 때 삭제된 댓글에 답글을 작성하면 예외가 발생한다.")
    void registerRepliedTechCommentDeletedTechCommentException() {
        // given
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Company company = createCompany("꿈빛 파티시엘", "https://example.png", "https://example.com", "https://example.com");
        companyRepository.save(company);

        TechArticle techArticle = TechArticle.createTechArticle(new Title("기술블로그 제목"), new Url("https://example.com"),
                new Count(1L),
                new Count(1L), new Count(1L), new Count(1L), null, company);
        techArticleRepository.save(techArticle);
        Long techArticleId = techArticle.getId();

        TechComment techComment = TechComment.createMainTechComment(new CommentContents("댓글입니다."), member, techArticle);
        techCommentRepository.save(techComment);
        Long techCommentId = techComment.getId();

        LocalDateTime deletedAt = LocalDateTime.of(2024, 10, 6, 0, 0, 0);
        techComment.changeDeletedAt(deletedAt, member);

        em.flush();
        em.clear();

        RegisterTechCommentRequest registerRepliedTechComment = new RegisterTechCommentRequest("답글입니다.");

        // when // then
        assertThatThrownBy(
                () -> memberTechCommentService.registerRepliedTechComment(techArticleId, techCommentId, techCommentId,
                        registerRepliedTechComment, authentication))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(INVALID_CAN_NOT_REPLY_DELETED_TECH_COMMENT_MESSAGE);
    }

    @Test
    @DisplayName("회원이 기술블로그 댓글에 답글을 작성할 때 회원이 없으면 예외가 발생한다.")
    void registerRepliedTechCommentNotFoundMemberException() {
        // given
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        em.flush();
        em.clear();

        RegisterTechCommentRequest registerRepliedTechComment = new RegisterTechCommentRequest("답글입니다.");

        // when // then
        assertThatThrownBy(
                () -> memberTechCommentService.registerRepliedTechComment(0L, 0L, 0L,
                        registerRepliedTechComment, authentication))
                .isInstanceOf(MemberException.class)
                .hasMessage(INVALID_MEMBER_NOT_FOUND_MESSAGE);
    }

    @Test
    @DisplayName("회원은 커서 방식으로 기술블로그 댓글/답글을 조회할 수 있다. (등록순)")
    void getTechCommentsSortByOLDEST() {
        // given
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Company company = createCompany("꿈빛 파티시엘", "https://example.png", "https://example.com", "https://example.com");
        companyRepository.save(company);

        TechArticle techArticle = TechArticle.createTechArticle(new Title("기술블로그 제목"), new Url("https://example.com"),
                new Count(1L),
                new Count(1L), new Count(12L), new Count(1L), null, company);
        techArticleRepository.save(techArticle);
        Long techArticleId = techArticle.getId();

        TechComment originParentTechComment1 = createMainTechComment(new CommentContents("최상위 댓글1"), member,
                techArticle, new Count(0L), new Count(0L), new Count(0L));
        TechComment originParentTechComment2 = createMainTechComment(new CommentContents("최상위 댓글2"), member,
                techArticle, new Count(0L), new Count(0L), new Count(0L));
        TechComment originParentTechComment3 = createMainTechComment(new CommentContents("최상위 댓글3"), member,
                techArticle, new Count(0L), new Count(0L), new Count(0L));
        TechComment originParentTechComment4 = createMainTechComment(new CommentContents("최상위 댓글4"), member,
                techArticle, new Count(0L), new Count(0L), new Count(0L));
        TechComment originParentTechComment5 = createMainTechComment(new CommentContents("최상위 댓글5"), member,
                techArticle, new Count(0L), new Count(0L), new Count(0L));
        TechComment originParentTechComment6 = createMainTechComment(new CommentContents("최상위 댓글6"), member,
                techArticle, new Count(0L), new Count(0L), new Count(0L));

        TechComment parentTechComment1 = createRepliedTechComment(new CommentContents("최상위 댓글1의 답글1"), member,
                techArticle, originParentTechComment1, originParentTechComment1, new Count(0L), new Count(0L),
                new Count(0L));
        TechComment parentTechComment2 = createRepliedTechComment(new CommentContents("최상위 댓글1의 답글2"), member,
                techArticle, originParentTechComment1, originParentTechComment1, new Count(0L), new Count(0L),
                new Count(0L));
        TechComment parentTechComment3 = createRepliedTechComment(new CommentContents("최상위 댓글2의 답글1"), member,
                techArticle, originParentTechComment2, originParentTechComment2, new Count(0L), new Count(0L),
                new Count(0L));
        TechComment parentTechComment4 = createRepliedTechComment(new CommentContents("최상위 댓글2의 답글2"), member,
                techArticle, originParentTechComment2, originParentTechComment2, new Count(0L), new Count(0L),
                new Count(0L));

        TechComment techcomment1 = createRepliedTechComment(new CommentContents("최상위 댓글1의 답글1의 답글"), member,
                techArticle, originParentTechComment1, parentTechComment1, new Count(0L), new Count(0L), new Count(0L));
        TechComment techcomment2 = createRepliedTechComment(new CommentContents("최상위 댓글1의 답글2의 답글"), member,
                techArticle, originParentTechComment1, parentTechComment2, new Count(0L), new Count(0L), new Count(0L));

        techCommentRepository.saveAll(List.of(
                originParentTechComment1, originParentTechComment2, originParentTechComment3,
                originParentTechComment4, originParentTechComment5, originParentTechComment6,
                parentTechComment1, parentTechComment2, parentTechComment3, parentTechComment4,
                techcomment1, techcomment2
        ));

        Pageable pageable = PageRequest.of(0, 5);

        em.flush();
        em.clear();

        // when
        SliceCommentCustom<TechCommentsResponse> response = memberTechCommentService.getTechComments(techArticleId,
                null, TechCommentSort.OLDEST, pageable, authentication);

        // then
        assertThat(response.getTotalOriginParentComments()).isEqualTo(6L);
        assertThat(response).hasSizeLessThanOrEqualTo(pageable.getPageSize())
                .extracting(
                        "techCommentId",
                        "memberId",
                        "author",
                        "maskedEmail",
                        "contents",
                        "replyTotalCount",
                        "recommendTotalCount",
                        "isCommentAuthor",
                        "isRecommended",
                        "isModified",
                        "isDeleted"
                )
                .containsExactly(
                        Tuple.tuple(originParentTechComment1.getId(),
                                member.getId(),
                                member.getNickname().getNickname(),
                                CommonResponseUtil.sliceAndMaskEmail(member.getEmailAsString()),
                                originParentTechComment1.getContents().getCommentContents(),
                                originParentTechComment1.getReplyTotalCount().getCount(),
                                originParentTechComment1.getRecommendTotalCount().getCount(),
                                true,
                                false,
                                false,
                                false
                        ),
                        Tuple.tuple(originParentTechComment2.getId(),
                                member.getId(),
                                member.getNickname().getNickname(),
                                CommonResponseUtil.sliceAndMaskEmail(member.getEmailAsString()),
                                originParentTechComment2.getContents().getCommentContents(),
                                originParentTechComment2.getReplyTotalCount().getCount(),
                                originParentTechComment2.getRecommendTotalCount().getCount(),
                                true,
                                false,
                                false,
                                false
                        ),
                        Tuple.tuple(originParentTechComment3.getId(),
                                member.getId(),
                                member.getNickname().getNickname(),
                                CommonResponseUtil.sliceAndMaskEmail(member.getEmailAsString()),
                                originParentTechComment3.getContents().getCommentContents(),
                                originParentTechComment3.getReplyTotalCount().getCount(),
                                originParentTechComment3.getRecommendTotalCount().getCount(),
                                true,
                                false,
                                false,
                                false
                        ),
                        Tuple.tuple(originParentTechComment4.getId(),
                                member.getId(),
                                member.getNickname().getNickname(),
                                CommonResponseUtil.sliceAndMaskEmail(member.getEmailAsString()),
                                originParentTechComment4.getContents().getCommentContents(),
                                originParentTechComment4.getReplyTotalCount().getCount(),
                                originParentTechComment4.getRecommendTotalCount().getCount(),
                                true,
                                false,
                                false,
                                false
                        ),
                        Tuple.tuple(originParentTechComment5.getId(),
                                member.getId(),
                                member.getNickname().getNickname(),
                                CommonResponseUtil.sliceAndMaskEmail(member.getEmailAsString()),
                                originParentTechComment5.getContents().getCommentContents(),
                                originParentTechComment5.getReplyTotalCount().getCount(),
                                originParentTechComment5.getRecommendTotalCount().getCount(),
                                true,
                                false,
                                false,
                                false
                        )
                );

        TechCommentsResponse techCommentsResponse1 = response.getContent().get(0);
        List<TechRepliedCommentsResponse> replies1 = techCommentsResponse1.getReplies();
        assertThat(replies1).hasSize(4)
                .extracting(
                        "techCommentId",
                        "memberId",
                        "techParentCommentId",
                        "techParentCommentMemberId",
                        "techParentCommentAuthor",
                        "techOriginParentCommentId",
                        "author",
                        "maskedEmail",
                        "contents",
                        "recommendTotalCount",
                        "isCommentAuthor",
                        "isRecommended",
                        "isModified",
                        "isDeleted"
                )
                .containsExactly(
                        Tuple.tuple(parentTechComment1.getId(),
                                member.getId(),
                                originParentTechComment1.getId(),
                                originParentTechComment1.getCreatedBy().getId(),
                                member.getNicknameAsString(),
                                originParentTechComment1.getId(),
                                member.getNicknameAsString(),
                                CommonResponseUtil.sliceAndMaskEmail(member.getEmailAsString()),
                                parentTechComment1.getContents().getCommentContents(),
                                parentTechComment1.getRecommendTotalCount().getCount(),
                                true,
                                false,
                                false,
                                false
                        ),
                        Tuple.tuple(parentTechComment2.getId(),
                                member.getId(),
                                originParentTechComment1.getId(),
                                originParentTechComment1.getCreatedBy().getId(),
                                member.getNicknameAsString(),
                                originParentTechComment1.getId(),
                                member.getNicknameAsString(),
                                CommonResponseUtil.sliceAndMaskEmail(member.getEmailAsString()),
                                parentTechComment2.getContents().getCommentContents(),
                                parentTechComment2.getRecommendTotalCount().getCount(),
                                true,
                                false,
                                false,
                                false
                        ),
                        Tuple.tuple(techcomment1.getId(),
                                member.getId(),
                                parentTechComment1.getId(),
                                parentTechComment1.getCreatedBy().getId(),
                                member.getNicknameAsString(),
                                originParentTechComment1.getId(),
                                member.getNicknameAsString(),
                                CommonResponseUtil.sliceAndMaskEmail(member.getEmailAsString()),
                                techcomment1.getContents().getCommentContents(),
                                techcomment1.getRecommendTotalCount().getCount(),
                                true,
                                false,
                                false,
                                false
                        ),
                        Tuple.tuple(techcomment2.getId(),
                                member.getId(),
                                parentTechComment2.getId(),
                                parentTechComment2.getCreatedBy().getId(),
                                member.getNicknameAsString(),
                                originParentTechComment1.getId(),
                                member.getNicknameAsString(),
                                CommonResponseUtil.sliceAndMaskEmail(member.getEmailAsString()),
                                techcomment2.getContents().getCommentContents(),
                                techcomment2.getRecommendTotalCount().getCount(),
                                true,
                                false,
                                false,
                                false
                        )
                );

        TechCommentsResponse techCommentsResponse2 = response.getContent().get(1);
        List<TechRepliedCommentsResponse> replies2 = techCommentsResponse2.getReplies();
        assertThat(replies2).hasSize(2)
                .extracting(
                        "techCommentId",
                        "memberId",
                        "techParentCommentId",
                        "techParentCommentMemberId",
                        "techParentCommentAuthor",
                        "techOriginParentCommentId",
                        "author",
                        "maskedEmail",
                        "contents",
                        "recommendTotalCount",
                        "isCommentAuthor",
                        "isRecommended",
                        "isModified",
                        "isDeleted"
                )
                .containsExactly(
                        Tuple.tuple(parentTechComment3.getId(),
                                member.getId(),
                                originParentTechComment2.getId(),
                                originParentTechComment2.getCreatedBy().getId(),
                                member.getNicknameAsString(),
                                originParentTechComment2.getId(),
                                member.getNicknameAsString(),
                                CommonResponseUtil.sliceAndMaskEmail(member.getEmailAsString()),
                                parentTechComment3.getContents().getCommentContents(),
                                parentTechComment3.getRecommendTotalCount().getCount(),
                                true,
                                false,
                                false,
                                false
                        ),
                        Tuple.tuple(parentTechComment4.getId(),
                                member.getId(),
                                originParentTechComment2.getId(),
                                originParentTechComment2.getCreatedBy().getId(),
                                member.getNicknameAsString(),
                                originParentTechComment2.getId(),
                                member.getNicknameAsString(),
                                CommonResponseUtil.sliceAndMaskEmail(member.getEmailAsString()),
                                parentTechComment4.getContents().getCommentContents(),
                                parentTechComment4.getRecommendTotalCount().getCount(),
                                true,
                                false,
                                false,
                                false
                        )
                );

        TechCommentsResponse techCommentsResponse3 = response.getContent().get(2);
        List<TechRepliedCommentsResponse> replies3 = techCommentsResponse3.getReplies();
        assertThat(replies3).hasSize(0);

        TechCommentsResponse techCommentsResponse4 = response.getContent().get(3);
        List<TechRepliedCommentsResponse> replies4 = techCommentsResponse4.getReplies();
        assertThat(replies4).hasSize(0);

        TechCommentsResponse techCommentsResponse5 = response.getContent().get(4);
        List<TechRepliedCommentsResponse> replies5 = techCommentsResponse5.getReplies();
        assertThat(replies5).hasSize(0);
    }

    @Test
    @DisplayName("회원은 커서 방식으로 기술블로그 댓글/답글을 조회할 수 있다. (기본 정렬은 최신순)")
    void getTechCommentsSortByLATEST() {
        // given
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Company company = createCompany("꿈빛 파티시엘", "https://example.png", "https://example.com", "https://example.com");
        companyRepository.save(company);

        TechArticle techArticle = TechArticle.createTechArticle(new Title("기술블로그 제목"), new Url("https://example.com"),
                new Count(1L),
                new Count(1L), new Count(12L), new Count(1L), null, company);
        techArticleRepository.save(techArticle);
        Long techArticleId = techArticle.getId();

        TechComment originParentTechComment1 = createMainTechComment(new CommentContents("최상위 댓글1"), member,
                techArticle, new Count(0L), new Count(0L), new Count(0L));
        TechComment originParentTechComment2 = createMainTechComment(new CommentContents("최상위 댓글2"), member,
                techArticle, new Count(0L), new Count(0L), new Count(0L));
        TechComment originParentTechComment3 = createMainTechComment(new CommentContents("최상위 댓글3"), member,
                techArticle, new Count(0L), new Count(0L), new Count(0L));
        TechComment originParentTechComment4 = createMainTechComment(new CommentContents("최상위 댓글4"), member,
                techArticle, new Count(0L), new Count(0L), new Count(0L));
        TechComment originParentTechComment5 = createMainTechComment(new CommentContents("최상위 댓글5"), member,
                techArticle, new Count(0L), new Count(0L), new Count(0L));
        TechComment originParentTechComment6 = createMainTechComment(new CommentContents("최상위 댓글6"), member,
                techArticle, new Count(0L), new Count(0L), new Count(0L));

        TechComment parentTechComment1 = createRepliedTechComment(new CommentContents("최상위 댓글1의 답글1"), member,
                techArticle, originParentTechComment1, originParentTechComment1, new Count(0L), new Count(0L),
                new Count(0L));
        TechComment parentTechComment2 = createRepliedTechComment(new CommentContents("최상위 댓글1의 답글2"), member,
                techArticle, originParentTechComment1, originParentTechComment1, new Count(0L), new Count(0L),
                new Count(0L));
        TechComment parentTechComment3 = createRepliedTechComment(new CommentContents("최상위 댓글2의 답글1"), member,
                techArticle, originParentTechComment2, originParentTechComment2, new Count(0L), new Count(0L),
                new Count(0L));
        TechComment parentTechComment4 = createRepliedTechComment(new CommentContents("최상위 댓글2의 답글2"), member,
                techArticle, originParentTechComment2, originParentTechComment2, new Count(0L), new Count(0L),
                new Count(0L));

        TechComment techcomment1 = createRepliedTechComment(new CommentContents("최상위 댓글1의 답글1의 답글"), member,
                techArticle, originParentTechComment1, parentTechComment1, new Count(0L), new Count(0L), new Count(0L));
        TechComment techcomment2 = createRepliedTechComment(new CommentContents("최상위 댓글1의 답글2의 답글"), member,
                techArticle, originParentTechComment1, parentTechComment2, new Count(0L), new Count(0L), new Count(0L));

        techCommentRepository.saveAll(List.of(
                originParentTechComment1, originParentTechComment2, originParentTechComment3,
                originParentTechComment4, originParentTechComment5, originParentTechComment6,
                parentTechComment1, parentTechComment2, parentTechComment3, parentTechComment4,
                techcomment1, techcomment2
        ));

        Pageable pageable = PageRequest.of(0, 5);

        em.flush();
        em.clear();

        // when
        SliceCommentCustom<TechCommentsResponse> response = memberTechCommentService.getTechComments(techArticleId,
                null, TechCommentSort.LATEST, pageable, authentication);

        // then
        assertThat(response.getTotalOriginParentComments()).isEqualTo(6L);
        assertThat(response).hasSizeLessThanOrEqualTo(pageable.getPageSize())
                .extracting(
                        "techCommentId",
                        "memberId",
                        "author",
                        "maskedEmail",
                        "contents",
                        "replyTotalCount",
                        "recommendTotalCount",
                        "isCommentAuthor",
                        "isRecommended",
                        "isModified",
                        "isDeleted"
                )
                .containsExactly(
                        Tuple.tuple(originParentTechComment6.getId(),
                                member.getId(),
                                member.getNickname().getNickname(),
                                CommonResponseUtil.sliceAndMaskEmail(member.getEmailAsString()),
                                originParentTechComment6.getContents().getCommentContents(),
                                originParentTechComment6.getReplyTotalCount().getCount(),
                                originParentTechComment6.getRecommendTotalCount().getCount(),
                                true,
                                false,
                                false,
                                false
                        ),
                        Tuple.tuple(originParentTechComment5.getId(),
                                member.getId(),
                                member.getNickname().getNickname(),
                                CommonResponseUtil.sliceAndMaskEmail(member.getEmailAsString()),
                                originParentTechComment5.getContents().getCommentContents(),
                                originParentTechComment5.getReplyTotalCount().getCount(),
                                originParentTechComment5.getRecommendTotalCount().getCount(),
                                true,
                                false,
                                false,
                                false
                        ),
                        Tuple.tuple(originParentTechComment4.getId(),
                                member.getId(),
                                member.getNickname().getNickname(),
                                CommonResponseUtil.sliceAndMaskEmail(member.getEmailAsString()),
                                originParentTechComment4.getContents().getCommentContents(),
                                originParentTechComment4.getReplyTotalCount().getCount(),
                                originParentTechComment4.getRecommendTotalCount().getCount(),
                                true,
                                false,
                                false,
                                false
                        ),
                        Tuple.tuple(originParentTechComment3.getId(),
                                member.getId(),
                                member.getNickname().getNickname(),
                                CommonResponseUtil.sliceAndMaskEmail(member.getEmailAsString()),
                                originParentTechComment3.getContents().getCommentContents(),
                                originParentTechComment3.getReplyTotalCount().getCount(),
                                originParentTechComment3.getRecommendTotalCount().getCount(),
                                true,
                                false,
                                false,
                                false
                        ),
                        Tuple.tuple(originParentTechComment2.getId(),
                                member.getId(),
                                member.getNickname().getNickname(),
                                CommonResponseUtil.sliceAndMaskEmail(member.getEmailAsString()),
                                originParentTechComment2.getContents().getCommentContents(),
                                originParentTechComment2.getReplyTotalCount().getCount(),
                                originParentTechComment2.getRecommendTotalCount().getCount(),
                                true,
                                false,
                                false,
                                false)
                );

        TechCommentsResponse techCommentsResponse6 = response.getContent().get(0);
        List<TechRepliedCommentsResponse> replies6 = techCommentsResponse6.getReplies();
        assertThat(replies6).hasSize(0);

        TechCommentsResponse techCommentsResponse5 = response.getContent().get(1);
        List<TechRepliedCommentsResponse> replies5 = techCommentsResponse5.getReplies();
        assertThat(replies5).hasSize(0);

        TechCommentsResponse techCommentsResponse4 = response.getContent().get(2);
        List<TechRepliedCommentsResponse> replies4 = techCommentsResponse4.getReplies();
        assertThat(replies4).hasSize(0);

        TechCommentsResponse techCommentsResponse3 = response.getContent().get(3);
        List<TechRepliedCommentsResponse> replies3 = techCommentsResponse3.getReplies();
        assertThat(replies3).hasSize(0);

        TechCommentsResponse techCommentsResponse2 = response.getContent().get(4);
        List<TechRepliedCommentsResponse> replies2 = techCommentsResponse2.getReplies();
        assertThat(replies2).hasSize(2)
                .extracting("techCommentId")
                .containsExactly(parentTechComment3.getId(), parentTechComment4.getId());
    }

    @Test
    @DisplayName("회원은 커서 방식으로 기술블로그 댓글/답글을 조회할 수 있다. (댓글 많은 순)")
    void getTechCommentsSortByMostCommented() {
        // given
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Company company = createCompany("꿈빛 파티시엘", "https://example.png", "https://example.com", "https://example.com");
        companyRepository.save(company);

        TechArticle techArticle = TechArticle.createTechArticle(new Title("기술블로그 제목"), new Url("https://example.com"),
                new Count(1L),
                new Count(1L), new Count(12L), new Count(1L), null, company);
        techArticleRepository.save(techArticle);
        Long techArticleId = techArticle.getId();

        TechComment originParentTechComment1 = createMainTechComment(new CommentContents("최상위 댓글1"), member,
                techArticle, new Count(0L), new Count(0L), new Count(0L));
        TechComment originParentTechComment2 = createMainTechComment(new CommentContents("최상위 댓글2"), member,
                techArticle, new Count(0L), new Count(0L), new Count(4L));
        TechComment originParentTechComment3 = createMainTechComment(new CommentContents("최상위 댓글3"), member,
                techArticle, new Count(0L), new Count(0L), new Count(0L));
        TechComment originParentTechComment4 = createMainTechComment(new CommentContents("최상위 댓글4"), member,
                techArticle, new Count(0L), new Count(0L), new Count(2L));
        TechComment originParentTechComment5 = createMainTechComment(new CommentContents("최상위 댓글5"), member,
                techArticle, new Count(0L), new Count(0L), new Count(0L));
        TechComment originParentTechComment6 = createMainTechComment(new CommentContents("최상위 댓글6"), member,
                techArticle, new Count(0L), new Count(0L), new Count(0L));

        TechComment parentTechComment1 = createRepliedTechComment(new CommentContents("최상위 댓글2의 답글1"), member,
                techArticle, originParentTechComment2, originParentTechComment2, new Count(0L), new Count(0L),
                new Count(0L));
        TechComment parentTechComment2 = createRepliedTechComment(new CommentContents("최상위 댓글2의 답글2"), member,
                techArticle, originParentTechComment2, originParentTechComment2, new Count(0L), new Count(0L),
                new Count(0L));
        TechComment parentTechComment3 = createRepliedTechComment(new CommentContents("최상위 댓글4의 답글1"), member,
                techArticle, originParentTechComment4, originParentTechComment4, new Count(0L), new Count(0L),
                new Count(0L));
        TechComment parentTechComment4 = createRepliedTechComment(new CommentContents("최상위 댓글4의 답글2"), member,
                techArticle, originParentTechComment4, originParentTechComment4, new Count(0L), new Count(0L),
                new Count(0L));

        TechComment techcomment1 = createRepliedTechComment(new CommentContents("최상위 댓글2의 답글1의 답글"), member,
                techArticle, originParentTechComment2, parentTechComment1, new Count(0L), new Count(0L), new Count(0L));
        TechComment techcomment2 = createRepliedTechComment(new CommentContents("최상위 댓글2의 답글2의 답글"), member,
                techArticle, originParentTechComment2, parentTechComment2, new Count(0L), new Count(0L), new Count(0L));

        techCommentRepository.saveAll(List.of(
                originParentTechComment1, originParentTechComment2, originParentTechComment3,
                originParentTechComment4, originParentTechComment5, originParentTechComment6,
                parentTechComment1, parentTechComment2, parentTechComment3, parentTechComment4,
                techcomment1, techcomment2
        ));

        Pageable pageable = PageRequest.of(0, 5);

        em.flush();
        em.clear();

        // when
        SliceCommentCustom<TechCommentsResponse> response = memberTechCommentService.getTechComments(techArticleId,
                null, TechCommentSort.MOST_COMMENTED, pageable, authentication);

        // then
        assertThat(response.getTotalOriginParentComments()).isEqualTo(6L);
        assertThat(response).hasSizeLessThanOrEqualTo(pageable.getPageSize())
                .extracting(
                        "techCommentId",
                        "memberId",
                        "author",
                        "maskedEmail",
                        "contents",
                        "replyTotalCount",
                        "recommendTotalCount",
                        "isCommentAuthor",
                        "isRecommended",
                        "isModified",
                        "isDeleted"
                )
                .containsExactly(
                        Tuple.tuple(originParentTechComment2.getId(),
                                member.getId(),
                                member.getNickname().getNickname(),
                                CommonResponseUtil.sliceAndMaskEmail(member.getEmailAsString()),
                                originParentTechComment2.getContents().getCommentContents(),
                                originParentTechComment2.getReplyTotalCount().getCount(),
                                originParentTechComment2.getRecommendTotalCount().getCount(),
                                true,
                                false,
                                false,
                                false
                        ),
                        Tuple.tuple(originParentTechComment4.getId(),
                                member.getId(),
                                member.getNickname().getNickname(),
                                CommonResponseUtil.sliceAndMaskEmail(member.getEmailAsString()),
                                originParentTechComment4.getContents().getCommentContents(),
                                originParentTechComment4.getReplyTotalCount().getCount(),
                                originParentTechComment4.getRecommendTotalCount().getCount(),
                                true,
                                false,
                                false,
                                false
                        ),
                        Tuple.tuple(originParentTechComment6.getId(),
                                member.getId(),
                                member.getNickname().getNickname(),
                                CommonResponseUtil.sliceAndMaskEmail(member.getEmailAsString()),
                                originParentTechComment6.getContents().getCommentContents(),
                                originParentTechComment6.getReplyTotalCount().getCount(),
                                originParentTechComment6.getRecommendTotalCount().getCount(),
                                true,
                                false,
                                false,
                                false
                        ),
                        Tuple.tuple(originParentTechComment5.getId(),
                                member.getId(),
                                member.getNickname().getNickname(),
                                CommonResponseUtil.sliceAndMaskEmail(member.getEmailAsString()),
                                originParentTechComment5.getContents().getCommentContents(),
                                originParentTechComment5.getReplyTotalCount().getCount(),
                                originParentTechComment5.getRecommendTotalCount().getCount(),
                                true,
                                false,
                                false,
                                false
                        ),
                        Tuple.tuple(originParentTechComment3.getId(),
                                member.getId(),
                                member.getNickname().getNickname(),
                                CommonResponseUtil.sliceAndMaskEmail(member.getEmailAsString()),
                                originParentTechComment3.getContents().getCommentContents(),
                                originParentTechComment3.getReplyTotalCount().getCount(),
                                originParentTechComment3.getRecommendTotalCount().getCount(),
                                true,
                                false,
                                false,
                                false
                        )
                );

        TechCommentsResponse techCommentsResponse1 = response.getContent().get(0);
        List<TechRepliedCommentsResponse> replies1 = techCommentsResponse1.getReplies();
        assertThat(replies1).hasSize(4)
                .extracting(
                        "techCommentId",
                        "memberId",
                        "techParentCommentId",
                        "techParentCommentMemberId",
                        "techParentCommentAuthor",
                        "techOriginParentCommentId",
                        "author",
                        "maskedEmail",
                        "contents",
                        "recommendTotalCount",
                        "isCommentAuthor",
                        "isRecommended",
                        "isModified",
                        "isDeleted"
                )
                .containsExactly(
                        Tuple.tuple(parentTechComment1.getId(),
                                member.getId(),
                                originParentTechComment2.getId(),
                                originParentTechComment2.getCreatedBy().getId(),
                                member.getNicknameAsString(),
                                originParentTechComment2.getId(),
                                member.getNicknameAsString(),
                                CommonResponseUtil.sliceAndMaskEmail(member.getEmailAsString()),
                                parentTechComment1.getContents().getCommentContents(),
                                parentTechComment1.getRecommendTotalCount().getCount(),
                                true,
                                false,
                                false,
                                false
                        ),
                        Tuple.tuple(parentTechComment2.getId(),
                                member.getId(),
                                originParentTechComment2.getId(),
                                originParentTechComment2.getCreatedBy().getId(),
                                member.getNicknameAsString(),
                                originParentTechComment2.getId(),
                                member.getNicknameAsString(),
                                CommonResponseUtil.sliceAndMaskEmail(member.getEmailAsString()),
                                parentTechComment2.getContents().getCommentContents(),
                                parentTechComment2.getRecommendTotalCount().getCount(),
                                true,
                                false,
                                false,
                                false
                        ),
                        Tuple.tuple(techcomment1.getId(),
                                member.getId(),
                                parentTechComment1.getId(),
                                parentTechComment1.getCreatedBy().getId(),
                                member.getNicknameAsString(),
                                originParentTechComment2.getId(),
                                member.getNicknameAsString(),
                                CommonResponseUtil.sliceAndMaskEmail(member.getEmailAsString()),
                                techcomment1.getContents().getCommentContents(),
                                techcomment1.getRecommendTotalCount().getCount(),
                                true,
                                false,
                                false,
                                false
                        ),
                        Tuple.tuple(techcomment2.getId(),
                                member.getId(),
                                parentTechComment2.getId(),
                                parentTechComment2.getCreatedBy().getId(),
                                member.getNicknameAsString(),
                                originParentTechComment2.getId(),
                                member.getNicknameAsString(),
                                CommonResponseUtil.sliceAndMaskEmail(member.getEmailAsString()),
                                techcomment2.getContents().getCommentContents(),
                                techcomment2.getRecommendTotalCount().getCount(),
                                true,
                                false,
                                false,
                                false
                        )
                );

        TechCommentsResponse techCommentsResponse2 = response.getContent().get(1);
        List<TechRepliedCommentsResponse> replies2 = techCommentsResponse2.getReplies();
        assertThat(replies2).hasSize(2)
                .extracting(
                        "techCommentId",
                        "memberId",
                        "techParentCommentId",
                        "techParentCommentMemberId",
                        "techParentCommentAuthor",
                        "techOriginParentCommentId",
                        "author",
                        "maskedEmail",
                        "contents",
                        "recommendTotalCount",
                        "isCommentAuthor",
                        "isRecommended",
                        "isModified",
                        "isDeleted"
                )
                .containsExactly(
                        Tuple.tuple(parentTechComment3.getId(),
                                member.getId(),
                                originParentTechComment4.getId(),
                                originParentTechComment4.getCreatedBy().getId(),
                                member.getNicknameAsString(),
                                originParentTechComment4.getId(),
                                member.getNicknameAsString(),
                                CommonResponseUtil.sliceAndMaskEmail(member.getEmailAsString()),
                                parentTechComment3.getContents().getCommentContents(),
                                parentTechComment3.getRecommendTotalCount().getCount(),
                                true,
                                false,
                                false,
                                false
                        ),
                        Tuple.tuple(parentTechComment4.getId(),
                                member.getId(),
                                originParentTechComment4.getId(),
                                originParentTechComment4.getCreatedBy().getId(),
                                member.getNicknameAsString(),
                                originParentTechComment4.getId(),
                                member.getNicknameAsString(),
                                CommonResponseUtil.sliceAndMaskEmail(member.getEmailAsString()),
                                parentTechComment4.getContents().getCommentContents(),
                                parentTechComment4.getRecommendTotalCount().getCount(),
                                true,
                                false,
                                false,
                                false
                        )
                );

        TechCommentsResponse techCommentsResponse3 = response.getContent().get(2);
        List<TechRepliedCommentsResponse> replies3 = techCommentsResponse3.getReplies();
        assertThat(replies3).hasSize(0);

        TechCommentsResponse techCommentsResponse4 = response.getContent().get(3);
        List<TechRepliedCommentsResponse> replies4 = techCommentsResponse4.getReplies();
        assertThat(replies4).hasSize(0);

        TechCommentsResponse techCommentsResponse5 = response.getContent().get(4);
        List<TechRepliedCommentsResponse> replies5 = techCommentsResponse5.getReplies();
        assertThat(replies5).hasSize(0);
    }

    @Test
    @DisplayName("회원은 커서 방식으로 기술블로그 댓글/답글을 조회할 수 있다. (추천 많은 순)")
    void getTechCommentsSortByMostRecommended() {
        // given
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Company company = createCompany("꿈빛 파티시엘", "https://example.png", "https://example.com", "https://example.com");
        companyRepository.save(company);

        TechArticle techArticle = TechArticle.createTechArticle(new Title("기술블로그 제목"), new Url("https://example.com"),
                new Count(1L),
                new Count(1L), new Count(12L), new Count(1L), null, company);
        techArticleRepository.save(techArticle);
        Long techArticleId = techArticle.getId();

        TechComment originParentTechComment1 = createMainTechComment(new CommentContents("최상위 댓글1"), member,
                techArticle, new Count(0L), new Count(3L), new Count(0L));
        TechComment originParentTechComment2 = createMainTechComment(new CommentContents("최상위 댓글2"), member,
                techArticle, new Count(0L), new Count(1L), new Count(0L));
        TechComment originParentTechComment3 = createMainTechComment(new CommentContents("최상위 댓글3"), member,
                techArticle, new Count(0L), new Count(5L), new Count(0L));
        TechComment originParentTechComment4 = createMainTechComment(new CommentContents("최상위 댓글4"), member,
                techArticle, new Count(0L), new Count(4L), new Count(0L));
        TechComment originParentTechComment5 = createMainTechComment(new CommentContents("최상위 댓글5"), member,
                techArticle, new Count(0L), new Count(2L), new Count(0L));
        TechComment originParentTechComment6 = createMainTechComment(new CommentContents("최상위 댓글6"), member,
                techArticle, new Count(0L), new Count(6L), new Count(0L));

        techCommentRepository.saveAll(List.of(
                originParentTechComment1, originParentTechComment2, originParentTechComment3,
                originParentTechComment4, originParentTechComment5, originParentTechComment6
        ));

        Pageable pageable = PageRequest.of(0, 5);

        em.flush();
        em.clear();

        // when
        SliceCommentCustom<TechCommentsResponse> response = memberTechCommentService.getTechComments(techArticleId,
                null, TechCommentSort.MOST_LIKED, pageable, authentication);

        // then
        assertThat(response.getTotalOriginParentComments()).isEqualTo(6L);
        assertThat(response).hasSizeLessThanOrEqualTo(pageable.getPageSize())
                .extracting(
                        "techCommentId",
                        "memberId",
                        "author",
                        "maskedEmail",
                        "contents",
                        "replyTotalCount",
                        "recommendTotalCount",
                        "isCommentAuthor",
                        "isRecommended",
                        "isModified",
                        "isDeleted"
                )
                .containsExactly(
                        Tuple.tuple(originParentTechComment6.getId(),
                                member.getId(),
                                member.getNickname().getNickname(),
                                CommonResponseUtil.sliceAndMaskEmail(member.getEmailAsString()),
                                originParentTechComment6.getContents().getCommentContents(),
                                originParentTechComment6.getReplyTotalCount().getCount(),
                                originParentTechComment6.getRecommendTotalCount().getCount(),
                                true,
                                false,
                                false,
                                false
                        ),
                        Tuple.tuple(originParentTechComment3.getId(),
                                member.getId(),
                                member.getNickname().getNickname(),
                                CommonResponseUtil.sliceAndMaskEmail(member.getEmailAsString()),
                                originParentTechComment3.getContents().getCommentContents(),
                                originParentTechComment3.getReplyTotalCount().getCount(),
                                originParentTechComment3.getRecommendTotalCount().getCount(),
                                true,
                                false,
                                false,
                                false
                        ),
                        Tuple.tuple(originParentTechComment4.getId(),
                                member.getId(),
                                member.getNickname().getNickname(),
                                CommonResponseUtil.sliceAndMaskEmail(member.getEmailAsString()),
                                originParentTechComment4.getContents().getCommentContents(),
                                originParentTechComment4.getReplyTotalCount().getCount(),
                                originParentTechComment4.getRecommendTotalCount().getCount(),
                                true,
                                false,
                                false,
                                false
                        ),
                        Tuple.tuple(originParentTechComment1.getId(),
                                member.getId(),
                                member.getNickname().getNickname(),
                                CommonResponseUtil.sliceAndMaskEmail(member.getEmailAsString()),
                                originParentTechComment1.getContents().getCommentContents(),
                                originParentTechComment1.getReplyTotalCount().getCount(),
                                originParentTechComment1.getRecommendTotalCount().getCount(),
                                true,
                                false,
                                false,
                                false
                        ),
                        Tuple.tuple(originParentTechComment5.getId(),
                                member.getId(),
                                member.getNickname().getNickname(),
                                CommonResponseUtil.sliceAndMaskEmail(member.getEmailAsString()),
                                originParentTechComment5.getContents().getCommentContents(),
                                originParentTechComment5.getReplyTotalCount().getCount(),
                                originParentTechComment5.getRecommendTotalCount().getCount(),
                                true,
                                false,
                                false,
                                false
                        )
                );

        TechCommentsResponse techCommentsResponse6 = response.getContent().get(0);
        List<TechRepliedCommentsResponse> replies6 = techCommentsResponse6.getReplies();
        assertThat(replies6).hasSize(0);

        TechCommentsResponse techCommentsResponse3 = response.getContent().get(1);
        List<TechRepliedCommentsResponse> replies3 = techCommentsResponse3.getReplies();
        assertThat(replies3).hasSize(0);

        TechCommentsResponse techCommentsResponse4 = response.getContent().get(2);
        List<TechRepliedCommentsResponse> replies4 = techCommentsResponse4.getReplies();
        assertThat(replies4).hasSize(0);

        TechCommentsResponse techCommentsResponse1 = response.getContent().get(3);
        List<TechRepliedCommentsResponse> replies1 = techCommentsResponse1.getReplies();
        assertThat(replies1).hasSize(0);

        TechCommentsResponse techCommentsResponse5 = response.getContent().get(4);
        List<TechRepliedCommentsResponse> replies5 = techCommentsResponse5.getReplies();
        assertThat(replies5).hasSize(0);
    }

    @Test
    @DisplayName("회원은 커서 방식으로 커서 다음의 기술블로그 댓글/답글을 조회할 수 있다.")
    void getTechCommentsByCursor() {
        // given
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Company company = createCompany("꿈빛 파티시엘", "https://example.png", "https://example.com", "https://example.com");
        companyRepository.save(company);

        TechArticle techArticle = TechArticle.createTechArticle(new Title("기술블로그 제목"), new Url("https://example.com"),
                new Count(1L),
                new Count(1L), new Count(12L), new Count(1L), null, company);
        techArticleRepository.save(techArticle);
        Long techArticleId = techArticle.getId();

        TechComment originParentTechComment1 = createMainTechComment(new CommentContents("최상위 댓글1"), member,
                techArticle, new Count(0L), new Count(0L), new Count(0L));
        TechComment originParentTechComment2 = createMainTechComment(new CommentContents("최상위 댓글2"), member,
                techArticle, new Count(0L), new Count(0L), new Count(0L));
        TechComment originParentTechComment3 = createMainTechComment(new CommentContents("최상위 댓글3"), member,
                techArticle, new Count(0L), new Count(0L), new Count(0L));
        TechComment originParentTechComment4 = createMainTechComment(new CommentContents("최상위 댓글4"), member,
                techArticle, new Count(0L), new Count(0L), new Count(0L));
        TechComment originParentTechComment5 = createMainTechComment(new CommentContents("최상위 댓글5"), member,
                techArticle, new Count(0L), new Count(0L), new Count(0L));
        TechComment originParentTechComment6 = createMainTechComment(new CommentContents("최상위 댓글6"), member,
                techArticle, new Count(0L), new Count(0L), new Count(0L));

        originParentTechComment6.changeDeletedAt(LocalDateTime.now(), member);

        techCommentRepository.saveAll(List.of(
                originParentTechComment1, originParentTechComment2, originParentTechComment3,
                originParentTechComment4, originParentTechComment5, originParentTechComment6
        ));

        Pageable pageable = PageRequest.of(0, 5);

        em.flush();
        em.clear();

        // when
        SliceCommentCustom<TechCommentsResponse> response = memberTechCommentService.getTechComments(techArticleId,
                originParentTechComment6.getId(), null, pageable, authentication);

        // then
        assertThat(response.getTotalOriginParentComments()).isEqualTo(5L); // 삭제된 댓글은 카운트하지 않는다
        assertThat(response).hasSizeLessThanOrEqualTo(pageable.getPageSize())
                .extracting(
                        "techCommentId",
                        "memberId",
                        "author",
                        "maskedEmail",
                        "contents",
                        "replyTotalCount",
                        "recommendTotalCount",
                        "isCommentAuthor",
                        "isRecommended",
                        "isModified",
                        "isDeleted"
                )
                .containsExactly(
                        Tuple.tuple(originParentTechComment5.getId(),
                                member.getId(),
                                member.getNickname().getNickname(),
                                CommonResponseUtil.sliceAndMaskEmail(member.getEmailAsString()),
                                originParentTechComment5.getContents().getCommentContents(),
                                originParentTechComment5.getReplyTotalCount().getCount(),
                                originParentTechComment5.getRecommendTotalCount().getCount(),
                                true,
                                false,
                                false,
                                false
                        ),
                        Tuple.tuple(originParentTechComment4.getId(),
                                member.getId(),
                                member.getNickname().getNickname(),
                                CommonResponseUtil.sliceAndMaskEmail(member.getEmailAsString()),
                                originParentTechComment4.getContents().getCommentContents(),
                                originParentTechComment4.getReplyTotalCount().getCount(),
                                originParentTechComment4.getRecommendTotalCount().getCount(),
                                true,
                                false,
                                false,
                                false
                        ),
                        Tuple.tuple(originParentTechComment3.getId(),
                                member.getId(),
                                member.getNickname().getNickname(),
                                CommonResponseUtil.sliceAndMaskEmail(member.getEmailAsString()),
                                originParentTechComment3.getContents().getCommentContents(),
                                originParentTechComment3.getReplyTotalCount().getCount(),
                                originParentTechComment3.getRecommendTotalCount().getCount(),
                                true,
                                false,
                                false,
                                false
                        ),
                        Tuple.tuple(originParentTechComment2.getId(),
                                member.getId(),
                                member.getNickname().getNickname(),
                                CommonResponseUtil.sliceAndMaskEmail(member.getEmailAsString()),
                                originParentTechComment2.getContents().getCommentContents(),
                                originParentTechComment2.getReplyTotalCount().getCount(),
                                originParentTechComment2.getRecommendTotalCount().getCount(),
                                true,
                                false,
                                false,
                                false
                        ),
                        Tuple.tuple(originParentTechComment1.getId(),
                                member.getId(),
                                member.getNickname().getNickname(),
                                CommonResponseUtil.sliceAndMaskEmail(member.getEmailAsString()),
                                originParentTechComment1.getContents().getCommentContents(),
                                originParentTechComment1.getReplyTotalCount().getCount(),
                                originParentTechComment1.getRecommendTotalCount().getCount(),
                                true,
                                false,
                                false,
                                false
                        )
                );

        TechCommentsResponse techCommentsResponse6 = response.getContent().get(0);
        List<TechRepliedCommentsResponse> replies6 = techCommentsResponse6.getReplies();
        assertThat(replies6).hasSize(0);

        TechCommentsResponse techCommentsResponse3 = response.getContent().get(1);
        List<TechRepliedCommentsResponse> replies3 = techCommentsResponse3.getReplies();
        assertThat(replies3).hasSize(0);

        TechCommentsResponse techCommentsResponse4 = response.getContent().get(2);
        List<TechRepliedCommentsResponse> replies4 = techCommentsResponse4.getReplies();
        assertThat(replies4).hasSize(0);

        TechCommentsResponse techCommentsResponse1 = response.getContent().get(3);
        List<TechRepliedCommentsResponse> replies1 = techCommentsResponse1.getReplies();
        assertThat(replies1).hasSize(0);

        TechCommentsResponse techCommentsResponse5 = response.getContent().get(4);
        List<TechRepliedCommentsResponse> replies5 = techCommentsResponse5.getReplies();
        assertThat(replies5).hasSize(0);
    }

    @Test
    @DisplayName("회원은 기술블로그 댓글의 답글을 추천할 수 있다.")
    void recommendTechComment() {
        // given
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Company company = createCompany("꿈빛 파티시엘", "https://example.png", "https://example.com", "https://example.com");
        companyRepository.save(company);

        TechArticle techArticle = TechArticle.createTechArticle(new Title("기술블로그 제목"), new Url("https://example.com"),
                new Count(1L),
                new Count(1L), new Count(2L), new Count(1L), null, company);
        techArticleRepository.save(techArticle);
        Long techArticleId = techArticle.getId();

        TechComment techComment = TechComment.createMainTechComment(new CommentContents("댓글입니다."), member, techArticle);
        techCommentRepository.save(techComment);

        // when
        TechCommentRecommendResponse techCommentRecommendResponse = memberTechCommentService.recommendTechComment(
                techArticleId, techComment.getId(), authentication);

        // then
        assertAll(
                () -> assertThat(techCommentRecommendResponse.getIsRecommended()).isTrue(),
                () -> assertThat(techCommentRecommendResponse.getRecommendTotalCount()).isEqualTo(1L)
        );
    }

    @Test
    @DisplayName("회원은 기술블로그 댓글의 답글을 추천할 수 있다. 이때 이미 추천 상태라면 취소된다.")
    void recommendTechCommentCancel() {
        // given
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Company company = createCompany("꿈빛 파티시엘", "https://example.png", "https://example.com", "https://example.com");
        companyRepository.save(company);

        TechArticle techArticle = TechArticle.createTechArticle(new Title("기술블로그 제목"), new Url("https://example.com"),
                new Count(1L),
                new Count(1L), new Count(2L), new Count(1L), null, company);
        techArticleRepository.save(techArticle);
        Long techArticleId = techArticle.getId();

        TechComment techComment = TechComment.createMainTechComment(new CommentContents("댓글입니다."), member, techArticle);
        techCommentRepository.save(techComment);

        TechCommentRecommend techCommentRecommend = TechCommentRecommend.create(techComment, member);
        techCommentRecommendRepository.save(techCommentRecommend);

        // when
        TechCommentRecommendResponse techCommentRecommendResponse = memberTechCommentService.recommendTechComment(
                techArticleId, techComment.getId(), authentication);

        // then
        assertAll(
                () -> assertThat(techCommentRecommendResponse.getIsRecommended()).isFalse(),
                () -> assertThat(techCommentRecommendResponse.getRecommendTotalCount()).isEqualTo(0L)
        );
    }

    @Test
    @DisplayName("회원이 기술블로그 댓글의 답글을 추천할 때 존재하지 않는 댓글을 추천하면 예외가 발생한다.")
    void recommendTechCommentNotFoundTechCommentException() {
        // given
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Company company = createCompany("꿈빛 파티시엘", "https://example.png", "https://example.com", "https://example.com");
        companyRepository.save(company);

        TechArticle techArticle = TechArticle.createTechArticle(new Title("기술블로그 제목"), new Url("https://example.com"),
                new Count(1L),
                new Count(1L), new Count(1L), new Count(1L), null, company);
        techArticleRepository.save(techArticle);
        Long techArticleId = techArticle.getId();

        TechComment techComment = TechComment.createMainTechComment(new CommentContents("댓글입니다."), member, techArticle);
        techCommentRepository.save(techComment);
        Long techCommentId = techComment.getId() + 1;

        // when // then
        assertThatThrownBy(
                () -> memberTechCommentService.recommendTechComment(techArticleId, techCommentId, authentication))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(INVALID_NOT_FOUND_TECH_COMMENT_MESSAGE);
    }

    @Test
    @DisplayName("회원이 기술블로그 댓글의 답글을 추천할 때 삭제된 댓글을 추천하면 예외가 발생한다.")
    void recommendTechCommentDeletedTechCommentException() {
        // given
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Company company = createCompany("꿈빛 파티시엘", "https://example.png", "https://example.com", "https://example.com");
        companyRepository.save(company);

        TechArticle techArticle = TechArticle.createTechArticle(new Title("기술블로그 제목"), new Url("https://example.com"),
                new Count(1L),
                new Count(1L), new Count(1L), new Count(1L), null, company);
        techArticleRepository.save(techArticle);
        Long techArticleId = techArticle.getId();

        TechComment techComment = TechComment.createMainTechComment(new CommentContents("댓글입니다."), member, techArticle);
        techCommentRepository.save(techComment);
        Long techCommentId = techComment.getId();

        LocalDateTime deletedAt = LocalDateTime.of(2024, 10, 6, 0, 0, 0);
        techComment.changeDeletedAt(deletedAt, member);

        em.flush();
        em.clear();

        // when // then
        assertThatThrownBy(
                () -> memberTechCommentService.recommendTechComment(techArticleId, techCommentId, authentication))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(INVALID_CAN_NOT_RECOMMEND_DELETED_TECH_COMMENT_MESSAGE);
    }

    @Test
    @DisplayName("회원이 기술블로그 댓글의 답글을 추천할 때 회원이 없으면 예외가 발생한다.")
    void recommendTechCommentNotFoundMemberException() {
        // given
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        em.flush();
        em.clear();

        RegisterTechCommentRequest registerRepliedTechComment = new RegisterTechCommentRequest("답글입니다.");

        // when // then
        assertThatThrownBy(
                () -> memberTechCommentService.recommendTechComment(0L, 0L, authentication))
                .isInstanceOf(MemberException.class)
                .hasMessage(INVALID_MEMBER_NOT_FOUND_MESSAGE);
    }

    @Test
    @DisplayName("익명회원인 경우 회원 전용 기술블로그 베스트 댓글 조회 메소드를 호출하면 예외가 발생한다.")
    void findTechBestCommentsNotAnonymousMember() {
        // given
        // 익명회원 목킹
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(AuthenticationMemberUtils.ANONYMOUS_USER);

        // when // then
        assertThatThrownBy(() -> memberTechCommentService.findTechBestComments(3, 0L, authentication))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(INVALID_ANONYMOUS_CAN_NOT_USE_THIS_FUNCTION_MESSAGE);
    }

    @Test
    @DisplayName("회원이 offset에 정책에 맞게 기술블로그 베스트 댓글을 조회한다.")
    void findTechBestComments() {
        // given
        // 회원 생성
        SocialMemberDto socialMemberDto1 = createSocialDto("user1", name, "nickname1", password, "user1@gmail.com",
                socialType, Role.ROLE_ADMIN.name());
        SocialMemberDto socialMemberDto2 = createSocialDto("user2", name, "nickname2", password, "user2@gmail.com",
                socialType, role);
        SocialMemberDto socialMemberDto3 = createSocialDto("user3", name, "nickname3", password, "user3@gmail.com",
                socialType, role);

        Member member1 = Member.createMemberBy(socialMemberDto1);
        Member member2 = Member.createMemberBy(socialMemberDto2);
        Member member3 = Member.createMemberBy(socialMemberDto3);
        memberRepository.saveAll(List.of(member1, member2, member3));

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member1);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 회사 생성
        Company company = createCompany("꿈빛 파티시엘", "https://example.png", "https://example.com", "https://example.com");
        companyRepository.save(company);

        // 기술 블로그 생성
        TechArticle techArticle = TechArticle.createTechArticle(new Title("기술블로그 제목"), new Url("https://example.com"),
                new Count(1L),
                new Count(1L), new Count(12L), new Count(1L), null, company);
        techArticleRepository.save(techArticle);

        // 댓글 생성
        TechComment originParentTechComment1 = createMainTechComment(new CommentContents("최상위 댓글1"), member1,
                techArticle, new Count(0L), new Count(3L), new Count(0L));
        originParentTechComment1.modifyCommentContents(new CommentContents("최상위 댓글1 수정"), LocalDateTime.now());
        TechComment originParentTechComment2 = createMainTechComment(new CommentContents("최상위 댓글2"), member2,
                techArticle, new Count(0L), new Count(2L), new Count(0L));
        TechComment originParentTechComment3 = createMainTechComment(new CommentContents("최상위 댓글3"), member3,
                techArticle, new Count(0L), new Count(1L), new Count(0L));
        techCommentRepository.saveAll(
                List.of(originParentTechComment1, originParentTechComment2, originParentTechComment3));

        // 추천 생성
        TechCommentRecommend techCommentRecommend = createTechCommentRecommend(true, originParentTechComment1, member1);
        techCommentRecommendRepository.save(techCommentRecommend);

        // 답글 생성
        TechComment repliedTechComment = createRepliedTechComment(new CommentContents("최상위 댓글1의 답글1"), member3,
                techArticle, originParentTechComment1, originParentTechComment1, new Count(0L), new Count(0L),
                new Count(0L));
        techCommentRepository.save(repliedTechComment);

        // when
        List<TechCommentsResponse> response = memberTechCommentService.findTechBestComments(3, techArticle.getId(),
                authentication);

        // then
        assertThat(response).hasSize(3)
                .extracting(
                        "techCommentId",
                        "memberId",
                        "author",
                        "maskedEmail",
                        "contents",
                        "replyTotalCount",
                        "recommendTotalCount",
                        "isCommentAuthor",
                        "isRecommended",
                        "isModified",
                        "isDeleted"
                )
                .containsExactly(
                        Tuple.tuple(originParentTechComment1.getId(),
                                member1.getId(),
                                member1.getNickname().getNickname(),
                                CommonResponseUtil.sliceAndMaskEmail(member1.getEmailAsString()),
                                originParentTechComment1.getContents().getCommentContents(),
                                originParentTechComment1.getReplyTotalCount().getCount(),
                                originParentTechComment1.getRecommendTotalCount().getCount(),
                                true,
                                true,
                                true,
                                false
                        ),
                        Tuple.tuple(originParentTechComment2.getId(),
                                member2.getId(),
                                member2.getNickname().getNickname(),
                                CommonResponseUtil.sliceAndMaskEmail(member2.getEmailAsString()),
                                originParentTechComment2.getContents().getCommentContents(),
                                originParentTechComment2.getReplyTotalCount().getCount(),
                                originParentTechComment2.getRecommendTotalCount().getCount(),
                                false,
                                false,
                                false,
                                false
                        ),
                        Tuple.tuple(originParentTechComment3.getId(),
                                member3.getId(),
                                member3.getNickname().getNickname(),
                                CommonResponseUtil.sliceAndMaskEmail(member3.getEmailAsString()),
                                originParentTechComment3.getContents().getCommentContents(),
                                originParentTechComment3.getReplyTotalCount().getCount(),
                                originParentTechComment3.getRecommendTotalCount().getCount(),
                                false,
                                false,
                                false,
                                false
                        )
                );

        TechCommentsResponse techCommentsResponse = response.get(0);
        List<TechRepliedCommentsResponse> replies = techCommentsResponse.getReplies();
        assertThat(replies).hasSize(1)
                .extracting(
                        "techCommentId",
                        "memberId",
                        "techParentCommentMemberId",
                        "techParentCommentId",
                        "techOriginParentCommentId",
                        "techParentCommentAuthor",
                        "author",
                        "maskedEmail",
                        "contents",
                        "recommendTotalCount",
                        "isCommentAuthor",
                        "isRecommended",
                        "isModified",
                        "isDeleted"
                ).containsExactly(
                        Tuple.tuple(repliedTechComment.getId(),
                                member3.getId(),
                                repliedTechComment.getParent().getCreatedBy().getId(),
                                repliedTechComment.getParent().getId(),
                                repliedTechComment.getOriginParent().getId(),
                                repliedTechComment.getOriginParent().getCreatedBy().getNicknameAsString(),
                                member3.getNickname().getNickname(),
                                CommonResponseUtil.sliceAndMaskEmail(member3.getEmailAsString()),
                                repliedTechComment.getContents().getCommentContents(),
                                repliedTechComment.getRecommendTotalCount().getCount(),
                                false,
                                false,
                                false,
                                false
                        )
                );
    }

    @Test
    @DisplayName("회원이 offset에 정책에 맞게 기술블로그 베스트 댓글을 조회할 때 추천수가 1개 미만인 댓글은 베스트 댓글에서 제외된다.")
    void findTechBestCommentsExcludeLessThanOneRecommend() {
        // given
        // 회원 생성
        SocialMemberDto socialMemberDto1 = createSocialDto("user1", name, "nickname1", password, "user1@gmail.com",
                socialType, Role.ROLE_ADMIN.name());
        SocialMemberDto socialMemberDto2 = createSocialDto("user2", name, "nickname2", password, "user2@gmail.com",
                socialType, role);
        SocialMemberDto socialMemberDto3 = createSocialDto("user3", name, "nickname3", password, "user3@gmail.com",
                socialType, role);

        Member member1 = Member.createMemberBy(socialMemberDto1);
        Member member2 = Member.createMemberBy(socialMemberDto2);
        Member member3 = Member.createMemberBy(socialMemberDto3);
        memberRepository.saveAll(List.of(member1, member2, member3));

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member1);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 회사 생성
        Company company = createCompany("꿈빛 파티시엘", "https://example.png", "https://example.com", "https://example.com");
        companyRepository.save(company);

        // 기술 블로그 생성
        TechArticle techArticle = TechArticle.createTechArticle(new Title("기술블로그 제목"), new Url("https://example.com"),
                new Count(1L),
                new Count(1L), new Count(12L), new Count(1L), null, company);
        techArticleRepository.save(techArticle);

        // 댓글 생성
        TechComment originParentTechComment1 = createMainTechComment(new CommentContents("최상위 댓글1"), member1,
                techArticle, new Count(0L), new Count(3L), new Count(0L));
        originParentTechComment1.modifyCommentContents(new CommentContents("최상위 댓글1 수정"), LocalDateTime.now());
        TechComment originParentTechComment2 = createMainTechComment(new CommentContents("최상위 댓글2"), member2,
                techArticle, new Count(0L), new Count(0L), new Count(0L));
        TechComment originParentTechComment3 = createMainTechComment(new CommentContents("최상위 댓글3"), member3,
                techArticle, new Count(0L), new Count(0L), new Count(0L));
        techCommentRepository.saveAll(
                List.of(originParentTechComment1, originParentTechComment2, originParentTechComment3));

        // 추천 생성
        TechCommentRecommend techCommentRecommend = createTechCommentRecommend(true, originParentTechComment1, member1);
        techCommentRecommendRepository.save(techCommentRecommend);

        // 답글 생성
        TechComment repliedTechComment = createRepliedTechComment(new CommentContents("최상위 댓글1의 답글1"), member3,
                techArticle, originParentTechComment1, originParentTechComment1, new Count(0L), new Count(0L),
                new Count(0L));
        techCommentRepository.save(repliedTechComment);

        // when
        List<TechCommentsResponse> response = memberTechCommentService.findTechBestComments(3, techArticle.getId(),
                authentication);

        // then
        assertThat(response).hasSize(1)
                .extracting(
                        "techCommentId",
                        "memberId",
                        "author",
                        "maskedEmail",
                        "contents",
                        "replyTotalCount",
                        "recommendTotalCount",
                        "isCommentAuthor",
                        "isRecommended",
                        "isModified",
                        "isDeleted"
                )
                .containsExactly(
                        Tuple.tuple(originParentTechComment1.getId(),
                                member1.getId(),
                                member1.getNickname().getNickname(),
                                CommonResponseUtil.sliceAndMaskEmail(member1.getEmailAsString()),
                                originParentTechComment1.getContents().getCommentContents(),
                                originParentTechComment1.getReplyTotalCount().getCount(),
                                originParentTechComment1.getRecommendTotalCount().getCount(),
                                true,
                                true,
                                true,
                                false
                        )
                );

        TechCommentsResponse techCommentsResponse = response.get(0);
        List<TechRepliedCommentsResponse> replies = techCommentsResponse.getReplies();
        assertThat(replies).hasSize(1)
                .extracting(
                        "techCommentId",
                        "memberId",
                        "techParentCommentMemberId",
                        "techParentCommentId",
                        "techOriginParentCommentId",
                        "techParentCommentAuthor",
                        "author",
                        "maskedEmail",
                        "contents",
                        "recommendTotalCount",
                        "isCommentAuthor",
                        "isRecommended",
                        "isModified",
                        "isDeleted"
                ).containsExactly(
                        Tuple.tuple(repliedTechComment.getId(),
                                member3.getId(),
                                repliedTechComment.getParent().getCreatedBy().getId(),
                                repliedTechComment.getParent().getId(),
                                repliedTechComment.getOriginParent().getId(),
                                repliedTechComment.getOriginParent().getCreatedBy().getNicknameAsString(),
                                member3.getNickname().getNickname(),
                                CommonResponseUtil.sliceAndMaskEmail(member3.getEmailAsString()),
                                repliedTechComment.getContents().getCommentContents(),
                                repliedTechComment.getRecommendTotalCount().getCount(),
                                false,
                                false,
                                false,
                                false
                        )
                );
    }

    private TechCommentRecommend createTechCommentRecommend(Boolean recommendedStatus, TechComment techComment,
                                                            Member member) {
        TechCommentRecommend techCommentRecommend = TechCommentRecommend.builder()
                .recommendedStatus(recommendedStatus)
                .techComment(techComment)
                .member(member)
                .build();

        techCommentRecommend.changeTechComment(techComment);

        return techCommentRecommend;
    }

    private static TechComment createMainTechComment(CommentContents contents, Member createdBy,
                                                     TechArticle techArticle,
                                                     Count blameTotalCount, Count recommendTotalCount,
                                                     Count replyTotalCount) {
        return TechComment.builder()
                .contents(contents)
                .createdBy(createdBy)
                .techArticle(techArticle)
                .blameTotalCount(blameTotalCount)
                .recommendTotalCount(recommendTotalCount)
                .replyTotalCount(replyTotalCount)
                .build();
    }

    private static TechComment createRepliedTechComment(CommentContents contents, Member createdBy,
                                                        TechArticle techArticle,
                                                        TechComment originParent, TechComment parent,
                                                        Count blameTotalCount, Count recommendTotalCount,
                                                        Count replyTotalCount) {
        return TechComment.builder()
                .contents(contents)
                .createdBy(createdBy)
                .techArticle(techArticle)
                .blameTotalCount(blameTotalCount)
                .recommendTotalCount(recommendTotalCount)
                .replyTotalCount(replyTotalCount)
                .originParent(originParent)
                .parent(parent)
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

    private static Company createCompany(String companyName, String officialImageUrl, String officialUrl,
                                         String careerUrl) {
        return Company.builder()
                .name(new CompanyName(companyName))
                .officialUrl(new Url(officialUrl))
                .careerUrl(new Url(careerUrl))
                .officialImageUrl(officialImageUrl)
                .build();
    }
}
