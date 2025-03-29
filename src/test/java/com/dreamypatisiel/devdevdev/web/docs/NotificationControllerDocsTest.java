package com.dreamypatisiel.devdevdev.web.docs;

import com.dreamypatisiel.devdevdev.domain.exception.NotificationExceptionMessage;
import com.dreamypatisiel.devdevdev.domain.service.notification.NotificationService;
import com.dreamypatisiel.devdevdev.exception.NotFoundException;
import com.dreamypatisiel.devdevdev.global.constant.SecurityConstant;
import com.dreamypatisiel.devdevdev.web.dto.response.notification.NotificationReadResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import java.nio.charset.StandardCharsets;

import static com.dreamypatisiel.devdevdev.global.constant.SecurityConstant.AUTHORIZATION_HEADER;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.JsonFieldType.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class NotificationControllerDocsTest extends SupportControllerDocsTest {

    @MockBean
    NotificationService notificationService;

    @Test
    @DisplayName("회원이 단건 알림을 읽으면 isRead가 true로 변경된 응답을 받는다.")
    void readNotification_success() throws Exception {
        // given
        Long notificationId = 1L;
        given(notificationService.readNotification(anyLong(), any()))
                .willReturn(new NotificationReadResponse(notificationId, true));

        // when
        ResultActions actions = mockMvc.perform(patch(DEFAULT_PATH_V1 + "/notifications/{notificationId}/read", notificationId)
                        .header(SecurityConstant.AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().isOk());

        // docs
        actions.andDo(document("read-notification",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                        headerWithName(AUTHORIZATION_HEADER).description("Bearer 엑세스 토큰")
                ),
                pathParameters(
                        parameterWithName("notificationId").description("알림 아이디")
                ),
                responseFields(
                        fieldWithPath("resultType").type(STRING).description("응답 결과"),
                        fieldWithPath("data").type(OBJECT).description("응답 데이터"),
                        fieldWithPath("data.id").type(NUMBER).description("알림 아이디"),
                        fieldWithPath("data.isRead").type(BOOLEAN).description("알림 읽음 여부")
                )
        ));
    }

    @Test
    @DisplayName("회원이 자신의 알림이 아닌 알림을 조회하면 예외가 발생한다.")
    void readNotification_notOwner_throwsException() throws Exception {
        // given
        Long notificationId = 2L;
        given(notificationService.readNotification(anyLong(), any()))
                .willThrow(new NotFoundException(NotificationExceptionMessage.NOT_FOUND_NOTIFICATION_MESSAGE));

        // when
        ResultActions actions = mockMvc.perform(patch(DEFAULT_PATH_V1 + "/notifications/{notificationId}/read", notificationId)
                        .header(SecurityConstant.AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().isNotFound());

        // docs
        actions.andDo(document("read-notification-not-found",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                        headerWithName(AUTHORIZATION_HEADER).description("Bearer 엑세스 토큰")
                ),
                pathParameters(
                        parameterWithName("notificationId").description("알림 아이디")
                ),
                exceptionResponseFields()
        ));
    }
}