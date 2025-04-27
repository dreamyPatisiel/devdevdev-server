package com.dreamypatisiel.devdevdev.web.controller.notification;

import static io.lettuce.core.BitFieldArgs.OverflowType.FAIL;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dreamypatisiel.devdevdev.domain.entity.enums.NotificationType;
import com.dreamypatisiel.devdevdev.domain.exception.NotificationExceptionMessage;
import com.dreamypatisiel.devdevdev.domain.service.ApiKeyService;
import com.dreamypatisiel.devdevdev.domain.service.notification.NotificationService;
import com.dreamypatisiel.devdevdev.exception.NotFoundException;
import com.dreamypatisiel.devdevdev.global.constant.SecurityConstant;
import com.dreamypatisiel.devdevdev.redis.pub.NotificationPublisher;
import com.dreamypatisiel.devdevdev.redis.sub.NotificationMessageDto;
import com.dreamypatisiel.devdevdev.web.controller.SupportControllerTest;

import com.dreamypatisiel.devdevdev.web.dto.SliceCustom;
import com.dreamypatisiel.devdevdev.web.dto.response.ResultType;
import com.dreamypatisiel.devdevdev.web.dto.response.notification.*;
import com.dreamypatisiel.devdevdev.web.dto.response.techArticle.CompanyResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.techArticle.TechArticleMainResponse;

import com.dreamypatisiel.devdevdev.web.dto.request.publish.PublishTechArticle;
import com.dreamypatisiel.devdevdev.web.dto.request.publish.PublishTechArticleRequest;
import com.dreamypatisiel.devdevdev.web.dto.response.notification.NotificationReadResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

class NotificationControllerTest extends SupportControllerTest {

    @MockBean
    NotificationService notificationService;

    @MockBean
    NotificationPublisher notificationPublisher;
    @MockBean
    ApiKeyService apiKeyService;

