package com.dreamypatisiel.devdevdev.global.security.jwt.service;

import static com.dreamypatisiel.devdevdev.exception.TokenInvalidException.REFRESH_TOKEN_INVALID_EXCEPTION_MESSAGE;

import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.SocialType;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Email;
import com.dreamypatisiel.devdevdev.domain.repository.MemberRepository;
import com.dreamypatisiel.devdevdev.exception.MemberException;
import com.dreamypatisiel.devdevdev.exception.TokenInvalidException;
import com.dreamypatisiel.devdevdev.global.security.jwt.model.JwtClaimConstant;
import com.dreamypatisiel.devdevdev.global.security.jwt.model.Token;
import io.jsonwebtoken.Claims;
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
    private final TokenService tokenService;

    @Transactional
    public Token validationRefreshTokenAndUpdateMemberRefreshTokenAndGetNewToken(String refreshToken) throws TokenInvalidException {

        // 토큰 검증
        tokenService.validateToken(refreshToken);

        String email = tokenService.getEmail(refreshToken);
        String socialType = tokenService.getSocialType(refreshToken);

        // 회원 조회
        Member findMember = memberRepository.findMemberByEmailAndSocialType(new Email(email), SocialType.valueOf(socialType))
                .orElseThrow(() -> new MemberException(MemberException.INVALID_MEMBER_NOT_FOUND_MESSAGE));

        // 회원이 가지고 있는 리프레시 토큰과 일치 하는지?
        if(!findMember.isRefreshTokenEquals(refreshToken)) {
            throw new TokenInvalidException(REFRESH_TOKEN_INVALID_EXCEPTION_MESSAGE);
        }

        // 새로운 토큰 생성
        String role = findMember.getRole().name();
        Token newToken = tokenService.generateTokenBy(email, socialType, role);

        // 리프레시 토큰 저장
        findMember.updateRefreshToken(newToken.getRefreshToken());

        return newToken;
    }

    @Transactional
    public void updateMemberRefreshToken(String refreshToken) {
        Claims claims = tokenService.getClaims(refreshToken);
        String email = claims.get(JwtClaimConstant.email).toString();
        String socialType = claims.get(JwtClaimConstant.socialType).toString();

        // 회원 조회
        Member findMember = memberRepository.findMemberByEmailAndSocialType(new Email(email),
                        SocialType.valueOf(socialType))
                .orElseThrow(() -> new MemberException(MemberException.INVALID_MEMBER_NOT_FOUND_MESSAGE));
        // 리프레시 토큰 갱신
        findMember.updateRefreshToken(refreshToken);
    }
}
