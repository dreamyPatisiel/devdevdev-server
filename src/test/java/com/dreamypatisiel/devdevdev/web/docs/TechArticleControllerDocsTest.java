package com.dreamypatisiel.devdevdev.web.docs;

import static com.dreamypatisiel.devdevdev.domain.exception.MemberExceptionMessage.INVALID_MEMBER_NOT_FOUND_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.TechArticleExceptionMessage.KEYWORD_WITH_SPECIAL_SYMBOLS_EXCEPTION_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.TechArticleExceptionMessage.NOT_FOUND_CURSOR_SCORE_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.TechArticleExceptionMessage.NOT_FOUND_TECH_ARTICLE_MESSAGE;
import static com.dreamypatisiel.devdevdev.global.constant.SecurityConstant.AUTHORIZATION_HEADER;
import static com.dreamypatisiel.devdevdev.web.docs.format.ApiDocsFormatGenerator.authenticationType;
import static com.dreamypatisiel.devdevdev.web.docs.format.ApiDocsFormatGenerator.techArticleSortType;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechArticleSort;
import com.dreamypatisiel.devdevdev.domain.service.techArticle.techArticle.GuestTechArticleService;
import com.dreamypatisiel.devdevdev.domain.service.techArticle.techArticle.MemberTechArticleService;
import com.dreamypatisiel.devdevdev.exception.MemberException;
import com.dreamypatisiel.devdevdev.exception.NotFoundException;
import com.dreamypatisiel.devdevdev.exception.TechArticleException;
import com.dreamypatisiel.devdevdev.global.constant.SecurityConstant;
import com.dreamypatisiel.devdevdev.web.dto.SliceCustom;
import com.dreamypatisiel.devdevdev.web.dto.response.ResultType;
import com.dreamypatisiel.devdevdev.web.dto.response.techArticle.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

public class TechArticleControllerDocsTest extends SupportControllerDocsTest {

    @MockBean
    GuestTechArticleService guestTechArticleService;
    
    @MockBean
    MemberTechArticleService memberTechArticleService;


