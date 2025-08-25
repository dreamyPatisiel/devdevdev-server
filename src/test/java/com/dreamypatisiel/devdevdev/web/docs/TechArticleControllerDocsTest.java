package com.dreamypatisiel.devdevdev.web.docs;

import static com.dreamypatisiel.devdevdev.domain.exception.MemberExceptionMessage.INVALID_MEMBER_NOT_FOUND_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.TechArticleExceptionMessage.KEYWORD_WITH_SPECIAL_SYMBOLS_EXCEPTION_MESSAGE;
import static com.dreamypatisiel.devdevdev.global.constant.SecurityConstant.AUTHORIZATION_HEADER;
import static com.dreamypatisiel.devdevdev.web.docs.format.ApiDocsFormatGenerator.authenticationType;
import static com.dreamypatisiel.devdevdev.web.docs.format.ApiDocsFormatGenerator.techArticleSortType;
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

import com.dreamypatisiel.devdevdev.domain.entity.Bookmark;
import com.dreamypatisiel.devdevdev.domain.entity.Company;
import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.TechArticle;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.CompanyName;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Count;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Title;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Url;
import com.dreamypatisiel.devdevdev.domain.entity.enums.Role;
import com.dreamypatisiel.devdevdev.domain.entity.enums.SocialType;
import com.dreamypatisiel.devdevdev.domain.repository.CompanyRepository;
import com.dreamypatisiel.devdevdev.domain.repository.member.MemberRepository;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.BookmarkRepository;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechArticleRepository;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechArticleSort;
import com.dreamypatisiel.devdevdev.global.constant.SecurityConstant;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.SocialMemberDto;
import com.dreamypatisiel.devdevdev.web.dto.response.ResultType;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

public class TechArticleControllerDocsTest extends SupportControllerDocsTest {

    private static final int TEST_ARTICLES_COUNT = 20;
    private static Company company;
    private static TechArticle firstTechArticle;
    private static List<TechArticle> techArticles;

    @Autowired
    TechArticleRepository techArticleRepository;
    @Autowired
    CompanyRepository companyRepository;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    BookmarkRepository bookmarkRepository;

    @BeforeAll
    static void setup(@Autowired TechArticleRepository techArticleRepository,
                      @Autowired CompanyRepository companyRepository) {
        company = createCompany("꿈빛 파티시엘", "https://example.com/company.png", "https://example.com",
                "https://example.com");
        companyRepository.save(company);

        List<TechArticle> techArticles = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            TechArticle techArticle = createTechArticle(i, company);
            techArticles.add(techArticle);
        }
        techArticleRepository.saveAll(techArticles);
    }

    @AfterAll
    static void tearDown(@Autowired TechArticleRepository techArticleRepository,
                         @Autowired CompanyRepository companyRepository) {
        techArticleRepository.deleteAllInBatch();
        companyRepository.deleteAllInBatch();
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
            if (creatRandomBoolean()) {
                Bookmark bookmark = createBookmark(member, techArticle, true);
                bookmarks.add(bookmark);
            }
        }
        bookmarkRepository.saveAll(bookmarks);

        Pageable pageable = PageRequest.of(0, 1);
        String techArticleId = "1";
        String keyword = "타이틀";
        String companyId = company.getId().toString();

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
        Long id = firstTechArticle.getId();
        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        member.updateRefreshToken(refreshToken);
        memberRepository.save(member);

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
        Long id = firstTechArticle.getId();
        // given
        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        member.updateRefreshToken(refreshToken);

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
        TechArticle techArticle = createTechArticle(1, company);
        techArticleRepository.save(techArticle);
        Long id = techArticle.getId() + 1;

        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        member.updateRefreshToken(refreshToken);
        memberRepository.save(member);

        // when // then
        ResultActions actions = mockMvc.perform(get("/devdevdev/api/v1/articles/{techArticleId}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header(AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken))
                .andDo(print());

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
        Long id = firstTechArticle.getId();
        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        member.updateRefreshToken(refreshToken);
        memberRepository.save(member);

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
        Long id = firstTechArticle.getId();
        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        member.updateRefreshToken(refreshToken);
        memberRepository.save(member);

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
                )
        ));
    }

    private SocialMemberDto createSocialDto(String userId, String name, String nickName, String password, String email,
                                            String socialType, String role) {
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

    private static Company createCompany(String companyName, String officialImageUrl, String officialUrl,
                                         String careerUrl) {
        return Company.builder()
                .name(new CompanyName(companyName))
                .officialImageUrl(new Url(officialImageUrl))
                .careerUrl(new Url(careerUrl))
                .officialUrl(new Url(officialUrl))
                .build();
    }

    private static TechArticle createTechArticle(int i, Company company) {
        return TechArticle.builder()
                .title(new Title("타이틀 " + i))
                .contents("내용 " + i)
                .company(company)
                .author("작성자")
                .regDate(LocalDate.now())
                .techArticleUrl(new Url("https://example.com/article"))
                .thumbnailUrl(new Url("https://example.com/images/thumbnail.png"))
                .commentTotalCount(new Count(i))
                .recommendTotalCount(new Count(i))
                .viewTotalCount(new Count(i))
                .popularScore(new Count(i))
                .build();
    }
}
