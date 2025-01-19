package com.dreamypatisiel.devdevdev.domain.service.techArticle;

import static com.dreamypatisiel.devdevdev.domain.exception.MemberExceptionMessage.INVALID_MEMBER_NOT_FOUND_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.TechArticleExceptionMessage.NOT_FOUND_ELASTIC_ID_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.TechArticleExceptionMessage.NOT_FOUND_ELASTIC_TECH_ARTICLE_MESSAGE;
import static com.dreamypatisiel.devdevdev.domain.exception.TechArticleExceptionMessage.NOT_FOUND_TECH_ARTICLE_MESSAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.dreamypatisiel.devdevdev.domain.entity.*;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Count;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Title;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Url;
import com.dreamypatisiel.devdevdev.domain.entity.enums.Role;
import com.dreamypatisiel.devdevdev.domain.entity.enums.SocialType;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.BookmarkRepository;
import com.dreamypatisiel.devdevdev.domain.repository.member.MemberRepository;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechArticleRecommendRepository;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechArticleRepository;
import com.dreamypatisiel.devdevdev.domain.service.techArticle.techArticle.MemberTechArticleService;
import com.dreamypatisiel.devdevdev.elastic.domain.service.ElasticsearchSupportTest;
import com.dreamypatisiel.devdevdev.exception.MemberException;
import com.dreamypatisiel.devdevdev.exception.NotFoundException;
import com.dreamypatisiel.devdevdev.exception.TechArticleException;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.SocialMemberDto;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.UserPrincipal;
import com.dreamypatisiel.devdevdev.global.utils.AuthenticationMemberUtils;
import com.dreamypatisiel.devdevdev.web.dto.response.techArticle.BookmarkResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.techArticle.TechArticleDetailResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.techArticle.TechArticleMainResponse;
import com.dreamypatisiel.devdevdev.web.dto.response.techArticle.TechArticleRecommendResponse;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;

class MemberTechArticleServiceTest extends ElasticsearchSupportTest {

    @Autowired
    MemberTechArticleService memberTechArticleService;
    @Autowired
    TechArticleRepository techArticleRepository;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    BookmarkRepository bookmarkRepository;
    @Autowired
    TechArticleRecommendRepository techArticleRecommendRepository;
    @Autowired
    EntityManager em;

    String userId = "dreamy5patisiel";
    String name = "꿈빛파티시엘";
    String nickname = "행복한 꿈빛파티시엘";
    String email = "dreamy5patisiel@kakao.com";
    String password = "password";
    String socialType = SocialType.KAKAO.name();
    String role = Role.ROLE_USER.name();

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
                .hasSize(pageable.getPageSize());
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
                    assertThat(article.getIsBookmarked()).isNotNull();
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
        TechArticle techArticle = TechArticle.createTechArticle(new Title("기술블로그 제목"), new Url("https://example.com"),
                new Count(1L),
                new Count(1L),
                new Count(1L),
                new Count(1L), null, company);
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
    @DisplayName("회원이 기술블로그 상세를 조회할 때 엘라스틱ID가 존재하지 않으면 예외가 발생한다.")
    void getTechArticleNotFoundElasticIdException() {
        // given
        TechArticle techArticle = TechArticle.createTechArticle(new Title("기술블로그 제목"), new Url("https://example.com"),
                new Count(1L),
                new Count(1L),
                new Count(1L),
                new Count(1L), null, company);
        TechArticle savedTechArticle = techArticleRepository.save(techArticle);
        Long id = savedTechArticle.getId();

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
                .isInstanceOf(TechArticleException.class)
                .hasMessage(NOT_FOUND_ELASTIC_ID_MESSAGE);
    }

    @Test
    @DisplayName("회원이 기술블로그 상세를 조회할 때 엘라스틱 기술블로그가 존재하지 않으면 예외가 발생한다.")
    void getTechArticleNotFoundElasticTechArticleException() {
        // given
        TechArticle techArticle = TechArticle.createTechArticle(new Title("기술블로그 제목"), new Url("https://example.com"),
                new Count(1L),
                new Count(1L),
                new Count(1L),
                new Count(1L), "elasticId", company);
        TechArticle savedTechArticle = techArticleRepository.save(techArticle);
        Long id = savedTechArticle.getId();

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
                .hasMessage(NOT_FOUND_ELASTIC_TECH_ARTICLE_MESSAGE);
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
        Count popularScore = firstTechArticle.getPopularScore();
        Count recommendTotalCount = firstTechArticle.getRecommendTotalCount();

        TechArticleRecommend techArticleRecommend = TechArticleRecommend.create(member, firstTechArticle);
        techArticleRecommendRepository.save(techArticleRecommend);

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
}