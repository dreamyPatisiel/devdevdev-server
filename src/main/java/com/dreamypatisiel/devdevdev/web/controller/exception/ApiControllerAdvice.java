package com.dreamypatisiel.devdevdev.web.controller.exception;

import static com.dreamypatisiel.devdevdev.domain.exception.TechArticleExceptionMessage.KEYWORD_WITH_SPECIAL_SYMBOLS_EXCEPTION_MESSAGE;
import static com.dreamypatisiel.devdevdev.web.controller.exception.SystemErrorConstant.EXTERNAL_SYSTEM_ERROR_MESSAGE;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.dreamypatisiel.devdevdev.exception.ImageFileException;
import com.dreamypatisiel.devdevdev.exception.InternalServerException;
import com.dreamypatisiel.devdevdev.exception.MemberException;
import com.dreamypatisiel.devdevdev.exception.NotFoundException;
import com.dreamypatisiel.devdevdev.exception.PickOptionImageNameException;
import com.dreamypatisiel.devdevdev.exception.TokenInvalidException;
import com.dreamypatisiel.devdevdev.exception.TokenNotFoundException;
import com.dreamypatisiel.devdevdev.global.security.jwt.model.JwtCookieConstant;
import com.dreamypatisiel.devdevdev.global.utils.CookieUtils;
import com.dreamypatisiel.devdevdev.web.dto.response.BasicResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.UncategorizedElasticsearchException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@Slf4j
@RestControllerAdvice
public class ApiControllerAdvice {

    @ExceptionHandler(ImageFileException.class)
    public ResponseEntity<BasicResponse<Object>> maxUploadSizeExceededException(ImageFileException e) {
        return new ResponseEntity<>(BasicResponse.fail(e.getMessage(), HttpStatus.BAD_REQUEST.value()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<BasicResponse<Object>> maxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        log.debug("sizeLimitExceededException={}", e.getMessage(), e);
        String errorMessage = "업로드 가능한 파일 용량(10MB)을 초과했습니다.";
        return new ResponseEntity<>(BasicResponse.fail(errorMessage, HttpStatus.BAD_REQUEST.value()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UncategorizedElasticsearchException.class)
    public ResponseEntity<BasicResponse<Object>> uncategorizedElasticsearchException(
            UncategorizedElasticsearchException e) {
        log.debug("UncategorizedElasticsearchException={}", e.getMessage(), e);
        return new ResponseEntity<>(
                BasicResponse.fail(KEYWORD_WITH_SPECIAL_SYMBOLS_EXCEPTION_MESSAGE, HttpStatus.BAD_REQUEST.value()),
                HttpStatus.BAD_REQUEST);
    }

    // 요청을 하거나 응답을 처리하는 동안 클라이언트에서 오류가 발생한 경우.
    @ExceptionHandler(SdkClientException.class)
    public ResponseEntity<BasicResponse<Object>> sdkClientException(SdkClientException e) {
        log.error("sdkClientException={}", e.getMessage(), e);
        return new ResponseEntity<>(
                BasicResponse.fail(EXTERNAL_SYSTEM_ERROR_MESSAGE, HttpStatus.SERVICE_UNAVAILABLE.value()),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // 요청을 처리하는 동안 Amazon S3에서 오류가 발생한 경우.
    @ExceptionHandler(AmazonServiceException.class)
    public ResponseEntity<BasicResponse<Object>> amazonServiceException(AmazonServiceException e) {
        log.error("amazonServiceException={}", e.getMessage(), e);
        return new ResponseEntity<>(
                BasicResponse.fail(EXTERNAL_SYSTEM_ERROR_MESSAGE, HttpStatus.SERVICE_UNAVAILABLE.value()),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // 권한 문제가 발생한 경우(403)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<BasicResponse<Object>> accessDeniedException(AccessDeniedException e) {
        return new ResponseEntity<>(BasicResponse.fail(e.getMessage(), HttpStatus.FORBIDDEN.value()),
                HttpStatus.FORBIDDEN);
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

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<BasicResponse<Object>> notFoundException(NotFoundException e) {
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
    public ResponseEntity<BasicResponse<Object>> tokenInvalidException(HttpServletRequest request,
                                                                       HttpServletResponse response,
                                                                       TokenInvalidException e)
            throws ServletException {
        request.logout(); // 로그아웃 처리
        CookieUtils.deleteCookieFromResponse(request, response, JwtCookieConstant.DEVDEVDEV_ACCESS_TOKEN);
        CookieUtils.deleteCookieFromResponse(request, response, JwtCookieConstant.DEVDEVDEV_REFRESH_TOKEN);

        return new ResponseEntity<>(BasicResponse.fail(e.getMessage(), HttpStatus.UNAUTHORIZED.value()),
                HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BasicResponse<Object>> methodArgumentNotValidException(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();
        String defaultMessage = Objects.requireNonNull(bindingResult.getFieldError()).getDefaultMessage();

        return new ResponseEntity<>(BasicResponse.fail(defaultMessage, HttpStatus.BAD_REQUEST.value()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InternalServerException.class)
    public ResponseEntity<BasicResponse<Object>> amazonServiceException(InternalServerException e) {
        log.error("internalServerException={}", e.getMessage(), e);
        return new ResponseEntity<>(
                BasicResponse.fail(EXTERNAL_SYSTEM_ERROR_MESSAGE, HttpStatus.SERVICE_UNAVAILABLE.value()),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
