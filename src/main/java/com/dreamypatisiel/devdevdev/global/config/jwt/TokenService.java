package com.dreamypatisiel.devdevdev.global.config.jwt;

import com.dreamypatisiel.devdevdev.global.config.jwt.model.Token;
import com.dreamypatisiel.devdevdev.global.config.security.oauth2.service.AppOAuth2MemberService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.Key;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

@Slf4j
@Service
public class TokenService implements InitializingBean {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private final static String KAKAO_ACCOUNT = "kakao_account";

    private final String secret;
    private final long accessTokenValidityInMilliseconds;
    private final long refreshTokenValidityInMilliseconds;
    private final AppOAuth2MemberService appOAuth2MemberService;
    private Key key;

    public TokenService(@Value("${jwt.secret}") String secret,
                        @Value("${jwt.expiration.refresh}") long accessTokenValidityInMilliseconds,
                        @Value("${jwt.expiration.access}") long refreshTokenValidityInMilliseconds,
                        AppOAuth2MemberService appOAuth2MemberService) {
        this.secret = secret;
        this.accessTokenValidityInMilliseconds = accessTokenValidityInMilliseconds * 1000;
        this.refreshTokenValidityInMilliseconds = refreshTokenValidityInMilliseconds * 1000;
        this.appOAuth2MemberService = appOAuth2MemberService;
    }


    @Override
    public void afterPropertiesSet() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public Token generateToken(OAuth2User oAuth2User) {
        oAuth2User.getName();
        Map<String, Object>  kakaoObejct =  (Map<String, Object>)(oAuth2User.getAttributes().get(KAKAO_ACCOUNT));
        String email = kakaoObejct.get("email").toString();
        log.info("email = "+email);
        Claims claims = Jwts.claims().setSubject(email);
        claims.put("role", "USER");

        Instant now = Instant.now();
        return new Token(makeJwtValue(claims, now, accessTokenValidityInMilliseconds),
                makeJwtValue(claims, now, refreshTokenValidityInMilliseconds));
    }

    private String makeJwtValue(Claims claims, Instant now, long accessTokenValidityInMilliseconds) {
        return Jwts.builder().setClaims(claims).setIssuedAt(new Date(now.toEpochMilli()))
                .setExpiration(new Date(now.toEpochMilli() + accessTokenValidityInMilliseconds))
                .signWith(key, SignatureAlgorithm.HS256).compact();
    }

    public boolean validateToken(String token) {
        if (token == null || !token.startsWith(BEARER_PREFIX)) {
            return false;
        }

        String value = token.substring(BEARER_PREFIX.length());

        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(value);
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

    public Authentication getAuthentication(String accessToken) {
        Claims claims = parseClaims(accessToken);
        String email = claims.getSubject();
//        log.info("Authenticated Member Id:" + String.valueOf(memberId));
        return new UsernamePasswordAuthenticationToken(email, "",
                Arrays.asList(new SimpleGrantedAuthority("USER")));
    }

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

}