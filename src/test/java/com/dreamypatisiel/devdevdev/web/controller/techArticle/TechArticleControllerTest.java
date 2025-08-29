package com.dreamypatisiel.devdevdev.web.controller.techArticle;

import static com.dreamypatisiel.devdevdev.domain.exception.MemberExceptionMessage.INVALID_MEMBER_NOT_FOUND_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.TechArticleExceptionMessage.KEYWORD_WITH_SPECIAL_SYMBOLS_EXCEPTION_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.TechArticleExceptionMessage.NOT_FOUND_CURSOR_SCORE_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.TechArticleExceptionMessage.NOT_FOUND_TECH_ARTICLE_MESSAGE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechArticleSort;
import com.dreamypatisiel.devdevdev.domain.service.techArticle.techArticle.GuestTechArticleService;
import com.dreamypatisiel.devdevdev.domain.service.techArticle.techArticle.MemberTechArticleService;
import com.dreamypatisiel.devdevdev.exception.MemberException;
import com.dreamypatisiel.devdevdev.exception.NotFoundException;
import com.dreamypatisiel.devdevdev.exception.TechArticleException;
import com.dreamypatisiel.devdevdev.web.dto.SliceCustom;
import com.dreamypatisiel.devdevdev.web.dto.response.techArticle.*;
import com.dreamypatisiel.devdevdev.global.constant.SecurityConstant;
import com.dreamypatisiel.devdevdev.web.controller.SupportControllerTest;
import com.dreamypatisiel.devdevdev.web.dto.response.ResultType;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

class TechArticleControllerTest extends SupportControllerTest {

    @MockBean
    GuestTechArticleService guestTechArticleService;
    
    @MockBean
    MemberTechArticleService memberTechArticleService;

    @Test
    @DisplayName("익명 사용자가 기술블로그 메인을 조회한다.")
    void getTechArticlesByAnonymous() throws Exception {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        String techArticleId = "1";
        String keyword = "타이틀";
        String companyId = "1";
        
        TechArticleMainResponse response = createTechArticleMainResponse(
                1L, "http://thumbnail.com", false, "http://article.com", "타이틀 1", "내용 1",
                1L, "회사명", "http://career.com", "http://official.com", LocalDate.now(), "작성자",
                10L, 5L, 100L, null, 10.0f
        );
        
        SliceCustom<TechArticleMainResponse> mockSlice = new SliceCustom<>(
                List.of(response), pageable, false, 1L
        );
        
        given(guestTechArticleService.getTechArticles(any(), any(), any(), any(), any(), any(), any()))
                .willReturn(mockSlice);

        // when // then
        mockMvc.perform(get("/devdevdev/api/v1/articles")
                        .queryParam("size", String.valueOf(pageable.getPageSize()))
                        .queryParam("techArticleSort", TechArticleSort.LATEST.name())
                        .queryParam("keyword", keyword)
                        .queryParam("techArticleId", techArticleId)
                        .queryParam("companyId", companyId)
                        .queryParam("score", "10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultType").value(ResultType.SUCCESS.name()))
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.[0].id").isNumber())
                .andExpect(jsonPath("$.data.content.[0].thumbnailUrl").isString())
                .andExpect(jsonPath("$.data.content.[0].techArticleUrl").isString())
                .andExpect(jsonPath("$.data.content.[0].title").isString())
                .andExpect(jsonPath("$.data.content.[0].contents").isString())
                .andExpect(jsonPath("$.data.content.[0].company").isMap())
                .andExpect(jsonPath("$.data.content.[0].company.id").isNumber())
                .andExpect(jsonPath("$.data.content.[0].company.name").isString())
                .andExpect(jsonPath("$.data.content.[0].company.careerUrl").isString())
                .andExpect(jsonPath("$.data.content.[0].regDate").isString())
                .andExpect(jsonPath("$.data.content.[0].author").isString())
                .andExpect(jsonPath("$.data.content.[0].viewTotalCount").isNumber())
                .andExpect(jsonPath("$.data.content.[0].recommendTotalCount").isNumber())
                .andExpect(jsonPath("$.data.content.[0].commentTotalCount").isNumber())
                .andExpect(jsonPath("$.data.content.[0].popularScore").isNumber())
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
                .andExpect(jsonPath("$.data.totalElements").isNumber())
                .andExpect(jsonPath("$.data.empty").isBoolean());
    }

