package com.dreamypatisiel.devdevdev.web.controller;

import com.dreamypatisiel.devdevdev.global.security.jwt.model.JwtCookieConstant;
import com.dreamypatisiel.devdevdev.global.security.jwt.service.JwtMemberService;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.UserPrincipal;
import com.dreamypatisiel.devdevdev.global.utils.AuthenticationMemberUtils;
import com.dreamypatisiel.devdevdev.global.utils.CookieUtils;
import com.dreamypatisiel.devdevdev.global.utils.UriUtils;
import com.dreamypatisiel.devdevdev.web.response.BasicResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/devdevdev/api/v1")
@RequiredArgsConstructor
public class LogoutController {

    private final JwtMemberService jwtMemberService;

    @Operation(summary = "로그아웃 요청", description = "쿠키에 담긴 RefreshToken을 초기화합니다.", deprecated = true)
    @PostMapping("/logout")
    public ResponseEntity<BasicResponse<Object>> logout(HttpServletRequest request, HttpServletResponse response)
            throws ServletException {

        // 회원의 리프레시 토큰을 비활성화 상태로 변경
        UserPrincipal userPrincipal = AuthenticationMemberUtils.getUserPrincipal();
        jwtMemberService.updateMemberRefreshTokenToDisabled(userPrincipal);

        // 로그아웃 처리
        request.logout();
        // 인증 객체 초기화
        SecurityContextHolder.clearContext();
        // 쿠키 설정
        CookieUtils.deleteCookieFromResponse(request, response, JwtCookieConstant.DEVDEVDEV_REFRESH_TOKEN);
        CookieUtils.addCookieToResponse(response, JwtCookieConstant.DEVDEVDEV_LOGIN_STATUS,
                CookieUtils.INACTIVE, CookieUtils.DEFAULT_MAX_AGE, false, false);

        return ResponseEntity.ok().body(BasicResponse.success());
    }
}
