package com.dreamypatisiel.devdevdev.domain.repository.techArticle.custom;

import static com.dreamypatisiel.devdevdev.domain.entity.QMember.member;
import static com.dreamypatisiel.devdevdev.domain.entity.QTechArticle.techArticle;
import static com.dreamypatisiel.devdevdev.domain.entity.QTechComment.techComment;

import com.dreamypatisiel.devdevdev.domain.entity.TechComment;
import com.dreamypatisiel.devdevdev.domain.repository.comment.MyWrittenCommentDto;
import com.dreamypatisiel.devdevdev.domain.repository.comment.QMyWrittenCommentDto;
import com.dreamypatisiel.devdevdev.domain.repository.techArticle.TechCommentSort;
import com.dreamypatisiel.devdevdev.web.dto.SliceCustom;
import com.dreamypatisiel.devdevdev.web.dto.request.comment.MyWrittenCommentFilter;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPQLQueryFactory;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.util.ObjectUtils;

@RequiredArgsConstructor
public class TechCommentRepositoryImpl implements TechCommentRepositoryCustom {

    public static final int MINIMUM_RECOMMENDATION_COUNT = 1;
    private final JPQLQueryFactory query;

    @Override
    public Slice<TechComment> findOriginParentTechCommentsByCursor(Long techArticleId, Long techCommentId,
                                                                   TechCommentSort techCommentSort, Pageable pageable) {
        List<TechComment> contents = query.selectFrom(techComment)
                .innerJoin(techComment.techArticle, techArticle).on(techArticle.id.eq(techArticleId))
                .innerJoin(techComment.createdBy, member).fetchJoin()
                .where(techComment.parent.isNull()
                        .and(techComment.originParent.isNull())
                        .and(getCursorCondition(techCommentSort, techCommentId))
                )
                .orderBy(techCommentSort(techCommentSort), techComment.id.desc())
                .limit(pageable.getPageSize())
                .fetch();

        return new SliceImpl<>(contents, pageable, hasNextPage(contents, pageable.getPageSize()));
    }

    @Override
    public List<TechComment> findOriginParentTechBestCommentsByTechArticleIdAndOffset(Long techArticleId, int size) {

        return query.selectFrom(techComment)
                .innerJoin(techComment.techArticle, techArticle).on(techArticle.id.eq(techArticleId))
                .innerJoin(techComment.createdBy, member).fetchJoin()
                .where(techComment.parent.isNull()
                        .and(techComment.originParent.isNull())
                        .and(techComment.deletedAt.isNull())
                        // 좋아요가 N개 이상인 댓글만 조회
                        .and(techComment.recommendTotalCount.count.goe(MINIMUM_RECOMMENDATION_COUNT))
                )
                .orderBy(techCommentSort(TechCommentSort.MOST_LIKED), techComment.id.desc())
                .limit(size)
                .fetch();
    }

    @Override
    public SliceCustom<MyWrittenCommentDto> findMyWrittenTechCommentsByCursor(Long memberId, Long techCommentId,
                                                                              Pageable pageable) {

        // 회원이 작성한 기술블로그 댓글 조회
        List<MyWrittenCommentDto> contents = query.select(
                        new QMyWrittenCommentDto(techArticle.id,
                                techArticle.title.title,
                                techComment.id,
                                Expressions.constant(MyWrittenCommentFilter.TECH_ARTICLE.name()),
                                techComment.contents.commentContents,
                                techComment.recommendTotalCount.count,
                                techComment.createdAt,
                                Expressions.nullExpression(),
                                Expressions.nullExpression()))
                .from(techComment)
                .innerJoin(techComment.techArticle, techArticle)
                .where(techComment.createdBy.id.eq(memberId)
                        .and(techComment.deletedAt.isNull())
                        .and(techComment.id.lt(techCommentId)))
                .orderBy(techComment.createdAt.desc())
                .limit(pageable.getPageSize())
                .fetch();

        // 회원이 작성한 기술 블로그 댓글 총 갯수(삭제 미포함)
        long totalElements = query.select(techComment.count())
                .from(techComment)
                .where(techComment.createdBy.id.eq(memberId)
                        .and(techComment.deletedAt.isNull()))
                .fetchCount();

        return new SliceCustom<>(contents, pageable, hasNextPage(contents, pageable.getPageSize()), totalElements);
    }

    private BooleanExpression getCursorCondition(TechCommentSort techCommentSort, Long techCommentId) {
        if (ObjectUtils.isEmpty(techCommentId)) {
            return null;
        }

        // 커서가 될 기술블로그 댓글 조회
        TechComment findTechComment = query.selectFrom(techComment)
                .where(techComment.id.eq(techCommentId))
                .fetchOne();

        // 댓글이 없으면 내림차순 조회
        if (ObjectUtils.isEmpty(findTechComment)) {
            return techComment.id.loe(techCommentId);
        }

        // sort 조건에 맞는 getCursorCondition 반환
        return Arrays.stream(TechCommentSort.values())
                .filter(sort -> sort.equals(techCommentSort))
                .findFirst()
                .map(sort -> sort.getCursorCondition(findTechComment))
                .orElse(TechCommentSort.LATEST.getCursorCondition(findTechComment));
    }

    private OrderSpecifier<?> techCommentSort(TechCommentSort techCommentSort) {
        return Optional.ofNullable(techCommentSort)
                .orElse(TechCommentSort.LATEST).getOrderSpecifierByTechCommentSort();
    }

    private <T> boolean hasNextPage(List<T> contents, int pageSize) {
        return contents.size() >= pageSize;
    }
}
