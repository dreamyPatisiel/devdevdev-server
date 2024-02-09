package com.dreamypatisiel.devdevdev.web;

import static com.dreamypatisiel.devdevdev.global.constant.SecurityConstant.PREFLIGHT_MAX_AGE;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest
@AutoConfigureMockMvc
public class CorsTest {

    @Autowired
    MockMvc mockMvc;
    String urlTemplate = "/devdevdev/api/v1/public";

    @Test
    @DisplayName("CORS 허용 테스트")
    void corsAllowedTest() throws Exception {
        // given
        String originUrl = "https://www.devdevdev.co.kr";

        // when // then
        mockMvc.perform(MockMvcRequestBuilders.options(urlTemplate)
                        .header(HttpHeaders.ORIGIN, originUrl)
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, HttpMethod.GET)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.header()
                        .string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, originUrl))
                .andExpect(MockMvcResultMatchers.header()
                        .string(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, HttpMethod.GET.name()))
                .andExpect(MockMvcResultMatchers.header()
                        .string(HttpHeaders.ACCESS_CONTROL_MAX_AGE, String.valueOf(PREFLIGHT_MAX_AGE)));
    }
}
