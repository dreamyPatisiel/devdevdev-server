package com.dreamypatisiel.devdevdev.domain.service.techArticle;

import com.dreamypatisiel.devdevdev.domain.entity.Company;
import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.TechArticle;
import com.dreamypatisiel.devdevdev.domain.entity.TechComment;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.CommentContents;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.CompanyName;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Count;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Url;
import com.dreamypatisiel.devdevdev.domain.entity.enums.Role;
import com.dreamypatisiel.devdevdev.domain.entity.enums.SocialType;
import com.dreamypatisiel.devdevdev.domain.repository.CompanyRepository;
import com.dreamypatisiel.devdevdev.domain.repository.member.MemberRepository;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechArticleRepository;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechCommentRepository;
import com.dreamypatisiel.devdevdev.domain.service.response.TechCommentResponse;
import com.dreamypatisiel.devdevdev.exception.MemberException;
import com.dreamypatisiel.devdevdev.exception.NotFoundException;
import com.dreamypatisiel.devdevdev.global.common.TimeProvider;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.SocialMemberDto;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.UserPrincipal;
import com.dreamypatisiel.devdevdev.web.controller.techArticle.request.ModifyTechCommentRequest;
import com.dreamypatisiel.devdevdev.web.controller.techArticle.request.RegisterTechCommentRequest;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.transaction.annotation.Transactional;

