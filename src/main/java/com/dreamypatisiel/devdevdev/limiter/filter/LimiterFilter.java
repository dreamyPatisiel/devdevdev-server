package com.dreamypatisiel.devdevdev.limiter.filter;

import com.dreamypatisiel.devdevdev.limiter.LimiterPlan;
import com.dreamypatisiel.devdevdev.limiter.TokenBucketResolver;
import com.dreamypatisiel.devdevdev.limiter.exception.LimiterException;
import com.dreamypatisiel.devdevdev.web.response.BasicResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.entity.ContentType;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * @Note: 스프링 부트를 사용하면 내장 톰캣을 지원하기 때문에톰캣과 같은 서블릿 컨테이너까지 스프링 부트가 제어 가능
 * @Author: 장세웅
 * @Since: 2024.06.26
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class LimiterFilter extends OncePerRequestFilter {

    private final TokenBucketResolver tokenBucketResolver;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String limiterKey = LimiterPlan.createLimiterKey(request);
            log.info("limiterKey={} ", limiterKey);
            tokenBucketResolver.checkBucketCounter(limiterKey);

            filterChain.doFilter(request, response);
        } catch (LimiterException e) {
            sendError(e, response, HttpStatus.TOO_MANY_REQUESTS);
        }
    }

    private void sendError(Exception e, HttpServletResponse response, HttpStatus httpStatus)
            throws IOException {

        ObjectMapper om = new ObjectMapper();
        BasicResponse<Object> failResponse = BasicResponse.fail(e.getMessage(), httpStatus.value());

        response.setContentType(ContentType.APPLICATION_JSON.toString());
        response.setStatus(httpStatus.value());
        response.getWriter().write(om.writeValueAsString(failResponse));
    }
}
