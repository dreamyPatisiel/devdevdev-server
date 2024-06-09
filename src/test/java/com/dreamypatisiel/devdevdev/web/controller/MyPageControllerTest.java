package com.dreamypatisiel.devdevdev.web.controller;

import static com.dreamypatisiel.devdevdev.domain.exception.MemberExceptionMessage.INVALID_MEMBER_NOT_FOUND_MESSAGE;
import static com.dreamypatisiel.devdevdev.global.constant.SecurityConstant.AUTHORIZATION_HEADER;
import static com.dreamypatisiel.devdevdev.global.security.jwt.model.JwtCookieConstant.DEVDEVDEV_LOGIN_STATUS;
import static com.dreamypatisiel.devdevdev.global.security.jwt.model.JwtCookieConstant.DEVDEVDEV_REFRESH_TOKEN;
import static com.dreamypatisiel.devdevdev.web.response.ResultType.SUCCESS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
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
import com.dreamypatisiel.devdevdev.global.common.TimeProvider;
import com.dreamypatisiel.devdevdev.global.constant.SecurityConstant;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.SocialMemberDto;
import com.dreamypatisiel.devdevdev.global.utils.CookieUtils;
import com.dreamypatisiel.devdevdev.web.controller.request.RecordMemberExitSurveyAnswerRequest;
import com.dreamypatisiel.devdevdev.web.controller.request.RecordMemberExitSurveyQuestionOptionsRequest;
import com.dreamypatisiel.devdevdev.web.response.ResultType;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.Cookie;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.ResultActions;

class MyPageControllerTest extends SupportControllerTest {

    private static Long FIRST_TECH_ARTICLE_ID;

    @Autowired
    TechArticleRepository techArticleRepository;
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
    TimeProvider timeProvider;
    @Autowired
    EntityManager em;

    private static List<TechArticle> techArticles;

