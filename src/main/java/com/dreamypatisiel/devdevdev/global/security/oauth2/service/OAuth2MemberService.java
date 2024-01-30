package com.dreamypatisiel.devdevdev.global.security.oauth2.service;

import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Email;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Password;
import com.dreamypatisiel.devdevdev.domain.repository.MemberRepository;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.OAuth2UserProvider;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.SocialMemberDto;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.UserPrincipal;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OAuth2MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserPrincipal register(OAuth2UserProvider oAuth2UserProvider, OAuth2User oAuth2User) {
        Optional<Member> optionalMember = findMemberByOAuth2UserProvider(oAuth2UserProvider);
        if(optionalMember.isPresent()) {
            return UserPrincipal.create(optionalMember.get());
        }

        // 데이터베이스 회원이 없으면 회원가입 시킨다.
        String encodePassword = passwordEncoder.encode(new Password().getPassword());
        SocialMemberDto socialMemberDto = SocialMemberDto.from(oAuth2UserProvider, encodePassword);
        Member newMember = memberRepository.save(Member.createMemberBy(socialMemberDto));

        return UserPrincipal.create(newMember, oAuth2User.getAttributes());
    }

    private Optional<Member> findMemberByOAuth2UserProvider(OAuth2UserProvider oAuth2UserProvider) {
        return memberRepository.findMemberByEmailAndSocialType(
                new Email(oAuth2UserProvider.getEmail()), oAuth2UserProvider.getSocialType()
        );
    }
}
