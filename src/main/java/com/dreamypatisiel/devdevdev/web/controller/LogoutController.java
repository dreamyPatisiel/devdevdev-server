package com.dreamypatisiel.devdevdev.web.controller;

import com.dreamypatisiel.devdevdev.global.security.jwt.model.JwtCookieConstant;
import com.dreamypatisiel.devdevdev.global.utils.CookieUtils;
import com.dreamypatisiel.devdevdev.global.utils.UriUtils;
import com.dreamypatisiel.devdevdev.web.response.BasicResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/devdevdev/api/v1")
public class LogoutController {


    @Value("${domain.frontend.host}")
    private String domain;
    @Value("${jwt.redirectUri.path}")
    private String endpoint;

    @PostMapping("/logout")
    public ResponseEntity<BasicResponse<Object>> logout(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        SecurityContextHolder.clearContext();

        request.logout(); // 로그아웃 처리
        // 쿠키가 없으면 어떻게 하지..?
        CookieUtils.deleteCookieFromResponse(request, response, JwtCookieConstant.DEVDEVDEV_REFRESH_TOKEN);

        String redirectUri = UriUtils.createUriByDomainAndEndpoint(domain, endpoint);
        response.sendRedirect(redirectUri);

        return ResponseEntity.ok().body(BasicResponse.success());
    }
}
