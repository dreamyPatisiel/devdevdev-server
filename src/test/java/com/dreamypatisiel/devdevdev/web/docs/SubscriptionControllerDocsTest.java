package com.dreamypatisiel.devdevdev.web.docs;

import static com.dreamypatisiel.devdevdev.global.constant.SecurityConstant.AUTHORIZATION_HEADER;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.JsonFieldType.ARRAY;
import static org.springframework.restdocs.payload.JsonFieldType.BOOLEAN;
import static org.springframework.restdocs.payload.JsonFieldType.NUMBER;
import static org.springframework.restdocs.payload.JsonFieldType.OBJECT;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dreamypatisiel.devdevdev.domain.exception.CompanyExceptionMessage;
import com.dreamypatisiel.devdevdev.domain.exception.SubscriptionExceptionMessage;
import com.dreamypatisiel.devdevdev.domain.service.techArticle.subscription.MemberSubscriptionService;
import com.dreamypatisiel.devdevdev.exception.NotFoundException;
import com.dreamypatisiel.devdevdev.global.constant.SecurityConstant;
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
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.test.web.servlet.ResultActions;

class SubscriptionControllerDocsTest extends SupportControllerDocsTest {

    @MockBean
    MemberSubscriptionService memberSubscriptionService;

    @Test
    @DisplayName("회원이 기업을 구독한다.")
    void subscribe() throws Exception {
        // given
        given(memberSubscriptionService.subscribe(anyLong(), any())).willReturn(new SubscriptionResponse(1L));

        // when // then
        ResultActions actions = mockMvc.perform(post(DEFAULT_PATH_V1 + "/subscriptions/{companyId}", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(SecurityConstant.AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().isOk());

        // docs
        actions.andDo(document("subscribe-company",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                        headerWithName(AUTHORIZATION_HEADER).description("Bearer 엑세스 토큰")
                ),
                pathParameters(
                        parameterWithName("companyId").description("기업 아이디")
                ),
                responseFields(
                        fieldWithPath("resultType").type(STRING).description("응답 결과"),
                        fieldWithPath("data").type(OBJECT).description("응답 데이터"),
                        fieldWithPath("data.id").type(NUMBER).description("구독 아이디")
                )
        ));
    }

    @Test
    @DisplayName("회원이 기업을 구독할 때 기업이 존재하지 않으면 예외가 발생한다.")
    void subscribeNotFoundException() throws Exception {
        // given
        given(memberSubscriptionService.subscribe(anyLong(), any())).willThrow(
                new NotFoundException(CompanyExceptionMessage.NOT_FOUND_COMPANY_MESSAGE));

        // when // then
        ResultActions actions = mockMvc.perform(post(DEFAULT_PATH_V1 + "/subscriptions/{companyId}", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(SecurityConstant.AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().is4xxClientError());

        // docs
        actions.andDo(document("subscribe-company-not-found-company",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                        headerWithName(AUTHORIZATION_HEADER).description("Bearer 엑세스 토큰")
                ),
                pathParameters(
                        parameterWithName("companyId").description("기업 아이디")
                ),
                exceptionResponseFields()
        ));
    }

    @Test
    @DisplayName("회원이 기업을 구독 취소한다.")
    void unsubscribe() throws Exception {
        // given // when
        doNothing().when(memberSubscriptionService).unsubscribe(anyLong(), any());

        // then
        ResultActions actions = mockMvc.perform(delete(DEFAULT_PATH_V1 + "/subscriptions/{companyId}", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().isOk());

        // docs
        actions.andDo(document("unsubscribe-company",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                        headerWithName(AUTHORIZATION_HEADER).description("Bearer 엑세스 토큰")
                ),
                pathParameters(
                        parameterWithName("companyId").description("기업 아이디")
                ),
                responseFields(
                        fieldWithPath("resultType").type(STRING).description("응답 결과")
                )
        ));
    }

    @Test
    @DisplayName("회원이 기업을 구독 취소할 때 구독 이력이 없으면 예외가 발생한다.")
    void unsubscribeSubscriptionException() throws Exception {
        // given // when
        doThrow(new NotFoundException(SubscriptionExceptionMessage.NOT_FOUND_SUBSCRIPTION_MESSAGE))
                .when(memberSubscriptionService).unsubscribe(anyLong(), any());

        // then
        ResultActions actions = mockMvc.perform(delete(DEFAULT_PATH_V1 + "/subscriptions/{companyId}", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().is4xxClientError());

        // docs
        actions.andDo(document("unsubscribe-company-not-found-subscription",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                        headerWithName(AUTHORIZATION_HEADER).description("Bearer 엑세스 토큰")
                ),
                pathParameters(
                        parameterWithName("companyId").description("기업 아이디")
                ),
                exceptionResponseFields()
        ));
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
        ResultActions actions = mockMvc.perform(
                        RestDocumentationRequestBuilders.get(DEFAULT_PATH_V1 + "/subscriptions/companies")
                                .queryParam("size", "10")
                                .queryParam("companyId", "105")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                                .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().isOk());

        // docs
        actions.andDo(document("subscribable-companies",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                        headerWithName(AUTHORIZATION_HEADER).description("Bearer 엑세스 토큰")
                ),
                queryParameters(
                        parameterWithName("size").optional().description("조회되는 데이터 수"),
                        parameterWithName("companyId").optional().description("기업 아이디")
                ),
                responseFields(
                        fieldWithPath("resultType").type(STRING).description("응답 결과"),
                        fieldWithPath("data").type(OBJECT).description("응답 데이터"),

                        fieldWithPath("data.content").type(ARRAY).description("구독 가능한 기업 목록 메인 배열"),
                        fieldWithPath("data.content[].companyId").type(NUMBER).description("기업 아이디"),
                        fieldWithPath("data.content[].companyImageUrl").type(STRING).description("기업 로고 이미지 url"),
                        fieldWithPath("data.content[].isSubscribed").type(BOOLEAN).description("회원의 구독 여부"),

                        fieldWithPath("data.pageable").type(OBJECT).description("픽픽픽 메인 페이지네이션 정보"),
                        fieldWithPath("data.pageable.pageNumber").type(NUMBER).description("페이지 번호"),
                        fieldWithPath("data.pageable.pageSize").type(NUMBER).description("페이지 사이즈"),

                        fieldWithPath("data.pageable.sort").type(OBJECT).description("정렬 정보"),
                        fieldWithPath("data.pageable.sort.empty").type(BOOLEAN).description("정렬 정보가 비어있는지 여부"),
                        fieldWithPath("data.pageable.sort.sorted").type(BOOLEAN).description("정렬 여부"),
                        fieldWithPath("data.pageable.sort.unsorted").type(BOOLEAN).description("비정렬 여부"),

                        fieldWithPath("data.pageable.offset").type(NUMBER).description("페이지 오프셋 (페이지 크기 * 페이지 번호)"),
                        fieldWithPath("data.pageable.paged").type(BOOLEAN).description("페이지 정보 포함 여부"),
                        fieldWithPath("data.pageable.unpaged").type(BOOLEAN).description("페이지 정보 비포함 여부"),

                        fieldWithPath("data.first").type(BOOLEAN).description("현재 페이지가 첫 페이지 여부"),
                        fieldWithPath("data.last").type(BOOLEAN).description("현재 페이지가 마지막 페이지 여부"),
                        fieldWithPath("data.size").type(NUMBER).description("페이지 크기"),
                        fieldWithPath("data.number").type(NUMBER).description("현재 페이지"),

                        fieldWithPath("data.sort").type(OBJECT).description("정렬 정보"),
                        fieldWithPath("data.sort.empty").type(BOOLEAN).description("정렬 정보가 비어있는지 여부"),
                        fieldWithPath("data.sort.sorted").type(BOOLEAN).description("정렬 상태 여부"),
                        fieldWithPath("data.sort.unsorted").type(BOOLEAN).description("비정렬 상태 여부"),
                        fieldWithPath("data.numberOfElements").type(NUMBER).description("현재 페이지 데이터 수"),
                        fieldWithPath("data.empty").type(BOOLEAN).description("현재 빈 페이지 여부")
                )
        ));
    }
}