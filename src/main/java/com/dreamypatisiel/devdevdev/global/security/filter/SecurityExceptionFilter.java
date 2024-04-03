package com.dreamypatisiel.devdevdev.global.security.filter;

import com.dreamypatisiel.devdevdev.exception.JwtAccessDeniedException;
import com.dreamypatisiel.devdevdev.exception.JwtAuthenticationException;
import com.dreamypatisiel.devdevdev.exception.OAuth2LoginException;
import com.dreamypatisiel.devdevdev.exception.TokenInvalidException;
import com.dreamypatisiel.devdevdev.web.response.ResultType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 시큐리티 필터 체인에서 발생하는 예외를 처리하는 필터
 */
@Slf4j
@Component
public class SecurityExceptionFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws IOException {

        try {
            log.info("SecurityExceptionFilter 시작");
            filterChain.doFilter(request, response);
        } catch (TokenInvalidException | JwtAuthenticationException e) {
            sendErrorMessage(response, HttpServletResponse.SC_UNAUTHORIZED, e);
        } catch (JwtAccessDeniedException e) {
            sendErrorMessage(response, HttpServletResponse.SC_FORBIDDEN, e);
        } catch (OAuth2LoginException e) {
            sendErrorMessage(response, HttpServletResponse.SC_SERVICE_UNAVAILABLE, e);
        } catch (Exception e) {
            sendErrorMessage(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e);
        }
    }

    private void sendErrorMessage(HttpServletResponse response, int status, Exception e) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
        response.setStatus(status);
        response.getWriter().write(
                ErrorResponse.of(e.getMessage(), status)
                .convertToJson()
        );
    }

    @Data
    static class ErrorResponse {
        private ResultType resultType;
        private String message;
        private int errorCode;

        public static ErrorResponse of(String message, int errorCode) {
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.resultType = ResultType.FAIL;
            errorResponse.message = message;
            errorResponse.errorCode = errorCode;

            return errorResponse;
        }

        private String convertToJson() throws JsonProcessingException {
            ObjectMapper om = new ObjectMapper();
            return om.writeValueAsString(this);
        }
    }
}