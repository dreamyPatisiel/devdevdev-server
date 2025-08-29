package com.dreamypatisiel.devdevdev.domain.service.techArticle;

import static com.dreamypatisiel.devdevdev.domain.exception.MemberExceptionMessage.INVALID_MEMBER_NOT_FOUND_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.TechArticleExceptionMessage.NOT_FOUND_TECH_ARTICLE_MESSAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.dreamypatisiel.devdevdev.domain.entity.*;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.CompanyName;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Count;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Title;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Url;
import com.dreamypatisiel.devdevdev.domain.entity.enums.Role;
import com.dreamypatisiel.devdevdev.domain.entity.enums.SocialType;
import com.dreamypatisiel.devdevdev.domain.repository.CompanyRepository;
import com.dreamypatisiel.devdevdev.domain.repository.member.MemberRepository;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.BookmarkRepository;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechArticleRecommendRepository;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechArticleRepository;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechArticleSort;
import com.dreamypatisiel.devdevdev.domain.service.techArticle.techArticle.MemberTechArticleService;
import com.dreamypatisiel.devdevdev.exception.MemberException;
import com.dreamypatisiel.devdevdev.exception.NotFoundException;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.SocialMemberDto;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.UserPrincipal;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import com.dreamypatisiel.devdevdev.web.dto.response.techArticle.BookmarkResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.techArticle.TechArticleDetailResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.techArticle.TechArticleMainResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.techArticle.TechArticleRecommendResponse;
import jakarta.persistence.EntityManager;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@SpringBootTest
@Transactional
@Testcontainers
class MemberTechArticleServiceTest {
    @Autowired
    MemberTechArticleService memberTechArticleService;
    @Autowired
    TechArticleRepository techArticleRepository;
    @Autowired
    CompanyRepository companyRepository;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    BookmarkRepository bookmarkRepository;
    @Autowired
    TechArticleRecommendRepository techArticleRecommendRepository;
    @Autowired
    EntityManager em;
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

    String userId = "dreamy5patisiel";
    String name = "꿈빛파티시엘";
    String nickname = "행복한 꿈빛파티시엘";
    String email = "dreamy5patisiel@kakao.com";
    String password = "password";
    String socialType = SocialType.KAKAO.name();
    String role = Role.ROLE_USER.name();

    private static final int TEST_ARTICLES_COUNT = 20;
    private static Company testCompany;
    private static List<TechArticle> testTechArticles;
    private static TechArticle firstTechArticle;
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
            firstTechArticle = testTechArticles.get(1);
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
    @DisplayName("회원이 커서 방식으로 기술블로그를 조회하여 응답을 생성한다.")
    void getTechArticles() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // when
        Slice<TechArticleMainResponse> techArticles = memberTechArticleService.getTechArticles(pageable, null, null,
                null, null, null, authentication);

