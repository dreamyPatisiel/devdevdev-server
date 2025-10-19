package com.dreamypatisiel.devdevdev.domain.service.pick;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.dreamypatisiel.devdevdev.domain.entity.enums.SocialType;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.UserPrincipal;
import com.dreamypatisiel.devdevdev.web.controller.ApiVersion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class PickServiceStrategyTest {

    @Autowired
    PickServiceStrategy pickServiceStrategy;

    @Mock
    Authentication authentication;

    @Mock
    SecurityContext securityContext;

    @Test
    @DisplayName("V1이면서 익명 사용자이면 GuestPickService를 반환한다.")
    void getPickService_V1_Anonymous() {
        // given
        when(authentication.getPrincipal()).thenReturn("anonymousUser");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // when
        PickService result = pickServiceStrategy.getPickService(ApiVersion.V1);

        // then
        assertThat(result).isInstanceOf(GuestPickService.class);
    }

    @Test
    @DisplayName("V1이면서 로그인 사용자이면 MemberPickService를 반환한다.")
    void getPickService_V1_Member() {
        // given
        UserPrincipal userPrincipal = UserPrincipal.createByEmailAndRoleAndSocialType(
                "email@test.com", "ROLE_USER", SocialType.KAKAO.name());

        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // when
        PickService result = pickServiceStrategy.getPickService(ApiVersion.V1);

        // then
        assertThat(result).isInstanceOf(MemberPickService.class);
    }

    @Test
    @DisplayName("V2이면서 익명 사용자이면 GuestPickServiceV2를 반환한다.")
    void getPickService_V2_Anonymous() {
        // given
        when(authentication.getPrincipal()).thenReturn("anonymousUser");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // when
        PickService result = pickServiceStrategy.getPickService(ApiVersion.V2);

        // then
        assertThat(result).isInstanceOf(GuestPickServiceV2.class);
    }

    @Test
    @DisplayName("V2이면서 로그인 사용자이면 MemberPickServiceV2를 반환한다.")
    void getPickService_V2_Member() {
        // given
        UserPrincipal userPrincipal = UserPrincipal.createByEmailAndRoleAndSocialType(
                "email@test.com", "ROLE_USER", SocialType.KAKAO.name());

        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // when
        PickService result = pickServiceStrategy.getPickService(ApiVersion.V2);

        // then
        assertThat(result).isInstanceOf(MemberPickServiceV2.class);
    }
}
