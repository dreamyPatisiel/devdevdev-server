package com.dreamypatisiel.devdevdev.global.config.security.oauth2.model;

import com.dreamypatisiel.devdevdev.domain.entity.SocialType;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Password;
import lombok.Builder;
import lombok.Data;

@Data
public class SocialMemberDto {
    private String userId;
    private String name;
    private String email;
    private String nickName;
    private String password;
    private SocialType socialType;

    @Builder
    private SocialMemberDto(String userId, String name, String email, String nickName, String password, SocialType socialType) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.nickName = nickName;
        this.password = password;
        this.socialType = socialType;
    }

    public static SocialMemberDto from(OAuth2UserProvider oAuth2UserProvider, String encodedPassword) {
        return SocialMemberDto.builder()
                .userId(oAuth2UserProvider.getId())
                .name(oAuth2UserProvider.getUserName())
                .email(oAuth2UserProvider.getEmail())
                .nickName(oAuth2UserProvider.getUserName())
                .socialType(oAuth2UserProvider.getSocialType())
                .password(encodedPassword)
                .build();
    }
}
