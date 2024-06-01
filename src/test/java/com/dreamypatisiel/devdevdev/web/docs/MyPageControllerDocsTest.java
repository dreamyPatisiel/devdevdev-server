package com.dreamypatisiel.devdevdev.web.docs;

import static com.dreamypatisiel.devdevdev.exception.MemberException.INVALID_MEMBER_NOT_FOUND_MESSAGE;
import static com.dreamypatisiel.devdevdev.global.constant.SecurityConstant.AUTHORIZATION_HEADER;
import static com.dreamypatisiel.devdevdev.global.security.jwt.model.JwtCookieConstant.DEVDEVDEV_LOGIN_STATUS;
import static com.dreamypatisiel.devdevdev.global.security.jwt.model.JwtCookieConstant.DEVDEVDEV_REFRESH_TOKEN;
import static com.dreamypatisiel.devdevdev.web.docs.format.ApiDocsFormatGenerator.authenticationType;
import static com.dreamypatisiel.devdevdev.web.docs.format.ApiDocsFormatGenerator.bookmarkSortType;
import static com.dreamypatisiel.devdevdev.web.docs.format.ApiDocsFormatGenerator.contentStatusType;
import static org.springframework.restdocs.cookies.CookieDocumentation.cookieWithName;
import static org.springframework.restdocs.cookies.CookieDocumentation.responseCookies;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.JsonFieldType.ARRAY;
import static org.springframework.restdocs.payload.JsonFieldType.BOOLEAN;
import static org.springframework.restdocs.payload.JsonFieldType.NUMBER;
import static org.springframework.restdocs.payload.JsonFieldType.OBJECT;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dreamypatisiel.devdevdev.domain.entity.Bookmark;
import com.dreamypatisiel.devdevdev.domain.entity.Company;
import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.Pick;
import com.dreamypatisiel.devdevdev.domain.entity.PickOption;
import com.dreamypatisiel.devdevdev.domain.entity.PickVote;
import com.dreamypatisiel.devdevdev.domain.entity.SurveyQuestion;
import com.dreamypatisiel.devdevdev.domain.entity.SurveyQuestionOption;
import com.dreamypatisiel.devdevdev.domain.entity.SurveyVersion;
import com.dreamypatisiel.devdevdev.domain.entity.SurveyVersionQuestionMapper;
import com.dreamypatisiel.devdevdev.domain.entity.TechArticle;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.CompanyName;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Count;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.PickOptionContents;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Title;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Url;
import com.dreamypatisiel.devdevdev.domain.entity.enums.ContentStatus;
import com.dreamypatisiel.devdevdev.domain.entity.enums.PickOptionType;
import com.dreamypatisiel.devdevdev.domain.entity.enums.Role;
import com.dreamypatisiel.devdevdev.domain.entity.enums.SocialType;
import com.dreamypatisiel.devdevdev.domain.repository.BookmarkRepository;
import com.dreamypatisiel.devdevdev.domain.repository.CompanyRepository;
import com.dreamypatisiel.devdevdev.domain.repository.member.MemberRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickOptionRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickRepository;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickVoteRepository;
import com.dreamypatisiel.devdevdev.domain.repository.survey.SurveyQuestionOptionRepository;
import com.dreamypatisiel.devdevdev.domain.repository.survey.SurveyQuestionRepository;
import com.dreamypatisiel.devdevdev.domain.repository.survey.SurveyVersionQuestionMapperRepository;
import com.dreamypatisiel.devdevdev.domain.repository.survey.SurveyVersionRepository;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.BookmarkSort;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechArticleRepository;
import com.dreamypatisiel.devdevdev.elastic.domain.document.ElasticTechArticle;
import com.dreamypatisiel.devdevdev.elastic.domain.repository.ElasticTechArticleRepository;
import com.dreamypatisiel.devdevdev.global.constant.SecurityConstant;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.SocialMemberDto;
import com.dreamypatisiel.devdevdev.web.response.ResultType;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.Cookie;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
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
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;

