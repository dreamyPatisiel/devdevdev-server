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
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AppOAuth2MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void register(OAuth2UserProvider oAuth2UserProvider) {
        Optional<Member> optionalMember = memberRepository
                .findMemberByUserIdAndSocialType(oAuth2UserProvider.getId(), oAuth2UserProvider.getSocialType());
        if(optionalMember.isPresent()) {
            return;
        }

        // 데이터베이스 회원이 없으면 회원가입 시킨다.
        String encodePassword = passwordEncoder.encode(new Password().getPassword());
        SocialMemberDto socialMemberDto = SocialMemberDto.from(oAuth2UserProvider, encodePassword);
        memberRepository.save(Member.createMemberBy(socialMemberDto));
    }
}