    @BeforeAll
    static void setup(@Autowired TechArticleRepository techArticleRepository,
                      @Autowired CompanyRepository companyRepository,
                      @Autowired ElasticTechArticleRepository elasticTechArticleRepository) {

        List<ElasticTechArticle> elasticTechArticles = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            ElasticTechArticle elasticTechArticle = createElasticTechArticle("타이틀" + i, createRandomDate(), "내용",
                    "http://example.com/" + i, "설명", "http://example.com/", "작성자", "DP", (long) i, (long) i, (long) i,
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
        ResultActions actions = mockMvc.perform(
                        get(DEFAULT_PATH_V1 + "/mypage/bookmarks")
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
        ResultActions actions = mockMvc.perform(
                        get(DEFAULT_PATH_V1 + "/mypage/bookmarks")
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
        ResultActions actions = mockMvc.perform(
                        get(DEFAULT_PATH_V1 + "/mypage/bookmarks")
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

    @Test
    @DisplayName("회원탈퇴를 하면 회원 정보가 비활성화되고"
            + " 리프레시 토큰 쿠키를 초기화 하고"
            + " 로그인 활성화 유무 쿠키를 비활성화 한다.")
    void deleteMember() throws Exception {
        // given
        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        member.updateRefreshToken(refreshToken);
        memberRepository.save(member);

        Cookie cookie = new Cookie(DEVDEVDEV_REFRESH_TOKEN, refreshToken);

        // when
        ResultActions actions = mockMvc.perform(
                        delete(DEFAULT_PATH_V1 + "/mypage/profile")
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding(StandardCharsets.UTF_8)
                                .header(AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultType").value(ResultType.SUCCESS.name()));

        // then
        MockHttpServletResponse response = actions.andReturn().getResponse();
        Cookie responseLoginStatusCookie = response.getCookie(DEVDEVDEV_LOGIN_STATUS);
        Cookie responseRefreshCookie = response.getCookie(DEVDEVDEV_REFRESH_TOKEN);
        assertThat(responseLoginStatusCookie).isNotNull();
        assertAll(
                () -> assertThat(responseRefreshCookie.getValue()).isEqualTo(CookieUtils.BLANK),
                () -> assertThat(responseLoginStatusCookie.getValue()).isEqualTo(CookieUtils.INACTIVE),
                () -> assertThat(responseLoginStatusCookie.getPath()).isEqualTo(CookieUtils.DEFAULT_PATH),
                () -> assertThat(responseLoginStatusCookie.getMaxAge()).isEqualTo(CookieUtils.DEFAULT_MAX_AGE),
                () -> assertThat(responseLoginStatusCookie.getDomain()).isEqualTo(CookieUtils.DEVDEVDEV_DOMAIN)
        );

        em.flush();
        em.clear();

        // 회원 정보가 비활성화됨에 따라 조회 불가
        Optional<Member> findMember = memberRepository.findMemberByEmailAndSocialTypeAndIsDeletedIsFalse(
                member.getEmail(), member.getSocialType());
        assertThat(findMember.isEmpty()).isEqualTo(true);
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
        ResultActions actions = mockMvc.perform(delete("/devdevdev/api/v1/mypage/profile")
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

        Pageable pageable = PageRequest.of(0, 10);

        // when // then
        mockMvc.perform(get("/devdevdev/api/v1/mypage/picks")
                        .queryParam("size", String.valueOf(pageable.getPageSize()))
                        .queryParam("pickId", String.valueOf(Long.MAX_VALUE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header(SecurityConstant.AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultType").value(SUCCESS.name()))
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.[0].id").isNumber())
                .andExpect(jsonPath("$.data.content.[0].title").isString())
                .andExpect(jsonPath("$.data.content.[0].voteTotalCount").isNumber())
                .andExpect(jsonPath("$.data.content.[0].commentTotalCount").isNumber())
                .andExpect(jsonPath("$.data.content.[0].viewTotalCount").isNumber())
                .andExpect(jsonPath("$.data.content.[0].contentStatus").isString())
                .andExpect(jsonPath("$.data.content.[0].isVoted").isBoolean())
                .andExpect(jsonPath("$.data.content.[0].pickOptions").isArray())
                .andExpect(jsonPath("$.data.content.[0].pickOptions.[0].id").isNumber())
                .andExpect(jsonPath("$.data.content.[0].pickOptions.[0].title").isString())
                .andExpect(jsonPath("$.data.content.[0].pickOptions.[0].percent").isNumber())
                .andExpect(jsonPath("$.data.content.[0].pickOptions.[0].isPicked").isBoolean())
                .andExpect(jsonPath("$.data.content.[0].pickOptions.[1].id").isNumber())
                .andExpect(jsonPath("$.data.content.[0].pickOptions.[1].title").isString())
                .andExpect(jsonPath("$.data.content.[0].pickOptions.[1].percent").isNumber())
                .andExpect(jsonPath("$.data.content.[0].pickOptions.[1].isPicked").isBoolean())
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
                .title("채용정보가 부족해요.")
                .sortOrder(0)
                .build();
        option1.changeSurveyQuestion(question);

        surveyQuestionOptionRepository.save(option1);

        em.flush();
        em.clear();

        // when // then
        mockMvc.perform(get("/devdevdev/api/v1/mypage/exit-survey")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header(SecurityConstant.AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultType").value(SUCCESS.name()))
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andExpect(jsonPath("$.data.surveyVersionId").isNumber())
                .andExpect(jsonPath("$.data.surveyQuestions").isArray())
                .andExpect(jsonPath("$.data.surveyQuestions.[0].id").isNumber())
                .andExpect(jsonPath("$.data.surveyQuestions.[0].title").isString())
                .andExpect(jsonPath("$.data.surveyQuestions.[0].content").isString())
                .andExpect(jsonPath("$.data.surveyQuestions.[0].sortOrder").isNumber())
                .andExpect(jsonPath("$.data.surveyQuestions.[0].surveyQuestionOptions").isArray())
                .andExpect(jsonPath("$.data.surveyQuestions.[0].surveyQuestionOptions.[0].id").isNumber())
                .andExpect(jsonPath("$.data.surveyQuestions.[0].surveyQuestionOptions.[0].title").isString())
                .andExpect(jsonPath("$.data.surveyQuestions.[0].surveyQuestionOptions.[0].content").isEmpty())
                .andExpect(jsonPath("$.data.surveyQuestions.[0].surveyQuestionOptions.[0].sortOrder").isNumber());
    }

    @Test
    @DisplayName("회원의 회원탈퇴 서베이 이력을 저장한다.")
    void recordMemberExitSurvey() throws Exception {
        // given
        // 회원 생성
        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        // 서베이 질문 생성
        SurveyQuestion question = SurveyQuestion.builder()
                .title("#nickName님 회원 탈퇴하는 이유를 알려주세요.")
                .content("회원 탈퇴하는 이유를 상세하게 알려주세요.")
                .sortOrder(0)
                .build();
        surveyQuestionRepository.save(question);

        // 서베이 질문 옵션 생성
        SurveyQuestionOption option1 = SurveyQuestionOption.builder()
                .title("채용정보가 부족해요.")
                .sortOrder(0)
                .build();
        option1.changeSurveyQuestion(question);

        SurveyQuestionOption option2 = SurveyQuestionOption.builder()
                .title("기타")
                .content("10자 이상 입력해주세요.")
                .sortOrder(0)
                .build();
        option2.changeSurveyQuestion(question);
        surveyQuestionOptionRepository.saveAll(List.of(option1, option2));

        // 요청 생성
        RecordMemberExitSurveyQuestionOptionsRequest memberExitSurveyQuestionOptions1 = RecordMemberExitSurveyQuestionOptionsRequest.builder()
                .id(option1.getId())
                .build();

        RecordMemberExitSurveyQuestionOptionsRequest memberExitSurveyQuestionOptions2 = RecordMemberExitSurveyQuestionOptionsRequest.builder()
                .id(option2.getId())
                .message("i think so.. this service is...")
                .build();

        RecordMemberExitSurveyAnswerRequest request = RecordMemberExitSurveyAnswerRequest.builder()
                .questionId(question.getId())
                .memberExitSurveyQuestionOptions(
                        List.of(memberExitSurveyQuestionOptions1, memberExitSurveyQuestionOptions2))
                .build();

        // when // then
        mockMvc.perform(post("/devdevdev/api/v1/mypage/exit-survey")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header(SecurityConstant.AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                        .content(om.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultType").value(SUCCESS.name()));
    }

    @Test
    @DisplayName("회원의 회원탈퇴 서베이 이력을 저장할 때 qestionId가 null 이면 예외가 발생한다.")
    void recordMemberExitSurveyQuestionIdNotNullException() throws Exception {
        // given
        // 회원 생성
        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        // 요청 생성
        RecordMemberExitSurveyQuestionOptionsRequest memberExitSurveyQuestionOptions1 = RecordMemberExitSurveyQuestionOptionsRequest.builder()
                .id(1L)
                .build();

        RecordMemberExitSurveyQuestionOptionsRequest memberExitSurveyQuestionOptions2 = RecordMemberExitSurveyQuestionOptionsRequest.builder()
                .id(2L)
                .message("i think so.. this service is...")
                .build();

        RecordMemberExitSurveyAnswerRequest request = RecordMemberExitSurveyAnswerRequest.builder()
                .questionId(null)
                .memberExitSurveyQuestionOptions(
                        List.of(memberExitSurveyQuestionOptions1, memberExitSurveyQuestionOptions2))
                .build();

        // when // then
        mockMvc.perform(post("/devdevdev/api/v1/mypage/exit-survey")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header(SecurityConstant.AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                        .content(om.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultType").value(ResultType.FAIL.name()))
                .andExpect(jsonPath("$.message").isString())
                .andExpect(jsonPath("$.errorCode").value(HttpStatus.BAD_REQUEST.value()));
    }

    @Test
    @DisplayName("회원의 회원탈퇴 서베이 이력을 저장할 때 optionId가 null 이면 예외가 발생한다.")
    void recordMemberExitSurveyOptionIdNotNullException() throws Exception {
        // given
        // 회원 생성
        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘",
                "꿈빛파티시엘", "1234", email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        // 요청 생성
        RecordMemberExitSurveyQuestionOptionsRequest memberExitSurveyQuestionOptions1 = RecordMemberExitSurveyQuestionOptionsRequest.builder()
                .id(null)
                .build();

        RecordMemberExitSurveyQuestionOptionsRequest memberExitSurveyQuestionOptions2 = RecordMemberExitSurveyQuestionOptionsRequest.builder()
                .id(2L)
                .message("i think so.. this service is...")
                .build();

        RecordMemberExitSurveyAnswerRequest request = RecordMemberExitSurveyAnswerRequest.builder()
                .questionId(1L)
                .memberExitSurveyQuestionOptions(
                        List.of(memberExitSurveyQuestionOptions1, memberExitSurveyQuestionOptions2))
                .build();

        // when // then
        mockMvc.perform(post("/devdevdev/api/v1/mypage/exit-survey")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header(SecurityConstant.AUTHORIZATION_HEADER, SecurityConstant.BEARER_PREFIX + accessToken)
                        .content(om.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultType").value(ResultType.FAIL.name()))
                .andExpect(jsonPath("$.message").isString())
                .andExpect(jsonPath("$.errorCode").value(HttpStatus.BAD_REQUEST.value()));
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