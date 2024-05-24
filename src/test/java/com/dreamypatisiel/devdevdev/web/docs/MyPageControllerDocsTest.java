package com.dreamypatisiel.devdevdev.web.docs;

import static com.dreamypatisiel.devdevdev.exception.MemberException.INVALID_MEMBER_NOT_FOUND_MESSAGE;
import static com.dreamypatisiel.devdevdev.global.constant.SecurityConstant.AUTHORIZATION_HEADER;
import static com.dreamypatisiel.devdevdev.global.security.jwt.model.JwtCookieConstant.DEVDEVDEV_LOGIN_STATUS;
import static com.dreamypatisiel.devdevdev.global.security.jwt.model.JwtCookieConstant.DEVDEVDEV_REFRESH_TOKEN;
import static com.dreamypatisiel.devdevdev.web.docs.format.ApiDocsFormatGenerator.authenticationType;
import static com.dreamypatisiel.devdevdev.web.docs.format.ApiDocsFormatGenerator.bookmarkSortType;
import static org.springframework.restdocs.cookies.CookieDocumentation.cookieWithName;
import static org.springframework.restdocs.cookies.CookieDocumentation.responseCookies;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
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
import com.dreamypatisiel.devdevdev.domain.entity.TechArticle;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.CompanyName;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Url;
import com.dreamypatisiel.devdevdev.domain.entity.enums.Role;
import com.dreamypatisiel.devdevdev.domain.entity.enums.SocialType;
import com.dreamypatisiel.devdevdev.domain.repository.BookmarkRepository;
import com.dreamypatisiel.devdevdev.domain.repository.CompanyRepository;
import com.dreamypatisiel.devdevdev.domain.repository.member.MemberRepository;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.BookmarkSort;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechArticleRepository;
import com.dreamypatisiel.devdevdev.elastic.domain.document.ElasticTechArticle;
import com.dreamypatisiel.devdevdev.elastic.domain.repository.ElasticTechArticleRepository;
import com.dreamypatisiel.devdevdev.global.constant.SecurityConstant;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.SocialMemberDto;
import com.dreamypatisiel.devdevdev.web.response.ResultType;
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
                        headerWithName(AUTHORIZATION_HEADER).optional().description("Bearer 엑세스 토큰")
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
                                .type(JsonFieldType.BOOLEAN).description("회원의 북마크 여부(익명 사용자는 필드가 없다)"),
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
                        RestDocumentationRequestBuilders.delete("/devdevdev/api/v1/mypage/delete")
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
                        RestDocumentationRequestBuilders.delete("/devdevdev/api/v1/mypage/delete")
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
