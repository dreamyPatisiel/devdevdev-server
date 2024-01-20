package com.dreamypatisiel.devdevdev.global.security.jwt.service;

import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.SocialType;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Email;
import com.dreamypatisiel.devdevdev.domain.repository.MemberRepository;
import com.dreamypatisiel.devdevdev.domain.service.response.MemberResponse;
import com.dreamypatisiel.devdevdev.exception.MemberException;
import io.jsonwebtoken.Claims;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JwtMemberService {

    private final MemberRepository memberRepository;

    @Deprecated
    public void registerRefreshToken(String refreshToken) {
        Member findMember = memberRepository.findMemberByRefreshToken(refreshToken)
                .orElseThrow(() -> new MemberException(MemberException.INVALID_MEMBER_NOT_FOUND_MESSAGE));

        findMember.updateRefreshToken(refreshToken);
    }

    public void findMemberByEmailAndSocialTypeAndSaveRefreshToken(Email email, SocialType socialType, String refreshToken) {
        Member findMember = memberRepository.findMemberByEmailAndSocialType(email, socialType)
                .orElseThrow(() -> new MemberException(MemberException.INVALID_MEMBER_NOT_FOUND_MESSAGE));

        findMember.updateRefreshToken(refreshToken);
    }

    @Transactional
    public MemberResponse updateMemberRefreshToken(String refreshToken, Claims claims) {
        String email = claims.get("email").toString();
        log.info("email={}", email);
        String socialType = claims.get("socialType").toString();

        Member findMember = memberRepository.findMemberByEmailAndSocialType(new Email(email), SocialType.valueOf(socialType))
                .orElseThrow(() -> new MemberException(MemberException.INVALID_MEMBER_NOT_FOUND_MESSAGE));
        findMember.updateRefreshToken(refreshToken);

        return MemberResponse.of(findMember);
    }
}
