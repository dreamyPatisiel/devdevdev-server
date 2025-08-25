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
import com.dreamypatisiel.devdevdev.domain.service.member.AnonymousMemberService;
import com.dreamypatisiel.devdevdev.domain.service.techArticle.techArticle.GuestTechArticleService;
import com.dreamypatisiel.devdevdev.exception.NotFoundException;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.UserPrincipal;
import com.dreamypatisiel.devdevdev.global.utils.AuthenticationMemberUtils;
import com.dreamypatisiel.devdevdev.web.dto.response.techArticle.TechArticleDetailResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.techArticle.TechArticleMainResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.techArticle.TechArticleRecommendResponse;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static com.dreamypatisiel.devdevdev.domain.exception.TechArticleExceptionMessage.NOT_FOUND_TECH_ARTICLE_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.service.techArticle.techArticle.GuestTechArticleService.INVALID_ANONYMOUS_CAN_NOT_USE_THIS_FUNCTION_MESSAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
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

    String email = "dreamy5patisiel@kakao.com";
    String socialType = SocialType.KAKAO.name();
    String role = Role.ROLE_USER.name();

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
                .hasSize(pageable.getPageSize());
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
                    assertThat(article.getIsBookmarked()).isFalse();
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

        Count popularScore = techArticle.getPopularScore();
        Count recommendTotalCount = techArticle.getRecommendTotalCount();

        AnonymousMember anonymousMember = anonymousMemberService.findOrCreateAnonymousMember(anonymousMemberId);
        TechArticleRecommend techArticleRecommend = TechArticleRecommend.create(anonymousMember, techArticle);
        techArticleRecommendRepository.save(techArticleRecommend);

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
}