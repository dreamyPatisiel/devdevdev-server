package com.dreamypatisiel.devdevdev.domain.repository.techArticle.custom;

import com.dreamypatisiel.devdevdev.domain.entity.*;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.BookmarkSort;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.JPQLQueryFactory;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.dreamypatisiel.devdevdev.domain.entity.QBookmark.bookmark;
import static com.dreamypatisiel.devdevdev.domain.entity.QTechArticle.techArticle;

@RequiredArgsConstructor
public class TechArticleRepositoryImpl implements TechArticleRepositoryCustom {

    private final JPQLQueryFactory query;

    @Override
    public List<TechArticle> findAllByElasticIdIn(List<String> elasticIds) {

        List<TechArticle> findTechArticles = query.selectFrom(techArticle)
                .where(techArticle.elasticId.in(elasticIds))
                .fetch();

        // elasticId 목록의 순서를 기반으로 결과 목록 재정렬(h2 database에서는 order by Field() 쿼리를 지원하지 않으므로 재정렬 필요)
        Map<String, TechArticle> techArticles = findTechArticles.stream()
                .collect(Collectors.toMap(TechArticle::getElasticId, Function.identity()));

        return elasticIds.stream()
                .map(techArticles::get)
                .collect(Collectors.toList());
    }

    @Override
    public Slice<TechArticle> findBookmarkedByCursor(Pageable pageable, Long techArticleId, BookmarkSort bookmarkSort, Member member) {

        List<TechArticle> contents = query.selectFrom(techArticle)
                .innerJoin(bookmark)
                .on(techArticle.eq(bookmark.techArticle))
                .where(bookmark.member.eq(member), bookmark.status.isTrue(),
                        getCursorCondition(bookmarkSort, techArticleId))
                .orderBy(bookmarkSort(bookmarkSort), techArticle.id.desc())
                .limit(pageable.getPageSize())
                .fetch();

        return new SliceImpl<>(contents, pageable, hasNextPage(contents, pageable.getPageSize()));
    }

    private Predicate getCursorCondition(BookmarkSort bookmarkSort, Long techArticleId) {
        // techArticleId로 북마크, 기술블로그 조회
        Bookmark findBookmark = query.selectFrom(bookmark)
                .where(bookmark.techArticle.id.eq(techArticleId))
                .fetchOne();

        // 일치하는 북마크 없다면
        if(ObjectUtils.isEmpty(findBookmark)) {
            return techArticle.id.loe(techArticleId);
        }

        TechArticle findTechArticle = findBookmark.getTechArticle();

        if(ObjectUtils.isEmpty(findTechArticle)) {
            return techArticle.id.loe(techArticleId);
        }

        return Optional.ofNullable(bookmarkSort)
                .orElse(BookmarkSort.BOOKMARKED).getCursorCondition(findBookmark, findTechArticle);
    }

    private OrderSpecifier<?> bookmarkSort(BookmarkSort bookmarkSort) {
        return Optional.ofNullable(bookmarkSort)
                .orElse(BookmarkSort.BOOKMARKED).getOrderSpecifierByBookmarkSort();

    }

    private boolean hasNextPage(List<TechArticle> contents, int pageSize) {
        return contents.size() > pageSize;
    }
}
