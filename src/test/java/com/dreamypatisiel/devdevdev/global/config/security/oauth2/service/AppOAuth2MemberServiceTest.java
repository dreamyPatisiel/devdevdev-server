package com.dreamypatisiel.devdevdev.global.config.security.oauth2.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.SocialType;
import com.dreamypatisiel.devdevdev.domain.repository.MemberRepository;
import com.dreamypatisiel.devdevdev.global.config.security.oauth2.model.OAuth2UserProvider;
import com.dreamypatisiel.devdevdev.global.config.security.oauth2.model.SocialMemberDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class AppOAuth2MemberServiceTest {

    @Autowired
    MemberRepository memberRepository;
    @Autowired
    AppOAuth2MemberService appOAuth2MemberService;

    @Test
    @DisplayName("가입한 이력이 없으면 데이터베이스에 회원을 저장한다.")
    void register() {
        // given
        String userId = "123";
        String userName = "user";
        String email = "user@kakao.com";
        SocialType socialType = SocialType.KAKAO;

        OAuth2UserProvider mockOAuth2UserProvider = mock(OAuth2UserProvider.class);
        when(mockOAuth2UserProvider.getId()).thenReturn(userId);
        when(mockOAuth2UserProvider.getUserName()).thenReturn(userName);
        when(mockOAuth2UserProvider.getSocialType()).thenReturn(socialType);
        when(mockOAuth2UserProvider.getEmail()).thenReturn(email);

        // when
        appOAuth2MemberService.register(mockOAuth2UserProvider);

        // then
        Member member = memberRepository.findByUserIdAndSocialType(userId, socialType).get();
        assertThat(member).isNotNull();
    }

    /**
     * 어떻게 테스트해야할까?
     */
//    @Test
//    @DisplayName("가입한 이력이 있으면 데이터베이스에 회원을 저장하지 않는다.")
//    void test() {
//        // given
//        String userId = "123";
//        String userName = "user";
//        String email = "user@kakao.com";
//        SocialType socialType = SocialType.KAKAO;
//
//        OAuth2UserProvider mockOAuth2UserProvider = mock(OAuth2UserProvider.class);
//        when(mockOAuth2UserProvider.getId()).thenReturn(userId);
//        when(mockOAuth2UserProvider.getUserName()).thenReturn(userName);
//        when(mockOAuth2UserProvider.getSocialType()).thenReturn(socialType);
//        when(mockOAuth2UserProvider.getEmail()).thenReturn(email);
//
//        SocialMemberDto socialMemberDto
//
//        memberRepository.save(Member.createMemberBy())
//        // when
//
//        // then
//        assertThatCode()
//                .doesNotThrowAnyException();
//    }
}