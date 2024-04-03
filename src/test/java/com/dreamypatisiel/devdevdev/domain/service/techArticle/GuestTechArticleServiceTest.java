package com.dreamypatisiel.devdevdev.domain.service.techArticle;

import com.dreamypatisiel.devdevdev.domain.entity.Role;
import com.dreamypatisiel.devdevdev.domain.entity.SocialType;
import com.dreamypatisiel.devdevdev.domain.entity.TechArticle;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Count;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Url;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechArticleRepository;
import com.dreamypatisiel.devdevdev.domain.service.response.TechArticleResponse;
import com.dreamypatisiel.devdevdev.elastic.domain.service.ElasticsearchSupportTest;
import com.dreamypatisiel.devdevdev.exception.NotFoundException;
import com.dreamypatisiel.devdevdev.exception.TechArticleException;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.UserPrincipal;
import com.dreamypatisiel.devdevdev.global.utils.AuthenticationMemberUtils;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;

import static com.dreamypatisiel.devdevdev.domain.exception.TechArticleExceptionMessage.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

class GuestTechArticleServiceTest extends ElasticsearchSupportTest {

    @Autowired
    GuestTechArticleService guestTechArticleService;
    @Autowired
    TechArticleRepository techArticleRepository;
    @Autowired
    EntityManager em;
    @Mock
    Authentication authentication;
    @Mock
    SecurityContext securityContext;

    String email = "dreamy5patisiel@kakao.com";
    String socialType = SocialType.KAKAO.name();
    String role = Role.ROLE_USER.name();

    @Test
    @DisplayName("익명 사용자가 커서 방식으로 기술블로그를 조회하여 응답을 생성한다.")
    void getTechArticles() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        when(authentication.getPrincipal()).thenReturn("anonymousUser");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // when
        Slice<TechArticleResponse> techArticles = guestTechArticleService.getTechArticles(pageable, null, null, null, null, authentication);

        // then
        assertThat(techArticles)
                .hasSize(pageable.getPageSize());
    }

    @Test
    @DisplayName("커서 방식으로 익명 사용자 전용 기술블로그 메인을 조회할 때 익명 사용자가 아니면 예외가 발생한다.")
    void getTechArticlesNotAnonymousUserException() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        UserPrincipal userPrincipal = UserPrincipal.createByEmailAndRoleAndSocialType(email, role, socialType);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // when // then
        assertThatThrownBy(() -> guestTechArticleService.getTechArticles(pageable, null, null, null, null, authentication))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(AuthenticationMemberUtils.INVALID_METHODS_CALL_MESSAGE);
    }

    @Test
    @DisplayName("익명 사용자가 기술블로그 상세를 조회한다. 이때 북마크 여부는 응답에 포함되지 않는다.")
    void getTechArticle() {
        // given
        Long id = FIRST_TECH_ARTICLE_ID;

        when(authentication.getPrincipal()).thenReturn("anonymousUser");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // when
        TechArticleResponse techArticleResponse = guestTechArticleService.getTechArticle(id, authentication);

        // then
        assertThat(techArticleResponse)
                .isNotNull()
                .isInstanceOf(TechArticleResponse.class)
                .satisfies(article -> {
                    assertThat(article.getId()).isNotNull();
                    assertThat(article.getIsBookmarked()).isNull();
                });
    }

    @Test
    @DisplayName("익명 사용자가 기술블로그 상세를 조회할 때 익명 사용자가 아니면 예외가 발생한다.")
    void getTechArticleNotAnonymousUserException() {
        // given
        Long id = FIRST_TECH_ARTICLE_ID;

        UserPrincipal userPrincipal = UserPrincipal.createByEmailAndRoleAndSocialType(email, role, socialType);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // when // then
        assertThatThrownBy(() -> guestTechArticleService.getTechArticle(id, authentication))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(AuthenticationMemberUtils.INVALID_METHODS_CALL_MESSAGE);
    }

    @Test
    @DisplayName("익명 사용자가 기술블로그 상세를 조회할 때 기술블로그가 존재하지 않으면 예외가 발생한다.")
    void getTechArticleNotFoundTechArticleException() {
        // given
        TechArticle techArticle = TechArticle.of(new Url("https://example.com"), new Count(1L), new Count(1L), new Count(1L),
                new Count(1L), null, null);
        TechArticle savedTechArticle = techArticleRepository.save(techArticle);
        Long id = savedTechArticle.getId()+1;

        when(authentication.getPrincipal()).thenReturn("anonymousUser");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // when // then
        assertThatThrownBy(() -> guestTechArticleService.getTechArticle(id, authentication))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(NOT_FOUND_TECH_ARTICLE_MESSAGE);
    }

    @Test
    @DisplayName("익명 사용자가 기술블로그 상세를 조회할 때 엘라스틱ID가 존재하지 않으면 예외가 발생한다.")
    void getTechArticleNotFoundElasticIdException() {
        // given
        TechArticle techArticle = TechArticle.of(new Url("https://example.com"), new Count(1L), new Count(1L), new Count(1L),
                new Count(1L), null, null);
        TechArticle savedTechArticle = techArticleRepository.save(techArticle);
        Long id = savedTechArticle.getId();

        when(authentication.getPrincipal()).thenReturn("anonymousUser");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // when // then
        assertThatThrownBy(() -> guestTechArticleService.getTechArticle(id, authentication))
                .isInstanceOf(TechArticleException.class)
                .hasMessage(NOT_FOUND_ELASTIC_ID_MESSAGE);
    }

    @Test
    @DisplayName("익명 사용자가 기술블로그 상세를 조회할 때 엘라스틱 기술블로그가 존재하지 않으면 예외가 발생한다.")
    void getTechArticleNotFoundElasticTechArticleException() {
        // given
        TechArticle techArticle = TechArticle.of(new Url("https://example.com"), new Count(1L), new Count(1L), new Count(1L),
                new Count(1L), "elasticId", null);
        TechArticle savedTechArticle = techArticleRepository.save(techArticle);
        Long id = savedTechArticle.getId();

        when(authentication.getPrincipal()).thenReturn("anonymousUser");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // when // then
        assertThatThrownBy(() -> guestTechArticleService.getTechArticle(id, authentication))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(NOT_FOUND_ELASTIC_TECH_ARTICLE_MESSAGE);
    }
}