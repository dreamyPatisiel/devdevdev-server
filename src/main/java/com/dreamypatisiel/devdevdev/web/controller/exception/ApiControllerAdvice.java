package com.dreamypatisiel.devdevdev.web.controller.exception;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
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
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.impl.SizeLimitExceededException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@Slf4j
@RestControllerAdvice
public class ApiControllerAdvice {

    // 요청을 하거나 응답을 처리하는 동안 클라이언트에서 오류가 발생한 경우.
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<BasicResponse<Object>> maxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        log.debug("sizeLimitExceededException={}", e.getMessage(), e);
        String errorMessage = "업로드 가능한 파일 용량(10MB)을 초과했습니다.";
        return new ResponseEntity<>(BasicResponse.fail(errorMessage, HttpStatus.BAD_REQUEST.value()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(SdkClientException.class)
    public ResponseEntity<BasicResponse<Object>> sdkClientException(SdkClientException e) {
        log.error("sdkClientException={}", e.getMessage(), e);
        String errorMessage = "시스템 오류가 발생했습니다.";
        return new ResponseEntity<>(BasicResponse.fail(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR.value()),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // 요청을 처리하는 동안 Amazon S3에서 오류가 발생한 경우.
    @ExceptionHandler(AmazonServiceException.class)
    public ResponseEntity<BasicResponse<Object>> amazonServiceException(AmazonServiceException e) {
        log.error("amazonServiceException={}", e.getMessage(), e);
        String errorMessage = "시스템 오류가 발생했습니다.";
        return new ResponseEntity<>(BasicResponse.fail(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR.value()),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

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
