package com.dreamypatisiel.devdevdev.global.security.jwt.service;

import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.Role;
import com.dreamypatisiel.devdevdev.domain.entity.SocialType;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Email;
import com.dreamypatisiel.devdevdev.domain.repository.MemberRepository;
import com.dreamypatisiel.devdevdev.exception.MemberException;
import com.dreamypatisiel.devdevdev.exception.TokenInvalidException;
import com.dreamypatisiel.devdevdev.global.common.TimeProvider;
import com.dreamypatisiel.devdevdev.global.constant.SecurityConstant;
import com.dreamypatisiel.devdevdev.global.security.jwt.model.JwtClaimConstant;
import com.dreamypatisiel.devdevdev.global.security.jwt.model.Token;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.OAuth2UserProvider;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.SocialMemberDto;
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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import static com.dreamypatisiel.devdevdev.exception.TokenInvalidException.REFRESH_TOKEN_INVALID_EXCEPTION_MESSAGE;
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

    public Token generateToken(String email, String socialType) {
        Claims claims = configClaims(email, socialType);

        String accessToken = createAccessToken(claims);
        String refreshToken = createRefreshToken(claims);

        return new Token(accessToken, refreshToken);
    }

    private Claims configClaims(String email, String socialType) {
        Claims claims = Jwts.claims();
        claims.put(JwtClaimConstant.email, email);
        claims.put(JwtClaimConstant.socialType, socialType);

        return claims;
    }

    public Token generateToken(OAuth2UserProvider oAuth2UserProvider) {
        String email = oAuth2UserProvider.getEmail();
        SocialType socialType = oAuth2UserProvider.getSocialType();
        Claims claims = configClaims(email, socialType.name());

        String accessToken = createAccessToken(claims);
        String refreshToken = createRefreshToken(claims);

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
        log.info("claims={}", claims);
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(timeProvider.getDateNow())
                .setExpiration(new Date(timeProvider.getDateNow().getTime() + ACCESS_TOKEN_EXPIRE_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Authentication getAuthentication(String token) {
        String email = getEmail(token);
        String socialType = getSocialType(token);
        String role = getRole(token);

        SocialMemberDto socialMemberDto = SocialMemberDto.of(email, socialType, role);
        log.info("User role = {}",role);

        return new UsernamePasswordAuthenticationToken(socialMemberDto, null,
                List.of(new SimpleGrantedAuthority(role)));
    }

    public String getAccessTokenByRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(SecurityConstant.AUTHORIZATION_HEADER);
        if (!StringUtils.hasText(bearerToken) || !bearerToken.startsWith(SecurityConstant.BEARER_PREFIX)) {
            return null;
        }
        return bearerToken.substring(SecurityConstant.BEARER_PREFIX.length());
    }

    public boolean validateToken(String token) {
        try {
            if(!StringUtils.hasText(token)) {
                return false;
//                throw new TokenNotFoundException(TOKEN_NOT_FOUND_EXCEPTION_MESSAGE);
            }
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

    public Claims getClaims(String validToken) {
        return Jwts.parserBuilder().setSigningKey(key)
                .build()
                .parseClaimsJws(validToken)
                .getBody();
    }

    public void validateRefreshToken(String refreshToken) throws TokenInvalidException {

        validateToken(refreshToken);

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
        Claims claims = getClaims(token);
        return claims.get(JwtClaimConstant.socialType).toString();
    }

    public String getEmail(String token) {
        Claims claims = getClaims(token);
        return claims.get(JwtClaimConstant.email).toString();
    }

    public String getRole(String token) {
        Claims claims = getClaims(token);
        return claims.get(JwtClaimConstant.role).toString();
    }
}