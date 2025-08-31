package com.dreamypatisiel.devdevdev.domain.repository.techArticle.custom;

import static com.dreamypatisiel.devdevdev.domain.entity.QBookmark.bookmark;
import static com.dreamypatisiel.devdevdev.domain.entity.QTechArticle.techArticle;

import com.dreamypatisiel.devdevdev.domain.entity.Bookmark;
import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.TechArticle;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.BookmarkSort;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechArticleSort;
import com.dreamypatisiel.devdevdev.web.dto.SliceCustom;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechArticleDto;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberTemplate;
import com.querydsl.jpa.JPQLQueryFactory;
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

    public static final String MATCH_AGAINST_FUNCTION = "match_against";
    public static final String MATCH_AGAINST_NL_FUNCTION = "match_against_nl";

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
    public SliceCustom<TechArticleDto> findTechArticlesByCursor(Pageable pageable, Long techArticleId,
                                                             TechArticleSort techArticleSort, Long companyId,
                                                             String keyword, Double score
    ) {
        // 키워드가 있는 경우 FULLTEXT 검색, 없는 경우 일반 조회
        if (StringUtils.hasText(keyword)) {
            return findTechArticlesByCursorWithKeyword(pageable, techArticleId, techArticleSort, companyId, keyword, score);
        }
        return findTechArticlesByCursorWithoutKeyword(pageable, techArticleId, techArticleSort, companyId);
    }

    // 키워드 검색
    public SliceCustom<TechArticleDto> findTechArticlesByCursorWithKeyword(Pageable pageable, Long techArticleId,
                                                                         TechArticleSort techArticleSort, Long companyId,
                                                                         String keyword, Double score
    ) {
        // FULLTEXT 검색 조건 생성
        BooleanExpression titleMatch = Expressions.booleanTemplate(
                "function('" + MATCH_AGAINST_FUNCTION + "', {0}, {1}) > 0.0",
                techArticle.title.title, keyword
        );
        
        BooleanExpression contentsMatch = Expressions.booleanTemplate(
                "function('" + MATCH_AGAINST_FUNCTION + "', {0}, {1}) > 0.0",
                techArticle.contents, keyword
        );
        
        // 스코어 계산을 위한 expression (Natural Language Mode)
        NumberTemplate<Double> titleScore = Expressions.numberTemplate(Double.class,
                "function('" + MATCH_AGAINST_NL_FUNCTION + "', {0}, {1})",
                techArticle.title.title, keyword
        );
        NumberTemplate<Double> contentsScore = Expressions.numberTemplate(Double.class,
                "function('" + MATCH_AGAINST_NL_FUNCTION + "', {0}, {1})",
                techArticle.contents, keyword
        );
        
        // 전체 스코어 계산 (제목 가중치 2배, 안전한 범위로 제한)
        NumberTemplate<Double> totalScore = Expressions.numberTemplate(Double.class,
                "(LEAST({0}, 100000) * 2.0) + LEAST({1}, 100000)", titleScore, contentsScore
        );
        
        // TechArticle과 score를 함께 조회
        List<Tuple> results = query.select(techArticle, totalScore)
                .from(techArticle)
                .where(titleMatch.or(contentsMatch))
                .where(getCompanyIdCondition(companyId))
                .where(getCursorConditionForKeywordSearch(techArticleSort, techArticleId, score, totalScore))
                .orderBy(getOrderSpecifierForKeywordSearch(techArticleSort, totalScore), techArticle.id.desc())
                .limit(pageable.getPageSize())
                .fetch();
        
        // Tuple을 TechArticleDto로 변환
        List<TechArticleDto> contents = results.stream()
                .map(result -> TechArticleDto.of(
                        result.get(techArticle), result.get(totalScore)))
                .toList();
        
        // 키워드 검색 결과 총 갯수
        long totalElements = query.select(techArticle.count())
                .from(techArticle)
                .where(titleMatch.or(contentsMatch))
                .where(getCompanyIdCondition(companyId))
                .fetchCount();

        return new SliceCustom<>(contents, pageable, totalElements);
    }

    // 일반 조회
    private SliceCustom<TechArticleDto> findTechArticlesByCursorWithoutKeyword(Pageable pageable, Long techArticleId,
                                                                            TechArticleSort techArticleSort, Long companyId
    ) {
        List<TechArticle> results = query.selectFrom(techArticle)
                .where(getCursorConditionFromTechArticleSort(techArticleSort, techArticleId))
                .where(getCompanyIdCondition(companyId))
                .orderBy(techArticleSort(techArticleSort), techArticle.id.desc())
                .limit(pageable.getPageSize())
                .fetch();

        // Tuple을 TechArticleDto로 변환
        List<TechArticleDto> contents = results.stream()
                .map(result -> TechArticleDto.of(result, null))
                .toList();

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

    // 키워드 검색을 위한 커서 조건 생성
    private Predicate getCursorConditionForKeywordSearch(TechArticleSort techArticleSort, Long techArticleId, 
                                                        Double score, NumberTemplate<Double> totalScore) {
        if (ObjectUtils.isEmpty(techArticleId)) {
            return null;
        }
        
        // HIGHEST_SCORE(정확도순)인 경우 스코어 기반 커서 사용
        if (techArticleSort == TechArticleSort.HIGHEST_SCORE || ObjectUtils.isEmpty(techArticleSort)) {
            return totalScore.lt(score.doubleValue())
                    .or(totalScore.eq(score.doubleValue())
                            .and(techArticle.id.lt(techArticleId)));
        }
        
        // 다른 정렬 방식인 경우 기존 커서 조건 사용
        TechArticle findTechArticle = query.selectFrom(techArticle)
                .where(techArticle.id.eq(techArticleId))
                .fetchOne();
        
        if (ObjectUtils.isEmpty(findTechArticle)) {
            return techArticle.id.loe(techArticleId);
        }
        
        return Optional.ofNullable(techArticleSort)
                .orElse(TechArticleSort.HIGHEST_SCORE).getCursorCondition(findTechArticle);
    }

    // 키워드 검색을 위한 정렬 조건 생성
    private OrderSpecifier<?> getOrderSpecifierForKeywordSearch(TechArticleSort techArticleSort, 
                                                               NumberTemplate<Double> totalScore) {
        // HIGHEST_SCORE(정확도순)인 경우 스코어 기반 정렬
        if (techArticleSort == TechArticleSort.HIGHEST_SCORE || ObjectUtils.isEmpty(techArticleSort)) {
            return totalScore.desc();
        }
        
        // 다른 정렬 방식인 경우 기존 정렬 사용
        return Optional.ofNullable(techArticleSort)
                .orElse(TechArticleSort.LATEST).getOrderSpecifierByTechArticleSort();
    }

    public BooleanExpression getCompanyIdCondition(Long companyId) {
        if(companyId == null) {
            return null;
        }
        return techArticle.company.id.eq(companyId);
    }

    private boolean hasNextPage(List<TechArticle> contents, int pageSize) {
        return contents.size() >= pageSize;
    }
}
