package com.dreamypatisiel.devdevdev.domain.service.techArticle;

import static com.dreamypatisiel.devdevdev.domain.exception.MemberExceptionMessage.INVALID_MEMBER_NOT_FOUND_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.TechArticleExceptionMessage.NOT_FOUND_TECH_ARTICLE_MESSAGE;
import static com.dreamypatisiel.devdevdev.elastic.domain.service.ElasticsearchSupportTest.FIRST_TECH_ARTICLE_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.dreamypatisiel.devdevdev.domain.entity.Company;
import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.TechArticle;
import com.dreamypatisiel.devdevdev.domain.entity.TechComment;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.CompanyName;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Count;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Url;
import com.dreamypatisiel.devdevdev.domain.entity.enums.Role;
import com.dreamypatisiel.devdevdev.domain.entity.enums.SocialType;
import com.dreamypatisiel.devdevdev.domain.repository.CompanyRepository;
import com.dreamypatisiel.devdevdev.domain.repository.member.MemberRepository;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechArticleRepository;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechCommentRepository;
import com.dreamypatisiel.devdevdev.domain.service.response.TechCommentRegisterResponse;
import com.dreamypatisiel.devdevdev.exception.MemberException;
import com.dreamypatisiel.devdevdev.exception.NotFoundException;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.SocialMemberDto;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.UserPrincipal;
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
    @DisplayName("회원은 댓글을 작성할 수 있다.")
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
        TechCommentRegisterResponse techCommentRegisterResponse = memberTechCommentService.registerTechComment(
                id, registerTechCommentRequest, authentication);
        em.flush();

        // then
        assertThat(techCommentRegisterResponse.getTechCommentId()).isNotNull();

        TechComment findTechComment = techCommentRepository.findById(techCommentRegisterResponse.getTechCommentId())
                .get();

        assertAll(
                () -> assertThat(findTechComment.getContents().getCommentContents()).isEqualTo("댓글입니다."),
                () -> assertThat(findTechComment.getBlameTotalCount().getCount()).isEqualTo(0L),
                () -> assertThat(findTechComment.getRecommendTotalCount().getCount()).isEqualTo(0L),
                () -> assertThat(findTechComment.getMember().getId()).isEqualTo(member.getId()),
                () -> assertThat(findTechComment.getTechArticle().getId()).isEqualTo(id)
        );
    }

    @Test
    @DisplayName("회원이 존재하지 않는 기술블로그에 댓글을 작성하면 예외가 발생한다.")
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
                () -> memberTechCommentService.registerTechComment(id, registerTechCommentRequest, authentication))
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

        Long id = FIRST_TECH_ARTICLE_ID;
        RegisterTechCommentRequest registerTechCommentRequest = new RegisterTechCommentRequest("댓글입니다.");

        // when // then
        assertThatThrownBy(
                () -> memberTechCommentService.registerTechComment(id, registerTechCommentRequest, authentication))
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
