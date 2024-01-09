package com.dreamypatisiel.devdevdev.global.config.security.oauth2.service;

import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Password;
import com.dreamypatisiel.devdevdev.domain.repository.MemberRepository;
import com.dreamypatisiel.devdevdev.global.config.security.oauth2.model.OAuth2UserProvider;
import com.dreamypatisiel.devdevdev.global.config.security.oauth2.model.SocialMemberDto;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AppOAuth2MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public Member register(OAuth2UserProvider oAuth2UserProvider) {
        Optional<Member> optionalMember = memberRepository.findByUserId(oAuth2UserProvider.getId());
        if(optionalMember.isPresent()) {
            return optionalMember.get();
        }

        String encodePassword = passwordEncoder.encode(new Password().getPassword());
        SocialMemberDto socialMemberDto = SocialMemberDto.from(oAuth2UserProvider, encodePassword);
        memberRepository.save(Member.createMemberBy(socialMemberDto));

        return Member.createMemberBy(socialMemberDto);
    }
}
