package com.dreamypatisiel.devdevdev.domain.service.techArticle;

import com.dreamypatisiel.devdevdev.domain.entity.SocialType;
import com.dreamypatisiel.devdevdev.elastic.domain.service.ElasticsearchSupportTest;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.UserPrincipal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TechArticleServiceStrategyTestTest extends ElasticsearchSupportTest {

    @Autowired
    TechArticleServiceStrategy techArticleServiceStrategy;
    @Mock
    Authentication authentication;
    @Mock
    SecurityContext securityContext;

    @Test
    @DisplayName("익명 사용자이면 GuestTechArticleService를 반환한다.")
    void getTechArticleServiceAnonymous() {
        // given
        when(authentication.getPrincipal()).thenReturn("anonymousUser");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // when
        TechArticleService result = techArticleServiceStrategy.getTechArticleService();

        // then
        assertThat(result).isInstanceOf(GuestTechArticleService.class);
    }

    @Test
    @DisplayName("익명 사용자가 아니면 MemberTechArticleService를 반환한다.")
    void getTechArticleService() {
        // given
        UserPrincipal userPrincipal = UserPrincipal.createByEmailAndRoleAndSocialType(
                "email", "role", SocialType.KAKAO.name());

        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // when
        TechArticleService result = techArticleServiceStrategy.getTechArticleService();

        // then
        assertThat(result).isInstanceOf(MemberTechArticleService.class);
    }
}