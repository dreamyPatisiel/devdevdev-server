package com.dreamypatisiel.devdevdev.global.security.oauth2.model;

import com.dreamypatisiel.devdevdev.domain.entity.Role;
import com.dreamypatisiel.devdevdev.domain.entity.SocialType;
import lombok.Builder;
import lombok.Data;

@Data
public class SocialMemberDto {
    private String userId;
    private String name;
    private String email;
    private String nickname;
    private String password;
    private SocialType socialType;
    private Role role;

    @Builder
    private SocialMemberDto(String userId, String name, String email, String nickname, String password, SocialType socialType, Role role) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.nickname = nickname;
        this.password = password;
        this.socialType = socialType;
        this.role = role;
    }

    public static SocialMemberDto from(OAuth2UserProvider oAuth2UserProvider, String encodedPassword) {
        return SocialMemberDto.builder()
                .userId(oAuth2UserProvider.getId())
                .name(oAuth2UserProvider.getUserName())
                .email(oAuth2UserProvider.getEmail())
                .nickname(oAuth2UserProvider.getUserName())
                .socialType(oAuth2UserProvider.getSocialType())
                .role(Role.ROLE_USER)
                .password(encodedPassword)
                .build();
    }

    public static SocialMemberDto of(String email, String socialType, String role) {
        return SocialMemberDto.builder()
                .email(email)
                .socialType(SocialType.valueOf(socialType))
                .role(Role.valueOf(role))
                .build();
    }

    public static SocialMemberDto of(String email, String socialType, String role, String nickname) {
        return SocialMemberDto.builder()
                .email(email)
                .socialType(SocialType.valueOf(socialType))
                .role(Role.valueOf(role))
                .nickname(nickname)
                .build();
    }
}
