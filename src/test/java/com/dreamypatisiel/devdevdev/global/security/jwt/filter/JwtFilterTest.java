package com.dreamypatisiel.devdevdev.global.security.jwt.filter;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dreamypatisiel.devdevdev.global.constant.SecurityConstant;
import com.dreamypatisiel.devdevdev.global.security.jwt.service.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MockMvcBuilder;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
class JwtFilterTest {

    @Autowired
    MockMvc mockMvc;
    @MockBean
    JwtFilter jwtFilter;
    @Autowired
    WebApplicationContext applicationContext;

    @BeforeEach
    void setupMockMvc() {
        mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext)
                .addFilter(jwtFilter)
                .alwaysDo(print())
                .build();
    }

    @ParameterizedTest
    @MethodSource("whiteList")
    @DisplayName("JWT_FILTER_WHITELIST_URL은 JwtFilter를 실행하지 않는다.")
    void doFilterInternal(String whiteList) throws Exception {
        // given // when
        mockMvc.perform(get(whiteList));

        // then
        verify(jwtFilter, never()).doFilterInternal(any(), any(), any());
    }

    private static String[] whiteList() {
        return SecurityConstant.JWT_FILTER_WHITELIST_URL;
    }
}