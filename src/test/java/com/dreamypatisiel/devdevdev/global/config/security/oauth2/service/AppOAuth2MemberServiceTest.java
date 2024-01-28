package com.dreamypatisiel.devdevdev.global.config.security.oauth2.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.SocialType;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Email;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Nickname;
import com.dreamypatisiel.devdevdev.domain.repository.MemberRepository;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.OAuth2UserProvider;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.SocialMemberDto;
import com.dreamypatisiel.devdevdev.global.security.oauth2.service.OAuth2MemberService;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class AppOAuth2MemberServiceTest {

    @Autowired
    MemberRepository memberRepository;
    @Autowired
    OAuth2MemberService oAuth2MemberService;

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

        OAuth2User mockOAuth2User = mock(OAuth2User.class);
        when(mockOAuth2User.getAttributes()).thenReturn(Map.of());

        // when
        oAuth2MemberService.register(mockOAuth2UserProvider, mockOAuth2User);

        // then
        Member member = memberRepository.findMemberByUserIdAndSocialType(userId, socialType).get();
        assertThat(member).isNotNull();
    }

    @Test
    @DisplayName("가입한 이력이 있는 회원이면 데이터베이스에 회원을 저장하지 않는다.")
    void registerDuplicatedMember() {
        // given
        String userId = "123";
        String userName = "user";
        String email = "user@kakao.com";
        SocialType socialType = SocialType.KAKAO;
        String password = "1234";

        OAuth2UserProvider mockOAuth2UserProvider = mock(OAuth2UserProvider.class);
        when(mockOAuth2UserProvider.getId()).thenReturn(userId);
        when(mockOAuth2UserProvider.getUserName()).thenReturn(userName);
        when(mockOAuth2UserProvider.getSocialType()).thenReturn(socialType);
        when(mockOAuth2UserProvider.getEmail()).thenReturn(email);

        SocialMemberDto socialMemberDto = SocialMemberDto.from(mockOAuth2UserProvider, password);
        memberRepository.save(Member.createMemberBy(socialMemberDto));

        // when
        oAuth2MemberService.register(mockOAuth2UserProvider, null);

        // then
        List<Member> members = memberRepository.findMembersByUserIdAndSocialType(userId, socialType);
        assertThat(members).hasSize(1)
                .extracting("userId", "name", "email", "nickname")
                .contains(
                        tuple(userId, userName, new Email(email), new Nickname(userName))
                );
    }
}