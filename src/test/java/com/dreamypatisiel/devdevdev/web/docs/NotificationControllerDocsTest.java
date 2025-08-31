package com.dreamypatisiel.devdevdev.web.docs;

import com.dreamypatisiel.devdevdev.domain.entity.enums.NotificationType;
import com.dreamypatisiel.devdevdev.domain.entity.enums.Role;
import com.dreamypatisiel.devdevdev.domain.exception.NotificationExceptionMessage;
import com.dreamypatisiel.devdevdev.domain.service.ApiKeyService;
import com.dreamypatisiel.devdevdev.domain.service.notification.NotificationService;
import com.dreamypatisiel.devdevdev.exception.NotFoundException;
import com.dreamypatisiel.devdevdev.global.constant.SecurityConstant;
import com.dreamypatisiel.devdevdev.global.security.jwt.model.Token;
import com.dreamypatisiel.devdevdev.redis.sub.NotificationMessageDto;
import com.dreamypatisiel.devdevdev.web.dto.SliceCustom;
import com.dreamypatisiel.devdevdev.web.dto.request.publish.PublishTechArticle;
import com.dreamypatisiel.devdevdev.web.dto.request.publish.PublishTechArticleRequest;
import com.dreamypatisiel.devdevdev.web.dto.response.ResultType;
import com.dreamypatisiel.devdevdev.web.dto.response.notification.*;
import com.dreamypatisiel.devdevdev.web.dto.response.techArticle.CompanyResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.techArticle.TechArticleMainResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static com.dreamypatisiel.devdevdev.global.constant.SecurityConstant.AUTHORIZATION_HEADER;
import static com.dreamypatisiel.devdevdev.web.docs.custom.CustomPreprocessors.modifyResponseBody;
import static com.dreamypatisiel.devdevdev.web.docs.format.ApiDocsFormatGenerator.notificationType;
import static com.dreamypatisiel.devdevdev.web.docs.format.ApiDocsFormatGenerator.resultType;
import static io.lettuce.core.BitFieldArgs.OverflowType.FAIL;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.JsonFieldType.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class NotificationControllerDocsTest extends SupportControllerDocsTest {

    @MockBean
    NotificationService notificationService;
    @MockBean
    ApiKeyService apiKeyService;

    @Test
    @DisplayName("회원이 단건 알림을 읽으면 isRead가 true로 변경된 응답을 받는다.")
    void readNotification() throws Exception {
        // given
        Long notificationId = 1L;
        given(notificationService.readNotification(anyLong(), any()))
                .willReturn(new NotificationReadResponse(notificationId, true));

        // when
        ResultActions actions = mockMvc.perform(
                        patch(DEFAULT_PATH_V1 + "/notifications/{notificationId}/read", notificationId)
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
    void readNotificationNotOwnerException() throws Exception {
        // given
        Long notificationId = 2L;
        given(notificationService.readNotification(anyLong(), any()))
                .willThrow(new NotFoundException(NotificationExceptionMessage.NOT_FOUND_NOTIFICATION_MESSAGE));

        // when
        ResultActions actions = mockMvc.perform(
                        patch(DEFAULT_PATH_V1 + "/notifications/{notificationId}/read", notificationId)
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

    @Test
    @DisplayName("회원이 모든 알림을 읽는다.")
    void readAllNotifications() throws Exception {
        // given
        doNothing().when(notificationService).readAllNotifications(any());

        // when // then
        ResultActions actions = mockMvc.perform(patch(DEFAULT_PATH_V1 + "/notifications/read-all")
                        .header(SecurityConstant.AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().isOk());

        // docs
        actions.andDo(document("read-all-notifications",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                        headerWithName(AUTHORIZATION_HEADER).description("Bearer 엑세스 토큰")
                ),
                responseFields(
                        fieldWithPath("resultType").type(STRING).description("응답 결과")
                )
        ));
    }


    @Test
    @DisplayName("회원이 알림 팝업창을 조회한다.")
    void getNotificationPopup() throws Exception {
        // given
        PageRequest pageable = PageRequest.of(0, 1);
        List<NotificationPopupResponse> response = List.of(
                new NotificationPopupNewArticleResponse(1L, "기술블로그 타이틀", LocalDateTime.now(), false,
                        "기업명", 1L));
        given(notificationService.getNotificationPopup(any(), any()))
                .willReturn(new SliceCustom<>(response, pageable, false, 1L));

        // when
        ResultActions actions = mockMvc.perform(get(DEFAULT_PATH_V1 + "/notifications/popup")
                        .header(SecurityConstant.AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                        .queryParam("size", String.valueOf(pageable.getPageSize()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().isOk());

        // docs
        actions.andDo(document("get-notification-popup",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                        headerWithName(AUTHORIZATION_HEADER).description("Bearer 엑세스 토큰")
                ),
                queryParameters(
                        parameterWithName("size").description("페이지 크기")
                ),
                responseFields(
                        fieldWithPath("resultType").type(STRING).description("응답 결과"),
                        fieldWithPath("data").type(OBJECT).description("응답 데이터"),
                        fieldWithPath("data.content").type(ARRAY).description("알림 팝업 리스트"),
                        fieldWithPath("data.content[].id").type(NUMBER).description("알림 ID"),
                        fieldWithPath("data.content[].type").type(STRING).description("알림 타입").attributes(notificationType()),
                        fieldWithPath("data.content[].title").type(STRING).description("알림 제목"),
                        fieldWithPath("data.content[].isRead").type(BOOLEAN).description("회원의 읽음 여부"),
                        fieldWithPath("data.content[].createdAt").type(STRING).description("알림 생성 일시"),
                        fieldWithPath("data.content[].companyName").type(STRING).description("기업 이름"),
                        fieldWithPath("data.content[].techArticleId").type(NUMBER).description("기술블로그 id"),
                        fieldWithPath("data.pageable").type(OBJECT).description("페이지 정보"),
                        fieldWithPath("data.pageable.pageNumber").type(NUMBER).description("페이지 번호"),
                        fieldWithPath("data.pageable.pageSize").type(NUMBER).description("페이지 크기"),
                        fieldWithPath("data.pageable.sort").type(OBJECT).description("정렬 정보"),
                        fieldWithPath("data.pageable.sort.empty").type(BOOLEAN).description("정렬 여부 - 비어있는지"),
                        fieldWithPath("data.pageable.sort.sorted").type(BOOLEAN).description("정렬 여부 - 정렬됨"),
                        fieldWithPath("data.pageable.sort.unsorted").type(BOOLEAN).description("정렬 여부 - 정렬되지 않음"),
                        fieldWithPath("data.pageable.offset").type(NUMBER).description("데이터 시작 위치"),
                        fieldWithPath("data.pageable.paged").type(BOOLEAN).description("페이징 적용 여부"),
                        fieldWithPath("data.pageable.unpaged").type(BOOLEAN).description("페이징 미적용 여부"),
                        fieldWithPath("data.totalElements").type(NUMBER).description("회원이 읽지 않은 알림 총 개수"),
                        fieldWithPath("data.first").type(BOOLEAN).description("첫 페이지 여부"),
                        fieldWithPath("data.last").type(BOOLEAN).description("마지막 페이지 여부"),
                        fieldWithPath("data.size").type(NUMBER).description("페이지 크기"),
                        fieldWithPath("data.number").type(NUMBER).description("현재 페이지 번호"),
                        fieldWithPath("data.sort").type(OBJECT).description("정렬 정보"),
                        fieldWithPath("data.sort.empty").type(BOOLEAN).description("정렬 정보 - 비어있는지"),
                        fieldWithPath("data.sort.sorted").type(BOOLEAN).description("정렬 정보 - 정렬됨"),
                        fieldWithPath("data.sort.unsorted").type(BOOLEAN).description("정렬 정보 - 정렬되지 않음"),
                        fieldWithPath("data.numberOfElements").type(NUMBER).description("현재 페이지 요소 수"),
                        fieldWithPath("data.empty").type(BOOLEAN).description("비어있는 페이지인지 여부")
                  
                )
        ));
    }

    @Test
    @DisplayName("회원이 알림 개수를 조회하면 회원이 아직 읽지 않은 알림의 총 개수가 반환된다.")
    void getUnreadNotificationCount() throws Exception {
        // given
        Long unreadNotificationCount = 12L;
        given(notificationService.getUnreadNotificationCount(any()))
                .willReturn(unreadNotificationCount);

        // when // then
        ResultActions actions = mockMvc.perform(get(DEFAULT_PATH_V1 + "/notifications/unread-count")
                        .header(SecurityConstant.AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultType").value(ResultType.SUCCESS.name()))
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andExpect(jsonPath("$.data").isNumber());

        // docs
        actions.andDo(document("get-notification-unread-count",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                        headerWithName(AUTHORIZATION_HEADER).description("Bearer 엑세스 토큰")
                ),
                responseFields(
                        fieldWithPath("resultType").type(STRING).description("응답 결과"),
                        fieldWithPath("data").type(NUMBER).description("응답 데이터(읽지 않은 알림 개수)")
                )
        ));
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

        // 어드민 토큰 생성
        Token token = tokenService.generateTokenBy(email, socialType, Role.ROLE_ADMIN.name());
        accessToken = token.getAccessToken();

        // when // then
        ResultActions actions = mockMvc.perform(
                        post("/devdevdev/api/v1/notifications/{channel}", NotificationType.SUBSCRIPTION)
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding(StandardCharsets.UTF_8)
                                .header("service-name", "test-service")
                                .header("api-key", "test-key")
                                .content(om.writeValueAsBytes(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultType").value(ResultType.SUCCESS.name()));

        // docs
        actions.andDo(document("publish-notifications",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                        headerWithName("service-name").description("서비스 이름"),
                        headerWithName("api-key").description("api key")
                ),
                pathParameters(
                        parameterWithName("channel").description("알림 채널").attributes(notificationType())
                ),
                requestFields(
                        fieldWithPath("companyId").type(NUMBER).description("회사 아이디"),
                        fieldWithPath("techArticles").type(ARRAY).description("기술블로그 배열"),
                        fieldWithPath("techArticles.[].id").type(NUMBER).description("기술블로그 아이디")
                ),
                responseFields(
                        fieldWithPath("resultType").type(STRING).description("응답 결과").attributes(resultType())
                )
        ));
    }

    @Test
    @DisplayName("회원이 알림 페이지를 무한스크롤링으로 조회한다.")
    void getNotifications() throws Exception {
        // given
        PageRequest pageable = PageRequest.of(0, 1);
        TechArticleMainResponse techArticleMainResponse = createTechArticleMainResponse(
                1L, "http://thumbnailUrl.com", false,
                "http://techArticleUrl.com", "기술블로그 타이틀", "기술블로그 내용",
                1L, "기업명", "http://careerUrl.com", "http://officialImage.com", LocalDate.now(), "작성자",
                0L, 0L, 0L, false, null
        );

        List<NotificationResponse> response = List.of(
                new NotificationNewArticleResponse(1L, LocalDateTime.now(), false, techArticleMainResponse)
        );
        given(notificationService.getNotifications(any(), anyLong(), any()))
                .willReturn(new SliceCustom<>(response, pageable, true, 1L));

        // when
        ResultActions actions = mockMvc.perform(get(DEFAULT_PATH_V1 + "/notifications/page")
                        .header(SecurityConstant.AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                        .queryParam("size", String.valueOf(pageable.getPageSize()))
                        .queryParam("notificationId", String.valueOf(2L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().isOk());

        // docs
        actions.andDo(document("get-notifications",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                        headerWithName(AUTHORIZATION_HEADER).description("Bearer 엑세스 토큰")
                ),
                queryParameters(
                        parameterWithName("notificationId").description("마지막 알림 ID (커서)"),
                        parameterWithName("size").description("페이지 크기")
                ),
                responseFields(
                        fieldWithPath("resultType").type(STRING).description("응답 결과"),
                        fieldWithPath("data").type(OBJECT).description("응답 데이터"),
                        fieldWithPath("data.content").type(ARRAY).description("알림 목록"),
                        fieldWithPath("data.content[].notificationId").type(NUMBER).description("알림 ID"),
                        fieldWithPath("data.content[].type").type(STRING).description("알림 타입").attributes(notificationType()),
                        fieldWithPath("data.content[].createdAt").type(STRING).description("알림 생성 일시"),
                        fieldWithPath("data.content[].isRead").type(BOOLEAN).description("회원의 알림 읽음 여부"),
                        fieldWithPath("data.content[].techArticle").type(OBJECT).description("기술블로그 정보"),
                        fieldWithPath("data.content[].techArticle.id").type(NUMBER).description("기술블로그 ID"),
                        fieldWithPath("data.content[].techArticle.thumbnailUrl").type(STRING).description("썸네일 URL"),
                        fieldWithPath("data.content[].techArticle.isLogoImage").type(BOOLEAN).description("로고 이미지 여부"),
                        fieldWithPath("data.content[].techArticle.techArticleUrl").type(STRING).description("기술블로그 URL"),
                        fieldWithPath("data.content[].techArticle.title").type(STRING).description("기술블로그 제목"),
                        fieldWithPath("data.content[].techArticle.contents").type(STRING).description("기술블로그 내용"),
                        fieldWithPath("data.content[].techArticle.regDate").type(STRING).description("작성일"),
                        fieldWithPath("data.content[].techArticle.author").type(STRING).description("작성자"),
                        fieldWithPath("data.content[].techArticle.company").type(OBJECT).description("기업 정보"),
                        fieldWithPath("data.content[].techArticle.company.id").type(NUMBER).description("기업 ID"),
                        fieldWithPath("data.content[].techArticle.company.name").type(STRING).description("기업명"),
                        fieldWithPath("data.content[].techArticle.company.careerUrl").type(STRING).description("기업 채용공고 URL"),
                        fieldWithPath("data.content[].techArticle.company.officialImageUrl").type(STRING).description("기업 채용공고 URL"),
                        fieldWithPath("data.content[].techArticle.viewTotalCount").type(NUMBER).description("조회 수"),
                        fieldWithPath("data.content[].techArticle.recommendTotalCount").type(NUMBER).description("추천 수"),
                        fieldWithPath("data.content[].techArticle.commentTotalCount").type(NUMBER).description("댓글 수"),
                        fieldWithPath("data.content[].techArticle.popularScore").type(NUMBER).description("인기 점수"),
                        fieldWithPath("data.content[].techArticle.isBookmarked").type(BOOLEAN).description("북마크 여부"),
                        fieldWithPath("data.content[].techArticle.score").type(NULL).description("정확도 점수(null)"),
                        fieldWithPath("data.pageable").type(OBJECT).description("페이지 정보"),
                        fieldWithPath("data.pageable.pageNumber").type(NUMBER).description("페이지 번호"),
                        fieldWithPath("data.pageable.pageSize").type(NUMBER).description("페이지 크기"),
                        fieldWithPath("data.pageable.sort").type(OBJECT).description("정렬 정보"),
                        fieldWithPath("data.pageable.sort.empty").type(BOOLEAN).description("정렬 정보 - 비어있는지"),
                        fieldWithPath("data.pageable.sort.sorted").type(BOOLEAN).description("정렬 정보 - 정렬됨"),
                        fieldWithPath("data.pageable.sort.unsorted").type(BOOLEAN).description("정렬 정보 - 정렬되지 않음"),
                        fieldWithPath("data.pageable.offset").type(NUMBER).description("데이터 시작 위치"),
                        fieldWithPath("data.pageable.paged").type(BOOLEAN).description("페이징 적용 여부"),
                        fieldWithPath("data.pageable.unpaged").type(BOOLEAN).description("페이징 미적용 여부"),
                        fieldWithPath("data.totalElements").type(NUMBER).description("전체 요소 수"),
                        fieldWithPath("data.first").type(BOOLEAN).description("첫 페이지 여부"),
                        fieldWithPath("data.last").type(BOOLEAN).description("마지막 페이지 여부"),
                        fieldWithPath("data.size").type(NUMBER).description("요청한 페이지 크기"),
                        fieldWithPath("data.number").type(NUMBER).description("현재 페이지 번호"),
                        fieldWithPath("data.sort").type(OBJECT).description("정렬 정보"),
                        fieldWithPath("data.sort.empty").type(BOOLEAN).description("정렬 정보 - 비어있는지"),
                        fieldWithPath("data.sort.sorted").type(BOOLEAN).description("정렬 정보 - 정렬됨"),
                        fieldWithPath("data.sort.unsorted").type(BOOLEAN).description("정렬 정보 - 정렬되지 않음"),
                        fieldWithPath("data.numberOfElements").type(NUMBER).description("현재 페이지 요소 수"),
                        fieldWithPath("data.empty").type(BOOLEAN).description("비어있는 페이지인지 여부")
                )
        ));
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

        // 어드민 토큰 생성
        Token token = tokenService.generateTokenBy(email, socialType, Role.ROLE_ADMIN.name());
        accessToken = token.getAccessToken();

        // when // then
        ResultActions actions = mockMvc.perform(post("/devdevdev/api/v1/notifications/{channel}", "INVALID_CHANNEL")
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

        // docs
        actions.andDo(document("publish-notifications-exception",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                        headerWithName("service-name").description("서비스 이름"),
                        headerWithName("api-key").description("api key")
                ),
                requestFields(
                        fieldWithPath("companyId").type(NUMBER).description("회사 아이디"),
                        fieldWithPath("techArticles").type(ARRAY).description("기술블로그 배열"),
                        fieldWithPath("techArticles.[].id").type(NUMBER).description("기술블로그 아이디")
                ),
                exceptionResponseFields()
        ));
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
        mockMvc.perform(
                        MockMvcRequestBuilders.post("/devdevdev/api/v1/notifications/{channel}", NotificationType.SUBSCRIPTION)
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
  
    private TechArticleMainResponse createTechArticleMainResponse(Long id, String thumbnailUrl, Boolean isLogoImage,
                                                                String techArticleUrl, String title, String contents,
                                                                Long companyId, String companyName, String careerUrl, String officialImageUrl,
                                                                LocalDate regDate, String author, long recommendCount,
                                                                long commentCount, long viewCount, Boolean isBookmarked, Double score) {
      return TechArticleMainResponse.builder()
              .id(id)
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
}