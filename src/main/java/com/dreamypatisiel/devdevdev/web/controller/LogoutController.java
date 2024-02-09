package com.dreamypatisiel.devdevdev.web.controller;

import com.dreamypatisiel.devdevdev.global.security.jwt.model.JwtCookieConstant;
import com.dreamypatisiel.devdevdev.global.security.jwt.service.JwtMemberService;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.UserPrincipal;
import com.dreamypatisiel.devdevdev.global.utils.AuthenticationMemberUtils;
import com.dreamypatisiel.devdevdev.global.utils.CookieUtils;
import com.dreamypatisiel.devdevdev.global.utils.UriUtils;
import com.dreamypatisiel.devdevdev.web.response.BasicResponse;
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

    @Value("${domain.frontend.host}")
    private String domain;
    @Value("${jwt.redirectUri.path}")
    private String endpoint;

    private final JwtMemberService jwtMemberService;

    @PostMapping("/logout")
    public ResponseEntity<BasicResponse<Object>> logout(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 회원의 리프레시 토큰을 비활성화 상태로 변경
        UserPrincipal userPrincipal = AuthenticationMemberUtils.getUserPrincipal();
        jwtMemberService.updateMemberRefreshTokenToDisabled(userPrincipal);

        // 로그아웃 처리
        request.logout();
        // 인증 객체 초기화
        SecurityContextHolder.clearContext();
        // 쿠키 삭제
        CookieUtils.deleteCookieFromResponse(request, response, JwtCookieConstant.DEVDEVDEV_REFRESH_TOKEN);

        // 리다이렉트 설정
        String redirectUri = UriUtils.createUriByDomainAndEndpoint(domain, endpoint);
        response.sendRedirect(redirectUri);

        return ResponseEntity.ok().body(BasicResponse.success());
    }
}