    @Test
    @DisplayName("회원이 기술블로그 메인을 조회한다.")
    void getTechArticlesByMember() throws Exception {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        String techArticleId = "1";
        String keyword = "타이틀";
        String companyId = "1";
        
        TechArticleMainResponse response = createTechArticleMainResponse(
                1L, "http://thumbnail.com", false, "http://article.com", "타이틀 1", "내용 1",
                1L, "회사명", "http://career.com", "http://official.com", LocalDate.now(), "작성자",
                10L, 5L, 100L, true, 10.0f
        );
        
        SliceCustom<TechArticleMainResponse> mockSlice = new SliceCustom<>(
                List.of(response), pageable, false, 1L
        );
        
        given(memberTechArticleService.getTechArticles(any(), any(), any(), any(), any(), any(), any()))
                .willReturn(mockSlice);

        // when // then
        mockMvc.perform(get("/devdevdev/api/v1/articles")
                        .queryParam("size", String.valueOf(pageable.getPageSize()))
                        .queryParam("techArticleSort", TechArticleSort.LATEST.name())
                        .queryParam("keyword", keyword)
                        .queryParam("techArticleId", techArticleId)
                        .queryParam("companyId", companyId)
                        .queryParam("score", "10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header(SecurityConstant.AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultType").value(ResultType.SUCCESS.name()))
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.[0].id").isNumber())
                .andExpect(jsonPath("$.data.content.[0].thumbnailUrl").isString())
                .andExpect(jsonPath("$.data.content.[0].techArticleUrl").isString())
                .andExpect(jsonPath("$.data.content.[0].title").isString())
                .andExpect(jsonPath("$.data.content.[0].contents").isString())
                .andExpect(jsonPath("$.data.content.[0].company").isMap())
                .andExpect(jsonPath("$.data.content.[0].company.id").isNumber())
                .andExpect(jsonPath("$.data.content.[0].company.name").isString())
                .andExpect(jsonPath("$.data.content.[0].company.careerUrl").isString())
                .andExpect(jsonPath("$.data.content.[0].regDate").isString())
                .andExpect(jsonPath("$.data.content.[0].author").isString())
                .andExpect(jsonPath("$.data.content.[0].viewTotalCount").isNumber())
                .andExpect(jsonPath("$.data.content.[0].recommendTotalCount").isNumber())
                .andExpect(jsonPath("$.data.content.[0].commentTotalCount").isNumber())
                .andExpect(jsonPath("$.data.content.[0].popularScore").isNumber())
                .andExpect(jsonPath("$.data.content.[0].isBookmarked").isBoolean())
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
                .andExpect(jsonPath("$.data.totalElements").isNumber())
                .andExpect(jsonPath("$.data.empty").isBoolean());
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
        mockMvc.perform(get("/devdevdev/api/v1/articles")
                        .queryParam("size", String.valueOf(pageable.getPageSize()))
                        .queryParam("techArticleSort", TechArticleSort.HIGHEST_SCORE.name())
                        .queryParam("techArticleId", techArticleId)
                        .queryParam("keyword", keyword)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultType").value(ResultType.FAIL.name()))
                .andExpect(jsonPath("$.message").value(NOT_FOUND_CURSOR_SCORE_MESSAGE))
                .andExpect(jsonPath("$.errorCode").value(HttpStatus.BAD_REQUEST.value()));
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
        mockMvc.perform(get("/devdevdev/api/v1/articles")
                        .queryParam("size", String.valueOf(pageable.getPageSize()))
                        .queryParam("techArticleSort", TechArticleSort.LATEST.name())
                        .queryParam("keyword", keyword)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultType").value(ResultType.FAIL.name()))
                .andExpect(jsonPath("$.message").value(KEYWORD_WITH_SPECIAL_SYMBOLS_EXCEPTION_MESSAGE))
                .andExpect(jsonPath("$.errorCode").value(HttpStatus.BAD_REQUEST.value()));
    }

    @Test
    @DisplayName("익명 사용자가 기술블로그 상세를 조회한다.")
    void getTechArticleByAnonymous() throws Exception {
        // given
        Long id = 1L;
        String anonymousMemberId = "GA1.1.276672604.1715872960";
        
        TechArticleDetailResponse response = createTechArticleDetailResponse(
                "http://thumbnail.com", "http://article.com", "기술블로그 제목", "기술블로그 내용",
                1L, "회사명", "http://career.com", "http://official.com", LocalDate.now(), "작성자",
                100L, 10L, 5L, 50L, false, false
        );
        
        given(guestTechArticleService.getTechArticle(eq(id), any(), any()))
                .willReturn(response);

        // when // then
        mockMvc.perform(get("/devdevdev/api/v1/articles/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Anonymous-Member-Id", anonymousMemberId)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultType").value(ResultType.SUCCESS.name()))
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andExpect(jsonPath("$.data").isMap())
                .andExpect(jsonPath("$.data.thumbnailUrl").isString())
                .andExpect(jsonPath("$.data.techArticleUrl").isString())
                .andExpect(jsonPath("$.data.title").isString())
                .andExpect(jsonPath("$.data.contents").isString())
                .andExpect(jsonPath("$.data.company").isMap())
                .andExpect(jsonPath("$.data.company.id").isNumber())
                .andExpect(jsonPath("$.data.company.name").isString())
                .andExpect(jsonPath("$.data.company.careerUrl").isString())
                .andExpect(jsonPath("$.data.regDate").isString())
                .andExpect(jsonPath("$.data.author").isString())
                .andExpect(jsonPath("$.data.viewTotalCount").isNumber())
                .andExpect(jsonPath("$.data.recommendTotalCount").isNumber())
                .andExpect(jsonPath("$.data.commentTotalCount").isNumber())
                .andExpect(jsonPath("$.data.popularScore").isNumber())
                .andExpect(jsonPath("$.data.isRecommended").isBoolean());
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
        mockMvc.perform(get("/devdevdev/api/v1/articles/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header(SecurityConstant.AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultType").value(ResultType.SUCCESS.name()))
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andExpect(jsonPath("$.data").isMap())
                .andExpect(jsonPath("$.data.thumbnailUrl").isString())
                .andExpect(jsonPath("$.data.techArticleUrl").isString())
                .andExpect(jsonPath("$.data.title").isString())
                .andExpect(jsonPath("$.data.contents").isString())
                .andExpect(jsonPath("$.data.company").isMap())
                .andExpect(jsonPath("$.data.company.id").isNumber())
                .andExpect(jsonPath("$.data.company.name").isString())
                .andExpect(jsonPath("$.data.company.careerUrl").isString())
                .andExpect(jsonPath("$.data.regDate").isString())
                .andExpect(jsonPath("$.data.author").isString())
                .andExpect(jsonPath("$.data.viewTotalCount").isNumber())
                .andExpect(jsonPath("$.data.recommendTotalCount").isNumber())
                .andExpect(jsonPath("$.data.commentTotalCount").isNumber())
                .andExpect(jsonPath("$.data.popularScore").isNumber())
                .andExpect(jsonPath("$.data.isBookmarked").isBoolean())
                .andExpect(jsonPath("$.data.isRecommended").isBoolean());
    }

    @Test
    @DisplayName("회원이 기술블로그 상세를 조회할 때 회원이 없으면 예외가 발생한다.")
    void getTechArticleNotFoundMemberException() throws Exception {
        // given
        Long id = 1L;
        
        given(memberTechArticleService.getTechArticle(eq(id), any(), any()))
                .willThrow(new MemberException(INVALID_MEMBER_NOT_FOUND_MESSAGE));

        // when // then
        mockMvc.perform(get("/devdevdev/api/v1/articles/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header(SecurityConstant.AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultType").value(ResultType.FAIL.name()))
                .andExpect(jsonPath("$.message").value(INVALID_MEMBER_NOT_FOUND_MESSAGE))
                .andExpect(jsonPath("$.errorCode").value(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    @DisplayName("기술블로그 상세를 조회할 때 기술블로그가 존재하지 않으면 예외가 발생한다.")
    void getTechArticleNotFoundTechArticleException() throws Exception {
        // given
        Long id = 999L;
        
        given(guestTechArticleService.getTechArticle(eq(id), any(), any()))
                .willThrow(new NotFoundException(NOT_FOUND_TECH_ARTICLE_MESSAGE));

        // when // then
        mockMvc.perform(get("/devdevdev/api/v1/articles/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultType").value(ResultType.FAIL.name()))
                .andExpect(jsonPath("$.message").value(NOT_FOUND_TECH_ARTICLE_MESSAGE))
                .andExpect(jsonPath("$.errorCode").value(HttpStatus.NOT_FOUND.value()));
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
        mockMvc.perform(post("/devdevdev/api/v1/articles/{id}/bookmark", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header(SecurityConstant.AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultType").value(ResultType.SUCCESS.name()))
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andExpect(jsonPath("$.data").isMap())
                .andExpect(jsonPath("$.data.techArticleId").isNumber())
                .andExpect(jsonPath("$.data.status").isBoolean());
    }

    @Test
    @DisplayName("회원이 기술블로그 북마크를 요청할 때 존재하지 않는 기술블로그라면 예외가 발생한다.")
    void updateBookmarkNotFoundTechArticleException() throws Exception {
        // given
        Long id = 999L;
        
        given(memberTechArticleService.updateBookmark(eq(id), any()))
                .willThrow(new NotFoundException(NOT_FOUND_TECH_ARTICLE_MESSAGE));

        // when // then
        mockMvc.perform(post("/devdevdev/api/v1/articles/{id}/bookmark", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header(SecurityConstant.AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultType").value(ResultType.FAIL.name()))
                .andExpect(jsonPath("$.message").value(NOT_FOUND_TECH_ARTICLE_MESSAGE))
                .andExpect(jsonPath("$.errorCode").value(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    @DisplayName("회원이 기술블로그 북마크를 요청할 때 존재하지 않는 회원이라면 예외가 발생한다.")
    void updateBookmarkNotFoundMemberException() throws Exception {
        // given
        Long id = 1L;
        
        given(memberTechArticleService.updateBookmark(eq(id), any()))
                .willThrow(new MemberException(INVALID_MEMBER_NOT_FOUND_MESSAGE));

        // when // then
        mockMvc.perform(post("/devdevdev/api/v1/articles/{id}/bookmark", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header(SecurityConstant.AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultType").value(ResultType.FAIL.name()))
                .andExpect(jsonPath("$.message").value(INVALID_MEMBER_NOT_FOUND_MESSAGE))
                .andExpect(jsonPath("$.errorCode").value(HttpStatus.NOT_FOUND.value()));
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
        mockMvc.perform(post("/devdevdev/api/v1/articles/{id}/recommend", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header(SecurityConstant.AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultType").value(ResultType.SUCCESS.name()))
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andExpect(jsonPath("$.data").isMap())
                .andExpect(jsonPath("$.data.techArticleId").isNumber())
                .andExpect(jsonPath("$.data.status").isBoolean())
                .andExpect(jsonPath("$.data.recommendTotalCount").isNumber());

    }

    @Test
    @DisplayName("회원이 기술블로그 추천을 요청할 때 존재하지 않는 기술블로그라면 예외가 발생한다.")
    void updateRecommendNotFoundTechArticleException() throws Exception {
        // given
        Long id = 999L;
        
        given(memberTechArticleService.updateRecommend(eq(id), any(), any()))
                .willThrow(new NotFoundException(NOT_FOUND_TECH_ARTICLE_MESSAGE));

        // when // then
        mockMvc.perform(post("/devdevdev/api/v1/articles/{id}/recommend", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header(SecurityConstant.AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultType").value(ResultType.FAIL.name()))
                .andExpect(jsonPath("$.message").value(NOT_FOUND_TECH_ARTICLE_MESSAGE))
                .andExpect(jsonPath("$.errorCode").value(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    @DisplayName("회원이 기술블로그 추천을 요청할 때 존재하지 않는 회원이라면 예외가 발생한다.")
    void updateRecommendNotFoundMemberException() throws Exception {
        // given
        Long id = 1L;
        
        given(memberTechArticleService.updateRecommend(eq(id), any(), any()))
                .willThrow(new MemberException(INVALID_MEMBER_NOT_FOUND_MESSAGE));

        // when // then
        mockMvc.perform(post("/devdevdev/api/v1/articles/{id}/recommend", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header(SecurityConstant.AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultType").value(ResultType.FAIL.name()))
                .andExpect(jsonPath("$.message").value(INVALID_MEMBER_NOT_FOUND_MESSAGE))
                .andExpect(jsonPath("$.errorCode").value(HttpStatus.NOT_FOUND.value()));
    }

    private TechArticleMainResponse createTechArticleMainResponse(Long id, String thumbnailUrl, Boolean isLogoImage,
                                                                  String techArticleUrl, String title, String contents,
                                                                  Long companyId, String companyName, String careerUrl, String officialImageUrl,
                                                                  LocalDate regDate, String author, long recommendCount,
                                                                  long commentCount, long viewCount, Boolean isBookmarked, Float score) {
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