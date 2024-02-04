package com.dreamypatisiel.devdevdev.global.security.oauth2.model;

import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.SocialType;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Password;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

@RequiredArgsConstructor
public class UserPrincipal implements OAuth2User, UserDetails {

    private final String userId;
    @Getter
    private final String email;
    private final String password;
    @Getter
    private final SocialType socialType;
    private final Collection<? extends GrantedAuthority> authorities;
    @Setter(value = AccessLevel.PRIVATE)
    private Map<String, Object> attributes;

    public static UserPrincipal create(Member member) {
        List<SimpleGrantedAuthority> simpleGrantedAuthorities = Collections.singletonList(
                new SimpleGrantedAuthority(member.getRole().name()));

        String userId = createUserId();
        String password = new Password().getPassword();

        return new UserPrincipal(
                userId,
                member.getEmailAsString(),
                password,
                member.getSocialType(),
                simpleGrantedAuthorities
        );
    }

    public static UserPrincipal create(Member member, Map<String, Object> attributes) {
        UserPrincipal userPrincipal = UserPrincipal.create(member);
        userPrincipal.setAttributes(attributes);
        return userPrincipal;
    }

    public static UserPrincipal create(String email, String role, String socialType) {
        List<SimpleGrantedAuthority> simpleGrantedAuthorities = Collections.singletonList(
                new SimpleGrantedAuthority(role));

        return new UserPrincipal(
                createUserId(),
                email,
                new Password().getPassword(),
                SocialType.valueOf(socialType),
                simpleGrantedAuthorities
        );
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return userId;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getName() {
        return userId;
    }

    private static String createUserId() {
        return UUID.randomUUID().toString();
    }
}