public class MyPageControllerDocsTest extends SupportControllerDocsTest {

    private static Long FIRST_TECH_ARTICLE_ID;

    @Autowired
    TechArticleRepository techArticleRepository;
    @Autowired
    CompanyRepository companyRepository;
    @Autowired
    ElasticTechArticleRepository elasticTechArticleRepository;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    BookmarkRepository bookmarkRepository;
    @Autowired
    PickRepository pickRepository;
    @Autowired
    PickOptionRepository pickOptionRepository;
    @Autowired
    PickVoteRepository pickVoteRepository;
    @Autowired
    SurveyVersionRepository surveyVersionRepository;
    @Autowired
    SurveyVersionQuestionMapperRepository surveyVersionQuestionMapperRepository;
    @Autowired
    SurveyQuestionRepository surveyQuestionRepository;
    @Autowired
    SurveyQuestionOptionRepository surveyQuestionOptionRepository;
    @Autowired
    EntityManager em;

    private static List<TechArticle> techArticles;

    @BeforeAll
    static void setup(@Autowired TechArticleRepository techArticleRepository,
                      @Autowired CompanyRepository companyRepository,
                      @Autowired ElasticTechArticleRepository elasticTechArticleRepository) {

        List<ElasticTechArticle> elasticTechArticles = new ArrayList<>();
        for (int i = 1; i <= 1; i++) {
            ElasticTechArticle elasticTechArticle = createElasticTechArticle("타이틀" + i, createRandomDate(), "내용",
                    "http://example.com/" + i, "설명", "http://example.com/", "작성자", "회사", (long) i, (long) i, (long) i,
                    (long) i * 10);
            elasticTechArticles.add(elasticTechArticle);
        }
        Iterable<ElasticTechArticle> elasticTechArticleIterable = elasticTechArticleRepository.saveAll(
                elasticTechArticles);
        Company company = Company.of(new CompanyName("꿈빛 파티시엘"), new Url("https://example.com"),
                new Url("https://example.com"));
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
        ResultActions actions = mockMvc.perform(get("/devdevdev/api/v1/mypage/bookmarks")
                        .queryParam("size", String.valueOf(pageable.getPageSize()))
                        .queryParam("bookmarkSort", BookmarkSort.BOOKMARKED.name())
                        .queryParam("techArticleId", "")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header(AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken))
                .andDo(print())
                .andExpect(status().isOk());

        // docs
        actions.andDo(document("mypage-bookmarks",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                        headerWithName(AUTHORIZATION_HEADER).description("Bearer 엑세스 토큰")
                ),
                queryParameters(
                        parameterWithName("size").optional().description("조회되는 데이터 수"),
                        parameterWithName("bookmarkSort").optional().description("정렬 조건")
                                .attributes(bookmarkSortType()),
                        parameterWithName("techArticleId").optional().description("마지막 데이터의 아이디")
                ),
                responseFields(
                        fieldWithPath("resultType").type(JsonFieldType.STRING).description("응답 결과"),
                        fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),

                        fieldWithPath("data.content").type(JsonFieldType.ARRAY).description("기술블로그 메인 배열"),
                        fieldWithPath("data.content.[].id").type(JsonFieldType.NUMBER).description("기술블로그 아이디"),
                        fieldWithPath("data.content.[].elasticId").type(JsonFieldType.STRING)
                                .description("기술블로그 엘라스틱서치 아이디"),
                        fieldWithPath("data.content.[].techArticleUrl").type(JsonFieldType.STRING)
                                .description("기술블로그 Url"),
                        fieldWithPath("data.content.[].thumbnailUrl").type(JsonFieldType.STRING)
                                .description("기술블로그 썸네일 이미지"),
                        fieldWithPath("data.content.[].title").type(JsonFieldType.STRING).description("기술블로그 제목"),
                        fieldWithPath("data.content.[].contents").type(JsonFieldType.STRING).description("기술블로그 내용"),
                        fieldWithPath("data.content.[].company").type(JsonFieldType.OBJECT).description("기술블로그 회사"),
                        fieldWithPath("data.content.[].company.id").type(JsonFieldType.NUMBER)
                                .description("기술블로그 회사 id"),
                        fieldWithPath("data.content.[].company.name").type(JsonFieldType.STRING)
                                .description("기술블로그 회사 이름"),
                        fieldWithPath("data.content.[].company.careerUrl").type(JsonFieldType.STRING)
                                .description("기술블로그 회사 채용페이지"),
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
                                .type(JsonFieldType.BOOLEAN).description("회원의 북마크 여부"),
                        fieldWithPath("data.content.[].score").type(JsonFieldType.NULL)
                                .description("정확도 점수(북마크 목록에서는 사용X)"),

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
                        fieldWithPath("data.empty").type(JsonFieldType.BOOLEAN).description("현재 빈 페이지 여부")
                )
        ));
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
        ResultActions actions = mockMvc.perform(get("/devdevdev/api/v1/mypage/bookmarks")
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
    @DisplayName("회원이 회원탈퇴를 한다.")
    void deleteMember() throws Exception {
        // given
        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        member.updateRefreshToken(refreshToken);
        memberRepository.save(member);

        Cookie cookie = new Cookie(DEVDEVDEV_REFRESH_TOKEN, refreshToken);

        // when // then
        ResultActions actions = mockMvc.perform(
                        RestDocumentationRequestBuilders.delete("/devdevdev/api/v1/mypage/profile")
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding(StandardCharsets.UTF_8)
                                .header(AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultType").value(ResultType.SUCCESS.name()));

        // Docs
        actions.andDo(document("mypage-member-delete",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                        headerWithName(AUTHORIZATION_HEADER).description("Bearer 엑세스 토큰")
                ),
                responseCookies(
                        cookieWithName(DEVDEVDEV_REFRESH_TOKEN).description("리프레시 토큰"),
                        cookieWithName(DEVDEVDEV_LOGIN_STATUS).description("로그인 활성화 유뮤(active | inactive)")
                ),
                responseFields(
                        fieldWithPath("resultType").type(JsonFieldType.STRING).description("응답 결과")
                )
        ));
    }

    @Test
    @DisplayName("회원이 회원탈퇴를 할 때 회원이 없으면 예외가 발생한다.")
    void deleteMemberNotFound() throws Exception {
        // given
        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        member.updateRefreshToken(refreshToken);

        // when // then
        ResultActions actions = mockMvc.perform(
                        RestDocumentationRequestBuilders.delete("/devdevdev/api/v1/mypage/profile")
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
    @DisplayName("내가 작성한 픽픽픽 목록을 조회한다.")
    void getMyPicksMain() throws Exception {
        // given
        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        pickSetup(member, ContentStatus.APPROVAL, new Title("쏘영이의 주류 픽픽픽!"),
                new Title("쏘주가 최고다!"), new PickOptionContents("참이슬을 못참지!"),
                new Title("와인이 최고다!"), new PickOptionContents("레드와인은 못참지!"));

        em.flush();
        em.clear();

        Pageable pageable = PageRequest.of(0, 10);

        // when // then
        ResultActions actions = mockMvc.perform(get("/devdevdev/api/v1/mypage/picks")
                        .queryParam("size", String.valueOf(pageable.getPageSize()))
                        .queryParam("pickId", String.valueOf(Long.MAX_VALUE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header(AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken))
                .andDo(print())
                .andExpect(status().isOk());

        // docs
        actions.andDo(document("mypage-mypick-main",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                        headerWithName(AUTHORIZATION_HEADER).description("Bearer 엑세스 토큰")
                ),
                queryParameters(
                        parameterWithName("pickId").optional().description("픽픽픽 아이디"),
                        parameterWithName("size").optional().description("조회되는 데이터 수")
                ),
                responseFields(
                        fieldWithPath("resultType").type(STRING).description("응답 결과"),
                        fieldWithPath("data").type(OBJECT).description("응답 데이터"),

                        fieldWithPath("data.content").type(ARRAY).description("픽픽픽 메인 배열"),
                        fieldWithPath("data.content[].id").type(NUMBER).description("픽픽픽 아이디"),
                        fieldWithPath("data.content[].title").type(STRING).description("픽픽픽 제목"),
                        fieldWithPath("data.content[].voteTotalCount").type(NUMBER).description("픽픽픽 전체 투표 수"),
                        fieldWithPath("data.content[].commentTotalCount").type(NUMBER).description("픽픽픽 전체 댓글 수"),
                        fieldWithPath("data.content[].viewTotalCount").type(NUMBER).description("픽픽픽 조회 수"),
                        fieldWithPath("data.content[].contentStatus").type(STRING).description("픽픽픽 게시글 상태")
                                .attributes(contentStatusType()),
                        fieldWithPath("data.content[].isVoted").type(BOOLEAN).description("픽픽픽 투표 여부"),

                        fieldWithPath("data.content[].pickOptions").type(ARRAY).description("픽픽픽 옵션 배열"),
                        fieldWithPath("data.content[].pickOptions[].id").type(NUMBER).description("픽픽픽 옵션 아이디"),
                        fieldWithPath("data.content[].pickOptions[].title").type(STRING).description("픽픽픽 옵션 제목"),
                        fieldWithPath("data.content[].pickOptions[].percent").type(NUMBER).description("픽픽픽 옵션 투표율(%)"),
                        fieldWithPath("data.content[].pickOptions[].isPicked").type(BOOLEAN)
                                .description("픽픽픽 옵션 투표 여부"),
                        fieldWithPath("data.content[].pickOptions[].id").type(NUMBER).description("픽픽픽 옵션 아이디"),
                        fieldWithPath("data.content[].pickOptions[].title").type(STRING).description("픽픽픽 옵션 제목"),
                        fieldWithPath("data.content[].pickOptions[].percent").type(NUMBER).description("픽픽픽 옵션 투표율(%)"),
                        fieldWithPath("data.content[].pickOptions[].isPicked").type(BOOLEAN)
                                .description("픽픽픽 옵션 투표 여부"),

                        fieldWithPath("data.pageable").type(OBJECT).description("픽픽픽 메인 페이지네이션 정보"),
                        fieldWithPath("data.pageable.pageNumber").type(NUMBER).description("페이지 번호"),
                        fieldWithPath("data.pageable.pageSize").type(NUMBER).description("페이지 사이즈"),

                        fieldWithPath("data.pageable.sort").type(OBJECT).description("정렬 정보"),
                        fieldWithPath("data.pageable.sort.empty").type(BOOLEAN).description("정렬 정보가 비어있는지 여부"),
                        fieldWithPath("data.pageable.sort.sorted").type(BOOLEAN).description("정렬 여부"),
                        fieldWithPath("data.pageable.sort.unsorted").type(BOOLEAN).description("비정렬 여부"),

                        fieldWithPath("data.pageable.offset").type(NUMBER).description("페이지 오프셋 (페이지 크기 * 페이지 번호)"),
                        fieldWithPath("data.pageable.paged").type(BOOLEAN).description("페이지 정보 포함 여부"),
                        fieldWithPath("data.pageable.unpaged").type(BOOLEAN).description("페이지 정보 비포함 여부"),

                        fieldWithPath("data.first").type(BOOLEAN).description("현재 페이지가 첫 페이지 여부"),
                        fieldWithPath("data.last").type(BOOLEAN).description("현재 페이지가 마지막 페이지 여부"),
                        fieldWithPath("data.size").type(NUMBER).description("페이지 크기"),
                        fieldWithPath("data.number").type(NUMBER).description("현재 페이지"),

                        fieldWithPath("data.sort").type(OBJECT).description("정렬 정보"),
                        fieldWithPath("data.sort.empty").type(BOOLEAN).description("정렬 정보가 비어있는지 여부"),
                        fieldWithPath("data.sort.sorted").type(BOOLEAN).description("정렬 상태 여부"),
                        fieldWithPath("data.sort.unsorted").type(BOOLEAN).description("비정렬 상태 여부"),
                        fieldWithPath("data.numberOfElements").type(NUMBER).description("현재 페이지 데이터 수"),
                        fieldWithPath("data.empty").type(BOOLEAN).description("현재 빈 페이지 여부")
                )
        ));
    }

    @Test
    @DisplayName("회원이 회원 탈퇴 서베이 목록을 조회한다.")
    void getMemberExitSurvey() throws Exception {
        // given
        // 회원 생성
        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        // 서베이 버전 생성
        SurveyVersion surveyVersion1 = SurveyVersion.builder()
                .versionName("회원탈퇴-A")
                .isActive(true)
                .build();
        SurveyVersion surveyVersion2 = SurveyVersion.builder()
                .versionName("회원탈퇴-B")
                .isActive(false)
                .build();
        surveyVersionRepository.saveAll(List.of(surveyVersion1, surveyVersion2));

        // 서베이 질문 생성
        SurveyQuestion question = SurveyQuestion.builder()
                .title("#nickName님 회원 탈퇴하는 이유를 알려주세요.")
                .content("회원 탈퇴하는 이유를 상세하게 알려주세요.")
                .sortOrder(0)
                .build();
        surveyQuestionRepository.save(question);

        // 서베이 매퍼 생성
        SurveyVersionQuestionMapper mapper = SurveyVersionQuestionMapper.builder()
                .surveyVersion(surveyVersion1)
                .surveyQuestion(question)
                .build();
        surveyVersionQuestionMapperRepository.save(mapper);

        // 서베이 질문 옵션 생성
        SurveyQuestionOption option1 = SurveyQuestionOption.builder()
                .title("기타")
                .content("직접 입력해주세요.(10자 이상)")
                .sortOrder(0)
                .build();
        option1.changeSurveyQuestion(question);

        surveyQuestionOptionRepository.save(option1);

        em.flush();
        em.clear();

        // when // then
        ResultActions actions = mockMvc.perform(get("/devdevdev/api/v1/mypage/exit-survey")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header(AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken))
                .andDo(print())
                .andExpect(status().isOk());

        // docs
        actions.andDo(document("mypage-exit-survey",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestHeaders(
                        headerWithName(AUTHORIZATION_HEADER).description("Bearer 엑세스 토큰")
                ),
                responseFields(
                        fieldWithPath("resultType").type(STRING).description("응답 결과"),
                        fieldWithPath("data").type(OBJECT).description("응답 데이터"),
                        fieldWithPath("data.surveyVersionId").type(NUMBER).description("회원탈퇴 서베이 버전 아이디"),
                        fieldWithPath("data.surveyQuestions").type(ARRAY).description("회원탈퇴 서베이 질문 배열"),
                        fieldWithPath("data.surveyQuestions.[].id").type(NUMBER).description("회원탈퇴 서베이 질문 아이디"),
                        fieldWithPath("data.surveyQuestions.[].title").type(STRING).description("회원탈퇴 서베이 제목"),
                        fieldWithPath("data.surveyQuestions.[].content").type(STRING).description("회원탈퇴 서베이 내용"),
                        fieldWithPath("data.surveyQuestions.[].sortOrder").type(NUMBER).description("회원탈퇴 서베이 질문 정렬순서"),
                        fieldWithPath("data.surveyQuestions.[].surveyQuestionOptions").type(ARRAY)
                                .description("회원탈퇴 서베이 질문 답변 배열"),
                        fieldWithPath("data.surveyQuestions.[].surveyQuestionOptions.[].id").type(NUMBER)
                                .description("회원탈퇴 서베이 질문 답변 아이디"),
                        fieldWithPath("data.surveyQuestions.[].surveyQuestionOptions.[].title").type(STRING)
                                .description("회원탈퇴 서베이 질문 답변 제목"),
                        fieldWithPath("data.surveyQuestions.[].surveyQuestionOptions.[].content").type(STRING)
                                .description("회원탈퇴 서베이 질문 답변 내용"),
                        fieldWithPath("data.surveyQuestions.[].surveyQuestionOptions.[].sortOrder").type(NUMBER)
                                .description("회원탈퇴 서베이 질문 답변 정렬순서")
                )
        ));
    }

    private Long pickSetup(Member member, ContentStatus contentStatus, Title pickTitle, Title firstPickOptionTitle,
                           PickOptionContents firstPickOptionContents, Title secondPickOptinTitle,
                           PickOptionContents secondPickOptionContents) {
        // 픽픽픽 생성
        Count voteTotalCount = new Count(1);
        Pick pick = createPick(member, pickTitle, voteTotalCount, new Count(2), new Count(3), contentStatus);
        pickRepository.save(pick);

        // 픽픽픽 옵션 생성
        PickOption firstPickOption = createPickOption(pick, PickOptionType.firstPickOption, firstPickOptionTitle,
                firstPickOptionContents, voteTotalCount);
        PickOption secondPickOption = createPickOption(pick, PickOptionType.secondPickOption, secondPickOptinTitle,
                secondPickOptionContents, new Count(0));
        pickOptionRepository.saveAll(List.of(firstPickOption, secondPickOption));

        // 픽픽픽 투표 생성
        PickVote pickVote = createPickVote(pick, firstPickOption, member);
        pickVoteRepository.save(pickVote);

        return pick.getId();
    }

    private PickVote createPickVote(Pick pick, PickOption firstPickOption, Member member) {
        return PickVote.builder()
                .pick(pick)
                .pickOption(firstPickOption)
                .member(member)
                .build();
    }

    private PickOption createPickOption(Pick pick, PickOptionType pickOptionType, Title title,
                                        PickOptionContents contents, Count voteTotalCount) {
        PickOption pickOption = PickOption.builder()
                .pickOptionType(pickOptionType)
                .title(title)
                .contents(contents)
                .voteTotalCount(voteTotalCount)
                .build();

        pickOption.changePick(pick);

        return pickOption;
    }

    private Pick createPick(Member member, Title title, Count voteTotalCount, Count viewTotalCount,
                            Count commentTotalCount, ContentStatus contentStatus) {
        return Pick.builder()
                .member(member)
                .title(title)
                .voteTotalCount(voteTotalCount)
                .viewTotalCount(viewTotalCount)
                .commentTotalCount(commentTotalCount)
                .contentStatus(contentStatus)
                .build();
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

    private static LocalDate createRandomDate() {
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 3, 10);

        // 시작 날짜와 종료 날짜 사이의 차이 중 랜덤한 일 수 선택
        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate);
        long randomDays = ThreadLocalRandom.current().nextLong(daysBetween + 1);

        return startDate.plusDays(randomDays);
    }

    private static ElasticTechArticle createElasticTechArticle(String title, LocalDate regDate, String contents,
                                                               String techArticleUrl,
                                                               String description, String thumbnailUrl, String author,
                                                               String company,
                                                               Long viewTotalCount, Long recommendTotalCount,
                                                               Long commentTotalCount,
                                                               Long popularScore) {
        return ElasticTechArticle.builder()
                .title(title)
                .regDate(regDate)
                .contents(contents)
                .techArticleUrl(techArticleUrl)
                .description(description)
                .thumbnailUrl(thumbnailUrl)
                .author(author)
                .company(company)
                .viewTotalCount(viewTotalCount)
                .recommendTotalCount(recommendTotalCount)
                .commentTotalCount(commentTotalCount)
                .popularScore(popularScore)
                .build();
    }
}
