package com.dreamypatisiel.devdevdev.global.security.oauth2.service;

import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.OAuth2UserProvider;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.SocialUserProvider;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

/**
 * 로그인 순서(1)
 * userRequest를 이용해서 서버에 회원 정보 저장
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OAuth2UserServiceImpl implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    public static final String INVALID_SOCIAL_LOGIN_SUPPORT_MESSAGE = "지원하지 않은 소셜로그인 입니다.";

    private final OAuth2MemberService OAuth2MemberService;
    private final OAuth2UserService<OAuth2UserRequest, OAuth2User> defaultOAuth2UserService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        log.info("OAuth2UserServiceImpl");
        ClientRegistration clientRegistration = userRequest.getClientRegistration();
        OAuth2User oAuth2User = defaultOAuth2UserService.loadUser(userRequest);

        OAuth2UserProvider provider = OAuth2UserProvider.getOAuth2UserProvider(clientRegistration, oAuth2User);
        Member member = OAuth2MemberService.register(provider);
//        Collection<? extends GrantedAuthority> grantedAuthorities = provider.addAuthorities(member.getRole());
//        Collection<GrantedAuthority> oAuth2UserAuthorities = (Collection<GrantedAuthority>) oAuth2User.getAuthorities();
//        oAuth2UserAuthorities.addAll(grantedAuthorities); // from GPT4..
        /*

이 오류는 제네릭 타입 변환과 관련된 문제로 보입니다.
Collection<? extends GrantedAuthority> 타입을 다른 타입에 할당하려고 하고 있습니다.
이러한 경우에는 명시적인 타입 캐스팅이 필요할 수 있습니다.

아마도 oAuth2User.getAuthorities().add(grantedAuthorities); 부분에서 에러가 발생하는 것 같습니다.
oAuth2User.getAuthorities() 메서드가 어떤 타입을 반환하는지 알 수 없지만,
아마도 Collection<GrantedAuthority> 또는 그와 유사한 타입을 반환해야 합니다.

따라서 다음과 같이 명시적인 타입 캐스팅을 시도해 볼 수 있습니다:
이렇게 하면 grantedAuthorities를 oAuth2User의 권한 목록에 추가할 때 타입 캐스팅 문제가 해결될 수 있습니다.
그러나 주의해야 할 점은 실제로 oAuth2User.getAuthorities()가 어떤 타입을 반환하는지 확인하고 캐스팅을 시도해야 한다는 것입니다.
         */

        return oAuth2User;
    }
}
