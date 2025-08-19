package com.dreamypatisiel.devdevdev.domain.repository.techArticle.custom;

import static com.dreamypatisiel.devdevdev.domain.entity.QBookmark.bookmark;
import static com.dreamypatisiel.devdevdev.domain.entity.QTechArticle.techArticle;

import com.dreamypatisiel.devdevdev.domain.entity.Bookmark;
import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.TechArticle;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.BookmarkSort;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechArticleSort;
import com.dreamypatisiel.devdevdev.web.dto.SliceCustom;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.JPQLQueryFactory;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
public class TechArticleRepositoryImpl implements TechArticleRepositoryCustom {

    private final JPQLQueryFactory query;

    @Override
    public Slice<TechArticle> findBookmarkedByMemberAndCursor(Pageable pageable, Long techArticleId,
                                                              BookmarkSort bookmarkSort, Member member
    ) {
        List<TechArticle> contents = query.selectFrom(techArticle)
                .innerJoin(bookmark)
                .on(techArticle.eq(bookmark.techArticle))
                .where(bookmark.member.eq(member), bookmark.status.isTrue(),
                        getCursorConditionFromBookmarkSort(bookmarkSort, techArticleId, member))
                .orderBy(bookmarkSort(bookmarkSort), techArticle.id.desc())
                .limit(pageable.getPageSize())
                .fetch();

        return new SliceImpl<>(contents, pageable, hasNextPage(contents, pageable.getPageSize()));
    }

    @Override
    public SliceCustom<TechArticle> findTechArticlesByCursor(Pageable pageable, Long techArticleId,
                                                             TechArticleSort techArticleSort, Long companyId,
                                                             String keyword, Float score
    ) {
        // 키워드가 있는 경우 FULLTEXT 검색, 없는 경우 일반 조회
        if (StringUtils.hasText(keyword)) {
            return findTechArticlesByCursorWithKeyword(pageable, techArticleId, techArticleSort, companyId, keyword, score);
        } else {
            return findTechArticlesByCursorWithoutKeyword(pageable, techArticleId, techArticleSort, companyId);
        }
    }

    // 키워드 검색
    private SliceCustom<TechArticle> findTechArticlesByCursorWithKeyword(Pageable pageable, Long techArticleId,
                                                                         TechArticleSort techArticleSort, Long companyId,
                                                                         String keyword, Float score
    ) {
        List<TechArticle> contents = null;

        // 기술블로그 총 갯수
        long totalElements = query.select(techArticle.count())
                .from(techArticle)
                .fetchCount();

        return new SliceCustom<>(contents, pageable, totalElements);
    }

    // 일반 조회
    private SliceCustom<TechArticle> findTechArticlesByCursorWithoutKeyword(Pageable pageable, Long techArticleId,
                                                                            TechArticleSort techArticleSort, Long companyId
    ) {
        List<TechArticle> contents = query.selectFrom(techArticle)
                .where(getCursorConditionFromTechArticleSort(techArticleSort, techArticleId))
                .where(companyId != null ? techArticle.company.id.eq(companyId) : null)
                .orderBy(techArticleSort(techArticleSort), techArticle.id.desc())
                .limit(pageable.getPageSize())
                .fetch();

        // 기술블로그 총 갯수
        long totalElements = query.select(techArticle.count())
                .from(techArticle)
                .where(companyId != null ? techArticle.company.id.eq(companyId) : null)
                .fetchCount();

        return new SliceCustom<>(contents, pageable, totalElements);
    }

    private Predicate getCursorConditionFromBookmarkSort(BookmarkSort bookmarkSort, Long techArticleId, Member member) {
        if (ObjectUtils.isEmpty(techArticleId)) {
            return null;
        }

        // techArticleId로 북마크, 기술블로그 조회
        Bookmark findBookmark = query.selectFrom(bookmark)
                .where(bookmark.techArticle.id.eq(techArticleId), bookmark.member.eq(member))
                .fetchOne();

        // 일치하는 북마크 없다면
        if (ObjectUtils.isEmpty(findBookmark)) {
            return techArticle.id.loe(techArticleId);
        }

        TechArticle findTechArticle = findBookmark.getTechArticle();

        if (ObjectUtils.isEmpty(findTechArticle)) {
            return techArticle.id.loe(techArticleId);
        }

        return Optional.ofNullable(bookmarkSort)
                .orElse(BookmarkSort.BOOKMARKED).getCursorCondition(findBookmark, findTechArticle);
    }

    private OrderSpecifier<?> bookmarkSort(BookmarkSort bookmarkSort) {
        return Optional.ofNullable(bookmarkSort)
                .orElse(BookmarkSort.BOOKMARKED).getOrderSpecifierByBookmarkSort();

    }

    private Predicate getCursorConditionFromTechArticleSort(TechArticleSort techArticleSort, Long techArticleId) {
        if (ObjectUtils.isEmpty(techArticleId)) {
            return null;
        }

        // techArticleId로 기술블로그 조회
        TechArticle findTechArticle = query.selectFrom(techArticle)
                .where(techArticle.id.eq(techArticleId))
                .fetchOne();

        // 일치하는 기술블로그가 없으면
        if (ObjectUtils.isEmpty(findTechArticle)) {
            return techArticle.id.loe(techArticleId);
        }

        return Optional.ofNullable(techArticleSort)
                .orElse(TechArticleSort.LATEST).getCursorCondition(findTechArticle);
    }

    private OrderSpecifier<?> techArticleSort(TechArticleSort techArticleSort) {
        return Optional.ofNullable(techArticleSort)
                .orElse(TechArticleSort.LATEST).getOrderSpecifierByTechArticleSort();

    }

    private boolean hasNextPage(List<TechArticle> contents, int pageSize) {
        return contents.size() >= pageSize;
    }
}
