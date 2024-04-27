package com.dreamypatisiel.devdevdev.global.security.oauth2.model;

import static org.junit.jupiter.api.Assertions.*;

import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.enums.Role;
import com.dreamypatisiel.devdevdev.domain.entity.enums.SocialType;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class UserPrincipalTest {

    @Test
    @DisplayName("Member로 UserPrincipal 객체를 생성할 수 있다.")
    void createByMember() {
        // given
        String email = "dreamy5patisiel@kakao.com";
        String socialType = SocialType.KAKAO.name();
        String role = Role.ROLE_USER.name();
        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);

        // when
        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);

        // then
        assertAll(
                () -> assertThat(userPrincipal).isNotNull(),
                () -> assertThat(userPrincipal.getEmail()).isEqualTo(email),
                () -> assertThat(userPrincipal.getSocialType()).isEqualTo(SocialType.valueOf(socialType)),
                () -> assertThat(userPrincipal.getAuthorities()).hasSize(1)
                        .extracting("authority")
                        .contains(Role.ROLE_USER.name())
        );
    }

    @Test
    @DisplayName("Member와 attributes로 UserPrincipal 객체를 생성할 수 있다.")
    void createByMemberAndAttributes() {
        // given
        String email = "dreamy5patisiel@kakao.com";
        String socialType = SocialType.KAKAO.name();
        String role = Role.ROLE_USER.name();
        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);

        Map<String, Object> attributes = new HashMap<>();

        // when
        UserPrincipal userPrincipal = UserPrincipal.createByMemberAndAttributes(member, attributes);

        // then
        assertAll(
                () -> assertThat(userPrincipal).isNotNull(),
                () -> assertThat(userPrincipal.getEmail()).isEqualTo(email),
                () -> assertThat(userPrincipal.getSocialType()).isEqualTo(SocialType.valueOf(socialType)),
                () -> assertThat(userPrincipal.getAttributes()).isNotNull(),
                () -> assertThat(userPrincipal.getAuthorities()).hasSize(1)
                        .extracting("authority")
                        .contains(Role.ROLE_USER.name())
        );
    }

    @Test
    @DisplayName("이메일, 권한, 소셜타입으로 UserPrincipal 객체를 생성할 수 있다.")
    void createByEmailAndRoleAndSocialType() {
        // given
        String email = "dreamy5patisiel@kakao.com";
        String socialType = SocialType.KAKAO.name();
        String role = Role.ROLE_USER.name();

        // when
        UserPrincipal userPrincipal = UserPrincipal
                .createByEmailAndRoleAndSocialType(email, role, socialType);

        // then
        assertAll(
                () -> assertThat(userPrincipal).isNotNull(),
                () -> assertThat(userPrincipal.getEmail()).isEqualTo(email),
                () -> assertThat(userPrincipal.getSocialType()).isEqualTo(SocialType.valueOf(socialType)),
                () -> assertThat(userPrincipal.getAuthorities()).hasSize(1)
                        .extracting("authority")
                        .contains(Role.ROLE_USER.name())
        );
    }

    private SocialMemberDto createSocialDto(String userId, String name, String nickname, String password, String email, String socialType, String role) {
        return SocialMemberDto.builder()
                .userId(userId)
                .name(name)
                .nickname(nickname)
                .password(password)
                .email(email)
                .socialType(SocialType.valueOf(socialType))
                .role(Role.valueOf(role))
                .build();
    }
}