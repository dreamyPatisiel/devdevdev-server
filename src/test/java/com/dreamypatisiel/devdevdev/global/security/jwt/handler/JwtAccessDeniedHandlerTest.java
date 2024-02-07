package com.dreamypatisiel.devdevdev.global.security.jwt.handler;

import static org.junit.jupiter.api.Assertions.*;

import com.dreamypatisiel.devdevdev.exception.JwtAccessDeniedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class JwtAccessDeniedHandlerTest {

    @Autowired
    JwtAccessDeniedHandler jwtAccessDeniedHandler;

    @Test
    @DisplayName("해당 자원에 접근 권한이 없으면 예외가 발생한다.")
    void handle() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        AccessDeniedException accessDeniedException = new AccessDeniedException("exception");

        // when // then
        assertThatThrownBy(() -> jwtAccessDeniedHandler.handle(request, response, accessDeniedException))
                .isInstanceOf(JwtAccessDeniedException.class)
                .hasMessage(JwtAccessDeniedHandler.INVALID_ACCESS_DENIED_MESSAGE);
    }
}