    @Test
    @DisplayName("회원이 단건 알림을 읽으면 isRead가 true로 변경된 응답을 받는다.")
    void readNotification() throws Exception {
        // given
        Long notificationId = 1L;
        given(notificationService.readNotification(anyLong(), any()))
                .willReturn(new NotificationReadResponse(notificationId, true));

        // when // then
        mockMvc.perform(patch(DEFAULT_PATH_V1 + "/notifications/{notificationId}/read", notificationId)
                        .header(SecurityConstant.AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultType").value(ResultType.SUCCESS.name()))
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andExpect(jsonPath("$.data.id").value(notificationId))
                .andExpect(jsonPath("$.data.isRead").value(true));
    }

    @Test
    @DisplayName("회원이 자신의 알림이 아닌 알림을 조회하면 예외가 발생한다.")
    void readNotificationNotOwnerException() throws Exception {
        // given
        Long notificationId = 1L;
        given(notificationService.readNotification(anyLong(), any()))
                .willThrow(new NotFoundException(NotificationExceptionMessage.NOT_FOUND_NOTIFICATION_MESSAGE));

        // when // then
        mockMvc.perform(patch(DEFAULT_PATH_V1 + "/notifications/{notificationId}/read", notificationId)
                        .header(SecurityConstant.AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultType").value(ResultType.FAIL.name()))
                .andExpect(jsonPath("$.message").value(NotificationExceptionMessage.NOT_FOUND_NOTIFICATION_MESSAGE));
    }

    @Test
    @DisplayName("회원이 모든 알림을 읽는다.")
    void readAllNotifications() throws Exception {
        // given
        // 별도의 응답이 없으므로 void 메서드 호출만 mock
        doNothing().when(notificationService).readAllNotifications(any());

        // when // then
        mockMvc.perform(patch(DEFAULT_PATH_V1 + "/notifications/read-all")
                        .header(SecurityConstant.AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultType").value(ResultType.SUCCESS.name()))
                .andExpect(jsonPath("$.data").doesNotExist());

        // 호출 여부 확인
        verify(notificationService, times(1)).readAllNotifications(any());
    }

    @Test
    @DisplayName("회원이 알림 팝업창을 조회한다.")
    void getNotificationPopup() throws Exception {
        // given
        PageRequest pageable = PageRequest.of(0, 1);
        List<NotificationPopupResponse> response = List.of(
                new NotificationPopupNewArticleResponse(1L, "기술블로그 타이틀", LocalDate.now(), false,
                        "기업명", 1L));
        given(notificationService.getNotificationPopup(any(), any()))
                .willReturn(new SliceCustom<>(response, pageable, false, 1L));

        // when // then
        mockMvc.perform(get(DEFAULT_PATH_V1 + "/notifications/popup")
                        .header(SecurityConstant.AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                        .queryParam("size", String.valueOf(pageable.getPageSize()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultType").value(ResultType.SUCCESS.name()))
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.[0].id").isNumber())
                .andExpect(jsonPath("$.data.content.[0].type").isString())
                .andExpect(jsonPath("$.data.content.[0].title").isString())
                .andExpect(jsonPath("$.data.content.[0].isRead").isBoolean())
                .andExpect(jsonPath("$.data.content.[0].createdAt").isString())
                .andExpect(jsonPath("$.data.content.[0].companyName").isString())
                .andExpect(jsonPath("$.data.content.[0].techArticleId").isNumber())
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
                .andExpect(jsonPath("$.data.totalElements").value(1))
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

        // 호출 여부 확인
        verify(notificationService, times(1)).getNotificationPopup(any(), any());
    }

    @Test
    @DisplayName("회원이 알림 페이지를 무한스크롤링으로 조회한다.")
    void getNotifications() throws Exception {
        // given
        PageRequest pageable = PageRequest.of(0, 1);
        TechArticleMainResponse techArticleMainResponse = createTechArticleMainResponse(
                1L, "elasticId", "http://thumbnailUrl.com", false,
                "http://techArticleUrl.com", "기술블로그 타이틀", "기술블로그 내용",
                1L, "기업명", "http://careerUrl.com","http://officialImage.com", LocalDate.now(), "작성자",
                0L, 0L, 0L, false, null
        );
        List<NotificationResponse> response = List.of(
                new NotificationNewArticleResponse(1L, LocalDate.now(), false, techArticleMainResponse));
        given(notificationService.getNotifications(any(), anyLong(), any()))
                .willReturn(new SliceCustom<>(response, pageable, true, 1L));

        // when // then
        mockMvc.perform(get(DEFAULT_PATH_V1 + "/notifications/page")
                        .header(SecurityConstant.AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                        .queryParam("size", String.valueOf(pageable.getPageSize()))
                        .queryParam("notificationId", String.valueOf(2L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultType").value(ResultType.SUCCESS.name()))
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.[0].notificationId").isNumber())
                .andExpect(jsonPath("$.data.content.[0].type").isString())
                .andExpect(jsonPath("$.data.content.[0].createdAt").isString())
                .andExpect(jsonPath("$.data.content.[0].isRead").isBoolean())
                .andExpect(jsonPath("$.data.content.[0].techArticle").isNotEmpty())
                .andExpect(jsonPath("$.data.content.[0].techArticle.id").isNumber())
                .andExpect(jsonPath("$.data.content.[0].techArticle.elasticId").isString())
                .andExpect(jsonPath("$.data.content.[0].techArticle.thumbnailUrl").isString())
                .andExpect(jsonPath("$.data.content.[0].techArticle.isLogoImage").isBoolean())
                .andExpect(jsonPath("$.data.content.[0].techArticle.techArticleUrl").isString())
                .andExpect(jsonPath("$.data.content.[0].techArticle.title").isString())
                .andExpect(jsonPath("$.data.content.[0].techArticle.contents").isString())
                .andExpect(jsonPath("$.data.content.[0].techArticle.regDate").isString())
                .andExpect(jsonPath("$.data.content.[0].techArticle.author").isString())
                .andExpect(jsonPath("$.data.content.[0].techArticle.viewTotalCount").isNumber())
                .andExpect(jsonPath("$.data.content.[0].techArticle.recommendTotalCount").isNumber())
                .andExpect(jsonPath("$.data.content.[0].techArticle.commentTotalCount").isNumber())
                .andExpect(jsonPath("$.data.content.[0].techArticle.popularScore").isNumber())
                .andExpect(jsonPath("$.data.content.[0].techArticle.isBookmarked").isBoolean())
                .andExpect(jsonPath("$.data.content.[0].techArticle.company").isNotEmpty())
                .andExpect(jsonPath("$.data.content.[0].techArticle.company.id").isNumber())
                .andExpect(jsonPath("$.data.content.[0].techArticle.company.name").isString())
                .andExpect(jsonPath("$.data.content.[0].techArticle.company.careerUrl").isString())
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
                .andExpect(jsonPath("$.data.totalElements").value(1))
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

        // 호출 여부 확인
        verify(notificationService, times(1)).getNotifications(any(), anyLong(), any());
    }

    private TechArticleMainResponse createTechArticleMainResponse(Long id, String elasticId, String thumbnailUrl, Boolean isLogoImage,
                                                                  String techArticleUrl, String title, String contents,
                                                                  Long companyId, String companyName, String careerUrl, String officialImageUrl,
                                                                  LocalDate regDate, String author, long recommendCount,
                                                                  long commentCount, long viewCount, Boolean isBookmarked, Float score) {
        return TechArticleMainResponse.builder()
                .id(id)
                .elasticId(elasticId)
                .thumbnailUrl(thumbnailUrl)
                .isLogoImage(isLogoImage)
                .techArticleUrl(techArticleUrl)
                .title(title)
                .contents(contents)
                .company(CompanyResponse.of(companyId, companyName, careerUrl, officialImageUrl))
                .regDate(regDate)
                .author(author)
                .viewTotalCount(viewCount)
                .recommendTotalCount(recommendCount)
                .commentTotalCount(commentCount)
                .popularScore(0L)
                .isBookmarked(isBookmarked)
                .score(score)
                .build();
    }

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
        mockMvc.perform(get("/devdevdev/api/v1/notifications")
                        .contentType(MediaType.TEXT_EVENT_STREAM_VALUE)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header(SecurityConstant.AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM_VALUE))
                .andExpect(content().string(containsString("data")))
                .andExpect(content().string(containsString("message")))
                .andExpect(content().string(containsString("createdAt")));

        sseEmitter.complete();
    }

    @Test
    @DisplayName("알림을 생성한다.")
    void publishNotifications() throws Exception {
        // given
        PublishTechArticleRequest request = new PublishTechArticleRequest(
                1L,
                List.of(new PublishTechArticle(1L),
                        new PublishTechArticle(2L))
        );

        doNothing().when(apiKeyService).validateApiKey(any(), any());

        // when // then
        mockMvc.perform(post("/devdevdev/api/v1/notifications/{channel}", NotificationType.SUBSCRIPTION)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("service-name", "test-service")
                        .header("api-key", "test-key")
                        .content(om.writeValueAsBytes(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultType").value(ResultType.SUCCESS.name()));
    }

    @Test
    @DisplayName("알림을 생성할 때 잘못된 채널을 입력하면 예외가 발생한다.")
    void publishNotificationsException() throws Exception {
        // given
        PublishTechArticleRequest request = new PublishTechArticleRequest(
                1L,
                List.of(new PublishTechArticle(1L),
                        new PublishTechArticle(2L))
        );

        doNothing().when(apiKeyService).validateApiKey(any(), any());

        // when // then
        mockMvc.perform(post("/devdevdev/api/v1/notifications/{channel}", "INVALID_CHANNEL")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("service-name", "test-service")
                        .header("api-key", "test-key")
                        .content(om.writeValueAsBytes(request)))
                .andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.resultType").value(FAIL.name()))
                .andExpect(jsonPath("$.message").isString())
                .andExpect(jsonPath("$.errorCode").isNumber());
    }

    @Test
    @DisplayName("알림을 생성할 때 API Key가 없으면 예외가 발생한다.")
    void publishNotificationsNotFoundException() throws Exception {
        // given
        PublishTechArticleRequest request = new PublishTechArticleRequest(
                1L,
                List.of(new PublishTechArticle(1L),
                        new PublishTechArticle(2L))
        );

        doThrow(new NotFoundException("not found")).when(apiKeyService).validateApiKey(any(), any());

        // when // then
        mockMvc.perform(post("/devdevdev/api/v1/notifications/{channel}", NotificationType.SUBSCRIPTION)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("service-name", "test-service")
                        .header("api-key", "test-key")
                        .content(om.writeValueAsBytes(request)))
                .andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.resultType").value(FAIL.name()))
                .andExpect(jsonPath("$.message").isString())
                .andExpect(jsonPath("$.errorCode").isNumber());
    }
}