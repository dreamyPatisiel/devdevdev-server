package com.dreamypatisiel.devdevdev.web.docs;

import com.dreamypatisiel.devdevdev.domain.exception.NotificationExceptionMessage;
import com.dreamypatisiel.devdevdev.domain.service.notification.NotificationService;
import com.dreamypatisiel.devdevdev.exception.NotFoundException;
import com.dreamypatisiel.devdevdev.global.constant.SecurityConstant;
import com.dreamypatisiel.devdevdev.web.dto.SliceCustom;
import com.dreamypatisiel.devdevdev.web.dto.response.notification.*;
import com.dreamypatisiel.devdevdev.web.dto.response.techArticle.CompanyResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.techArticle.TechArticleMainResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import static com.dreamypatisiel.devdevdev.global.constant.SecurityConstant.AUTHORIZATION_HEADER;
import static com.dreamypatisiel.devdevdev.web.docs.format.ApiDocsFormatGenerator.notificationType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.JsonFieldType.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class NotificationControllerDocsTest extends SupportControllerDocsTest {

    @MockBean
    NotificationService notificationService;

    @Test
    @DisplayName("회원이 단건 알림을 읽으면 isRead가 true로 변경된 응답을 받는다.")
    void readNotification() throws Exception {
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
    void readNotificationNotOwnerException() throws Exception {
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
                new NotificationPopupNewArticleResponse(1L, "기술블로그 타이틀", LocalDate.now(), false,
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
                        fieldWithPath("data.content[].createdAt").type(STRING).description("알림 생성일"),
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
    @DisplayName("회원이 알림 페이지를 무한스크롤링으로 조회한다.")
    void getNotifications() throws Exception {
        // given
        PageRequest pageable = PageRequest.of(0, 1);
        TechArticleMainResponse techArticleMainResponse = createTechArticleMainResponse(
                1L, "elasticId", "http://thumbnailUrl.com", false,
                "http://techArticleUrl.com", "기술블로그 타이틀", "기술블로그 내용",
                1L, "기업명", "http://careerUrl.com", "http://officialImage.com", LocalDate.now(), "작성자",
                0L, 0L, 0L, false, null
        );

        List<NotificationResponse> response = List.of(
                new NotificationNewArticleResponse(1L, LocalDate.now(), false, techArticleMainResponse)
        );
        given(notificationService.getNotifications(any(), anyLong(), any()))
                .willReturn(new SliceCustom<>(response, pageable, true, 1L));

        // when
        ResultActions actions = mockMvc.perform(get(DEFAULT_PATH_V1 + "/notifications")
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
                        fieldWithPath("data.content[].createdAt").type(STRING).description("알림 생성일"),
                        fieldWithPath("data.content[].isRead").type(BOOLEAN).description("회원의 알림 읽음 여부"),
                        fieldWithPath("data.content[].techArticle").type(OBJECT).description("기술블로그 정보"),
                        fieldWithPath("data.content[].techArticle.id").type(NUMBER).description("기술블로그 ID"),
                        fieldWithPath("data.content[].techArticle.elasticId").type(STRING).description("엘라스틱서치 ID"),
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

}