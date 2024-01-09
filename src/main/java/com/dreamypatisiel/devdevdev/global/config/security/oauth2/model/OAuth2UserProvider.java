package com.dreamypatisiel.devdevdev.global.config.security.oauth2.model;

import java.util.List;
import java.util.Map;
import org.springframework.security.core.GrantedAuthority;

public interface OAuth2UserProvider {
    String getId();
    String getUserName();
    String getEmail();
    List<? extends GrantedAuthority> getAuthorities();
    Map<String, Object> getAttributes(); // 서비스 제공자로 부터 받는 값들
    String getSocialUserProvider();
}
