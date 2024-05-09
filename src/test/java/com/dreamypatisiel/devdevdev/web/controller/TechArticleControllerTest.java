package com.dreamypatisiel.devdevdev.web.controller;

import com.dreamypatisiel.devdevdev.domain.entity.*;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.CompanyName;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Count;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Url;
import com.dreamypatisiel.devdevdev.domain.entity.enums.Role;
import com.dreamypatisiel.devdevdev.domain.entity.enums.SocialType;
import com.dreamypatisiel.devdevdev.domain.repository.BookmarkRepository;
import com.dreamypatisiel.devdevdev.domain.repository.CompanyRepository;
import com.dreamypatisiel.devdevdev.domain.repository.MemberRepository;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechArticleRepository;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechArticleSort;
import com.dreamypatisiel.devdevdev.elastic.domain.document.ElasticTechArticle;
import com.dreamypatisiel.devdevdev.elastic.domain.repository.ElasticTechArticleRepository;
import com.dreamypatisiel.devdevdev.global.constant.SecurityConstant;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.SocialMemberDto;
import com.dreamypatisiel.devdevdev.web.response.ResultType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static com.dreamypatisiel.devdevdev.domain.exception.TechArticleExceptionMessage.*;
import static com.dreamypatisiel.devdevdev.exception.MemberException.INVALID_MEMBER_NOT_FOUND_MESSAGE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TechArticleControllerTest extends SupportControllerTest {

    private static Long FIRST_TECH_ARTICLE_ID;

    @Autowired
    TechArticleRepository techArticleRepository;
    @Autowired
    ElasticTechArticleRepository elasticTechArticleRepository;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    BookmarkRepository bookmarkRepository;

    private static List<TechArticle> techArticles;

    @BeforeAll
    static void setup(@Autowired TechArticleRepository techArticleRepository,
                      @Autowired CompanyRepository companyRepository,
                      @Autowired ElasticTechArticleRepository elasticTechArticleRepository) {

        List<ElasticTechArticle> elasticTechArticles = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            ElasticTechArticle elasticTechArticle = ElasticTechArticle.of("타이틀"+i, createRandomDate(), "내용", "http://example.com/"+i, "설명", "http://example.com/", "작성자", "DP", (long)i, (long)i, (long)i, (long)i*10);
            elasticTechArticles.add(elasticTechArticle);
        }
        Iterable<ElasticTechArticle> elasticTechArticleIterable = elasticTechArticleRepository.saveAll(elasticTechArticles);
        Company company = Company.of(new CompanyName("꿈빛 파티시엘"), new Url("https://example.com"), new Url("https://example.com"));
        Company savedCompany = companyRepository.save(company);

        techArticles = new ArrayList<>();
        for (ElasticTechArticle elasticTechArticle : elasticTechArticleIterable) {
            TechArticle techArticle = TechArticle.of(elasticTechArticle, savedCompany);
            techArticles.add(techArticle);
        }
        List<TechArticle> savedTechArticles = techArticleRepository.saveAll(techArticles);
        FIRST_TECH_ARTICLE_ID = savedTechArticles.getFirst().getId();
    }

    @AfterAll
    static void tearDown(@Autowired TechArticleRepository techArticleRepository,
                         @Autowired ElasticTechArticleRepository elasticTechArticleRepository) {
        elasticTechArticleRepository.deleteAll();
        techArticleRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("익명 사용자가 기술블로그 메인을 조회한다.")
    void getTechArticlesByAnonymous() throws Exception {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when // then
        mockMvc.perform(get("/devdevdev/api/v1/articles")
                        .queryParam("size", String.valueOf(pageable.getPageSize()))
                        .queryParam("techArticleSort", TechArticleSort.LATEST.name())
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultType").value(ResultType.SUCCESS.name()))
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.[0].id").isNumber())
                .andExpect(jsonPath("$.data.content.[0].elasticId").isString())
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
        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        member.updateRefreshToken(refreshToken);
        memberRepository.save(member);

        List<Bookmark> bookmarks = new ArrayList<>();
        for (TechArticle techArticle : techArticles) {
            if(createRandomBoolean()){
                Bookmark bookmark = createBookmark(member, techArticle, true);
                bookmarks.add(bookmark);
            }
        }
        bookmarkRepository.saveAll(bookmarks);

        Pageable pageable = PageRequest.of(0, 10);

        // when // then
        mockMvc.perform(get("/devdevdev/api/v1/articles")
                .queryParam("size", String.valueOf(pageable.getPageSize()))
                .queryParam("techArticleSort", TechArticleSort.LATEST.name())
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8)
                .header(SecurityConstant.AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultType").value(ResultType.SUCCESS.name()))
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.[0].id").isNumber())
                .andExpect(jsonPath("$.data.content.[0].elasticId").isString())
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
    @DisplayName("기술블로그 메인을 조회할 때 존재하지 않는 엘라스틱서치 ID를 조회하면 에러가 발생한다.")
    void getTechArticlesNotFoundElasticIdException() throws Exception {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when // then
        mockMvc.perform(get("/devdevdev/api/v1/articles")
                        .queryParam("size", String.valueOf(pageable.getPageSize()))
                        .queryParam("techArticleSort", TechArticleSort.LATEST.name())
                        .queryParam("elasticId", "elasticId")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultType").value(ResultType.FAIL.name()))
                .andExpect(jsonPath("$.message").value(NOT_FOUND_ELASTIC_TECH_ARTICLE_MESSAGE))
                .andExpect(jsonPath("$.errorCode").value(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    @DisplayName("커서 방식으로 다음 페이지의 엘라스틱서치 기술블로그를 검색어로 검색할 때," +
            "정확도 내림차순으로 조회하기 위한 점수가 없다면 예외가 발생한다.")
    void getTechArticlesWithKeywordWithCursorOrderByHIGHEST_SCOREWithoutScoreException() throws Exception {
        // given
        Pageable prevPageable = PageRequest.of(0, 1);
        Pageable pageable = PageRequest.of(0, 10);
        List<ElasticTechArticle> elasticTechArticles = elasticTechArticleRepository.findAll(prevPageable).stream().toList();
        ElasticTechArticle cursor = elasticTechArticles.getLast();
        String keyword = "타이틀";

        // when // then
        mockMvc.perform(get("/devdevdev/api/v1/articles")
                        .queryParam("size", String.valueOf(pageable.getPageSize()))
                        .queryParam("techArticleSort", TechArticleSort.HIGHEST_SCORE.name())
                        .queryParam("elasticId", cursor.getId())
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
    @DisplayName("익명 사용자가 기술블로그 상세를 조회한다.")
    void getTechArticleByAnonymous() throws Exception {
        // given
        Long id = FIRST_TECH_ARTICLE_ID;

        // when // then
        mockMvc.perform(get("/devdevdev/api/v1/articles/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultType").value(ResultType.SUCCESS.name()))
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andExpect(jsonPath("$.data").isMap())
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.elasticId").isString())
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
                .andExpect(jsonPath("$.data.popularScore").isNumber());
    }

    @Test
    @DisplayName("회원이 기술블로그 상세를 조회한다.")
    void getTechArticleByMember() throws Exception {
        // given
        Long id = FIRST_TECH_ARTICLE_ID;
        // given
        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        member.updateRefreshToken(refreshToken);
        memberRepository.save(member);

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
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.elasticId").isString())
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
                .andExpect(jsonPath("$.data.isBookmarked").isBoolean());
    }

    @Test
    @DisplayName("회원이 기술블로그 상세를 조회할 때 회원이 없으면 예외가 발생한다.")
    void getTechArticleNotFoundMemberException() throws Exception {
        // given
        Long id = FIRST_TECH_ARTICLE_ID;

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
        TechArticle techArticle = TechArticle.of(new Url("https://example.com"), new Count(1L), new Count(1L), new Count(1L),
                new Count(1L), null, null);
        TechArticle savedTechArticle = techArticleRepository.save(techArticle);
        Long id = savedTechArticle.getId()+1;

        // when // then
        mockMvc.perform(get("/devdevdev/api/v1/articles/{id}", id)
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
    @DisplayName("기술블로그 상세를 조회할 때 엘라스틱ID가 존재하지 않으면 예외가 발생한다.")
    void getTechArticleNotFoundElasticIdException() throws Exception {
        // given
        TechArticle techArticle = TechArticle.of(new Url("https://example.com"), new Count(1L), new Count(1L), new Count(1L),
                new Count(1L), null, null);
        TechArticle savedTechArticle = techArticleRepository.save(techArticle);
        Long id = savedTechArticle.getId();

        // when // then
        mockMvc.perform(get("/devdevdev/api/v1/articles/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header(SecurityConstant.AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultType").value(ResultType.FAIL.name()))
                .andExpect(jsonPath("$.message").value(NOT_FOUND_ELASTIC_ID_MESSAGE))
                .andExpect(jsonPath("$.errorCode").value(HttpStatus.BAD_REQUEST.value()));
    }

    @Test
    @DisplayName("기술블로그 상세를 조회할 때 엘라스틱 기술블로그가 존재하지 않으면 예외가 발생한다.")
    void getTechArticleNotFoundElasticTechArticleException() throws Exception {
        // given
        TechArticle techArticle = TechArticle.of(new Url("https://example.com"), new Count(1L), new Count(1L), new Count(1L),
                new Count(1L), "elasticId", null);
        TechArticle savedTechArticle = techArticleRepository.save(techArticle);
        Long id = savedTechArticle.getId();

        // when // then
        mockMvc.perform(get("/devdevdev/api/v1/articles/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header(SecurityConstant.AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultType").value(ResultType.FAIL.name()))
                .andExpect(jsonPath("$.message").value(NOT_FOUND_ELASTIC_TECH_ARTICLE_MESSAGE))
                .andExpect(jsonPath("$.errorCode").value(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    @DisplayName("회원이 기술블로그 북마크를 요청한다.")
    void updateBookmark() throws Exception {
        // given
        Long id = FIRST_TECH_ARTICLE_ID;
        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        member.updateRefreshToken(refreshToken);
        memberRepository.save(member);

        // when // then
        mockMvc.perform(post("/devdevdev/api/v1/articles/{id}/bookmark", id)
                        .queryParam("status", String.valueOf(true))
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
        TechArticle techArticle = TechArticle.of(new Url("https://example.com"), new Count(1L), new Count(1L), new Count(1L),
                new Count(1L), null, null);
        TechArticle savedTechArticle = techArticleRepository.save(techArticle);
        Long id = savedTechArticle.getId()+1;

        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        member.updateRefreshToken(refreshToken);
        memberRepository.save(member);

        // when // then
        mockMvc.perform(post("/devdevdev/api/v1/articles/{id}/bookmark", id)
                        .queryParam("status", String.valueOf(true))
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
        Long id = FIRST_TECH_ARTICLE_ID;

        // when // then
        mockMvc.perform(post("/devdevdev/api/v1/articles/{id}/bookmark", id)
                        .queryParam("status", String.valueOf(true))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header(SecurityConstant.AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultType").value(ResultType.FAIL.name()))
                .andExpect(jsonPath("$.message").value(INVALID_MEMBER_NOT_FOUND_MESSAGE))
                .andExpect(jsonPath("$.errorCode").value(HttpStatus.NOT_FOUND.value()));
    }

    private SocialMemberDto createSocialDto(String userId, String name, String nickName, String password, String email, String socialType, String role) {
        return SocialMemberDto.builder()
                .userId(userId)
                .name(name)
                .nickname(nickName)
                .password(password)
                .email(email)
                .socialType(SocialType.valueOf(socialType))
                .role(Role.valueOf(role))
                .build();
    }

    private Bookmark createBookmark(Member member, TechArticle techArticle, boolean status) {
        return Bookmark.builder()
                .member(member)
                .techArticle(techArticle)
                .status(status)
                .build();
    }
    
    private boolean createRandomBoolean() {
        return new Random().nextBoolean();
    }

    private static LocalDate createRandomDate() {
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 3, 10);

        // 시작 날짜와 종료 날짜 사이의 차이 중 랜덤한 일 수 선택
        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate);
        long randomDays = ThreadLocalRandom.current().nextLong(daysBetween + 1);

        return startDate.plusDays(randomDays);
    }
}