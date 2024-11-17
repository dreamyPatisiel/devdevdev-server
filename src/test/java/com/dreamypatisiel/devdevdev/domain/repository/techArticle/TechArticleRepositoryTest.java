package com.dreamypatisiel.devdevdev.domain.repository.techArticle;

import static org.assertj.core.api.Assertions.assertThat;

import com.dreamypatisiel.devdevdev.domain.entity.Bookmark;
import com.dreamypatisiel.devdevdev.domain.entity.Company;
import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.TechArticle;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.CompanyName;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Count;
import com.dreamypatisiel.devdevdev.domain.entity.enums.Role;
import com.dreamypatisiel.devdevdev.domain.entity.enums.SocialType;
import com.dreamypatisiel.devdevdev.domain.repository.BookmarkRepository;
import com.dreamypatisiel.devdevdev.domain.repository.CompanyRepository;
import com.dreamypatisiel.devdevdev.domain.repository.member.MemberRepository;
import com.dreamypatisiel.devdevdev.global.security.oauth2.model.SocialMemberDto;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.transaction.annotation.Transactional;

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
    CompanyRepository companyRepository;
    @Autowired
    EntityManager em;

    @Test
    @DisplayName("elasticIds 리스트의 elasticId에 해당하는 기술블로그 엔티티를 순서대로 가져올 수 있다.")
    void findAllByElasticIdIn() {
        // given
        Company company = Company.builder().name(new CompanyName("회사")).build();
        companyRepository.save(company);

        TechArticle techArticle1 = createTechArticle(company, "elasticId1");
        TechArticle techArticle2 = createTechArticle(company, "elasticId2");
        TechArticle techArticle3 = createTechArticle(company, "elasticId3");
        TechArticle techArticle4 = createTechArticle(company, "elasticId4");

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
        Company company = Company.builder().name(new CompanyName("회사")).build();
        companyRepository.save(company);

        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘", "행복한 꿈빛파티시엘",
                "password", "dreamy5patisiel@kakao.com", SocialType.KAKAO.name(), Role.ROLE_USER.name());
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        TechArticle techArticle1 = createTechArticle(company, new Count(1L));
        TechArticle techArticle2 = createTechArticle(company, new Count(1L));
        TechArticle techArticle3 = createTechArticle(company, new Count(1L));
        techArticleRepository.saveAll(List.of(techArticle1, techArticle2, techArticle3));

        Bookmark bookmark1 = createBookmark(member, techArticle1, true);
        Bookmark bookmark2 = createBookmark(member, techArticle2, false);
        Bookmark bookmark3 = createBookmark(member, techArticle3, true);
        bookmarkRepository.saveAll(List.of(bookmark1, bookmark2, bookmark3));

        Pageable pageable = PageRequest.of(0, 3);

        // when
        Slice<TechArticle> techArticles = techArticleRepository.findBookmarkedByMemberAndCursor(pageable, null,
                BookmarkSort.BOOKMARKED, member);

        // then
        assertThat(techArticles).hasSize(2)
                .containsExactly(techArticle3, techArticle1);
    }

    @Test
    @DisplayName("기술블로그 북마크 목록을 게시글 최신순으로 가져올 수 있다.")
    void findBookmarkedByCursorOrderByLatestDesc() {
        // given
        Company company = Company.builder().name(new CompanyName("회사")).build();
        companyRepository.save(company);

        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘", "행복한 꿈빛파티시엘",
                "password", "dreamy5patisiel@kakao.com", SocialType.KAKAO.name(), Role.ROLE_USER.name());
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        TechArticle techArticle1 = createTechArticle(company, new Count(1L));
        TechArticle techArticle2 = createTechArticle(company, new Count(1L));
        TechArticle techArticle3 = createTechArticle(company, new Count(1L));
        techArticleRepository.saveAll(List.of(techArticle1, techArticle2, techArticle3));

        Bookmark bookmark1 = createBookmark(member, techArticle1, true);
        Bookmark bookmark2 = createBookmark(member, techArticle2, false);
        Bookmark bookmark3 = createBookmark(member, techArticle3, true);
        bookmarkRepository.saveAll(List.of(bookmark1, bookmark2, bookmark3));

        Pageable pageable = PageRequest.of(0, 3);

        // when
        Slice<TechArticle> techArticles = techArticleRepository.findBookmarkedByMemberAndCursor(pageable, null,
                BookmarkSort.LATEST, member);

        // then
        assertThat(techArticles).hasSize(2)
                .containsExactly(techArticle3, techArticle1);
    }

    @Test
    @DisplayName("기술블로그 북마크 목록을 댓글수 내림차순으로 가져올 수 있다.")
    void findBookmarkedByCursorOrderByCommentDesc() {
        // given
        Company company = Company.builder().name(new CompanyName("회사")).build();
        companyRepository.save(company);

        SocialMemberDto socialMemberDto = createSocialDto("dreamy5patisiel", "꿈빛파티시엘", "행복한 꿈빛파티시엘",
                "password", "dreamy5patisiel@kakao.com", SocialType.KAKAO.name(), Role.ROLE_USER.name());
        Member member = Member.createMemberBy(socialMemberDto);
        memberRepository.save(member);

        TechArticle techArticle1 = createTechArticle(company, new Count(3L));
        TechArticle techArticle2 = createTechArticle(company, new Count(2L));
        TechArticle techArticle3 = createTechArticle(company, new Count(1L));
        techArticleRepository.saveAll(List.of(techArticle1, techArticle2, techArticle3));

        Bookmark bookmark1 = createBookmark(member, techArticle1, true);
        Bookmark bookmark2 = createBookmark(member, techArticle2, false);
        Bookmark bookmark3 = createBookmark(member, techArticle3, true);
        bookmarkRepository.saveAll(List.of(bookmark1, bookmark2, bookmark3));

        Pageable pageable = PageRequest.of(0, 3);

        // when
        Slice<TechArticle> techArticles = techArticleRepository.findBookmarkedByMemberAndCursor(pageable, null,
                BookmarkSort.MOST_COMMENTED, member);

        // then
        assertThat(techArticles).hasSize(2)
                .containsExactly(techArticle1, techArticle3);
    }

    private static TechArticle createTechArticle(Company company, String elasticId) {
        return TechArticle.builder()
                .company(company)
                .elasticId(elasticId)
                .build();
    }

    private static TechArticle createTechArticle(Company company, Count commentTotalCount) {
        return TechArticle.builder()
                .company(company)
                .commentTotalCount(commentTotalCount)
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
}