package com.dreamypatisiel.devdevdev.domain.service.techArticle;

import com.dreamypatisiel.devdevdev.domain.entity.AnonymousMember;
import com.dreamypatisiel.devdevdev.domain.entity.Company;
import com.dreamypatisiel.devdevdev.domain.entity.TechArticle;
import com.dreamypatisiel.devdevdev.domain.entity.TechArticleRecommend;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.CompanyName;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Count;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Title;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Url;
import com.dreamypatisiel.devdevdev.domain.entity.enums.Role;
import com.dreamypatisiel.devdevdev.domain.entity.enums.SocialType;
import com.dreamypatisiel.devdevdev.domain.repository.CompanyRepository;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechArticleRecommendRepository;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechArticleRepository;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechArticleSort;
import com.dreamypatisiel.devdevdev.domain.service.member.AnonymousMemberService;
import com.dreamypatisiel.devdevdev.domain.service.techArticle.techArticle.GuestTechArticleService;
import com.dreamypatisiel.devdevdev.exception.NotFoundException;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.UserPrincipal;
import com.dreamypatisiel.devdevdev.global.utils.AuthenticationMemberUtils;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import com.dreamypatisiel.devdevdev.web.dto.response.techArticle.TechArticleDetailResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.techArticle.TechArticleMainResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.techArticle.TechArticleRecommendResponse;
import jakarta.persistence.EntityManager;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static com.dreamypatisiel.devdevdev.domain.exception.TechArticleExceptionMessage.NOT_FOUND_TECH_ARTICLE_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.service.techArticle.techArticle.GuestTechArticleService.INVALID_ANONYMOUS_CAN_NOT_USE_THIS_FUNCTION_MESSAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
@Testcontainers
class GuestTechArticleServiceTest {
    @Autowired
    GuestTechArticleService guestTechArticleService;
    @Autowired
    TechArticleRepository techArticleRepository;
    @Autowired
    CompanyRepository companyRepository;
    @Autowired
    TechArticleRecommendRepository techArticleRecommendRepository;
    @Autowired
    AnonymousMemberService anonymousMemberService;
    @Autowired
    EntityManager em;
    @Mock
    Authentication authentication;
    @Mock
    SecurityContext securityContext;
    @Autowired
    DataSource dataSource;

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("devdevdev_test")
            .withUsername("test")
            .withPassword("test")
            .withCommand(
                "--character-set-server=utf8mb4", 
                "--collation-server=utf8mb4_general_ci",
                "--ngram_token_size=1"
            );

    String email = "dreamy5patisiel@kakao.com";
    String socialType = SocialType.KAKAO.name();
    String role = Role.ROLE_USER.name();

    private static final int TEST_ARTICLES_COUNT = 20;
    private static Company testCompany;
    private static List<TechArticle> testTechArticles;
    private static boolean indexesCreated = false;

    @BeforeTransaction
    public void initIndexes() throws SQLException {
        if (!indexesCreated) {
            // 인덱스 생성
            createFulltextIndexesWithJDBC();
            indexesCreated = true;

            // 데이터 추가
            testCompany = createCompany("꿈빛 파티시엘", "https://example.com/company.png", 
                                      "https://example.com", "https://example.com");
            companyRepository.save(testCompany);

            testTechArticles = new ArrayList<>();
            for (int i = 0; i < TEST_ARTICLES_COUNT; i++) {
                TechArticle techArticle = createTechArticle(i, testCompany);
                testTechArticles.add(techArticle);
            }
            techArticleRepository.saveAll(testTechArticles);
        }
    }

    /**
     * JDBC를 사용하여 MySQL fulltext 인덱스를 생성
     */
    private void createFulltextIndexesWithJDBC() throws SQLException {
        Connection connection = null;
        try {
            // 현재 테스트 클래스의 컨테이너에 직접 연결
            connection = DriverManager.getConnection(
                mysql.getJdbcUrl(),
                mysql.getUsername(), 
                mysql.getPassword()
            );
            connection.setAutoCommit(false); // 트랜잭션 시작

            try (Statement statement = connection.createStatement()) {
                try {
                    // 기존 인덱스가 있다면 삭제
                    statement.executeUpdate("DROP INDEX idx__ft__title ON tech_article");
                    statement.executeUpdate("DROP INDEX idx__ft__contents ON tech_article");
                    statement.executeUpdate("DROP INDEX idx__ft__title_contents ON tech_article");
                } catch (Exception e) {
                    System.out.println("인덱스 없음 (정상): " + e.getMessage());
                }

                // fulltext 인덱스 생성 (개별 + 복합)
                statement.executeUpdate("CREATE FULLTEXT INDEX idx__ft__title ON tech_article (title) WITH PARSER ngram");
                statement.executeUpdate("CREATE FULLTEXT INDEX idx__ft__contents ON tech_article (contents) WITH PARSER ngram");
                statement.executeUpdate("CREATE FULLTEXT INDEX idx__ft__title_contents ON tech_article (title, contents) WITH PARSER ngram");

                connection.commit(); // 트랜잭션 커밋
            }
        } finally {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        }
    }

