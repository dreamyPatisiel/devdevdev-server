package com.dreamypatisiel.devdevdev.global.security.oauth2.model;

import com.dreamypatisiel.devdevdev.domain.entity.SocialType;
import java.util.Map;
import org.springframework.security.oauth2.core.user.OAuth2User;

/**
 * @Note:
 * <p>필수 동의 항목</p>
 * <ul>
 *     <li>profile_nickname</li>
 *     <li>profile_image</li>
 *     <li>account_email</li>
 * </ul>
 */
public class KakaoMember extends SocialUserProvider {

    public final static String KAKAO_ACCOUNT = "kakao_account";
    private final static String PROPERTIES = "properties";
    private final static String NICKNAME = "nickname";
    private final static String EMAIL = "email";
    private static final String ID = "id";
    private static final String DEFAULT_EMAIL = "user@example.com";

    private final Map<String, Object> kakaoAccountAttributes;
    private final Map<String, Object> propertiesAttributes;

    @SuppressWarnings("unchecked cast")
    public KakaoMember(OAuth2User oAuth2User) {
        super(oAuth2User);
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

    @Override
    public SocialType getSocialType() {
        return SocialType.KAKAO;
    }
}
