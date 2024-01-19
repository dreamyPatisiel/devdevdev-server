package com.dreamypatisiel.devdevdev.global.security.oauth2.model;

import com.dreamypatisiel.devdevdev.domain.entity.SocialType;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

public interface OAuth2UserProvider /*extends OAuth2User*/ {
    String getId();
    String getUserName();
    String getEmail();
    List<? extends GrantedAuthority> getAuthorities();
    Map<String, Object> getAttributes(); // 서비스 제공자로 부터 받는 값들
    String getSocialUserProvider();
    SocialType getSocialType();
}
