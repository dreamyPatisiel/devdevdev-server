package com.dreamypatisiel.devdevdev.web.controller.techArticle;

import static com.dreamypatisiel.devdevdev.domain.exception.MemberExceptionMessage.INVALID_MEMBER_NOT_FOUND_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.TechArticleExceptionMessage.NOT_FOUND_TECH_ARTICLE_MESSAGE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import com.dreamypatisiel.devdevdev.domain.repository.BookmarkRepository;
import com.dreamypatisiel.devdevdev.domain.repository.CompanyRepository;
import com.dreamypatisiel.devdevdev.domain.repository.member.MemberRepository;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechArticleRepository;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechCommentRepository;
import com.dreamypatisiel.devdevdev.elastic.domain.repository.ElasticTechArticleRepository;
import com.dreamypatisiel.devdevdev.global.constant.SecurityConstant;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.SocialMemberDto;
import com.dreamypatisiel.devdevdev.web.controller.SupportControllerTest;
import com.dreamypatisiel.devdevdev.web.controller.techArticle.request.ModifyTechCommentRequest;
import com.dreamypatisiel.devdevdev.web.controller.techArticle.request.RegisterTechCommentRequest;
import com.dreamypatisiel.devdevdev.web.response.ResultType;
import jakarta.persistence.EntityManager;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

class TechArticleCommentControllerTest extends SupportControllerTest {

    @Autowired
    TechArticleRepository techArticleRepository;
    @Autowired
    ElasticTechArticleRepository elasticTechArticleRepository;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    BookmarkRepository bookmarkRepository;
    @Autowired
    CompanyRepository companyRepository;
    @Autowired
    TechCommentRepository techCommentRepository;
    @Autowired
    EntityManager em;

