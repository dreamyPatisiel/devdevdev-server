package com.dreamypatisiel.devdevdev.web;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.List;
import java.util.stream.Stream;

import static com.dreamypatisiel.devdevdev.global.constant.SecurityConstant.PREFLIGHT_MAX_AGE;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest
@AutoConfigureMockMvc
@EnableConfigurationProperties(CorsProperties.class)
public class CorsTest {

    @Autowired
    MockMvc mockMvc;
    String urlTemplate = "/devdevdev/api/v1/public";
    static List<String> origins;


    @ParameterizedTest
    @MethodSource("corsOriginProvider")
    @DisplayName("CORS 허용 테스트")
    void corsAllowedTest(String origin) throws Exception {
        // given // when // then
        mockMvc.perform(MockMvcRequestBuilders.options(urlTemplate)
                        .header(HttpHeaders.ORIGIN, origin)
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, HttpMethod.GET)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.header()
                        .string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin))
                .andExpect(MockMvcResultMatchers.header()
                        .string(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, HttpMethod.GET.name()))
                .andExpect(MockMvcResultMatchers.header()
                        .string(HttpHeaders.ACCESS_CONTROL_MAX_AGE, String.valueOf(PREFLIGHT_MAX_AGE)));
    }

    @BeforeAll
    static void setup(@Autowired CorsProperties corsProperties) {
        origins = corsProperties.getOrigin();
    }

    static Stream<Arguments> corsOriginProvider() {
        return origins.stream().map(Arguments::of);
    }

}
