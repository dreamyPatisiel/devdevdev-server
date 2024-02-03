package com.dreamypatisiel.devdevdev.web.controller;

import com.dreamypatisiel.devdevdev.global.security.jwt.model.JwtCookieConstant;
import com.dreamypatisiel.devdevdev.global.security.jwt.model.Token;
import com.dreamypatisiel.devdevdev.global.utils.CookieUtils;
import com.dreamypatisiel.devdevdev.global.security.jwt.service.TokenService;
import com.dreamypatisiel.devdevdev.global.security.jwt.service.JwtMemberService;
import com.dreamypatisiel.devdevdev.web.response.BasicResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/devdevdev/api/v1/token")
@RequiredArgsConstructor
public class TokenController {

    private final JwtMemberService jwtMemberService;

    @GetMapping("/refresh")
    public ResponseEntity<BasicResponse<Object>> getRefreshToken(HttpServletRequest request, HttpServletResponse response) {
        // 쿠키에서 refresh를 꺼내온다.
        String refreshToken = CookieUtils.getRequestCookieValueByName(request,
                JwtCookieConstant.DEVDEVDEV_REFRESH_TOKEN);

        // 쿠키 검증 및 토큰 재발급
        Token newToken = jwtMemberService
                .validationRefreshTokenAndUpdateMemberRefreshTokenAndGetNewToken(refreshToken);

        // 쿠키 설정
        CookieUtils.configJwtCookie(response, newToken);

        return ResponseEntity.ok(BasicResponse.success(null));
    }
}