import static com.dreamypatisiel.devdevdev.domain.exception.MemberExceptionMessage.INVALID_MEMBER_NOT_FOUND_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.TechArticleExceptionMessage.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

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
        company = companyRepository.save(company);

        TechArticle techArticle = TechArticle.createTechArticle(new Url("https://example.com"), new Count(1L),
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
        company = companyRepository.save(company);

        TechArticle techArticle = TechArticle.createTechArticle(new Url("https://example.com"), new Count(1L),
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
    @DisplayName("회원은 본인이 작성한 삭제되지 않은 댓글을 수정할 수 있다.")
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
        company = companyRepository.save(company);

        TechArticle techArticle = TechArticle.createTechArticle(new Url("https://example.com"), new Count(1L),
                new Count(1L), new Count(1L), new Count(1L), null, company);
        techArticleRepository.save(techArticle);
        Long techArticleId = techArticle.getId();

        TechComment techComment = TechComment.createMainTechComment(new CommentContents("댓글입니다"), member, techArticle);
        techCommentRepository.save(techComment);
        Long techCommentId = techComment.getId();

        ModifyTechCommentRequest modifyTechCommentRequest = new ModifyTechCommentRequest("댓글 수정입니다.");

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
                () -> assertThat(findTechComment.getId()).isEqualTo(techCommentId)
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
        company = companyRepository.save(company);

        TechArticle techArticle = TechArticle.createTechArticle(new Url("https://example.com"), new Count(1L),
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
        company = companyRepository.save(company);

        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        TechArticle techArticle = TechArticle.createTechArticle(new Url("https://example.com"), new Count(1L),
                new Count(1L), new Count(1L), new Count(1L), null, company);
        techArticleRepository.save(techArticle);
        Long techArticleId = techArticle.getId();

        TechComment techComment = TechComment.createMainTechComment(new CommentContents("댓글입니다"), member, techArticle);
        techCommentRepository.save(techComment);
        Long techCommentId = techComment.getId();

        techComment.changeDeletedAt(timeProvider.getLocalDateTimeNow(), member);
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
        company = companyRepository.save(company);

        TechArticle techArticle = TechArticle.createTechArticle(new Url("https://example.com"), new Count(1L),
                new Count(1L), new Count(1L), new Count(1L), null, company);
        techArticleRepository.save(techArticle);
        Long techArticleId = techArticle.getId();

        TechComment techComment = TechComment.createMainTechComment(new CommentContents("댓글입니다"), member, techArticle);
        techCommentRepository.save(techComment);
        Long techCommentId = techComment.getId();

        em.flush();

        // when
        memberTechCommentService.deleteTechComment(techArticleId, techCommentId, authentication);

        // then
        TechComment findTechComment = techCommentRepository.findById(techCommentId).get();

        assertAll(
                () -> assertThat(findTechComment.getDeletedAt()).isNotNull(),
                () -> assertThat(findTechComment.getDeletedBy().getId()).isEqualTo(member.getId())
        );
    }

    @Test
    @DisplayName("회원이 댓글을 삭제할 때, 이미 삭제된 댓글이라면 예외가 발생한다.")
    void deleteTechCommentAlreadyDeletedException() {
        // given
        Company company = createCompany("꿈빛 파티시엘", "https://example.png", "https://example.com", "https://example.com");
        company = companyRepository.save(company);

        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        TechArticle techArticle = TechArticle.createTechArticle(new Url("https://example.com"), new Count(1L),
                new Count(1L), new Count(1L), new Count(1L), null, company);
        techArticleRepository.save(techArticle);
        Long techArticleId = techArticle.getId();

        TechComment techComment = TechComment.createMainTechComment(new CommentContents("댓글입니다"), member, techArticle);
        techCommentRepository.save(techComment);
        Long techCommentId = techComment.getId();

        techComment.changeDeletedAt(timeProvider.getLocalDateTimeNow(), member);
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
        company = companyRepository.save(company);

        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        TechArticle techArticle = TechArticle.createTechArticle(new Url("https://example.com"), new Count(1L),
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
        company = companyRepository.save(company);

        TechArticle techArticle = TechArticle.createTechArticle(new Url("https://example.com"), new Count(1L),
                new Count(1L), new Count(1L), new Count(1L), null, company);
        techArticleRepository.save(techArticle);
        Long techArticleId = techArticle.getId();

        TechComment techComment = TechComment.createMainTechComment(new CommentContents("댓글입니다"), member, techArticle);
        techCommentRepository.save(techComment);
        Long techCommentId = techComment.getId();

        em.flush();

        // when
        memberTechCommentService.deleteTechComment(techArticleId, techCommentId, authentication);

        // then
        TechComment findTechComment = techCommentRepository.findById(techCommentId).get();

        assertAll(
                () -> assertThat(findTechComment.getDeletedAt()).isNotNull(),
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
        company = companyRepository.save(company);

        TechArticle techArticle = TechArticle.createTechArticle(new Url("https://example.com"), new Count(1L),
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
        company = companyRepository.save(company);

        TechArticle techArticle = TechArticle.createTechArticle(new Url("https://example.com"), new Count(1L),
                new Count(1L), new Count(1L), new Count(1L), null, company);
        techArticleRepository.save(techArticle);
        Long techArticleId = techArticle.getId();

        TechComment parentTechComment = TechComment.createMainTechComment(new CommentContents("댓글입니다."), member, techArticle);
        techCommentRepository.save(parentTechComment);
        Long parentTechCommentId = parentTechComment.getId();

        RegisterTechCommentRequest registerRepliedTechComment = new RegisterTechCommentRequest("답글입니다.");

        // when
        TechCommentResponse techCommentResponse = memberTechCommentService.registerRepliedTechComment(
                techArticleId, parentTechCommentId, parentTechCommentId, registerRepliedTechComment, authentication);
        em.flush();

        // then
        assertThat(techCommentResponse.getTechCommentId()).isNotNull();

        TechComment findRepliedTechComment = techCommentRepository.findById(techCommentResponse.getTechCommentId()).get();

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
                () -> assertThat(findRepliedTechComment.getOriginParent().getReplyTotalCount().getCount()).isEqualTo(1L),
                // 기술블로그 댓글 수 증가 확인
                () -> assertThat(findRepliedTechComment.getTechArticle().getCommentTotalCount().getCount()).isEqualTo(2L)
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
        company = companyRepository.save(company);

        TechArticle techArticle = TechArticle.createTechArticle(new Url("https://example.com"), new Count(1L),
                new Count(1L), new Count(2L), new Count(1L), null, company);
        techArticleRepository.save(techArticle);
        Long techArticleId = techArticle.getId();

        TechComment originParentTechComment = TechComment.createMainTechComment(new CommentContents("댓글입니다."), member, techArticle);
        techCommentRepository.save(originParentTechComment);
        Long originParentTechCommentId = originParentTechComment.getId();

        TechComment parentTechComment = TechComment.createRepliedTechComment(new CommentContents("답글입니다."), member,
                techArticle, originParentTechComment, originParentTechComment);
        techCommentRepository.save(parentTechComment);
        Long parentTechCommentId = parentTechComment.getId();

        RegisterTechCommentRequest registerRepliedTechComment = new RegisterTechCommentRequest("답글입니다.");

        // when
        TechCommentResponse techCommentResponse = memberTechCommentService.registerRepliedTechComment(
                techArticleId, originParentTechCommentId, parentTechCommentId, registerRepliedTechComment, authentication);
        em.flush();

        // then
        assertThat(techCommentResponse.getTechCommentId()).isNotNull();

        TechComment findRepliedTechComment = techCommentRepository.findById(techCommentResponse.getTechCommentId()).get();

        assertAll(
                () -> assertThat(findRepliedTechComment.getContents().getCommentContents()).isEqualTo("답글입니다."),
                () -> assertThat(findRepliedTechComment.getBlameTotalCount().getCount()).isEqualTo(0L),
                () -> assertThat(findRepliedTechComment.getRecommendTotalCount().getCount()).isEqualTo(0L),
                () -> assertThat(findRepliedTechComment.getReplyTotalCount().getCount()).isEqualTo(0L),
                () -> assertThat(findRepliedTechComment.getCreatedBy().getId()).isEqualTo(member.getId()),
                () -> assertThat(findRepliedTechComment.getParent().getId()).isEqualTo(parentTechCommentId),
                () -> assertThat(findRepliedTechComment.getOriginParent().getId()).isEqualTo(originParentTechCommentId),
                // 최상단 댓글의 답글 수 증가 확인
                () -> assertThat(findRepliedTechComment.getOriginParent().getReplyTotalCount().getCount()).isEqualTo(1L),
                // 기술블로그 댓글 수 증가 확인
                () -> assertThat(findRepliedTechComment.getTechArticle().getCommentTotalCount().getCount()).isEqualTo(3L)
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
        company = companyRepository.save(company);

        TechArticle techArticle = TechArticle.createTechArticle(new Url("https://example.com"), new Count(1L),
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
        company = companyRepository.save(company);

        TechArticle techArticle = TechArticle.createTechArticle(new Url("https://example.com"), new Count(1L),
                new Count(1L), new Count(1L), new Count(1L), null, company);
        techArticleRepository.save(techArticle);
        Long techArticleId = techArticle.getId();

        TechComment techComment = TechComment.createMainTechComment(new CommentContents("댓글입니다."), member, techArticle);
        techCommentRepository.save(techComment);
        Long techCommentId = techComment.getId();
        techComment.changeDeletedAt(timeProvider.getLocalDateTimeNow(), member);

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
