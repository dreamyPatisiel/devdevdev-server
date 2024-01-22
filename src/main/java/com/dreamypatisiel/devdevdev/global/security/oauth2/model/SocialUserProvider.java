package com.dreamypatisiel.devdevdev.global.security.oauth2.model;

import static com.dreamypatisiel.devdevdev.global.security.oauth2.service.OAuth2UserServiceImpl.INVALID_SOCIAL_LOGIN_SUPPORT_MESSAGE;

import com.dreamypatisiel.devdevdev.domain.entity.Role;
import com.dreamypatisiel.devdevdev.domain.entity.SocialType;
import com.dreamypatisiel.devdevdev.exception.OAuth2UserProviderException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.util.ObjectUtils;

@RequiredArgsConstructor
public abstract class SocialUserProvider implements OAuth2UserProvider {

    private final OAuth2User oAuth2User;
    private String socialType;

    @Override
    public List<? extends GrantedAuthority> getAuthorities() {
        return oAuth2User.getAuthorities().stream()
                .map(authority -> new SimpleGrantedAuthority(authority.getAuthority()))
                .toList();
    }

    @Override
    public String getSocialUserProvider() {
        return socialType;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return oAuth2User.getAttributes();
    }

    @Override
    public Role getRole() {
        GrantedAuthority grantedAuthority = oAuth2User.getAuthorities().stream()
                .findFirst()
                .orElse(null);

        if(!ObjectUtils.isEmpty(grantedAuthority) && "OAUTH2_USER".equals(grantedAuthority.getAuthority())) {
            return Role.ROLE_USER;
        }

        return Role.ROLE_ADMIN;
    }
}