    @Test
    @DisplayName("익명 사용자는 기술블로그 댓글을 작성할 수 없다.")
    void registerTechCommentByAnonymous() throws Exception {
        // given
        Company company = createCompany("꿈빛 파티시엘", "https://example.png", "https://example.com", "https://example.com");
        company = companyRepository.save(company);

        TechArticle techArticle = TechArticle.createTechArticle(new Url("https://example.com"), new Count(1L),
                new Count(1L),
                new Count(1L),
                new Count(1L), null, company);
        techArticleRepository.save(techArticle);
        Long id = techArticle.getId();

        RegisterTechCommentRequest registerTechCommentRequest = new RegisterTechCommentRequest("댓글 내용입니다.");

        // when // then
        mockMvc.perform(post("/devdevdev/api/v1/articles/{id}/comments", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(om.writeValueAsString(registerTechCommentRequest)))
                .andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.resultType").value(ResultType.FAIL.name()))
                .andExpect(jsonPath("$.message").isString())
                .andExpect(jsonPath("$.errorCode").value(HttpStatus.METHOD_NOT_ALLOWED.value()));
    }

    @Test
    @DisplayName("회원은 기술블로그 댓글을 작성할 수 있다.")
    void registerTechComment() throws Exception {
        // given
        Company company = createCompany("꿈빛 파티시엘", "https://example.png", "https://example.com", "https://example.com");
        company = companyRepository.save(company);

        TechArticle techArticle = TechArticle.createTechArticle(new Url("https://example.com"), new Count(1L),
                new Count(1L),
                new Count(1L),
                new Count(1L), null, company);
        techArticleRepository.save(techArticle);
        Long id = techArticle.getId();

        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        member.updateRefreshToken(refreshToken);
        memberRepository.save(member);

        RegisterTechCommentRequest registerTechCommentRequest = new RegisterTechCommentRequest("댓글 내용입니다.");

        // when // then
        mockMvc.perform(post("/devdevdev/api/v1/articles/{id}/comments", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header(SecurityConstant.AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                        .content(om.writeValueAsString(registerTechCommentRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultType").value(ResultType.SUCCESS.name()))
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andExpect(jsonPath("$.data").isMap())
                .andExpect(jsonPath("$.data.techCommentId").isNumber());
    }

    @Test
    @DisplayName("회원이 기술블로그 댓글을 작성할 때 존재하지 않는 기술블로그라면 예외가 발생한다.")
    void registerTechCommentNotFoundTechArticleException() throws Exception {
        // given
        Company company = createCompany("꿈빛 파티시엘", "https://example.png", "https://example.com", "https://example.com");
        company = companyRepository.save(company);

        TechArticle techArticle = TechArticle.createTechArticle(new Url("https://example.com"), new Count(1L),
                new Count(1L),
                new Count(1L),
                new Count(1L), null, company);
        TechArticle savedTechArticle = techArticleRepository.save(techArticle);
        Long id = savedTechArticle.getId() + 1;

        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        member.updateRefreshToken(refreshToken);
        memberRepository.save(member);

        RegisterTechCommentRequest registerTechCommentRequest = new RegisterTechCommentRequest("댓글 내용입니다.");

        // when // then
        mockMvc.perform(post("/devdevdev/api/v1/articles/{id}/comments", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header(SecurityConstant.AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                        .content(om.writeValueAsString(registerTechCommentRequest)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultType").value(ResultType.FAIL.name()))
                .andExpect(jsonPath("$.message").value(NOT_FOUND_TECH_ARTICLE_MESSAGE))
                .andExpect(jsonPath("$.errorCode").value(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    @DisplayName("회원이 기술블로그 댓글을 작성할 때 존재하지 않는 회원이라면 예외가 발생한다.")
    void registerTechCommentNotFoundMemberException() throws Exception {
        // given
        Company company = createCompany("꿈빛 파티시엘", "https://example.png", "https://example.com", "https://example.com");
        company = companyRepository.save(company);

        TechArticle techArticle = TechArticle.createTechArticle(new Url("https://example.com"), new Count(1L),
                new Count(1L),
                new Count(1L),
                new Count(1L), null, company);
        TechArticle savedTechArticle = techArticleRepository.save(techArticle);
        Long id = savedTechArticle.getId();

        RegisterTechCommentRequest registerTechCommentRequest = new RegisterTechCommentRequest("댓글 내용입니다.");

        // when // then
        mockMvc.perform(post("/devdevdev/api/v1/articles/{id}/comments", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header(SecurityConstant.AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                        .content(om.writeValueAsString(registerTechCommentRequest)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultType").value(ResultType.FAIL.name()))
                .andExpect(jsonPath("$.message").value(INVALID_MEMBER_NOT_FOUND_MESSAGE))
                .andExpect(jsonPath("$.errorCode").value(HttpStatus.NOT_FOUND.value()));
    }

    @ParameterizedTest
    @EmptySource
    @DisplayName("회원이 기술블로그 댓글을 작성할 때 댓글 내용이 공백이라면 예외가 발생한다.")
    void registerTechCommentContentsIsNullException(String contents) throws Exception {
        // given
        Company company = createCompany("꿈빛 파티시엘", "https://example.png", "https://example.com", "https://example.com");
        company = companyRepository.save(company);

        TechArticle techArticle = TechArticle.createTechArticle(new Url("https://example.com"), new Count(1L),
                new Count(1L),
                new Count(1L),
                new Count(1L), null, company);
        TechArticle savedTechArticle = techArticleRepository.save(techArticle);
        Long id = savedTechArticle.getId();

        RegisterTechCommentRequest registerTechCommentRequest = new RegisterTechCommentRequest(contents);

        // when // then
        mockMvc.perform(post("/devdevdev/api/v1/articles/{id}/comments", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header(SecurityConstant.AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                        .content(om.writeValueAsString(registerTechCommentRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultType").value(ResultType.FAIL.name()))
                .andExpect(jsonPath("$.message").isString())
                .andExpect(jsonPath("$.errorCode").value(HttpStatus.BAD_REQUEST.value()));
    }

    @Test
    @DisplayName("회원은 기술블로그 댓글을 수정할 수 있다.")
    void modifyTechComment() throws Exception {
        // given
        Company company = createCompany("꿈빛 파티시엘", "https://example.png", "https://example.com", "https://example.com");
        company = companyRepository.save(company);

        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        member.updateRefreshToken(refreshToken);
        memberRepository.save(member);

        TechArticle techArticle = TechArticle.createTechArticle(new Url("https://example.com"), new Count(1L),
                new Count(1L), new Count(1L), new Count(1L), null, company);
        techArticleRepository.save(techArticle);
        Long techArticleId = techArticle.getId();

        TechComment techComment = TechComment.create(new CommentContents("댓글입니다"), member, techArticle);
        techCommentRepository.save(techComment);
        Long techCommentId = techComment.getId();

        ModifyTechCommentRequest modifyTechCommentRequest = new ModifyTechCommentRequest("댓글 수정입니다.");

        // when // then
        mockMvc.perform(patch("/devdevdev/api/v1/articles/{techArticleId}/comments/{techCommentId}", techArticleId,
                        techCommentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header(SecurityConstant.AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                        .content(om.writeValueAsString(modifyTechCommentRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultType").value(ResultType.SUCCESS.name()))
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andExpect(jsonPath("$.data").isMap())
                .andExpect(jsonPath("$.data.techCommentId").isNumber());
    }

    @ParameterizedTest
    @EmptySource
    @DisplayName("회원이 기술블로그 댓글을 수정할 때 댓글 내용이 공백이라면 예외가 발생한다.")
    void modifyTechCommentContentsIsNullException(String contents) throws Exception {
        // given
        Company company = createCompany("꿈빛 파티시엘", "https://example.png", "https://example.com", "https://example.com");
        company = companyRepository.save(company);

        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        member.updateRefreshToken(refreshToken);
        memberRepository.save(member);

        TechArticle techArticle = TechArticle.createTechArticle(new Url("https://example.com"), new Count(1L),
                new Count(1L), new Count(1L), new Count(1L), null, company);
        techArticleRepository.save(techArticle);
        Long techArticleId = techArticle.getId();

        TechComment techComment = TechComment.create(new CommentContents("댓글입니다"), member, techArticle);
        techCommentRepository.save(techComment);
        Long techCommentId = techComment.getId();

        ModifyTechCommentRequest modifyTechCommentRequest = new ModifyTechCommentRequest(contents);

        // when // then
        mockMvc.perform(patch("/devdevdev/api/v1/articles/{techArticleId}/comments/{techCommentId}", techArticleId,
                        techCommentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header(SecurityConstant.AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                        .content(om.writeValueAsString(modifyTechCommentRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultType").value(ResultType.FAIL.name()))
                .andExpect(jsonPath("$.message").isString())
                .andExpect(jsonPath("$.errorCode").value(HttpStatus.BAD_REQUEST.value()));
    }

    @Test
    @DisplayName("회원이 기술블로그 댓글을 수정할 때 댓글이 존재하지 않으면 예외가 발생한다.")
    void modifyTechCommentNotFoundException() throws Exception {
        // given
        Company company = createCompany("꿈빛 파티시엘", "https://example.png", "https://example.com", "https://example.com");
        company = companyRepository.save(company);

        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        member.updateRefreshToken(refreshToken);
        memberRepository.save(member);

        TechArticle techArticle = TechArticle.createTechArticle(new Url("https://example.com"), new Count(1L),
                new Count(1L), new Count(1L), new Count(1L), null, company);
        techArticleRepository.save(techArticle);
        Long techArticleId = techArticle.getId();

        ModifyTechCommentRequest modifyTechCommentRequest = new ModifyTechCommentRequest("댓글 수정입니다");

        // when // then
        mockMvc.perform(patch("/devdevdev/api/v1/articles/{techArticleId}/comments/{techCommentId}", techArticleId, 0L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header(SecurityConstant.AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                        .content(om.writeValueAsString(modifyTechCommentRequest)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultType").value(ResultType.FAIL.name()))
                .andExpect(jsonPath("$.message").isString())
                .andExpect(jsonPath("$.errorCode").value(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    @DisplayName("회원이 기술블로그 댓글을 수정할 때 댓글 내용이 공백이라면 예외가 발생한다.")
    void modifyTechCommentAlreadyDeletedException() throws Exception {
        // given
        Company company = createCompany("꿈빛 파티시엘", "https://example.png", "https://example.com", "https://example.com");
        company = companyRepository.save(company);

        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        member.updateRefreshToken(refreshToken);
        memberRepository.save(member);

        TechArticle techArticle = TechArticle.createTechArticle(new Url("https://example.com"), new Count(1L),
                new Count(1L), new Count(1L), new Count(1L), null, company);
        techArticleRepository.save(techArticle);
        Long techArticleId = techArticle.getId();

        TechComment techComment = TechComment.create(new CommentContents("댓글입니다"), member, techArticle);
        techCommentRepository.save(techComment);
        Long techCommentId = techComment.getId();

        techComment.changeDeletedAt(LocalDateTime.now());
        em.flush();

        ModifyTechCommentRequest modifyTechCommentRequest = new ModifyTechCommentRequest("댓글 수정");

        // when // then
        mockMvc.perform(patch("/devdevdev/api/v1/articles/{techArticleId}/comments/{techCommentId}", techArticleId,
                        techCommentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header(SecurityConstant.AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                        .content(om.writeValueAsString(modifyTechCommentRequest)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultType").value(ResultType.FAIL.name()))
                .andExpect(jsonPath("$.message").isString())
                .andExpect(jsonPath("$.errorCode").value(HttpStatus.NOT_FOUND.value()));
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
                .officialImageUrl(officialImageUrl)
                .careerUrl(new Url(careerUrl))
                .officialUrl(new Url(officialUrl))
                .build();
    }
}