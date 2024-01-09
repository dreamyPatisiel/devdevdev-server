package com.dreamypatisiel.devdevdev.global.config.security.oauth2.model;

import com.dreamypatisiel.devdevdev.domain.entity.embedded.Password;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.user.OAuth2User;

@RequiredArgsConstructor
public abstract class SocialUserProvider implements OAuth2UserProvider {

    private final OAuth2User oAuth2User;
    private final ClientRegistration clientRegistration;

    @Override
    public List<? extends GrantedAuthority> getAuthorities() {
        return oAuth2User.getAuthorities().stream()
                .map(authority -> new SimpleGrantedAuthority(authority.getAuthority()))
                .toList();
    }

    @Override
    public String getSocialUserProvider() {
        return clientRegistration.getRegistrationId();
    }

    @Override
    public Map<String, Object> getAttributes() {
        return oAuth2User.getAttributes();
    }
}
