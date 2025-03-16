package com.dreamypatisiel.devdevdev.web.controller.subscription;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dreamypatisiel.devdevdev.domain.service.techArticle.subscription.MemberSubscriptionService;
import com.dreamypatisiel.devdevdev.global.constant.SecurityConstant;
import com.dreamypatisiel.devdevdev.web.controller.SupportControllerTest;
import com.dreamypatisiel.devdevdev.web.dto.response.ResultType;
import com.dreamypatisiel.devdevdev.web.dto.response.subscription.CompanyDetailResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.subscription.SubscriableCompanyResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.techArticle.SubscriptionResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.SliceImpl;
import org.springframework.http.MediaType;

class SubscriptionControllerTest extends SupportControllerTest {

    @MockBean
    MemberSubscriptionService memberSubscriptionService;

    @Test
    @DisplayName("회원이 기업을 구독한다.")
    void subscribe() throws Exception {
        // given
        given(memberSubscriptionService.subscribe(anyLong(), any())).willReturn(new SubscriptionResponse(1L));

        // when // then
        mockMvc.perform(post(DEFAULT_PATH_V1 + "/subscriptions/{companyId}", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(SecurityConstant.AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
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
        doNothing().when(memberSubscriptionService).unsubscribe(anyLong(), any());

        // then
        mockMvc.perform(delete(DEFAULT_PATH_V1 + "/subscriptions/{companyId}", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(SecurityConstant.AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultType").value(ResultType.SUCCESS.name()));

        // 호출 여부 확인
        verify(memberSubscriptionService, times(1)).unsubscribe(anyLong(), any());
    }

    @Test
    @DisplayName("구독 가능한 기업 목록을 조회한다.")
    void getSubscriptions() throws Exception {
        // given
        SubscriableCompanyResponse response = new SubscriableCompanyResponse(1L,
                "https://www.teuida.net/public/src/img/teuida_logo.png", true);
        given(memberSubscriptionService.getSubscribableCompany(any(), anyLong(), any()))
                .willReturn(new SliceImpl<>(List.of(response), PageRequest.of(0, 20), false));

        // when // then
        mockMvc.perform(get(DEFAULT_PATH_V1 + "/subscriptions/companies")
                        .queryParam("size", "10")
                        .queryParam("companyId", "105")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(SecurityConstant.AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.[0].companyId").isNumber())
                .andExpect(jsonPath("$.data.content.[0].companyImageUrl").isString())
                .andExpect(jsonPath("$.data.content.[0].isSubscribed").isBoolean())
                .andExpect(jsonPath("$.data.pageable").isNotEmpty())
                .andExpect(jsonPath("$.data.pageable.pageNumber").isNumber())
                .andExpect(jsonPath("$.data.pageable.pageSize").isNumber())
                .andExpect(jsonPath("$.data.pageable.sort").isNotEmpty())
                .andExpect(jsonPath("$.data.pageable.sort.empty").isBoolean())
                .andExpect(jsonPath("$.data.pageable.sort.sorted").isBoolean())
                .andExpect(jsonPath("$.data.pageable.sort.unsorted").isBoolean())
                .andExpect(jsonPath("$.data.pageable.offset").isNumber())
                .andExpect(jsonPath("$.data.pageable.paged").isBoolean())
                .andExpect(jsonPath("$.data.pageable.unpaged").isBoolean())
                .andExpect(jsonPath("$.data.first").isBoolean())
                .andExpect(jsonPath("$.data.last").isBoolean())
                .andExpect(jsonPath("$.data.size").isNumber())
                .andExpect(jsonPath("$.data.number").isNumber())
                .andExpect(jsonPath("$.data.sort").isNotEmpty())
                .andExpect(jsonPath("$.data.sort.empty").isBoolean())
                .andExpect(jsonPath("$.data.sort.sorted").isBoolean())
                .andExpect(jsonPath("$.data.sort.unsorted").isBoolean())
                .andExpect(jsonPath("$.data.numberOfElements").isNumber())
                .andExpect(jsonPath("$.data.empty").isBoolean());
    }

    @Test
    @DisplayName("구독 가능한 기업 상세 정보를 조회한다.")
    void getCompanyDetails() throws Exception {
        // given
        CompanyDetailResponse response = CompanyDetailResponse.builder()
                .companyId(1L)
                .companyName("Teuida")
                .industry("교육")
                .companyCareerUrl("https://www.wanted.co.kr/company/5908")
                .companyOfficialImageUrl("https://www.teuida.net/public/src/img/teuida_logo.png")
                .companyDescription("“외국인의 한국어가 트이다”\n"
                        + "영어 공부 오래 했지만 영어로 말할 때 주저하게 되죠? 외국인도 마찬가지예요. 한국어 말할 때 주저하게 돼요.\n"
                        + "트이다는 화면 속 한국인과 가상대화를 하는 경험을 통해 외국인이 한국어 회화에 자신감을 가질 수 있도록 도움을 주는 스타트업이에요.\n"
                        + "전 세계 130만 명 이상의 사용자가 웹드라마 속 배우랑 가상대화를 하면서 한국어를 배우고 한국어 말하기에 자신감을 향상하고 있어요.\n"
                        + "우리는 외국어를 학습하기보다 여행을 갔을 때, 외국인 친구를 사귀었을 때 자신이 하고 싶은 말을 할 수 있도록 재미있는 콘텐츠와 서비스를 제공하기 위해 노력하고 있어요. 모든 사람들이 자신의 생각, 의견을 어떤 언어로든 말할 수 있는 용기를 심어주고 싶어요.")
                .techArticleTotalCount(3980L)
                .isSubscribed(true)
                .build();

        given(memberSubscriptionService.getCompanyDetail(anyLong(), any())).willReturn(response);

        // when // then
        mockMvc.perform(get(DEFAULT_PATH_V1 + "/subscriptions/companies/{companyId}", 1L)
                        .queryParam("size", "10")
                        .queryParam("companyId", "105")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(SecurityConstant.AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultType").value(ResultType.SUCCESS.name()))
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andExpect(jsonPath("$.data.companyId").isNumber())
                .andExpect(jsonPath("$.data.companyName").isString())
                .andExpect(jsonPath("$.data.industry").isString())
                .andExpect(jsonPath("$.data.companyCareerUrl").isString())
                .andExpect(jsonPath("$.data.companyOfficialImageUrl").isString())
                .andExpect(jsonPath("$.data.companyDescription").isString())
                .andExpect(jsonPath("$.data.techArticleTotalCount").isNumber())
                .andExpect(jsonPath("$.data.isSubscribed").isBoolean());
    }
}