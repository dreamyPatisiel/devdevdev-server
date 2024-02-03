package com.dreamypatisiel.devdevdev.global.security.jwt.filter;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import com.dreamypatisiel.devdevdev.global.constant.SecurityConstant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

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