    @Test
    @DisplayName("익명 사용자가 커서 방식으로 기술블로그를 조회하여 응답을 생성한다.")
    void getTechArticles() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        when(authentication.getPrincipal()).thenReturn("anonymousUser");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // when
        Slice<TechArticleMainResponse> techArticles = guestTechArticleService.getTechArticles(pageable, null, null,
                null, null, null, authentication);

        // then
        assertThat(techArticles)
                .hasSize(pageable.getPageSize())
                .allSatisfy(article -> {
                    assertThat(article.getId()).isNotNull();
                    assertThat(article.getTitle()).isNotNull().isNotEmpty();
                    assertThat(article.getContents()).isNotNull();
                    assertThat(article.getAuthor()).isNotNull().isNotEmpty();
                    assertThat(article.getCompany()).isNotNull();
                    assertThat(article.getCompany().getId()).isNotNull();
                    assertThat(article.getCompany().getName()).isNotNull().isNotEmpty();
                    assertThat(article.getCompany().getCareerUrl()).isNotNull();
                    assertThat(article.getCompany().getOfficialImageUrl()).isNotNull();
                    assertThat(article.getRegDate()).isNotNull();
                    assertThat(article.getTechArticleUrl()).isNotNull().isNotEmpty();
                    assertThat(article.getThumbnailUrl()).isNotNull();
                    assertThat(article.getViewTotalCount()).isNotNull().isGreaterThanOrEqualTo(0L);
                    assertThat(article.getRecommendTotalCount()).isNotNull().isGreaterThanOrEqualTo(0L);
                    assertThat(article.getCommentTotalCount()).isNotNull().isGreaterThanOrEqualTo(0L);
                    assertThat(article.getPopularScore()).isNotNull().isGreaterThanOrEqualTo(0L);
                    assertThat(article.getIsLogoImage()).isNotNull();
                    assertThat(article.getIsBookmarked()).isNotNull().isFalse();
                })
                .extracting(TechArticleMainResponse::getRegDate)
                .isSortedAccordingTo(Comparator.reverseOrder()); // 기본 정렬은 최신순
    }

    @Test
    @DisplayName("커서 방식으로 익명 사용자 전용 기술블로그 메인을 조회할 때 익명 사용자가 아니면 예외가 발생한다.")
    void getTechArticlesNotAnonymousUserException() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        UserPrincipal userPrincipal = UserPrincipal.createByEmailAndRoleAndSocialType(email, role, socialType);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // when // then
        assertThatThrownBy(
                () -> guestTechArticleService.getTechArticles(pageable, null, null, null, null, null, authentication))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(AuthenticationMemberUtils.INVALID_METHODS_CALL_MESSAGE);
    }

    @Test
    @DisplayName("익명 사용자가 기술블로그 상세를 조회한다. 이때 북마크 값은 false 이다.")
    void getTechArticle() {
        // given
        Company company = createCompany("꿈빛 파티시엘", "https://example.com/company.png", "https://example.com",
                "https://example.com");
        companyRepository.save(company);

        TechArticle techArticle = createTechArticle(company);
        techArticleRepository.save(techArticle);
        Long techArticleId = techArticle.getId();

        when(authentication.getPrincipal()).thenReturn("anonymousUser");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // when
        TechArticleDetailResponse techArticleDetailResponse = guestTechArticleService.getTechArticle(techArticleId, null,
                authentication);

        // then
        assertThat(techArticleDetailResponse)
                .isNotNull()
                .isInstanceOf(TechArticleDetailResponse.class)
                .satisfies(article -> {
                    assertThat(article.getTitle()).isNotNull().isNotEmpty();
                    assertThat(article.getContents()).isNotNull();
                    assertThat(article.getAuthor()).isNotNull().isNotEmpty();
                    assertThat(article.getCompany()).isNotNull();
                    assertThat(article.getCompany().getId()).isNotNull();
                    assertThat(article.getCompany().getName()).isNotNull().isNotEmpty();
                    assertThat(article.getCompany().getCareerUrl()).isNotNull();
                    assertThat(article.getCompany().getOfficialImageUrl()).isNotNull();
                    assertThat(article.getRegDate()).isNotNull();
                    assertThat(article.getTechArticleUrl()).isNotNull().isNotEmpty();
                    assertThat(article.getThumbnailUrl()).isNotNull();
                    assertThat(article.getViewTotalCount()).isNotNull().isGreaterThanOrEqualTo(0L);
                    assertThat(article.getRecommendTotalCount()).isNotNull().isGreaterThanOrEqualTo(0L);
                    assertThat(article.getCommentTotalCount()).isNotNull().isGreaterThanOrEqualTo(0L);
                    assertThat(article.getPopularScore()).isNotNull().isGreaterThanOrEqualTo(0L);
                    assertThat(article.getIsBookmarked()).isFalse();
                    assertThat(article.getIsRecommended()).isNotNull();
                });
    }

    @Test
    @DisplayName("익명 사용자가 본인이 추천했던 기술블로그 상세를 조회한다. 이때 추천 값은 true 이다.")
    void getTechArticleWithRecommend() {
        // given
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(AuthenticationMemberUtils.ANONYMOUS_USER);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        String anonymousMemberId = "GA1.1.276672604.1715872960";

        Company company = createCompany("꿈빛 파티시엘", "https://example.com/company.png", "https://example.com",
                "https://example.com");
        companyRepository.save(company);

        TechArticle techArticle = createTechArticle(company);
        techArticleRepository.save(techArticle);
        Long techArticleId = techArticle.getId();

        AnonymousMember anonymousMember = anonymousMemberService.findOrCreateAnonymousMember(anonymousMemberId);
        TechArticleRecommend techArticleRecommend = TechArticleRecommend.create(anonymousMember, techArticle);
        techArticleRecommendRepository.save(techArticleRecommend);

        em.flush();
        em.clear();

        // when
        TechArticleDetailResponse techArticleDetailResponse = guestTechArticleService.getTechArticle(techArticleId, anonymousMemberId,
                authentication);

        // then
        assertThat(techArticleDetailResponse)
                .isNotNull()
                .isInstanceOf(TechArticleDetailResponse.class)
                .satisfies(article -> {
                    assertThat(article.getTitle()).isNotNull().isNotEmpty();
                    assertThat(article.getContents()).isNotNull();
                    assertThat(article.getAuthor()).isNotNull().isNotEmpty();
                    assertThat(article.getCompany()).isNotNull();
                    assertThat(article.getCompany().getId()).isNotNull();
                    assertThat(article.getCompany().getName()).isNotNull().isNotEmpty();
                    assertThat(article.getCompany().getCareerUrl()).isNotNull();
                    assertThat(article.getCompany().getOfficialImageUrl()).isNotNull();
                    assertThat(article.getRegDate()).isNotNull();
                    assertThat(article.getTechArticleUrl()).isNotNull().isNotEmpty();
                    assertThat(article.getThumbnailUrl()).isNotNull();
                    assertThat(article.getViewTotalCount()).isNotNull().isGreaterThanOrEqualTo(0L);
                    assertThat(article.getRecommendTotalCount()).isNotNull().isGreaterThanOrEqualTo(0L);
                    assertThat(article.getCommentTotalCount()).isNotNull().isGreaterThanOrEqualTo(0L);
                    assertThat(article.getPopularScore()).isNotNull().isGreaterThanOrEqualTo(0L);
                    assertThat(article.getIsBookmarked()).isFalse();
                    assertThat(article.getIsRecommended()).isTrue();
                });
    }

    @Test
    @DisplayName("익명 사용자가 기술블로그 상세를 조회하면 조회수가 1 증가한다.")
    void getTechArticleIncrementViewCount() {
        // given
        Company company = createCompany("꿈빛 파티시엘", "https://example.com/company.png", "https://example.com",
                "https://example.com");
        companyRepository.save(company);

        TechArticle techArticle = createTechArticle(company);
        techArticleRepository.save(techArticle);
        Long techArticleId = techArticle.getId();

        long prevViewTotalCount = techArticle.getViewTotalCount().getCount();
        long prevPopularScore = techArticle.getPopularScore().getCount();

        when(authentication.getPrincipal()).thenReturn("anonymousUser");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // when
        TechArticleDetailResponse techArticleDetailResponse = guestTechArticleService.getTechArticle(techArticleId, null,
                authentication);

        // then
        assertThat(techArticleDetailResponse)
                .satisfies(article -> {
                    // 모든 필드 검증
                    assertThat(article.getTitle()).isNotNull().isNotEmpty();
                    assertThat(article.getContents()).isNotNull();
                    assertThat(article.getAuthor()).isNotNull().isNotEmpty();
                    assertThat(article.getCompany()).isNotNull();
                    assertThat(article.getCompany().getId()).isNotNull();
                    assertThat(article.getCompany().getName()).isNotNull().isNotEmpty();
                    assertThat(article.getCompany().getCareerUrl()).isNotNull();
                    assertThat(article.getCompany().getOfficialImageUrl()).isNotNull();
                    assertThat(article.getRegDate()).isNotNull();
                    assertThat(article.getTechArticleUrl()).isNotNull().isNotEmpty();
                    assertThat(article.getThumbnailUrl()).isNotNull();
                    assertThat(article.getRecommendTotalCount()).isNotNull().isGreaterThanOrEqualTo(0L);
                    assertThat(article.getCommentTotalCount()).isNotNull().isGreaterThanOrEqualTo(0L);
                    assertThat(article.getIsBookmarked()).isFalse();
                    assertThat(article.getIsRecommended()).isNotNull();
                    assertThat(article.getViewTotalCount()).isEqualTo(prevViewTotalCount + 1);
                    assertThat(article.getPopularScore()).isEqualTo(prevPopularScore + 2);
                });
    }

    @Test
    @DisplayName("익명 사용자가 기술블로그 상세를 조회할 때 익명 사용자가 아니면 예외가 발생한다.")
    void getTechArticleNotAnonymousUserException() {
        // given
        Company company = createCompany("꿈빛 파티시엘", "https://example.com/company.png", "https://example.com",
                "https://example.com");
        companyRepository.save(company);

        TechArticle techArticle = createTechArticle(company);
        techArticleRepository.save(techArticle);
        Long techArticleId = techArticle.getId();

        UserPrincipal userPrincipal = UserPrincipal.createByEmailAndRoleAndSocialType(email, role, socialType);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // when // then
        assertThatThrownBy(() -> guestTechArticleService.getTechArticle(techArticleId, null, authentication))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(AuthenticationMemberUtils.INVALID_METHODS_CALL_MESSAGE);
    }

    @Test
    @DisplayName("익명 사용자가 기술블로그 상세를 조회할 때 기술블로그가 존재하지 않으면 예외가 발생한다.")
    void getTechArticleNotFoundTechArticleException() {
        // given
        Company company = createCompany("꿈빛 파티시엘", "https://example.com/company.png", "https://example.com",
                "https://example.com");
        companyRepository.save(company);

        TechArticle techArticle = createTechArticle(company);
        techArticleRepository.save(techArticle);
        Long techArticleId = techArticle.getId();
        Long id = techArticleId + 1;

        when(authentication.getPrincipal()).thenReturn("anonymousUser");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // when // then
        assertThatThrownBy(() -> guestTechArticleService.getTechArticle(id, null, authentication))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(NOT_FOUND_TECH_ARTICLE_MESSAGE);
    }

    @Test
    @DisplayName("익명 사용자가 기술블로그 북마크를 요청하면 예외가 발생한다.")
    void updateBookmarkAccessDeniedException() {
        // given
        when(authentication.getPrincipal()).thenReturn("anonymousUser");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        Company company = createCompany("꿈빛 파티시엘", "https://example.com/company.png", "https://example.com",
                "https://example.com");
        companyRepository.save(company);

        TechArticle techArticle = createTechArticle(company);
        techArticleRepository.save(techArticle);
        Long techArticleId = techArticle.getId();

        // when // then
        assertThatThrownBy(() -> guestTechArticleService.updateBookmark(techArticleId, authentication))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage(INVALID_ANONYMOUS_CAN_NOT_USE_THIS_FUNCTION_MESSAGE);
    }

    @Test
    @DisplayName("익명 사용자가 기술블로그를 추천하면 새로운 추천이 생성되고 기술블로그의 점수가 변경된다.")
    void createTechArticleRecommend() {
        // given
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(AuthenticationMemberUtils.ANONYMOUS_USER);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        String anonymousMemberId = "GA1.1.276672604.1715872960";

        Company company = createCompany("꿈빛 파티시엘", "https://example.com/company.png", "https://example.com",
                "https://example.com");
        companyRepository.save(company);

        TechArticle techArticle = createTechArticle(company);
        techArticleRepository.save(techArticle);
        Long techArticleId = techArticle.getId();

        Count popularScore = techArticle.getPopularScore();
        Count recommendTotalCount = techArticle.getRecommendTotalCount();

        // when
        TechArticleRecommendResponse techArticleRecommendResponse = guestTechArticleService.updateRecommend(techArticleId, anonymousMemberId, authentication);

        // then
        assertThat(techArticleRecommendResponse)
                .isNotNull()
                .satisfies(response -> {
                    assertThat(response.getTechArticleId()).isEqualTo(techArticleId);
                    assertThat(response.getStatus()).isTrue();
                    assertThat(response.getRecommendTotalCount()).isEqualTo(recommendTotalCount.getCount() + 1);
                });

        TechArticle findTechArticle = techArticleRepository.findById(techArticleId).get();
        assertThat(findTechArticle)
                .satisfies(article -> {
                    assertThat(article.getRecommendTotalCount().getCount()).isEqualTo(recommendTotalCount.getCount() + 1);
                    assertThat(article.getPopularScore().getCount()).isEqualTo(popularScore.getCount() + 4);
                });

        AnonymousMember anonymousMember = anonymousMemberService.findOrCreateAnonymousMember(anonymousMemberId);
        TechArticleRecommend techArticleRecommend = techArticleRecommendRepository.findByTechArticleAndAnonymousMember(techArticle, anonymousMember).get();
        assertThat(techArticleRecommend)
                .satisfies(recommend -> {
                    assertThat(recommend.getTechArticle().getId()).isEqualTo(techArticle.getId());
                    assertThat(recommend.getAnonymousMember()).isEqualTo(anonymousMember);
                    assertThat(recommend.isRecommended()).isTrue();
                });
    }

    @Test
    @DisplayName("익명 사용자가 기술블로그 추천을 취소하면 상태가 false로 바뀌고 기술블로그의 점수가 변경된다.")
    void cancelTechArticleRecommend() {
        // given
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(AuthenticationMemberUtils.ANONYMOUS_USER);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        String anonymousMemberId = "GA1.1.276672604.1715872960";

        Company company = createCompany("꿈빛 파티시엘", "https://example.com/company.png", "https://example.com",
                "https://example.com");
        companyRepository.save(company);

        TechArticle techArticle = createTechArticle(company);
        techArticleRepository.save(techArticle);
        Long techArticleId = techArticle.getId();

        AnonymousMember anonymousMember = anonymousMemberService.findOrCreateAnonymousMember(anonymousMemberId);
        TechArticleRecommend techArticleRecommend = TechArticleRecommend.create(anonymousMember, techArticle);
        techArticleRecommendRepository.save(techArticleRecommend);
        
        // 추천 후 상태 저장
        em.flush();
        em.clear();
        
        TechArticle updatedTechArticle = techArticleRepository.findById(techArticleId).get();
        Count popularScore = updatedTechArticle.getPopularScore();
        Count recommendTotalCount = updatedTechArticle.getRecommendTotalCount();

        // when
        TechArticleRecommendResponse techArticleRecommendResponse = guestTechArticleService.updateRecommend(techArticleId, anonymousMemberId, authentication);

        // then
        em.flush();
        em.clear();

        assertThat(techArticleRecommendResponse)
                .isNotNull()
                .satisfies(response -> {
                    assertThat(response.getTechArticleId()).isEqualTo(techArticleId);
                    assertThat(response.getStatus()).isFalse();
                });

        TechArticle findTechArticle = techArticleRepository.findById(techArticleId).get();
        assertThat(findTechArticle)
                .satisfies(article -> {
                    assertThat(article.getRecommendTotalCount().getCount()).isEqualTo(recommendTotalCount.getCount() - 1L);
                    assertThat(article.getPopularScore().getCount()).isEqualTo(popularScore.getCount() - 4L);
                });

        AnonymousMember findAnonymousMember = anonymousMemberService.findOrCreateAnonymousMember(anonymousMemberId);
        TechArticleRecommend findTechArticleRecommend = techArticleRecommendRepository.findByTechArticleAndAnonymousMember(techArticle, findAnonymousMember).get();
        assertThat(findTechArticleRecommend)
                .satisfies(recommend -> {
                    assertThat(recommend.getTechArticle().getId()).isEqualTo(techArticle.getId());
                    assertThat(recommend.getAnonymousMember()).isEqualTo(findAnonymousMember);
                    assertThat(recommend.isRecommended()).isFalse();
                });
    }

    @ParameterizedTest
    @EnumSource(value = TechArticleSort.class, names = {"LATEST", "MOST_VIEWED", "MOST_COMMENTED", "POPULAR"})
    @DisplayName("익명 사용자가 다양한 정렬 기준으로 기술블로그를 조회한다.")
    void getTechArticlesWithDifferentSorts(TechArticleSort sort) {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        
        when(authentication.getPrincipal()).thenReturn("anonymousUser");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // when
        Slice<TechArticleMainResponse> techArticles = guestTechArticleService.getTechArticles(
                pageable, null, sort, null, null, null, authentication);

        // then
        assertThat(techArticles).hasSize(pageable.getPageSize());
        
        List<TechArticleMainResponse> articles = techArticles.getContent();
        
        assertThat(articles).allSatisfy(article -> {
            assertThat(article.getId()).isNotNull();
            assertThat(article.getTitle()).isNotNull().isNotEmpty();
            assertThat(article.getContents()).isNotNull();
            assertThat(article.getAuthor()).isNotNull().isNotEmpty();
            assertThat(article.getCompany()).isNotNull();
            assertThat(article.getCompany().getId()).isNotNull();
            assertThat(article.getCompany().getName()).isNotNull().isNotEmpty();
            assertThat(article.getCompany().getCareerUrl()).isNotNull();
            assertThat(article.getCompany().getOfficialImageUrl()).isNotNull();
            assertThat(article.getRegDate()).isNotNull();
            assertThat(article.getTechArticleUrl()).isNotNull().isNotEmpty();
            assertThat(article.getThumbnailUrl()).isNotNull();
            assertThat(article.getViewTotalCount()).isNotNull().isGreaterThanOrEqualTo(0L);
            assertThat(article.getRecommendTotalCount()).isNotNull().isGreaterThanOrEqualTo(0L);
            assertThat(article.getCommentTotalCount()).isNotNull().isGreaterThanOrEqualTo(0L);
            assertThat(article.getPopularScore()).isNotNull().isGreaterThanOrEqualTo(0L);
            assertThat(article.getIsLogoImage()).isNotNull();
            assertThat(article.getIsBookmarked()).isNotNull().isFalse();
        });
        
        // 정렬 검증
        switch (sort) {
            case LATEST -> assertThat(articles)
                    .extracting(TechArticleMainResponse::getRegDate)
                    .isSortedAccordingTo(Comparator.reverseOrder());
            case MOST_VIEWED -> assertThat(articles)
                    .extracting(TechArticleMainResponse::getViewTotalCount)
                    .isSortedAccordingTo(Comparator.reverseOrder());
            case MOST_COMMENTED -> assertThat(articles)
                    .extracting(TechArticleMainResponse::getCommentTotalCount)
                    .isSortedAccordingTo(Comparator.reverseOrder());
            case POPULAR -> assertThat(articles)
                    .extracting(TechArticleMainResponse::getPopularScore)
                    .isSortedAccordingTo(Comparator.reverseOrder());
        }
    }

    @Test
    @DisplayName("익명 사용자가 커서 방식으로 다음 페이지의 기술블로그를 최신순으로 조회한다.")
    void getTechArticlesWithCursorOrderByLatest() {
        // given
        Pageable prevPageable = PageRequest.of(0, 1);
        Pageable pageable = PageRequest.of(0, 5);
        
        when(authentication.getPrincipal()).thenReturn("anonymousUser");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // 첫 번째 페이지 조회
        Slice<TechArticleMainResponse> firstPage = guestTechArticleService.getTechArticles(
                prevPageable, null, TechArticleSort.LATEST, null, null, null, authentication);
        
        TechArticleMainResponse cursor = firstPage.getContent().get(0);

        // when
        Slice<TechArticleMainResponse> secondPage = guestTechArticleService.getTechArticles(
                pageable, cursor.getId(), TechArticleSort.LATEST, null, null, null, authentication);

        // then
        assertThat(secondPage)
                .hasSize(pageable.getPageSize())
                .allSatisfy(article -> {
                    assertThat(article.getId()).isNotNull();
                    assertThat(article.getTitle()).isNotNull().isNotEmpty();
                    assertThat(article.getContents()).isNotNull();
                    assertThat(article.getAuthor()).isNotNull().isNotEmpty();
                    assertThat(article.getCompany()).isNotNull();
                    assertThat(article.getCompany().getId()).isNotNull();
                    assertThat(article.getCompany().getName()).isNotNull().isNotEmpty();
                    assertThat(article.getCompany().getCareerUrl()).isNotNull();
                    assertThat(article.getCompany().getOfficialImageUrl()).isNotNull();
                    assertThat(article.getRegDate()).isNotNull();
                    assertThat(article.getTechArticleUrl()).isNotNull().isNotEmpty();
                    assertThat(article.getThumbnailUrl()).isNotNull();
                    assertThat(article.getViewTotalCount()).isNotNull().isGreaterThanOrEqualTo(0L);
                    assertThat(article.getRecommendTotalCount()).isNotNull().isGreaterThanOrEqualTo(0L);
                    assertThat(article.getCommentTotalCount()).isNotNull().isGreaterThanOrEqualTo(0L);
                    assertThat(article.getPopularScore()).isNotNull().isGreaterThanOrEqualTo(0L);
                    assertThat(article.getIsLogoImage()).isNotNull();
                    assertThat(article.getIsBookmarked()).isNotNull().isFalse();
                })
                .extracting(TechArticleMainResponse::getRegDate)
                .isSortedAccordingTo(Comparator.reverseOrder())
                .allMatch(date -> !date.isAfter(cursor.getRegDate()));
    }

    @Test
    @DisplayName("익명 사용자가 커서 방식으로 다음 페이지의 기술블로그를 조회순으로 조회한다.")
    void getTechArticlesWithCursorOrderByMostViewed() {
        // given
        Pageable prevPageable = PageRequest.of(0, 1);
        Pageable pageable = PageRequest.of(0, 5);
        
        when(authentication.getPrincipal()).thenReturn("anonymousUser");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // 첫 번째 페이지 조회
        Slice<TechArticleMainResponse> firstPage = guestTechArticleService.getTechArticles(
                prevPageable, null, TechArticleSort.MOST_VIEWED, null, null, null, authentication);
        
        TechArticleMainResponse cursor = firstPage.getContent().get(0);

        // when
        Slice<TechArticleMainResponse> secondPage = guestTechArticleService.getTechArticles(
                pageable, cursor.getId(), TechArticleSort.MOST_VIEWED, null, null, null, authentication);

        // then
        assertThat(secondPage)
                .hasSize(pageable.getPageSize())
                .allSatisfy(article -> {
                    assertThat(article.getId()).isNotNull();
                    assertThat(article.getTitle()).isNotNull().isNotEmpty();
                    assertThat(article.getContents()).isNotNull();
                    assertThat(article.getAuthor()).isNotNull().isNotEmpty();
                    assertThat(article.getCompany()).isNotNull();
                    assertThat(article.getCompany().getId()).isNotNull();
                    assertThat(article.getCompany().getName()).isNotNull().isNotEmpty();
                    assertThat(article.getCompany().getCareerUrl()).isNotNull();
                    assertThat(article.getCompany().getOfficialImageUrl()).isNotNull();
                    assertThat(article.getRegDate()).isNotNull();
                    assertThat(article.getTechArticleUrl()).isNotNull().isNotEmpty();
                    assertThat(article.getThumbnailUrl()).isNotNull();
                    assertThat(article.getViewTotalCount()).isNotNull().isGreaterThanOrEqualTo(0L);
                    assertThat(article.getRecommendTotalCount()).isNotNull().isGreaterThanOrEqualTo(0L);
                    assertThat(article.getCommentTotalCount()).isNotNull().isGreaterThanOrEqualTo(0L);
                    assertThat(article.getPopularScore()).isNotNull().isGreaterThanOrEqualTo(0L);
                    assertThat(article.getIsLogoImage()).isNotNull();
                    assertThat(article.getIsBookmarked()).isNotNull().isFalse();
                })
                .extracting(TechArticleMainResponse::getViewTotalCount)
                .isSortedAccordingTo(Comparator.reverseOrder())
                .allMatch(viewCount -> viewCount <= cursor.getViewTotalCount());
    }

    @Test
    @DisplayName("익명 사용자가 커서 방식으로 다음 페이지의 기술블로그를 댓글순으로 조회한다.")
    void getTechArticlesWithCursorOrderByMostCommented() {
        // given
        Pageable prevPageable = PageRequest.of(0, 1);
        Pageable pageable = PageRequest.of(0, 5);
        
        when(authentication.getPrincipal()).thenReturn("anonymousUser");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // 첫 번째 페이지 조회
        Slice<TechArticleMainResponse> firstPage = guestTechArticleService.getTechArticles(
                prevPageable, null, TechArticleSort.MOST_COMMENTED, null, null, null, authentication);
        
        TechArticleMainResponse cursor = firstPage.getContent().get(0);

        // when
        Slice<TechArticleMainResponse> secondPage = guestTechArticleService.getTechArticles(
                pageable, cursor.getId(), TechArticleSort.MOST_COMMENTED, null, null, null, authentication);

        // then
        assertThat(secondPage)
                .hasSize(pageable.getPageSize())
                .allSatisfy(article -> {
                    assertThat(article.getId()).isNotNull();
                    assertThat(article.getTitle()).isNotNull().isNotEmpty();
                    assertThat(article.getContents()).isNotNull();
                    assertThat(article.getAuthor()).isNotNull().isNotEmpty();
                    assertThat(article.getCompany()).isNotNull();
                    assertThat(article.getCompany().getId()).isNotNull();
                    assertThat(article.getCompany().getName()).isNotNull().isNotEmpty();
                    assertThat(article.getCompany().getCareerUrl()).isNotNull();
                    assertThat(article.getCompany().getOfficialImageUrl()).isNotNull();
                    assertThat(article.getRegDate()).isNotNull();
                    assertThat(article.getTechArticleUrl()).isNotNull().isNotEmpty();
                    assertThat(article.getThumbnailUrl()).isNotNull();
                    assertThat(article.getViewTotalCount()).isNotNull().isGreaterThanOrEqualTo(0L);
                    assertThat(article.getRecommendTotalCount()).isNotNull().isGreaterThanOrEqualTo(0L);
                    assertThat(article.getCommentTotalCount()).isNotNull().isGreaterThanOrEqualTo(0L);
                    assertThat(article.getPopularScore()).isNotNull().isGreaterThanOrEqualTo(0L);
                    assertThat(article.getIsLogoImage()).isNotNull();
                    assertThat(article.getIsBookmarked()).isNotNull().isFalse();
                })
                .extracting(TechArticleMainResponse::getCommentTotalCount)
                .isSortedAccordingTo(Comparator.reverseOrder())
                .allMatch(commentCount -> commentCount <= cursor.getCommentTotalCount());
    }

    @Test
    @DisplayName("익명 사용자가 커서 방식으로 다음 페이지의 기술블로그를 인기순으로 조회한다.")
    void getTechArticlesWithCursorOrderByPopular() {
        // given
        Pageable prevPageable = PageRequest.of(0, 1);
        Pageable pageable = PageRequest.of(0, 5);
        
        when(authentication.getPrincipal()).thenReturn("anonymousUser");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // 첫 번째 페이지 조회
        Slice<TechArticleMainResponse> firstPage = guestTechArticleService.getTechArticles(
                prevPageable, null, TechArticleSort.POPULAR, null, null, null, authentication);
        
        TechArticleMainResponse cursor = firstPage.getContent().get(0);

        // when
        Slice<TechArticleMainResponse> secondPage = guestTechArticleService.getTechArticles(
                pageable, cursor.getId(), TechArticleSort.POPULAR, null, null, null, authentication);

        // then
        assertThat(secondPage)
                .hasSize(pageable.getPageSize())
                .allSatisfy(article -> {
                    assertThat(article.getId()).isNotNull();
                    assertThat(article.getTitle()).isNotNull().isNotEmpty();
                    assertThat(article.getContents()).isNotNull();
                    assertThat(article.getAuthor()).isNotNull().isNotEmpty();
                    assertThat(article.getCompany()).isNotNull();
                    assertThat(article.getCompany().getId()).isNotNull();
                    assertThat(article.getCompany().getName()).isNotNull().isNotEmpty();
                    assertThat(article.getCompany().getCareerUrl()).isNotNull();
                    assertThat(article.getCompany().getOfficialImageUrl()).isNotNull();
                    assertThat(article.getRegDate()).isNotNull();
                    assertThat(article.getTechArticleUrl()).isNotNull().isNotEmpty();
                    assertThat(article.getThumbnailUrl()).isNotNull();
                    assertThat(article.getViewTotalCount()).isNotNull().isGreaterThanOrEqualTo(0L);
                    assertThat(article.getRecommendTotalCount()).isNotNull().isGreaterThanOrEqualTo(0L);
                    assertThat(article.getCommentTotalCount()).isNotNull().isGreaterThanOrEqualTo(0L);
                    assertThat(article.getPopularScore()).isNotNull().isGreaterThanOrEqualTo(0L);
                    assertThat(article.getIsLogoImage()).isNotNull();
                    assertThat(article.getIsBookmarked()).isNotNull().isFalse();
                })
                .extracting(TechArticleMainResponse::getPopularScore)
                .isSortedAccordingTo(Comparator.reverseOrder())
                .allMatch(popularScore -> popularScore <= cursor.getPopularScore());
    }

    @Test
    @DisplayName("익명 사용자가 키워드로 기술블로그를 검색한다.")
    void getTechArticlesWithKeyword() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        String keyword = "내용";
        
        when(authentication.getPrincipal()).thenReturn("anonymousUser");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // when
        Slice<TechArticleMainResponse> techArticles = guestTechArticleService.getTechArticles(
                pageable, null, null, keyword, null, null, authentication);

        // then
        assertThat(techArticles.getContent())
                .isNotEmpty()
                .allSatisfy(article -> {
                    assertThat(article.getId()).isNotNull();
                    assertThat(article.getTitle()).isNotNull().isNotEmpty();
                    assertThat(article.getContents()).isNotNull();
                    assertThat(article.getAuthor()).isNotNull().isNotEmpty();
                    assertThat(article.getCompany()).isNotNull();
                    assertThat(article.getCompany().getId()).isNotNull();
                    assertThat(article.getCompany().getName()).isNotNull().isNotEmpty();
                    assertThat(article.getCompany().getCareerUrl()).isNotNull();
                    assertThat(article.getCompany().getOfficialImageUrl()).isNotNull();
                    assertThat(article.getRegDate()).isNotNull();
                    assertThat(article.getTechArticleUrl()).isNotNull().isNotEmpty();
                    assertThat(article.getThumbnailUrl()).isNotNull();
                    assertThat(article.getViewTotalCount()).isNotNull().isGreaterThanOrEqualTo(0L);
                    assertThat(article.getRecommendTotalCount()).isNotNull().isGreaterThanOrEqualTo(0L);
                    assertThat(article.getCommentTotalCount()).isNotNull().isGreaterThanOrEqualTo(0L);
                    assertThat(article.getPopularScore()).isNotNull().isGreaterThanOrEqualTo(0L);
                    assertThat(article.getIsLogoImage()).isNotNull();
                    assertThat(article.getIsBookmarked()).isNotNull().isFalse();
                    boolean containsKeyword = article.getTitle().contains(keyword) ||
                                            article.getContents().contains(keyword);
                    assertThat(containsKeyword).isTrue();
                });
    }

    @Test
    @DisplayName("익명 사용자가 특정 회사의 기술블로그만 필터링하여 조회한다.")
    void getTechArticlesFilterByCompany() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        
        when(authentication.getPrincipal()).thenReturn("anonymousUser");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // when
        Slice<TechArticleMainResponse> techArticles = guestTechArticleService.getTechArticles(
                pageable, null, TechArticleSort.LATEST, null, testCompany.getId(), null, authentication);

        // then
        assertThat(techArticles.getContent())
                .isNotEmpty()
                .allSatisfy(article -> {
                    assertThat(article.getId()).isNotNull();
                    assertThat(article.getTitle()).isNotNull().isNotEmpty();
                    assertThat(article.getContents()).isNotNull();
                    assertThat(article.getAuthor()).isNotNull().isNotEmpty();
                    assertThat(article.getCompany()).isNotNull();
                    assertThat(article.getCompany().getId()).isNotNull();
                    assertThat(article.getCompany().getName()).isNotNull().isNotEmpty();
                    assertThat(article.getCompany().getCareerUrl()).isNotNull();
                    assertThat(article.getCompany().getOfficialImageUrl()).isNotNull();
                    assertThat(article.getRegDate()).isNotNull();
                    assertThat(article.getTechArticleUrl()).isNotNull().isNotEmpty();
                    assertThat(article.getThumbnailUrl()).isNotNull();
                    assertThat(article.getViewTotalCount()).isNotNull().isGreaterThanOrEqualTo(0L);
                    assertThat(article.getRecommendTotalCount()).isNotNull().isGreaterThanOrEqualTo(0L);
                    assertThat(article.getCommentTotalCount()).isNotNull().isGreaterThanOrEqualTo(0L);
                    assertThat(article.getPopularScore()).isNotNull().isGreaterThanOrEqualTo(0L);
                    assertThat(article.getIsLogoImage()).isNotNull();
                    assertThat(article.getIsBookmarked()).isNotNull().isFalse();
                    assertThat(article.getCompany().getId()).isEqualTo(testCompany.getId());
                })
                .extracting(TechArticleMainResponse::getRegDate)
                .isSortedAccordingTo(Comparator.reverseOrder());
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

    private static TechArticle createTechArticle(Company company) {
        return TechArticle.builder()
                .title(new Title("타이틀 "))
                .contents("내용 ")
                .company(company)
                .author("작성자")
                .regDate(LocalDate.now())
                .techArticleUrl(new Url("https://example.com/article"))
                .thumbnailUrl(new Url("https://example.com/images/thumbnail.png"))
                .commentTotalCount(new Count(1))
                .recommendTotalCount(new Count(1))
                .viewTotalCount(new Count(1))
                .popularScore(new Count(10))
                .build();
    }

    private static TechArticle createTechArticle(int i, Company company) {
        return TechArticle.builder()
                .title(new Title("타이틀 " + i))
                .contents("내용 " + i)
                .company(company)
                .author("작성자")
                .regDate(LocalDate.now().minusDays(i))
                .techArticleUrl(new Url("https://example.com/article"))
                .thumbnailUrl(new Url("https://example.com/images/thumbnail.png"))
                .commentTotalCount(new Count(i))
                .recommendTotalCount(new Count(i))
                .viewTotalCount(new Count(i))
                .popularScore(new Count(10L *i))
                .build();
    }
}