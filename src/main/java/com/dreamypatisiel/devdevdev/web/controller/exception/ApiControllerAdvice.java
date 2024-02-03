package com.dreamypatisiel.devdevdev.web.controller.exception;

import com.dreamypatisiel.devdevdev.exception.TokenInvalidException;
import com.dreamypatisiel.devdevdev.exception.TokenNotFoundException;
import com.dreamypatisiel.devdevdev.global.security.jwt.model.JwtCookieConstant;
import com.dreamypatisiel.devdevdev.global.utils.CookieUtils;
import com.dreamypatisiel.devdevdev.web.response.BasicResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiControllerAdvice {

    @ExceptionHandler(TokenNotFoundException.class)
    public ResponseEntity<BasicResponse<Object>> tokenNotFoundException(TokenNotFoundException e) {
        return new ResponseEntity<>(BasicResponse.fail(e.getMessage(), HttpStatus.FORBIDDEN.value()),
                HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(TokenInvalidException.class)
    public ResponseEntity<BasicResponse<Object>> tokenInvalidException(HttpServletRequest request, HttpServletResponse response, TokenInvalidException e)
            throws ServletException {
        request.logout(); // 로그아웃 처리
        CookieUtils.deleteCookieFromResponse(request, response, JwtCookieConstant.DEVDEVDEV_ACCESS_TOKEN);
        CookieUtils.deleteCookieFromResponse(request, response, JwtCookieConstant.DEVDEVDEV_REFRESH_TOKEN);

        return new ResponseEntity<>(BasicResponse.fail(e.getMessage(), HttpStatus.UNAUTHORIZED.value()),
                HttpStatus.UNAUTHORIZED);
    }
}
