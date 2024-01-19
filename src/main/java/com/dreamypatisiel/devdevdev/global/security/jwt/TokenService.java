package com.dreamypatisiel.devdevdev.global.security.jwt;

import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.SocialType;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Email;
import com.dreamypatisiel.devdevdev.domain.repository.MemberRepository;
import com.dreamypatisiel.devdevdev.exception.MemberException;
import com.dreamypatisiel.devdevdev.exception.TokenInvalidException;
import com.dreamypatisiel.devdevdev.exception.TokenNotFoundException;
import com.dreamypatisiel.devdevdev.global.common.TimeProvider;
import com.dreamypatisiel.devdevdev.global.security.jwt.model.JwtClaimConstant;
import com.dreamypatisiel.devdevdev.global.security.jwt.model.Token;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.security.Key;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.dreamypatisiel.devdevdev.exception.TokenInvalidException.REFRESH_TOKEN_INVALID_EXCEPTION_MESSAGE;
import static com.dreamypatisiel.devdevdev.exception.TokenNotFoundException.TOKEN_NOT_FOUND_EXCEPTION_MESSAGE;
import static com.dreamypatisiel.devdevdev.global.security.jwt.model.TokenExpireTime.ACCESS_TOKEN_EXPIRE_TIME;
import static com.dreamypatisiel.devdevdev.global.security.jwt.model.TokenExpireTime.REFRESH_TOKEN_EXPIRE_TIME;

/**
 * JWT의 생성과 인증, 인가를 담당하는 책임을 가진 클래스
 * 토큰 생성, 검증, 쿠키에서 토큰 추출, 클레임 추출
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService implements InitializingBean {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String AUTHORIZATION_HEADER = "Authorization";

    @Value("${jwt.secret}")
    private String secret;

    private Key key;

    private final TimeProvider timeProvider;
    private final MemberRepository memberRepository;


    @Override
    public void afterPropertiesSet() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 토큰 발급
     */
    @Transactional
    public Token generateToken(String email, String socialType) {

        Claims claims = Jwts.claims().setSubject(email);
        claims.put(JwtClaimConstant.email, email);
        claims.put(JwtClaimConstant.socialType, socialType);
        claims.put(JwtClaimConstant.role, "ROLE_USER");

        String accessToken = createAccessToken(claims);
        String refreshToken = createRefreshToken(claims);

        Member findMember = memberRepository.findMemberByEmailAndSocialType(new Email(email), SocialType.valueOf(socialType))
                .orElseThrow(() -> new MemberException(MemberException.INVALID_MEMBER_NOT_FOUND_MESSAGE));
        findMember.updateRefreshToken(refreshToken);

        return new Token(accessToken, refreshToken);
    }

    private String createRefreshToken(Claims claims) {
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(timeProvider.getDateNow())
                .setExpiration(new Date(timeProvider.getDateNow().getTime() + REFRESH_TOKEN_EXPIRE_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    private String createAccessToken(Claims claims) {
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(timeProvider.getDateNow())
                .setExpiration(new Date(timeProvider.getDateNow().getTime() + ACCESS_TOKEN_EXPIRE_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    @Deprecated
    private String makeJwtValue(Claims claims, Instant now, long accessTokenValidityInMilliseconds) {
        return Jwts.builder().setClaims(claims).setIssuedAt(new Date(now.toEpochMilli()))
                .setExpiration(new Date(now.toEpochMilli() + accessTokenValidityInMilliseconds))
                .signWith(key, SignatureAlgorithm.HS256).compact();
    }

    public Authentication getAuthentication(String token) {
        String email = getEmail(token);
        String socialType = getSocialType(token);

        return new UsernamePasswordAuthenticationToken(email, null,
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    public String resolveToken(HttpServletRequest request) throws TokenInvalidException {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (!StringUtils.hasText(bearerToken) || !bearerToken.startsWith(BEARER_PREFIX)) {
            // throw new TokenNotFoundException(TOKEN_NOT_FOUND_EXCEPTION_MESSAGE);
            return null;
        }
        return bearerToken.substring(BEARER_PREFIX.length());
    }

    public boolean validateToken(String token) throws TokenInvalidException {
        if(!StringUtils.hasText(token)) {
//            throw new TokenNotFoundException(TOKEN_NOT_FOUND_EXCEPTION_MESSAGE);
            return false;
        }
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
//            throw new TokenInvalidException("잘못된 jwt 서명을 가진 토큰입니다");
        } catch (ExpiredJwtException e) {
//            throw new TokenInvalidException("만료된 jwt 토큰입니다");
        } catch (UnsupportedJwtException e) {
//            throw new TokenInvalidException("지원하지 않는 jwt 토큰입니다");
        } catch (IllegalArgumentException e) {
//            throw new TokenInvalidException("잘못된 jwt 토큰입니다");
        }
        return false;
    }

    private Claims parseClaims(String accessToken) {
        try {
            return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(accessToken).getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

    public void validateRefreshToken(String refreshToken) throws TokenInvalidException {
        try {
            validateToken(refreshToken);
        } catch (TokenInvalidException e){
            throw new TokenInvalidException(REFRESH_TOKEN_INVALID_EXCEPTION_MESSAGE);
        }

        String email = getEmail(refreshToken);
        String socialType = getSocialType(refreshToken);

        Member findMember = memberRepository.findMemberByEmailAndSocialType(new Email(email), SocialType.valueOf(socialType))
                .orElseThrow(() -> new MemberException(MemberException.INVALID_MEMBER_NOT_FOUND_MESSAGE));

        boolean isEqualsRefreshToken = findMember.isRefreshTokenEquals(refreshToken);
        if(!isEqualsRefreshToken) {
            throw new TokenInvalidException(REFRESH_TOKEN_INVALID_EXCEPTION_MESSAGE);
        }
    }

    public String getSocialType(String token) {
        Claims claims = parseClaims(token);
        return claims.get(JwtClaimConstant.socialType).toString();
    }

    public String getEmail(String token) {
        Claims claims = parseClaims(token);
        return claims.get(JwtClaimConstant.email).toString();
    }
}