        // then
        assertThat(techArticles)
                .hasSize(pageable.getPageSize())
                .allSatisfy(article -> {
                    // 모든 필드 검증
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
                    assertThat(article.getIsBookmarked()).isNotNull();
                })
                .extracting(TechArticleMainResponse::getRegDate)
                .isSortedAccordingTo(Comparator.reverseOrder()); // 기본 정렬은 최신순
    }


    @Test
    @DisplayName("커서 방식으로 기술블로그 메인을 조회할 때 회원이 없으면 예외가 발생한다.")
    void getTechArticlesNotFoundMemberException() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        UserPrincipal userPrincipal = UserPrincipal.createByEmailAndRoleAndSocialType(email, role, socialType);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // when // then
        assertThatThrownBy(
                () -> memberTechArticleService.getTechArticles(pageable, null, null, null, null, null, authentication))
                .isInstanceOf(MemberException.class)
                .hasMessage(INVALID_MEMBER_NOT_FOUND_MESSAGE);
    }

    @Test
    @DisplayName("회원이 기술블로그 상세를 조회한다.")
    void getTechArticle() {
        // given
        Long id = firstTechArticle.getId();

        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // when
        TechArticleDetailResponse techArticleDetailResponse = memberTechArticleService.getTechArticle(id, null,
                authentication);

        // then
        assertThat(techArticleDetailResponse)
                .isNotNull()
                .isInstanceOf(TechArticleDetailResponse.class)
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
                    assertThat(article.getViewTotalCount()).isNotNull().isGreaterThanOrEqualTo(0L);
                    assertThat(article.getRecommendTotalCount()).isNotNull().isGreaterThanOrEqualTo(0L);
                    assertThat(article.getCommentTotalCount()).isNotNull().isGreaterThanOrEqualTo(0L);
                    assertThat(article.getPopularScore()).isNotNull().isGreaterThanOrEqualTo(0L);
                    assertThat(article.getIsBookmarked()).isNotNull();
                    assertThat(article.getIsRecommended()).isNotNull();
                });
    }

    @Test
    @DisplayName("회원이 기술블로그 상세를 조회할 때 기술블로그를 추천한 이력이 있으면 추천이 true이다.")
    void getTechArticleWithRecommend() {
        // given
        Long id = firstTechArticle.getId();

        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        TechArticleRecommend techArticleRecommend = TechArticleRecommend.create(member, firstTechArticle);
        techArticleRecommendRepository.save(techArticleRecommend);

        // when
        TechArticleDetailResponse techArticleDetailResponse = memberTechArticleService.getTechArticle(id, null,
                authentication);

        // then
        assertThat(techArticleDetailResponse)
                .isNotNull()
                .isInstanceOf(TechArticleDetailResponse.class)
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
                    assertThat(article.getViewTotalCount()).isNotNull().isGreaterThanOrEqualTo(0L);
                    assertThat(article.getRecommendTotalCount()).isNotNull().isGreaterThanOrEqualTo(0L);
                    assertThat(article.getCommentTotalCount()).isNotNull().isGreaterThanOrEqualTo(0L);
                    assertThat(article.getPopularScore()).isNotNull().isGreaterThanOrEqualTo(0L);
                    assertThat(article.getIsBookmarked()).isNotNull();
                    assertThat(article.getIsRecommended()).isTrue();
                });
    }

    @Test
    @DisplayName("회원이 기술블로그 상세를 조회할 때 기술블로그를 추천한 이력이 없으면 추천이 false이다.")
    void getTechArticleWithoutRecommend() {
        // given
        Long id = firstTechArticle.getId();

        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // when
        TechArticleDetailResponse techArticleDetailResponse = memberTechArticleService.getTechArticle(id, null,
                authentication);

        // then
        assertThat(techArticleDetailResponse)
                .isNotNull()
                .isInstanceOf(TechArticleDetailResponse.class)
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
                    assertThat(article.getViewTotalCount()).isNotNull().isGreaterThanOrEqualTo(0L);
                    assertThat(article.getRecommendTotalCount()).isNotNull().isGreaterThanOrEqualTo(0L);
                    assertThat(article.getCommentTotalCount()).isNotNull().isGreaterThanOrEqualTo(0L);
                    assertThat(article.getPopularScore()).isNotNull().isGreaterThanOrEqualTo(0L);
                    assertThat(article.getIsBookmarked()).isNotNull();
                    assertThat(article.getIsRecommended()).isFalse();
                });
    }

    @Test
    @DisplayName("회원이 기술블로그 상세를 조회할 때 조회수가 1 증가한다.")
    void getTechArticleIncrementViewCount() {
        // given
        Long id = firstTechArticle.getId();
        long prevViewTotalCount = firstTechArticle.getViewTotalCount().getCount();
        long prevPopularScore = firstTechArticle.getPopularScore().getCount();

        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // when
        TechArticleDetailResponse techArticleDetailResponse = memberTechArticleService.getTechArticle(id, null,
                authentication);

        // then
        assertThat(techArticleDetailResponse)
                .isNotNull()
                .isInstanceOf(TechArticleDetailResponse.class)
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
                    assertThat(article.getIsBookmarked()).isNotNull();
                    assertThat(article.getIsRecommended()).isNotNull();
                    assertThat(article.getViewTotalCount()).isEqualTo(prevViewTotalCount + 1);
                    assertThat(article.getPopularScore()).isEqualTo(prevPopularScore + 2);
                });
    }

    @Test
    @DisplayName("회원이 기술블로그 상세를 조회할 때 회원이 없으면 예외가 발생한다.")
    void getTechArticleNotFoundMemberException() {
        // given
        Long id = firstTechArticle.getId();

        UserPrincipal userPrincipal = UserPrincipal.createByEmailAndRoleAndSocialType(email, role, socialType);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // when // then
        assertThatThrownBy(() -> memberTechArticleService.getTechArticle(id, null, authentication))
                .isInstanceOf(MemberException.class)
                .hasMessage(INVALID_MEMBER_NOT_FOUND_MESSAGE);
    }

    @Test
    @DisplayName("회원이 기술블로그 상세를 조회할 때 기술블로그가 존재하지 않으면 예외가 발생한다.")
    void getTechArticleNotFoundTechArticleException() {
        // given
        Company company = createCompany("꿈빛 파티시엘", "https://example.com/company.png", "https://example.com",
                "https://example.com");
        companyRepository.save(company);

        TechArticle techArticle = createTechArticle(1, company);
        TechArticle savedTechArticle = techArticleRepository.save(techArticle);
        Long id = savedTechArticle.getId() + 1;

        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // when // then
        assertThatThrownBy(() -> memberTechArticleService.getTechArticle(id, null, authentication))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(NOT_FOUND_TECH_ARTICLE_MESSAGE);
    }

    @Test
    @DisplayName("회원이 북마크 한 적 없는 기술블로그의 북마크를 요청하면 새로운 북마크가 생성된다.")
    void createBookmark() {
        // given
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Long id = firstTechArticle.getId();

        // when
        BookmarkResponse response = memberTechArticleService.updateBookmark(id, authentication);

        // then
        assertThat(response)
                .isNotNull()
                .extracting(techArticleId -> response.techArticleId, updatedStatus -> response.status)
                .containsExactly(id, true);
    }

    @Test
    @DisplayName("회원이 북마크한 적 있는 기술블로그의 북마크의 갱신을 요청하면 북마크 값이 갱신된다.")
    void updateBookmark() {
        // given
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        TechArticle techArticle = firstTechArticle;
        Bookmark bookmark = createBookmark(member, techArticle, true);
        bookmarkRepository.save(bookmark);

        Long id = firstTechArticle.getId();

        // when
        BookmarkResponse response = memberTechArticleService.updateBookmark(id, authentication);

        // then
        assertThat(response)
                .isNotNull()
                .extracting(techArticleId -> response.techArticleId, updatedStatus -> response.status)
                .containsExactly(id, false);
    }

    @Test
    @DisplayName("회원이 기술블로그를 추천하면 새로운 추천이 생성되고 기술블로그의 점수가 변경된다.")
    void createTechArticleRecommend() {
        // given
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Long techArticleId = firstTechArticle.getId();
        Count popularScore = firstTechArticle.getPopularScore();
        Count recommendTotalCount = firstTechArticle.getRecommendTotalCount();

        // when
        TechArticleRecommendResponse techArticleRecommendResponse = memberTechArticleService.updateRecommend(techArticleId, null, authentication);

        // then
        assertThat(techArticleRecommendResponse)
                .isNotNull()
                .satisfies(response -> {
                    assertThat(response.getTechArticleId()).isEqualTo(techArticleId);
                    assertThat(response.getStatus()).isTrue();
                });

        TechArticle techArticle = techArticleRepository.findById(techArticleId).get();
        assertThat(techArticle)
                .satisfies(article -> {
                    assertThat(article.getRecommendTotalCount().getCount()).isEqualTo(recommendTotalCount.getCount() + 1);
                    assertThat(article.getPopularScore().getCount()).isEqualTo(popularScore.getCount() + 4);
                });

        TechArticleRecommend techArticleRecommend = techArticleRecommendRepository.findByTechArticleAndMember(firstTechArticle, member).get();
        assertThat(techArticleRecommend)
                .satisfies(recommend -> {
                    assertThat(recommend.getTechArticle().getId()).isEqualTo(firstTechArticle.getId());
                    assertThat(recommend.getMember()).isEqualTo(member);
                    assertThat(recommend.isRecommended()).isTrue();
                });
    }

    @Test
    @DisplayName("익명 사용자가 기술블로그 추천을 취소하면 상태가 false로 바뀌고 기술블로그의 점수가 변경된다.")
    void cancelTechArticleRecommend() {
        // given
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Long techArticleId = firstTechArticle.getId();
        TechArticleRecommend techArticleRecommend = TechArticleRecommend.create(member, firstTechArticle);
        techArticleRecommendRepository.save(techArticleRecommend);

        Count popularScore = firstTechArticle.getPopularScore();
        Count recommendTotalCount = firstTechArticle.getRecommendTotalCount();

        // when
        TechArticleRecommendResponse techArticleRecommendResponse = memberTechArticleService.updateRecommend(techArticleId, null, authentication);

        // then
        assertThat(techArticleRecommendResponse)
                .isNotNull()
                .satisfies(response -> {
                    assertThat(response.getTechArticleId()).isEqualTo(techArticleId);
                    assertThat(response.getStatus()).isFalse();
                    assertThat(response.getRecommendTotalCount()).isEqualTo(recommendTotalCount.getCount() - 1);
                });

        TechArticle techArticle = techArticleRepository.findById(techArticleId).get();
        assertThat(techArticle)
                .satisfies(article -> {
                    assertThat(article.getRecommendTotalCount().getCount()).isEqualTo(recommendTotalCount.getCount() - 1L);
                    assertThat(article.getPopularScore().getCount()).isEqualTo(popularScore.getCount() - 4L);
                });

        TechArticleRecommend findTechArticleRecommend = techArticleRecommendRepository.findByTechArticleAndMember(firstTechArticle, member).get();
        assertThat(findTechArticleRecommend)
                .satisfies(recommend -> {
                    assertThat(recommend.getTechArticle().getId()).isEqualTo(firstTechArticle.getId());
                    assertThat(recommend.getMember()).isEqualTo(member);
                    assertThat(recommend.isRecommended()).isFalse();
                });
    }

    @ParameterizedTest
    @EnumSource(value = TechArticleSort.class, names = {"LATEST", "MOST_VIEWED", "MOST_COMMENTED", "POPULAR"})
    @DisplayName("회원이 다양한 정렬 기준으로 기술블로그를 조회한다.")
    void getTechArticlesWithDifferentSorts(TechArticleSort sort) {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // when
        Slice<TechArticleMainResponse> techArticles = memberTechArticleService.getTechArticles(
                pageable, null, sort, null, null, null, authentication);

        // then
        assertThat(techArticles).hasSize(pageable.getPageSize());
        
        List<TechArticleMainResponse> articles = techArticles.getContent();
        
        // 모든 응답 객체의 필드 검증
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
            assertThat(article.getIsBookmarked()).isNotNull();
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
    @DisplayName("회원이 커서 방식으로 다음 페이지의 기술블로그를 최신순으로 조회한다.")
    void getTechArticlesWithCursorOrderByLatest() {
        // given
        Pageable prevPageable = PageRequest.of(0, 1);
        Pageable pageable = PageRequest.of(0, 5);
        
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 첫 번째 페이지 조회
        Slice<TechArticleMainResponse> firstPage = memberTechArticleService.getTechArticles(
                prevPageable, null, TechArticleSort.LATEST, null, null, null, authentication);
        
        TechArticleMainResponse cursor = firstPage.getContent().get(0);

        // when
        Slice<TechArticleMainResponse> secondPage = memberTechArticleService.getTechArticles(
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
                    assertThat(article.getIsBookmarked()).isNotNull();
                })
                .extracting(TechArticleMainResponse::getRegDate)
                .isSortedAccordingTo(Comparator.reverseOrder())
                .allMatch(date -> !date.isAfter(cursor.getRegDate()));
    }

    @Test
    @DisplayName("회원이 커서 방식으로 다음 페이지의 기술블로그를 조회순으로 조회한다.")
    void getTechArticlesWithCursorOrderByMostViewed() {
        // given
        Pageable prevPageable = PageRequest.of(0, 1);
        Pageable pageable = PageRequest.of(0, 5);
        
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 첫 번째 페이지 조회
        Slice<TechArticleMainResponse> firstPage = memberTechArticleService.getTechArticles(
                prevPageable, null, TechArticleSort.MOST_VIEWED, null, null, null, authentication);
        
        TechArticleMainResponse cursor = firstPage.getContent().get(0);

        // when
        Slice<TechArticleMainResponse> secondPage = memberTechArticleService.getTechArticles(
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
                    assertThat(article.getIsBookmarked()).isNotNull();
                })
                .extracting(TechArticleMainResponse::getViewTotalCount)
                .isSortedAccordingTo(Comparator.reverseOrder())
                .allMatch(viewCount -> viewCount <= cursor.getViewTotalCount());
    }

    @Test
    @DisplayName("회원이 키워드로 기술블로그를 검색한다.")
    void getTechArticlesWithKeyword() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        String keyword = "내용";
        
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // when
        Slice<TechArticleMainResponse> techArticles = memberTechArticleService.getTechArticles(
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
                    assertThat(article.getIsBookmarked()).isNotNull();
                    boolean containsKeyword = article.getTitle().contains(keyword) ||
                            article.getContents().contains(keyword);
                    assertThat(containsKeyword).isTrue();
                });
    }

    @Test
    @DisplayName("회원이 특정 회사의 기술블로그만 필터링하여 조회한다.")
    void getTechArticlesFilterByCompany() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        
        SocialMemberDto socialMemberDto = createSocialDto(userId, name, nickname, password, email, socialType, role);
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        UserPrincipal userPrincipal = UserPrincipal.createByMember(member);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(new OAuth2AuthenticationToken(userPrincipal, userPrincipal.getAuthorities(),
                userPrincipal.getSocialType().name()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // when
        Slice<TechArticleMainResponse> techArticles = memberTechArticleService.getTechArticles(
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
                    assertThat(article.getIsBookmarked()).isNotNull();
                    assertThat(article.getCompany().getId()).isEqualTo(testCompany.getId());
                })
                .extracting(TechArticleMainResponse::getRegDate)
                .isSortedAccordingTo(Comparator.reverseOrder());
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