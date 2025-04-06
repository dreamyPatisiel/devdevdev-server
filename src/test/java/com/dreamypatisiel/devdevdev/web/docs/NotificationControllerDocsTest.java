package com.dreamypatisiel.devdevdev.web.docs;

import static com.dreamypatisiel.devdevdev.web.docs.custom.CustomPreprocessors.modifyResponseBody;
import static io.lettuce.core.BitFieldArgs.OverflowType.FAIL;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseBody;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dreamypatisiel.devdevdev.domain.entity.enums.NotificationType;
import com.dreamypatisiel.devdevdev.domain.service.NotificationService;
import com.dreamypatisiel.devdevdev.global.constant.SecurityConstant;
import com.dreamypatisiel.devdevdev.redis.pub.NotificationPublisher;
import com.dreamypatisiel.devdevdev.redis.sub.NotificationMessageDto;
import com.dreamypatisiel.devdevdev.web.dto.request.publish.PublishTechArticle;
import com.dreamypatisiel.devdevdev.web.dto.request.publish.PublishTechArticleRequest;
import com.dreamypatisiel.devdevdev.web.dto.response.ResultType;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

class NotificationControllerDocsTest extends SupportControllerDocsTest {

    @MockBean
    NotificationService notificationService;

    @MockBean
    NotificationPublisher notificationPublisher;

    /**
     * SseEmitter는 내부적으로 HttpServletResponse.getOutputStream() 또는 getWriter()를 직접 사용해서 데이터를 보냄 그래서
     * MockMvc.perform().characterEncoding("UTF-8")이 무시되는 거고, ISO-8859-1로 처리됨
     */
    @Test
    @DisplayName("회원이 실시간 알림을 수신한다.")
    void notification() throws Exception {
        // given
        SseEmitter sseEmitter = new SseEmitter();
        sseEmitter.send(
                SseEmitter.event()
                        .data(new NotificationMessageDto("트이다에서 새로운 기슬블로그 105개가 올라왔어요!",
                                LocalDateTime.of(2025, 4, 6, 0, 0, 0)))
        );

        given(notificationService.addClientAndSendNotification(any())).willReturn(sseEmitter);

        // when // then
        ResultActions actions = mockMvc.perform(get("/devdevdev/api/v1/notifications")
                        .contentType(MediaType.TEXT_EVENT_STREAM_VALUE)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header(SecurityConstant.AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM_VALUE));

        // docs
        actions.andDo(document("notifications",
                preprocessRequest(prettyPrint()),
                preprocessResponse(modifyResponseBody()),
                requestHeaders(
                        headerWithName(SecurityConstant.AUTHORIZATION_HEADER).description("Bearer Token"),
                        headerWithName("Content-Type").description(MediaType.TEXT_EVENT_STREAM_VALUE)
                ),
                responseHeaders(
                        headerWithName("Content-Type").description(MediaType.TEXT_EVENT_STREAM_VALUE)
                ),
                responseBody()
        ));
    }

    @Test
    @DisplayName("알림을 생성한다.")
    void publishNotifications() throws Exception {
        // given
        NotificationMessageDto notificationMessageDto = new NotificationMessageDto("트이다에서 새로운 글이 올라왔어요!",
                LocalDateTime.of(2025, 4, 6, 0, 0, 0));

        PublishTechArticleRequest request = new PublishTechArticleRequest(
                1L,
                List.of(new PublishTechArticle(1L),
                        new PublishTechArticle(2L))
        );

        // when
        doNothing().when(notificationPublisher).publish(NotificationType.SUBSCRIPTION, notificationMessageDto);

        // then
        mockMvc.perform(post("/devdevdev/api/v1/notifications/{channel}", NotificationType.SUBSCRIPTION)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header(SecurityConstant.AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                        .content(om.writeValueAsBytes(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultType").value(ResultType.SUCCESS.name()));
    }

    @Test
    @DisplayName("알림을 생성할 때 잘못된 채널을 입력하면 예외가 발생한다.")
    void publishNotificationsException() throws Exception {
        // given
        NotificationMessageDto notificationMessageDto = new NotificationMessageDto("트이다에서 새로운 글이 올라왔어요!",
                LocalDateTime.of(2025, 4, 6, 0, 0, 0));

        PublishTechArticleRequest request = new PublishTechArticleRequest(
                1L,
                List.of(new PublishTechArticle(1L),
                        new PublishTechArticle(2L))
        );

        // when
        doNothing().when(notificationPublisher).publish(NotificationType.SUBSCRIPTION, notificationMessageDto);

        // then
        mockMvc.perform(post("/devdevdev/api/v1/notifications/{channel}", "INVALID_CHANNEL")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header(SecurityConstant.AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                        .content(om.writeValueAsBytes(request)))
                .andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.resultType").value(FAIL.name()))
                .andExpect(jsonPath("$.message").isString())
                .andExpect(jsonPath("$.errorCode").isNumber());
    }
}