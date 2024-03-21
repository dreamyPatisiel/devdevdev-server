package com.dreamypatisiel.devdevdev.web.controller.exception;

import com.dreamypatisiel.devdevdev.exception.MemberException;
import com.dreamypatisiel.devdevdev.exception.PickOptionImageNameException;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiControllerAdvice {

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<BasicResponse<Object>> accessDeniedException(AccessDeniedException e) {
        return new ResponseEntity<>(BasicResponse.fail(e.getMessage(), HttpStatus.UNAUTHORIZED.value()),
                HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<BasicResponse<Object>> illegalArgumentException(IllegalArgumentException e) {
        return new ResponseEntity<>(BasicResponse.fail(e.getMessage(), HttpStatus.BAD_REQUEST.value()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<BasicResponse<Object>> illegalStateException(IllegalStateException e) {
        return new ResponseEntity<>(BasicResponse.fail(e.getMessage(), HttpStatus.METHOD_NOT_ALLOWED.value()),
                HttpStatus.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(MemberException.class)
    public ResponseEntity<BasicResponse<Object>> memberException(MemberException e) {
        return new ResponseEntity<>(BasicResponse.fail(e.getMessage(), HttpStatus.NOT_FOUND.value()),
                HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(TokenNotFoundException.class)
    public ResponseEntity<BasicResponse<Object>> tokenNotFoundException(TokenNotFoundException e) {
        return new ResponseEntity<>(BasicResponse.fail(e.getMessage(), HttpStatus.FORBIDDEN.value()),
                HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(PickOptionImageNameException.class)
    public ResponseEntity<BasicResponse<Object>> tokenNotFoundException(PickOptionImageNameException e) {
        return new ResponseEntity<>(BasicResponse.fail(e.getMessage(), HttpStatus.BAD_REQUEST.value()),
                HttpStatus.BAD_REQUEST);
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
