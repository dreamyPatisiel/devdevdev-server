package com.dreamypatisiel.devdevdev.web.controller;

import com.dreamypatisiel.devdevdev.global.security.jwt.model.JwtCookieConstant;
import com.dreamypatisiel.devdevdev.global.utils.CookieUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/devdevdev/api/v1")
public class LogoutController {

    private static final String LOGOUT_REDIRECT_URL = "/home";

    @GetMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        SecurityContextHolder.clearContext();

        request.logout(); // 로그아웃 처리
        CookieUtils.deleteCookieFromResponse(request, response, JwtCookieConstant.DEVDEVDEV_ACCESS_TOKEN);
        CookieUtils.deleteCookieFromResponse(request, response, JwtCookieConstant.DEVDEVDEV_REFRESH_TOKEN);

        response.sendRedirect(LOGOUT_REDIRECT_URL);

        return ResponseEntity.ok().body(null);
    }
}
