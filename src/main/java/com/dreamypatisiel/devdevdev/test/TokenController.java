package com.dreamypatisiel.devdevdev.test;

import com.dreamypatisiel.devdevdev.global.config.jwt.TokenService;
import com.dreamypatisiel.devdevdev.global.config.jwt.model.Token;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("1/devdevdev/api/v")
public class TokenController {

    private TokenService tokenService;

    @GetMapping("/token/expired")
    public String auth() {
        throw new RuntimeException();
    }

//    @GetMapping("/token/refresh")
//    public String refreshAuth(HttpServletRequest request, HttpServletResponse response) {
//        String token = request.getHeader("Refresh");
//
//        if (token != null && tokenService.validateToken(token)) {
//            String email = tokenService.resolveToken(request);
//            Token newToken = tokenService.generateToken(email, "USER");
//
//            response.addHeader("Auth", newToken.getToken());
//            response.addHeader("Refresh", newToken.getRefreshToken());
//            response.setContentType("application/json;charset=UTF-8");
//
//            return "HAPPY NEW TOKEN";
//        }
//
//        throw new RuntimeException();
//    }
}
