package com.dreamypatisiel.devdevdev.test;

import static io.lettuce.core.BitFieldArgs.OverflowType.FAIL;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import com.dreamypatisiel.devdevdev.limiter.LimiterPlan;
import com.dreamypatisiel.devdevdev.web.controller.SupportControllerTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest
@AutoConfigureMockMvc
class TestControllerTest extends SupportControllerTest {

    @Autowired
    MockMvc mockMvc;

    final String urlTemplate = "/devdevdev/api/v1/test/members";

    @Test
    @DisplayName("API 호출 테스트")
    void getMember() throws Exception {
        // given // when // then
        mockMvc.perform(MockMvcRequestBuilders.get(urlTemplate)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @DisplayName("API를 호출할 때 처리율 제한 장치 정책에 위반하면 예외가 발생한다.")
    void tooManyRequestGetMember() throws Exception {
        // given
        long capacity = LimiterPlan.TEST.getLimit().getCapacity();

        // when // then
        for (int i = 1; i < capacity; i++) {
            mockMvc.perform(MockMvcRequestBuilders.get(urlTemplate)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(MockMvcResultMatchers.status().isOk());
        }

        mockMvc.perform(MockMvcRequestBuilders.get(urlTemplate)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isTooManyRequests())
                .andExpect(jsonPath("$.resultType").value(FAIL.name()))
                .andExpect(jsonPath("$.message").isString())
                .andExpect(jsonPath("$.errorCode").isNumber());
    }
}