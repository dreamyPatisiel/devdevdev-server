package com.dreamypatisiel.devdevdev.global.security.jwt.handler;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.dreamypatisiel.devdevdev.exception.JwtAuthenticationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;

@SpringBootTest
class JwtAuthenticationEntryPointHandlerTest {

    @Autowired
    JwtAuthenticationEntryPointHandler authenticationEntryPointHandler;

    @Test
    @DisplayName("인증에 문제가 있을때 예외가 발생한다.")
    void commence() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        AuthenticationException exception = new BadCredentialsException("exception");

        // when // then
        assertThatThrownBy(() -> authenticationEntryPointHandler.commence(request, response, exception))
                .isInstanceOf(JwtAuthenticationException.class)
                .hasMessage(JwtAuthenticationEntryPointHandler.INVALID_MEMBER_MESSAGE);
    }
}