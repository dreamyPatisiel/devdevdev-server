package com.dreamypatisiel.devdevdev.global.config.security.oauth2.model;

import com.dreamypatisiel.devdevdev.domain.entity.embedded.Password;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.user.OAuth2User;

public class KakaoMember extends SocialUserProvider {

    private final static String KAKAO_ACCOUNT = "kakao_account";
    private final static String PROPERTIES = "properties"; //
    private final static String NICKNAME = "nickname";
    private final static String EMAIL = "email";
    private static final String ID = "id";
    private static final String DEFAULT_EMAIL = "user@example.com";

    private final Map<String, Object> kakaoAccountAttributes;
    private final Map<String, Object> propertiesAttributes;

    @SuppressWarnings("unchecked cast")
    public KakaoMember(OAuth2User oAuth2User, ClientRegistration clientRegistration) {
        super(oAuth2User, clientRegistration);
        this.kakaoAccountAttributes = (Map<String, Object>) oAuth2User.getAttributes().get(KAKAO_ACCOUNT);
        this.propertiesAttributes = (Map<String, Object>) oAuth2User.getAttributes().get(PROPERTIES);
    }

    @Override
    public String getId() {
        return getAttributes().get(ID).toString();
    }

    @Override
    public String getUserName() {
        return propertiesAttributes.get(NICKNAME).toString();
    }

    @Override
    public String getEmail() {
        if(kakaoAccountAttributes.containsKey(EMAIL)) {
            return kakaoAccountAttributes.get(EMAIL).toString();
        }
        return DEFAULT_EMAIL;
    }
}
