package com.dreamypatisiel.devdevdev.global.security.jwt;

import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.SocialType;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Email;
import com.dreamypatisiel.devdevdev.domain.repository.MemberRepository;
import com.dreamypatisiel.devdevdev.exception.MemberException;
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
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.security.Key;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

/**
 * 토큰 생성, 검증, 쿠키에서 토큰 추출, 클레임 추출
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService implements InitializingBean {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private final static String KAKAO_ACCOUNT = "kakao_account";

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration.refresh}")
    private long accessTokenValidityInMilliseconds;

    @Value("${jwt.expiration.access}")
    private long refreshTokenValidityInMilliseconds;
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
    @SuppressWarnings("unchecked")
    @Transactional
    public Token generateToken(OAuth2User oAuth2User) {

        Map<String, Object> kakaoAccountAttributes = (Map<String, Object>) (oAuth2User.getAttributes().get(KAKAO_ACCOUNT));
        String email = kakaoAccountAttributes.get(JwtClaimConstant.email).toString();
//        String socialType = oAuth2UserRequest.getClientRegistration().getRegistrationId();
        //String socialType = kakaoAccountAttributes.get(JwtClaimConstant.socialType).toString();

        Claims claims = Jwts.claims()
                .setSubject(email);
        claims.put("role", "USER");

        String accessToken = createAccessToken(claims);
        String refreshToken = createRefreshToken(claims);

        Member findMember = memberRepository.findMemberByEmailAndSocialType(new Email(email),
                        SocialType.KAKAO)
                .orElseThrow(() -> new MemberException(MemberException.INVALID_MEMBER_NOT_FOUND_MESSAGE));
        findMember.updateRefreshToken(refreshToken);

        return new Token(accessToken, refreshToken);
    }

    private String createRefreshToken(Claims claims) {
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(timeProvider.getDateNow())
                .setExpiration(new Date(timeProvider.getDateNow().getTime() + refreshTokenValidityInMilliseconds))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    private String createAccessToken(Claims claims) {
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(timeProvider.getDateNow())
                .setExpiration(new Date(timeProvider.getDateNow().getTime() + accessTokenValidityInMilliseconds))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    @Deprecated
    private String makeJwtValue(Claims claims, Instant now, long accessTokenValidityInMilliseconds) {
        return Jwts.builder().setClaims(claims).setIssuedAt(new Date(now.toEpochMilli()))
                .setExpiration(new Date(now.toEpochMilli() + accessTokenValidityInMilliseconds))
                .signWith(key, SignatureAlgorithm.HS256).compact();
    }

    public Authentication getAuthentication(String accessToken) {
        Claims claims = parseClaims(accessToken);
        String email = claims.getSubject();
//        log.info("Authenticated Member Id:" + String.valueOf(memberId));
        return new UsernamePasswordAuthenticationToken(email, "",
                Arrays.asList(new SimpleGrantedAuthority("USER")));
    }

//    @Deprecated
    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    private Claims parseClaims(String accessToken) {
        try {
            return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(accessToken).getBody();
        } catch (ExpiredJwtException e) {
            // ???
            return e.getClaims();
        }
    }

    /**
     *
     */
    public void validateRefreshToken(String refreshToken) {
        boolean isValidToken = validateToken(refreshToken);
        if (!isValidToken) {
            throw new JwtException("유효하지 않은 리프레시 토큰 입니다.");
        }

        Claims claims = parseClaims(refreshToken);
        String email = claims.get(JwtClaimConstant.email).toString();
        String socialType = claims.get(JwtClaimConstant.socialType).toString();

        Member findMember = memberRepository.findMemberByEmailAndSocialType(new Email(email), SocialType.valueOf(socialType))
                .orElseThrow(() -> new MemberException(MemberException.INVALID_MEMBER_NOT_FOUND_MESSAGE));

        boolean isEqualsRefreshToken = findMember.isRefreshTokenEquals(refreshToken);
        if(!isEqualsRefreshToken) {
            // 로그아웃(+쿠키 삭제)
            throw new JwtException("확인되지 않은 리프레시 토큰 입니다.");
        }
    }

    public boolean validateToken(String token) {
        if(!StringUtils.hasText(token)) {
            return false;
        }

        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.error("잘못된 jwt 서명을 가진 토큰입니다", e);
        } catch (ExpiredJwtException e) {
            log.error("만료된 jwt 토큰입니다", e);
        } catch (UnsupportedJwtException e) {
            log.error("지원하지 않는 jwt 토큰입니다", e);
        } catch (IllegalArgumentException e) {
            log.error("잘못된 jwt 토큰입니다", e);
        }
        return false;
    }
}