    @Test
    @DisplayName("회원이 기술블로그 메인을 조회한다.")
    void getTechArticlesByMember() throws Exception {
        // given
        Pageable pageable = PageRequest.of(0, 1);
        String techArticleId = "1";
        String keyword = "타이틀";
        String companyId = "1";
        
        TechArticleMainResponse response = createTechArticleMainResponse(
                1L, "http://thumbnail.com", false, "http://article.com", "타이틀 1", "내용 1",
                1L, "회사명", "http://career.com", "http://official.com", LocalDate.now(), "작성자",
                10L, 5L, 100L, true, 10.0
        );
        
        SliceCustom<TechArticleMainResponse> mockSlice = new SliceCustom<>(
                List.of(response), pageable, false, 1L
        );
        
        given(memberTechArticleService.getTechArticles(any(), any(), any(), any(), any(), any(), any()))
                .willReturn(mockSlice);

        // when // then
        ResultActions actions = mockMvc.perform(get("/devdevdev/api/v1/articles")
                        .queryParam("size", String.valueOf(pageable.getPageSize()))
                        .queryParam("techArticleSort", TechArticleSort.HIGHEST_SCORE.name())
                        .queryParam("keyword", keyword)
                        .queryParam("techArticleId", techArticleId)
                        .queryParam("companyId", companyId)
                        .queryParam("score", "10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header(AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken))
                .andDo(print())
                .andExpect(status().isOk());

        // docs
        actions.andDo(document("tech-article-main",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                        headerWithName(AUTHORIZATION_HEADER).optional().description("Bearer 엑세스 토큰")
                ),
                queryParameters(
                        parameterWithName("size").optional().description("조회되는 데이터 수"),
                        parameterWithName("techArticleSort").optional().description("정렬 조건")
                                .attributes(techArticleSortType()),
                        parameterWithName("keyword").optional().description("검색어"),
                        parameterWithName("companyId").optional().description("회사 아이디"),
                        parameterWithName("techArticleId").optional().description("마지막 데이터의 기술블로그 아이디"),
                        parameterWithName("score").optional().description("마지막 데이터의 정확도 점수(정확도순 검색일 때에만 필수)")
                ),
                responseFields(
                        fieldWithPath("resultType").type(JsonFieldType.STRING).description("응답 결과"),
                        fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),

                        fieldWithPath("data.content").type(JsonFieldType.ARRAY).description("기술블로그 메인 배열"),
                        fieldWithPath("data.content.[].id").type(JsonFieldType.NUMBER).description("기술블로그 아이디"),
                        fieldWithPath("data.content.[].techArticleUrl").type(JsonFieldType.STRING)
                                .description("기술블로그 Url"),
                        fieldWithPath("data.content.[].thumbnailUrl").type(JsonFieldType.STRING)
                                .description("기술블로그 썸네일 이미지"),
                        fieldWithPath("data.content.[].isLogoImage").type(JsonFieldType.BOOLEAN)
                                .description("썸네일 이미지의 회사 로고 여부"),
                        fieldWithPath("data.content.[].title").type(JsonFieldType.STRING).description("기술블로그 제목"),
                        fieldWithPath("data.content.[].contents").type(JsonFieldType.STRING).description("기술블로그 내용"),
                        fieldWithPath("data.content.[].company").type(JsonFieldType.OBJECT).description("기술블로그 회사"),
                        fieldWithPath("data.content.[].company.id").type(JsonFieldType.NUMBER)
                                .description("기술블로그 회사 id"),
                        fieldWithPath("data.content.[].company.name").type(JsonFieldType.STRING)
                                .description("기술블로그 회사 이름"),
                        fieldWithPath("data.content.[].company.careerUrl").type(JsonFieldType.STRING)
                                .description("기술블로그 회사 채용페이지"),
                        fieldWithPath("data.content.[].company.officialImageUrl").type(JsonFieldType.STRING)
                                .description("기술블로그 회사 로고 이미지"),
                        fieldWithPath("data.content.[].regDate").type(JsonFieldType.STRING).description("기술블로그 작성일"),
                        fieldWithPath("data.content.[].author").type(JsonFieldType.STRING).description("기술블로그 작성자"),
                        fieldWithPath("data.content.[].viewTotalCount").type(JsonFieldType.NUMBER)
                                .description("기술블로그 조회수"),
                        fieldWithPath("data.content.[].recommendTotalCount").type(JsonFieldType.NUMBER)
                                .description("기술블로그 추천수"),
                        fieldWithPath("data.content.[].commentTotalCount").type(JsonFieldType.NUMBER)
                                .description("기술블로그 댓글수"),
                        fieldWithPath("data.content.[].popularScore").type(JsonFieldType.NUMBER)
                                .description("기술블로그 인기점수"),
                        fieldWithPath("data.content.[].isBookmarked").attributes(authenticationType())
                                .type(JsonFieldType.BOOLEAN).description("회원의 북마크 여부(익명 사용자는 필드가 없다)"),
                        fieldWithPath("data.content.[].score").type(JsonFieldType.NUMBER).description("정확도 점수"),

                        fieldWithPath("data.pageable").type(JsonFieldType.OBJECT).description("페이지네이션 정보"),
                        fieldWithPath("data.pageable.pageNumber").type(JsonFieldType.NUMBER).description("페이지 번호"),
                        fieldWithPath("data.pageable.pageSize").type(JsonFieldType.NUMBER).description("페이지 사이즈"),

                        fieldWithPath("data.pageable.sort").type(JsonFieldType.OBJECT).description("정렬 정보"),
                        fieldWithPath("data.pageable.sort.empty").type(JsonFieldType.BOOLEAN)
                                .description("정렬 정보가 비어있는지 여부"),
                        fieldWithPath("data.pageable.sort.sorted").type(JsonFieldType.BOOLEAN).description("정렬 여부"),
                        fieldWithPath("data.pageable.sort.unsorted").type(JsonFieldType.BOOLEAN).description("비정렬 여부"),

                        fieldWithPath("data.pageable.offset").type(JsonFieldType.NUMBER)
                                .description("페이지 오프셋 (페이지 크기 * 페이지 번호)"),
                        fieldWithPath("data.pageable.paged").type(JsonFieldType.BOOLEAN).description("페이지 정보 포함 여부"),
                        fieldWithPath("data.pageable.unpaged").type(JsonFieldType.BOOLEAN).description("페이지 정보 비포함 여부"),

                        fieldWithPath("data.first").type(JsonFieldType.BOOLEAN).description("현재 페이지가 첫 페이지 여부"),
                        fieldWithPath("data.last").type(JsonFieldType.BOOLEAN).description("현재 페이지가 마지막 페이지 여부"),
                        fieldWithPath("data.size").type(JsonFieldType.NUMBER).description("페이지 크기"),
                        fieldWithPath("data.number").type(JsonFieldType.NUMBER).description("현재 페이지"),

                        fieldWithPath("data.sort").type(JsonFieldType.OBJECT).description("정렬 정보"),
                        fieldWithPath("data.sort.empty").type(JsonFieldType.BOOLEAN).description("정렬 정보가 비어있는지 여부"),
                        fieldWithPath("data.sort.sorted").type(JsonFieldType.BOOLEAN).description("정렬 상태 여부"),
                        fieldWithPath("data.sort.unsorted").type(JsonFieldType.BOOLEAN).description("비정렬 상태 여부"),
                        fieldWithPath("data.numberOfElements").type(JsonFieldType.NUMBER).description("현재 페이지 데이터 수"),
                        fieldWithPath("data.totalElements").type(JsonFieldType.NUMBER).description("전체 페이지 데이터 수"),
                        fieldWithPath("data.empty").type(JsonFieldType.BOOLEAN).description("현재 빈 페이지 여부")
                )
        ));
    }

