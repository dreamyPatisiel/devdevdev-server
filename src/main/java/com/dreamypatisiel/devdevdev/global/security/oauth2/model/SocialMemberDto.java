package com.dreamypatisiel.devdevdev.global.security.oauth2.model;

import com.dreamypatisiel.devdevdev.domain.entity.Role;
import com.dreamypatisiel.devdevdev.domain.entity.SocialType;
import java.util.Collections;
import lombok.Builder;
import lombok.Data;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Data
public class SocialMemberDto {
    private String userId;
    private String name;
    private String email;
    private String nickName;
    private String password;
    private SocialType socialType;
    private Role role;

    @Builder
    private SocialMemberDto(String userId, String name, String email, String nickName, String password, SocialType socialType, Role role) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.nickName = nickName;
        this.password = password;
        this.socialType = socialType;
        this.role = role;
    }

    public static SocialMemberDto of(OAuth2UserProvider oAuth2UserProvider, String encodedPassword) {
        return SocialMemberDto.builder()
                .userId(oAuth2UserProvider.getId())
                .name(oAuth2UserProvider.getUserName())
                .email(oAuth2UserProvider.getEmail())
                .nickName(oAuth2UserProvider.getUserName())
                .socialType(oAuth2UserProvider.getSocialType())
                .password(encodedPassword)
                .role(Role.ROLE_USER)
                .build();
    }
}