package com.dreamypatisiel.devdevdev.global.security.oauth2.service;

import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.Role;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Email;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Password;
import com.dreamypatisiel.devdevdev.domain.repository.MemberRepository;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.OAuth2UserProvider;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.SocialMemberDto;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OAuth2MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Member register(OAuth2UserProvider oAuth2UserProvider) {
        Optional<Member> optionalMember = findMemberByEmailAndSocialType(oAuth2UserProvider);
        if(optionalMember.isPresent()) {
            return optionalMember.get();
        }

        // 데이터베이스 회원이 없으면 회원가입 시킨다.
        String encodePassword = passwordEncoder.encode(new Password().getPassword());
        SocialMemberDto socialMemberDto = SocialMemberDto.from(oAuth2UserProvider, encodePassword);
        return memberRepository.save(Member.createMemberBy(socialMemberDto));
    }

    private Optional<Member> findMemberByEmailAndSocialType(OAuth2UserProvider oAuth2UserProvider) {
        return memberRepository.findMemberByEmailAndSocialType(
                new Email(oAuth2UserProvider.getEmail()), oAuth2UserProvider.getSocialType()
        );
    }
}
