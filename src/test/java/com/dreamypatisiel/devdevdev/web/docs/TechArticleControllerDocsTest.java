package com.dreamypatisiel.devdevdev.web.docs;

import com.dreamypatisiel.devdevdev.domain.entity.*;
import com.dreamypatisiel.devdevdev.domain.repository.BookmarkRepository;
import com.dreamypatisiel.devdevdev.domain.repository.MemberRepository;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechArticleRepository;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechArticleSort;
import com.dreamypatisiel.devdevdev.elastic.domain.document.ElasticTechArticle;
import com.dreamypatisiel.devdevdev.elastic.domain.repository.ElasticTechArticleRepository;
import com.dreamypatisiel.devdevdev.global.constant.SecurityConstant;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.SocialMemberDto;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static com.dreamypatisiel.devdevdev.global.constant.SecurityConstant.AUTHORIZATION_HEADER;
import static com.dreamypatisiel.devdevdev.web.docs.format.ApiDocsFormatGenerator.authenticationType;
import static com.dreamypatisiel.devdevdev.web.docs.format.ApiDocsFormatGenerator.techArticleSortType;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TechArticleControllerDocsTest extends SupportControllerDocsTest {

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
                      @Autowired ElasticTechArticleRepository elasticTechArticleRepository) {

        List<ElasticTechArticle> elasticTechArticles = new ArrayList<>();
        for (int i = 1; i <= 1; i++) {
            ElasticTechArticle elasticTechArticle = ElasticTechArticle.of("타이틀"+i, createRandomDate(), "내용", "http://example.com/"+i, "설명", "http://example.com/", "작성자", "회사", (long)i, (long)i, (long)i, (long)i*10);
            elasticTechArticles.add(elasticTechArticle);
        }
        Iterable<ElasticTechArticle> elasticTechArticleIterable = elasticTechArticleRepository.saveAll(elasticTechArticles);

        techArticles = new ArrayList<>();
        for (ElasticTechArticle elasticTechArticle : elasticTechArticleIterable) {
            TechArticle techArticle = TechArticle.from(elasticTechArticle);
            techArticles.add(techArticle);
        }
        techArticleRepository.saveAll(techArticles);
    }

    @AfterAll
    static void tearDown(@Autowired TechArticleRepository techArticleRepository,
                         @Autowired ElasticTechArticleRepository elasticTechArticleRepository) {
        elasticTechArticleRepository.deleteAll();
        techArticleRepository.deleteAllInBatch();
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
            if(creatRandomBoolean()){
                Bookmark bookmark = Bookmark.from(member, techArticle);
                bookmarks.add(bookmark);
            }
        }
        bookmarkRepository.saveAll(bookmarks);

        Pageable pageable1 = PageRequest.of(0, 10);
        Pageable pageable2 = PageRequest.of(0, 10);
        List<ElasticTechArticle> elasticTechArticles = elasticTechArticleRepository.findAll(pageable1).stream().toList();
        ElasticTechArticle cursor = elasticTechArticles.getLast();
        String keyword = "타이틀";

        // when // then
        ResultActions actions = mockMvc.perform(get("/devdevdev/api/v1/articles")
                        .queryParam("size", String.valueOf(pageable2.getPageSize()))
                        .queryParam("techArticleSort", TechArticleSort.HIGHEST_SCORE.name())
                        .queryParam("keyword", keyword)
                        .queryParam("elasticId", cursor.getId())
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
                        parameterWithName("techArticleSort").optional().description("정렬 조건").attributes(techArticleSortType()),
                        parameterWithName("keyword").optional().description("검색어"),
                        parameterWithName("elasticId").optional().description("마지막 데이터의 엘라스틱서치 아이디"),
                        parameterWithName("score").optional().description("마지막 데이터의 엘라스틱서치 아이디(정확도순 검색일 때에만 필수)")
                ),
                responseFields(
                        fieldWithPath("resultType").type(JsonFieldType.STRING).description("응답 결과"),
                        fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),

                        fieldWithPath("data.content").type(JsonFieldType.ARRAY).description("기술블로그 메인 배열"),
                        fieldWithPath("data.content.[].id").type(JsonFieldType.NUMBER).description("기술블로그 아이디"),
                        fieldWithPath("data.content.[].elasticId").type(JsonFieldType.STRING).description("기술블로그 엘라스틱서치 아이디"),
                        fieldWithPath("data.content.[].thumbnailUrl").type(JsonFieldType.STRING).description("기술블로그 썸네일 이미지"),
                        fieldWithPath("data.content.[].title").type(JsonFieldType.STRING).description("기술블로그 제목"),
                        fieldWithPath("data.content.[].company").type(JsonFieldType.STRING).description("기술블로그 회사"),
                        fieldWithPath("data.content.[].regDate").type(JsonFieldType.STRING).description("기술블로그 작성일"),
                        fieldWithPath("data.content.[].author").type(JsonFieldType.STRING).description("기술블로그 작성자"),
                        fieldWithPath("data.content.[].description").type(JsonFieldType.STRING).description("기술블로그 설명"),
                        fieldWithPath("data.content.[].viewTotalCount").type(JsonFieldType.NUMBER).description("기술블로그 조회수"),
                        fieldWithPath("data.content.[].recommendTotalCount").type(JsonFieldType.NUMBER).description("기술블로그 추천수"),
                        fieldWithPath("data.content.[].commentTotalCount").type(JsonFieldType.NUMBER).description("기술블로그 댓글수"),
                        fieldWithPath("data.content.[].popularScore").type(JsonFieldType.NUMBER).description("기술블로그 인기점수"),
                        fieldWithPath("data.content.[].isBookmarked").attributes(authenticationType()).type(JsonFieldType.BOOLEAN).description("회원의 북마크 여부(익명 사용자는 필드가 없다)"),
                        fieldWithPath("data.content.[].score").type(JsonFieldType.NUMBER).description("정확도 점수"),

                        fieldWithPath("data.pageable").type(JsonFieldType.OBJECT).description("페이지네이션 정보"),
                        fieldWithPath("data.pageable.pageNumber").type(JsonFieldType.NUMBER).description("페이지 번호"),
                        fieldWithPath("data.pageable.pageSize").type(JsonFieldType.NUMBER).description("페이지 사이즈"),

                        fieldWithPath("data.pageable.sort").type(JsonFieldType.OBJECT).description("정렬 정보"),
                        fieldWithPath("data.pageable.sort.empty").type(JsonFieldType.BOOLEAN).description("정렬 정보가 비어있는지 여부"),
                        fieldWithPath("data.pageable.sort.sorted").type(JsonFieldType.BOOLEAN).description("정렬 여부"),
                        fieldWithPath("data.pageable.sort.unsorted").type(JsonFieldType.BOOLEAN).description("비정렬 여부"),

                        fieldWithPath("data.pageable.offset").type(JsonFieldType.NUMBER).description("페이지 오프셋 (페이지 크기 * 페이지 번호)"),
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
    @DisplayName("기술블로그 메인을 조회할 때 존재하지 않는 엘라스틱서치 ID를 조회하면 에러가 발생한다.")
    void getTechArticlesNotFoundElasticIdException() throws Exception {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when // then
        ResultActions actions = mockMvc.perform(get("/devdevdev/api/v1/articles")
                        .queryParam("size", String.valueOf(pageable.getPageSize()))
                        .queryParam("techArticleSort", TechArticleSort.LATEST.name())
                        .queryParam("elasticId", "elasticId")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().isBadRequest());

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
    @DisplayName("커서 방식으로 다음 페이지의 엘라스틱서치 기술블로그를 검색어로 검색할 때," +
            "정확도 내림차순으로 조회하기 위한 점수가 없다면 예외가 발생한다.")
    void getTechArticlesWithKeywordWithCursorOrderByHIGHEST_SCOREWithoutScoreException() throws Exception {
        // given
        Pageable pageable1 = PageRequest.of(0, 1);
        Pageable pageable2 = PageRequest.of(0, 10);
        List<ElasticTechArticle> elasticTechArticles = elasticTechArticleRepository.findAll(pageable1).stream().toList();
        ElasticTechArticle cursor = elasticTechArticles.getLast();
        String keyword = "타이틀";

        // when // then
        ResultActions actions = mockMvc.perform(get("/devdevdev/api/v1/articles")
                        .queryParam("size", String.valueOf(pageable2.getPageSize()))
                        .queryParam("techArticleSort", TechArticleSort.HIGHEST_SCORE.name())
                        .queryParam("elasticId", cursor.getId())
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

    private boolean creatRandomBoolean() {
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
