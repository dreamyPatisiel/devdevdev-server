package com.dreamypatisiel.devdevdev.domain.repository.techArticle;

import com.dreamypatisiel.devdevdev.domain.entity.*;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Count;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Title;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Url;
import com.dreamypatisiel.devdevdev.domain.repository.BookmarkRepository;
import com.dreamypatisiel.devdevdev.domain.repository.MemberRepository;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.SocialMemberDto;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.UserPrincipal;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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

import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class TechArticleRepositoryTest {

    @Autowired
    TechArticleRepository techArticleRepository;
    @Autowired
    BookmarkRepository bookmarkRepository;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    EntityManager em;

    @Test
    @DisplayName("elasticIds 리스트의 elasticId에 해당하는 기술블로그 엔티티를 순서대로 가져올 수 있다.")
    void findAllByElasticIdIn() {
        // given
        TechArticle techArticle1 = createTechArticle("elasticId1");
        TechArticle techArticle2 = createTechArticle("elasticId2");
        TechArticle techArticle3 = createTechArticle("elasticId3");
        TechArticle techArticle4 = createTechArticle("elasticId4");

        techArticleRepository.saveAll(List.of(techArticle1, techArticle2, techArticle3, techArticle4));

        List<String> elasticIds = List.of("elasticId1", "elasticId3", "elasticId2");

        // when
        List<TechArticle> techArticles = techArticleRepository.findAllByElasticIdIn(elasticIds);

        // then
        assertThat(techArticles).hasSize(3)
                .extracting(TechArticle::getElasticId)
                .containsExactly("elasticId1", "elasticId3", "elasticId2");
    }

    @Test
    @DisplayName("기술블로그 북마크 목록을 북마크 등록시간 내림차순으로 가져올 수 있다.")
    void findBookmarkedByCursorOrderByBookmarkedDesc() {
        // given
        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘", "행복한 꿈빛파티시엘",
                "password", "dreamy5patisiel@kakao.com", SocialType.KAKAO.name(), Role.ROLE_USER.name());
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        TechArticle techArticle1 = createTechArticle(new Count(1L));
        TechArticle techArticle2 = createTechArticle(new Count(1L));
        TechArticle techArticle3 = createTechArticle(new Count(1L));
        techArticleRepository.saveAll(List.of(techArticle1, techArticle2, techArticle3));

        Bookmark bookmark1 = Bookmark.create(member, techArticle1, true);
        Bookmark bookmark2 = Bookmark.create(member, techArticle2, false);
        Bookmark bookmark3 = Bookmark.create(member, techArticle3, true);
        bookmarkRepository.saveAll(List.of(bookmark1, bookmark2, bookmark3));

        Pageable pageable = PageRequest.of(0, 3);

        // when
        Slice<TechArticle> techArticles = techArticleRepository.findBookmarkedByCursor(pageable, null, BookmarkSort.BOOKMARKED, member);

        // then
        assertThat(techArticles).hasSize(2)
                .containsExactly(techArticle3, techArticle1);
    }

    @Test
    @DisplayName("기술블로그 북마크 목록을 게시글 최신순으로 가져올 수 있다.")
    void findBookmarkedByCursorOrderByLatestDesc() {
        // given
        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘", "행복한 꿈빛파티시엘",
                "password", "dreamy5patisiel@kakao.com", SocialType.KAKAO.name(), Role.ROLE_USER.name());
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        TechArticle techArticle1 = createTechArticle(new Count(1L));
        TechArticle techArticle2 = createTechArticle(new Count(1L));
        TechArticle techArticle3 = createTechArticle(new Count(1L));
        techArticleRepository.saveAll(List.of(techArticle1, techArticle2, techArticle3));

        Bookmark bookmark1 = Bookmark.create(member, techArticle1, true);
        Bookmark bookmark2 = Bookmark.create(member, techArticle2, false);
        Bookmark bookmark3 = Bookmark.create(member, techArticle3, true);
        bookmarkRepository.saveAll(List.of(bookmark1, bookmark2, bookmark3));

        Pageable pageable = PageRequest.of(0, 3);

        // when
        Slice<TechArticle> techArticles = techArticleRepository.findBookmarkedByCursor(pageable, null, BookmarkSort.LATEST, member);

        // then
        assertThat(techArticles).hasSize(2)
                .containsExactly(techArticle3, techArticle1);
    }

    @Test
    @DisplayName("기술블로그 북마크 목록을 댓글수 내림차순으로 가져올 수 있다.")
    void findBookmarkedByCursorOrderByCommentDesc() {
        // given
        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘", "행복한 꿈빛파티시엘",
                "password", "dreamy5patisiel@kakao.com", SocialType.KAKAO.name(), Role.ROLE_USER.name());
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        TechArticle techArticle1 = createTechArticle(new Count(3L));
        TechArticle techArticle2 = createTechArticle(new Count(2L));
        TechArticle techArticle3 = createTechArticle(new Count(1L));
        techArticleRepository.saveAll(List.of(techArticle1, techArticle2, techArticle3));

        Bookmark bookmark1 = Bookmark.create(member, techArticle1, true);
        Bookmark bookmark2 = Bookmark.create(member, techArticle2, false);
        Bookmark bookmark3 = Bookmark.create(member, techArticle3, true);
        bookmarkRepository.saveAll(List.of(bookmark1, bookmark2, bookmark3));

        Pageable pageable = PageRequest.of(0, 3);

        // when
        Slice<TechArticle> techArticles = techArticleRepository.findBookmarkedByCursor(pageable, null, BookmarkSort.MOST_COMMENTED, member);

        // then
        assertThat(techArticles).hasSize(2)
                .containsExactly(techArticle1, techArticle3);
    }

    private static TechArticle createTechArticle(String elasticId) {
        return TechArticle.builder()
                .elasticId(elasticId)
                .build();
    }

    private static TechArticle createTechArticle(Count commentTotalCount) {
        return TechArticle.builder()
                .commentTotalCount(commentTotalCount)
                .build();
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
}