    @Test
    @DisplayName("커서 방식으로 다음 페이지의 기술블로그를 검색어로 검색할 때," +
            "정확도 내림차순으로 조회하기 위한 점수가 없다면 예외가 발생한다.")
    void getTechArticlesWithKeywordWithCursorOrderByHIGHEST_SCOREWithoutScoreException() throws Exception {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        String techArticleId = "1";
        String keyword = "타이틀";
        
        given(guestTechArticleService.getTechArticles(any(), any(), any(), any(), any(), any(), any()))
                .willThrow(new TechArticleException(NOT_FOUND_CURSOR_SCORE_MESSAGE));

        // when // then
        ResultActions actions = mockMvc.perform(get("/devdevdev/api/v1/articles")
                        .queryParam("size", String.valueOf(pageable.getPageSize()))
                        .queryParam("techArticleSort", TechArticleSort.HIGHEST_SCORE.name())
                        .queryParam("techArticleId", techArticleId)
                        .queryParam("keyword", keyword)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().isBadRequest());

        // Docs
        actions.andDo(document("not-found-score-exception",
                preprocessResponse(prettyPrint()),
                responseFields(
                        fieldWithPath("resultType").type(JsonFieldType.STRING).description("응답 결과"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("에러 메시지"),
                        fieldWithPath("errorCode").type(JsonFieldType.NUMBER).description("에러 코드")
                )
        ));
    }

    @Test
    @DisplayName("기술블로그 메인을 조회할 때 키워드에 특수문자가 있다면 에러가 발생한다.")
    void getTechArticlesSpecialSymbolException() throws Exception {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        String keyword = "!";
        
        given(guestTechArticleService.getTechArticles(any(), any(), any(), any(), any(), any(), any()))
                .willThrow(new TechArticleException(KEYWORD_WITH_SPECIAL_SYMBOLS_EXCEPTION_MESSAGE));

        // when // then
        ResultActions actions = mockMvc.perform(MockMvcRequestBuilders.get("/devdevdev/api/v1/articles")
                        .queryParam("size", String.valueOf(pageable.getPageSize()))
                        .queryParam("keyword", keyword)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultType").value(ResultType.FAIL.name()))
                .andExpect(jsonPath("$.message").value(KEYWORD_WITH_SPECIAL_SYMBOLS_EXCEPTION_MESSAGE))
                .andExpect(jsonPath("$.errorCode").value(HttpStatus.BAD_REQUEST.value()));

        // Docs
        actions.andDo(document("keyword-with-special-symbols-exception",
                preprocessResponse(prettyPrint()),
                responseFields(
                        fieldWithPath("resultType").type(JsonFieldType.STRING).description("응답 결과"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("에러 메시지"),
                        fieldWithPath("errorCode").type(JsonFieldType.NUMBER).description("에러 코드")
                )
        ));
    }

    @Test
    @DisplayName("회원이 기술블로그 상세를 조회한다.")
    void getTechArticleByMember() throws Exception {
        // given
        Long id = 1L;
        
        TechArticleDetailResponse response = createTechArticleDetailResponse(
                "http://thumbnail.com", "http://article.com", "기술블로그 제목", "기술블로그 내용",
                1L, "회사명", "http://career.com", "http://official.com", LocalDate.now(), "작성자",
                100L, 10L, 5L, 50L, false, true
        );

        given(memberTechArticleService.getTechArticle(eq(id), any(), any()))
                .willReturn(response);

        // when // then
        ResultActions actions = mockMvc.perform(get("/devdevdev/api/v1/articles/{techArticleId}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header(AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken))
                .andDo(print())
                .andExpect(status().isOk());

        // docs
        actions.andDo(document("tech-article-detail",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                        headerWithName(AUTHORIZATION_HEADER).optional().description("Bearer 엑세스 토큰"),
                        headerWithName("Anonymous-Member-Id").optional().description("익명 회원 아이디")
                ),
                pathParameters(
                        parameterWithName("techArticleId").description("기술블로그 아이디")
                ),
                responseFields(
                        fieldWithPath("resultType").type(JsonFieldType.STRING).description("응답 결과"),
                        fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),
                        fieldWithPath("data.techArticleUrl").type(JsonFieldType.STRING).description("기술블로그 Url"),
                        fieldWithPath("data.thumbnailUrl").type(JsonFieldType.STRING).description("기술블로그 썸네일 이미지"),
                        fieldWithPath("data.title").type(JsonFieldType.STRING).description("기술블로그 제목"),
                        fieldWithPath("data.contents").type(JsonFieldType.STRING).description("기술블로그 내용"),
                        fieldWithPath("data.company").type(JsonFieldType.OBJECT).description("기술블로그 회사"),
                        fieldWithPath("data.company.id").type(JsonFieldType.NUMBER).description("기술블로그 회사 id"),
                        fieldWithPath("data.company.name").type(JsonFieldType.STRING).description("기술블로그 회사 이름"),
                        fieldWithPath("data.company.careerUrl").type(JsonFieldType.STRING)
                                .description("기술블로그 회사 채용페이지"),
                        fieldWithPath("data.company.officialImageUrl").type(JsonFieldType.STRING)
                                .description("기술블로그 회사 로고 이미지"),
                        fieldWithPath("data.regDate").type(JsonFieldType.STRING).description("기술블로그 작성일"),
                        fieldWithPath("data.author").type(JsonFieldType.STRING).description("기술블로그 작성자"),
                        fieldWithPath("data.contents").type(JsonFieldType.STRING).description("기술블로그 내용"),
                        fieldWithPath("data.viewTotalCount").type(JsonFieldType.NUMBER).description("기술블로그 조회수"),
                        fieldWithPath("data.recommendTotalCount").type(JsonFieldType.NUMBER).description("기술블로그 추천수"),
                        fieldWithPath("data.commentTotalCount").type(JsonFieldType.NUMBER).description("기술블로그 댓글수"),
                        fieldWithPath("data.popularScore").type(JsonFieldType.NUMBER).description("기술블로그 인기점수"),
                        fieldWithPath("data.isBookmarked").attributes(authenticationType()).type(JsonFieldType.BOOLEAN)
                                .description("회원의 북마크 여부(익명 사용자는 필드가 없다)"),
                        fieldWithPath("data.isRecommended").attributes(authenticationType()).type(JsonFieldType.BOOLEAN)
                                .description("회원의 추천 여부")
                )
        ));
    }

    @Test
    @DisplayName("회원이 기술블로그 상세를 조회할 때 회원이 없으면 예외가 발생한다.")
    void getTechArticleNotFoundMemberException() throws Exception {
        // given
        Long id = 1L;
        
        given(memberTechArticleService.getTechArticle(eq(id), any(), any()))
                .willThrow(new MemberException(INVALID_MEMBER_NOT_FOUND_MESSAGE));

        // when // then
        ResultActions actions = mockMvc.perform(get("/devdevdev/api/v1/articles/{techArticleId}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header(SecurityConstant.AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultType").value(ResultType.FAIL.name()))
                .andExpect(jsonPath("$.message").value(INVALID_MEMBER_NOT_FOUND_MESSAGE))
                .andExpect(jsonPath("$.errorCode").value(HttpStatus.NOT_FOUND.value()));

        // Docs
        actions.andDo(document("not-found-member-exception",
                preprocessResponse(prettyPrint()),
                responseFields(
                        fieldWithPath("resultType").type(JsonFieldType.STRING).description("응답 결과"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("에러 메시지"),
                        fieldWithPath("errorCode").type(JsonFieldType.NUMBER).description("에러 코드")
                )
        ));
    }

    @Test
    @DisplayName("기술블로그 상세를 조회할 때 기술블로그가 존재하지 않으면 예외가 발생한다.")
    void getTechArticleNotFoundTechArticleException() throws Exception {
        // given
        Long id = 999L;
        
        given(guestTechArticleService.getTechArticle(eq(id), any(), any()))
                .willThrow(new NotFoundException(NOT_FOUND_TECH_ARTICLE_MESSAGE));

        // when // then
        ResultActions actions = mockMvc.perform(get("/devdevdev/api/v1/articles/{techArticleId}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultType").value(ResultType.FAIL.name()))
                .andExpect(jsonPath("$.message").value(NOT_FOUND_TECH_ARTICLE_MESSAGE))
                .andExpect(jsonPath("$.errorCode").value(HttpStatus.NOT_FOUND.value()));

        // Docs
        actions.andDo(document("not-found-tech-article-exception",
                preprocessResponse(prettyPrint()),
                responseFields(
                        fieldWithPath("resultType").type(JsonFieldType.STRING).description("응답 결과"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("에러 메시지"),
                        fieldWithPath("errorCode").type(JsonFieldType.NUMBER).description("에러 코드")
                )
        ));
    }

    @Test
    @DisplayName("회원이 기술블로그 북마크를 요청한다.")
    void updateBookmark() throws Exception {
        // given
        Long id = 1L;
        
        BookmarkResponse response = new BookmarkResponse(id, true);
        given(memberTechArticleService.updateBookmark(eq(id), any()))
                .willReturn(response);

        // when // then
        ResultActions actions = mockMvc.perform(post("/devdevdev/api/v1/articles/{techArticleId}/bookmark", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header(SecurityConstant.AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken))
                .andDo(print());

        // Docs
        actions.andDo(document("tech-article-bookmark",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                        headerWithName(AUTHORIZATION_HEADER).optional().description("Bearer 엑세스 토큰")
                ),
                pathParameters(
                        parameterWithName("techArticleId").description("기술블로그 아이디")
                ),
                responseFields(
                        fieldWithPath("resultType").type(JsonFieldType.STRING).description("응답 결과"),
                        fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),

                        fieldWithPath("data.techArticleId").type(JsonFieldType.NUMBER).description("기술블로그 아이디"),
                        fieldWithPath("data.status").type(JsonFieldType.BOOLEAN).description("북마크 상태")
                )
        ));
    }

    @Test
    @DisplayName("회원이 기술블로그 추천을 요청한다.")
    void updateRecommend() throws Exception {
        // given
        Long id = 1L;

        TechArticleRecommendResponse response = new TechArticleRecommendResponse(id, true, 11L);
        given(memberTechArticleService.updateRecommend(eq(id), any(), any()))
                .willReturn(response);

        // when // then
        ResultActions actions = mockMvc.perform(post("/devdevdev/api/v1/articles/{techArticleId}/recommend", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header(SecurityConstant.AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken))
                .andDo(print());

        // Docs
        actions.andDo(document("tech-article-recommend",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                        headerWithName(AUTHORIZATION_HEADER).optional().description("Bearer 엑세스 토큰"),
                        headerWithName("Anonymous-Member-Id").optional().description("익명 회원 아이디")
                ),
                pathParameters(
                        parameterWithName("techArticleId").description("기술블로그 아이디")
                ),
                responseFields(
                        fieldWithPath("resultType").type(JsonFieldType.STRING).description("응답 결과"),
                        fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),

                        fieldWithPath("data.techArticleId").type(JsonFieldType.NUMBER).description("기술블로그 아이디"),
                        fieldWithPath("data.status").type(JsonFieldType.BOOLEAN).description("추천 상태"),
                        fieldWithPath("data.recommendTotalCount").type(JsonFieldType.NUMBER).description("기술블로그 총 추천수")
                ))
        );
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
    
    private TechArticleDetailResponse createTechArticleDetailResponse(String thumbnailUrl, String techArticleUrl,
                                                                      String title, String contents, Long companyId,
                                                                      String companyName, String careerUrl, String officialImageUrl,
                                                                      LocalDate regDate, String author, long viewCount,
                                                                      long recommendCount, long commentCount, long popularScore,
                                                                      boolean isRecommended, boolean isBookmarked) {
        return TechArticleDetailResponse.builder()
                .thumbnailUrl(thumbnailUrl)
                .techArticleUrl(techArticleUrl)
                .title(title)
                .contents(contents)
                .company(CompanyResponse.of(companyId, companyName, careerUrl, officialImageUrl))
                .regDate(regDate)
                .author(author)
                .viewTotalCount(viewCount)
                .recommendTotalCount(recommendCount)
                .commentTotalCount(commentCount)
                .popularScore(popularScore)
                .isRecommended(isRecommended)
                .isBookmarked(isBookmarked)
                .build();
    }
}
