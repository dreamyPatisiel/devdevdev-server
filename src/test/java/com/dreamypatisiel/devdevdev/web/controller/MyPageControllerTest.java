package com.dreamypatisiel.devdevdev.web.controller;

import com.dreamypatisiel.devdevdev.domain.entity.*;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.CompanyName;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Count;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Url;
import com.dreamypatisiel.devdevdev.domain.repository.BookmarkRepository;
import com.dreamypatisiel.devdevdev.domain.repository.CompanyRepository;
import com.dreamypatisiel.devdevdev.domain.repository.MemberRepository;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.BookmarkSort;
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
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static com.dreamypatisiel.devdevdev.domain.exception.TechArticleExceptionMessage.*;
import static com.dreamypatisiel.devdevdev.exception.MemberException.INVALID_MEMBER_NOT_FOUND_MESSAGE;
import static com.dreamypatisiel.devdevdev.global.constant.SecurityConstant.AUTHORIZATION_HEADER;
import static com.dreamypatisiel.devdevdev.web.docs.format.ApiDocsFormatGenerator.authenticationType;
import static com.dreamypatisiel.devdevdev.web.docs.format.ApiDocsFormatGenerator.techArticleSortType;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MyPageControllerTest extends SupportControllerTest {

    private static Long FIRST_TECH_ARTICLE_ID;

    @Autowired
    TechArticleRepository techArticleRepository;
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
    @DisplayName("회원이 기술블로그 북마크 목록을 조회한다.")
    void getBookmarkedTechArticles() throws Exception {
        // given
        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        member.updateRefreshToken(refreshToken);
        memberRepository.save(member);

        List<Bookmark> bookmarks = new ArrayList<>();
        for (TechArticle techArticle : techArticles) {
            Bookmark bookmark = createBookmark(member, techArticle, true);
            bookmarks.add(bookmark);
        }
        bookmarkRepository.saveAll(bookmarks);

        Pageable pageable = PageRequest.of(0, 2);

        // when // then
        ResultActions actions = mockMvc.perform(RestDocumentationRequestBuilders.get("/devdevdev/api/v1/mypage/bookmarks")
                        .queryParam("size", String.valueOf(pageable.getPageSize()))
                        .queryParam("bookmarkSort", BookmarkSort.BOOKMARKED.name())
                        .queryParam("techArticleId", "")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header(AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken))
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
                .andExpect(jsonPath("$.data.content.[0].score").isEmpty())
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
    @DisplayName("회원이 기술블로그 북마크 목록을 조회할 때 회원이 없으면 예외가 발생한다.")
    void getBookmarkedTechArticlesNotFoundMemberException() throws Exception {
        // given
        Long id = FIRST_TECH_ARTICLE_ID;
        // given
        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        member.updateRefreshToken(refreshToken);

        // when // then
        ResultActions actions = mockMvc.perform(RestDocumentationRequestBuilders.get("/devdevdev/api/v1/mypage/bookmarks")
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
    @DisplayName("회원이 기술블로그 북마크 목록을 조회할 때 회원이 북마크한 내역이 없다면 빈 배열이 응답된다.")
    void getBookmarkedTechArticlesEmptyList() throws Exception {
        // given
        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        member.updateRefreshToken(refreshToken);
        memberRepository.save(member);

        Pageable pageable = PageRequest.of(0, 2);

        // when // then
        ResultActions actions = mockMvc.perform(RestDocumentationRequestBuilders.get("/devdevdev/api/v1/mypage/bookmarks")
                        .queryParam("size", String.valueOf(pageable.getPageSize()))
                        .queryParam("bookmarkSort", BookmarkSort.BOOKMARKED.name())
                        .queryParam("techArticleId", "")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header(AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultType").value(ResultType.SUCCESS.name()))
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andExpect(jsonPath("$.data.content").isEmpty())
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