package com.dreamypatisiel.devdevdev.web.controller.subscription;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dreamypatisiel.devdevdev.domain.service.techArticle.subscription.SubscriptionService;
import com.dreamypatisiel.devdevdev.global.constant.SecurityConstant;
import com.dreamypatisiel.devdevdev.web.controller.SupportControllerTest;
import com.dreamypatisiel.devdevdev.web.dto.request.subscription.SubscribeCompanyRequest;
import com.dreamypatisiel.devdevdev.web.dto.response.ResultType;
import com.dreamypatisiel.devdevdev.web.dto.response.techArticle.SubscriptionResponse;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

class SubscriptionControllerTest extends SupportControllerTest {

    @MockBean
    SubscriptionService subscriptionService;

    @Test
    @DisplayName("회원이 기업을 구독한다.")
    void subscribe() throws Exception {
        // given
        given(subscriptionService.subscribe(anyLong(), any())).willReturn(new SubscriptionResponse(1L));

        // when // then
        mockMvc.perform(post(DEFAULT_PATH_V1 + "/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(SecurityConstant.AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                        .content(om.writeValueAsBytes(new SubscribeCompanyRequest(1L)))
                        .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultType").value(ResultType.SUCCESS.name()))
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andExpect(jsonPath("$.data.id").isNumber());
    }

    @Test
    @DisplayName("회원이 기업을 구독 취소한다.")
    void unsubscribe() throws Exception {
        // given // when
        doNothing().when(subscriptionService).unsubscribe(anyLong(), any());

        // then
        mockMvc.perform(delete(DEFAULT_PATH_V1 + "/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(SecurityConstant.AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                        .content(om.writeValueAsBytes(new SubscribeCompanyRequest(1L)))
                        .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultType").value(ResultType.SUCCESS.name()));

        // 호출 여부 확인
        verify(subscriptionService, times(1)).unsubscribe(anyLong(), any());
    }
}