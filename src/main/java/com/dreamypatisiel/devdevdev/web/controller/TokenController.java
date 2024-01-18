package com.dreamypatisiel.devdevdev.web.controller;

import com.dreamypatisiel.devdevdev.global.security.jwt.CookieUtils;
import com.dreamypatisiel.devdevdev.global.security.jwt.TokenService;
import com.dreamypatisiel.devdevdev.global.security.jwt.model.Token;
import com.dreamypatisiel.devdevdev.global.security.jwt.service.JwtMemberService;
import com.dreamypatisiel.devdevdev.web.response.BasicResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("1/devdevdev/api/v")
public class TokenController {

    public static final String DEVDEVDEV_REFRESH_TOKEN = "DEVDEVDEV_REFRESH_TOKEN";
    public static final String DEVDEVDEV_ACCESS_TOKEN = "DEVDEVDEV_ACCESS_TOKEN";

    private TokenService tokenService;
    private JwtMemberService jwtMemberService;

    @GetMapping("/token/expired")
    public String auth() {
        throw new RuntimeException();
    }

    /**
     * 리프레시 토큰 요청
     * 1. 엑세스 토큰이 만료된 상황
     * 2. 클라이언트에서 리프레시 토큰을 요청한다.
     */
    @GetMapping("/token/refresh")
    public ResponseEntity<BasicResponse<Object>> refreshAuth(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = request.getHeader("Refresh");
        tokenService.validateRefreshToken(refreshToken);

        if (StringUtils.hasText(refreshToken)) {
            OAuth2User oAuth2User = (OAuth2User) SecurityContextHolder.getContext().getAuthentication();
            Token newToken = tokenService.generateToken(oAuth2User);

            // 쿠키 설정
            CookieUtils.configJwtCookie(response, newToken);

            // member에게 refresh token 저장

            return ResponseEntity.ok(BasicResponse.success("CREATE_NEW_TOKEN"));
        }

        throw new RuntimeException();
    }
}
