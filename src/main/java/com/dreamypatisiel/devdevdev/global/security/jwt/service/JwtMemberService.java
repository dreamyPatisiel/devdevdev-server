package com.dreamypatisiel.devdevdev.global.security.jwt.service;

import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.SocialType;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Email;
import com.dreamypatisiel.devdevdev.domain.repository.MemberRepository;
import com.dreamypatisiel.devdevdev.domain.service.response.MemberResponse;
import com.dreamypatisiel.devdevdev.exception.MemberException;
import com.dreamypatisiel.devdevdev.global.security.jwt.model.JwtClaimConstant;
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

    @Transactional
    public MemberResponse updateMemberRefreshToken(String refreshToken, Claims claims) {
        String email = claims.get(JwtClaimConstant.email).toString();
        String socialType = claims.get(JwtClaimConstant.socialType).toString();

        Member findMember = memberRepository.findMemberByEmailAndSocialType(new Email(email), SocialType.valueOf(socialType))
                .orElseThrow(() -> new MemberException(MemberException.INVALID_MEMBER_NOT_FOUND_MESSAGE));
        findMember.updateRefreshToken(refreshToken);

        return MemberResponse.of(findMember);
    }

    public Optional<Member> findMemberByEmailAndSocialType(String email, String socialType) {
        return memberRepository.findMemberByEmailAndSocialType(new Email(email), SocialType.valueOf(socialType));
    }
}
