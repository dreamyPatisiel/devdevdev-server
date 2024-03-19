package com.dreamypatisiel.devdevdev.domain.service.techArticle;

import com.dreamypatisiel.devdevdev.domain.entity.Role;
import com.dreamypatisiel.devdevdev.domain.entity.SocialType;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechArticleSort;
import com.dreamypatisiel.devdevdev.domain.service.response.TechArticleResponse;
import com.dreamypatisiel.devdevdev.elastic.domain.service.ElasticsearchSupportTest;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

class GuestTechArticleServiceTestTest extends ElasticsearchSupportTest {

    @Autowired
    GuestTechArticleService guestTechArticleService;
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
    void findTechArticles() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        String elasticId = "";

        when(authentication.getPrincipal()).thenReturn("anonymousUser");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // when
        Slice<TechArticleResponse> techArticles = guestTechArticleService.findTechArticles(pageable, elasticId, TechArticleSort.LATEST, authentication);

        // then
        assertThat(techArticles)
                .hasSizeLessThanOrEqualTo(pageable.getPageSize());
    }

    @Test
    @DisplayName("커서 방식으로 익명 사용자 전용 기술블로그 메인을 조회할 때 익명 사용자가 아니면 예외가 발생한다.")
    void findTechArticlesException() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        UserPrincipal userPrincipal = UserPrincipal.createByEmailAndRoleAndSocialType(email, role, socialType);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // when // then
        assertThatThrownBy(() -> guestTechArticleService.findTechArticles(pageable, null, null, authentication))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(AuthenticationMemberUtils.INVALID_METHODS_CALL_MESSAGE);
    }
}