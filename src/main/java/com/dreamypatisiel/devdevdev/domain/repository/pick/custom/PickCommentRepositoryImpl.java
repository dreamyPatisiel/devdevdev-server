package com.dreamypatisiel.devdevdev.domain.repository.pick.custom;

import static com.dreamypatisiel.devdevdev.domain.entity.QMember.member;
import static com.dreamypatisiel.devdevdev.domain.entity.QPick.pick;
import static com.dreamypatisiel.devdevdev.domain.entity.QPickComment.pickComment;
import static com.dreamypatisiel.devdevdev.domain.entity.QPickOption.pickOption;
import static com.dreamypatisiel.devdevdev.domain.entity.QPickVote.pickVote;

import com.dreamypatisiel.devdevdev.domain.entity.PickComment;
import com.dreamypatisiel.devdevdev.domain.entity.enums.ContentStatus;
import com.dreamypatisiel.devdevdev.domain.entity.enums.PickOptionType;
import com.dreamypatisiel.devdevdev.domain.repository.pick.PickCommentSort;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPQLQueryFactory;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.util.ObjectUtils;

@RequiredArgsConstructor
public class PickCommentRepositoryImpl implements PickCommentRepositoryCustom {

    private final JPQLQueryFactory query;

    @Override
    public Slice<PickComment> findOriginParentPickCommentsByCursor(Pageable pageable, Long pickId,
                                                                   Long pickCommentId, PickCommentSort pickCommentSort,
                                                                   EnumSet<PickOptionType> pickOptionTypes) {

        List<PickComment> contents = query.selectFrom(pickComment)
                .innerJoin(pickComment.pick, pick).on(pick.id.eq(pickId))
                .innerJoin(pickComment.createdBy, member).fetchJoin()
                .leftJoin(pickComment.pickVote, pickVote).fetchJoin()
                .leftJoin(pickVote.pickOption, pickOption).fetchJoin()
                .where(pick.contentStatus.eq(ContentStatus.APPROVAL)
                        .and(pickComment.parent.isNull())
                        .and(pickComment.originParent.isNull())
                        .and(getPickOptionTypeCondition(pickOptionTypes))
                        .and(getCursorCondition(pickCommentSort, pickCommentId))
                )
                .orderBy(pickCommentSort(pickCommentSort), pickComment.id.desc())
                .limit(pageable.getPageSize())
                .fetch();

        return new SliceImpl<>(contents, pageable, hasNextPage(contents, pageable.getPageSize()));
    }

    @Override
    public List<PickComment> findOriginParentPickBestCommentsByPickIdAndOffset(Long pickId, int size) {

        return query.selectFrom(pickComment)
                .innerJoin(pickComment.pick, pick).on(pick.id.eq(pickId))
                .innerJoin(pickComment.createdBy, member).fetchJoin()
                .leftJoin(pickComment.pickVote, pickVote).fetchJoin()
                .leftJoin(pickVote.pickOption, pickOption).fetchJoin()
                .where(
                        pick.contentStatus.eq(ContentStatus.APPROVAL)
                                .and(pickComment.parent.isNull())
                                .and(pickComment.originParent.isNull())
                                .and(pickComment.deletedAt.isNull())
                )
                .orderBy(pickCommentSort(PickCommentSort.MOST_LIKED), pickComment.id.desc())
                .limit(size)
                .fetch();
    }

    @Override
    public Long countByPickIdAndPickOptionTypeIn(Long pickId, EnumSet<PickOptionType> pickOptionTypes) {
        return query.selectFrom(pickComment)
                .where(pickComment.pick.contentStatus.eq(ContentStatus.APPROVAL)
                        .and(pickOptionTypeIn(pickOptionTypes)))
                .fetchCount();
    }

    private static BooleanExpression pickOptionTypeIn(EnumSet<PickOptionType> pickOptionTypes) {
        if (ObjectUtils.isEmpty(pickOptionTypes)) {
            return null;
        }

        // 자동으로 join 절 생성
        return pickComment.pickVote.pickOption.pickOptionType.in(pickOptionTypes);
    }

    private BooleanExpression getCursorCondition(PickCommentSort pickCommentSort, Long pickCommentId) {
        if (ObjectUtils.isEmpty(pickCommentId)) {
            return null;
        }

        // 픽픽픽 댓글 조회
        PickComment findPickComment = query.selectFrom(pickComment)
                .where(pickComment.id.eq(pickCommentId))
                .fetchOne();

        // 댓글이 없으면
        if (ObjectUtils.isEmpty(findPickComment)) {
            // id <= ? 조건으로 조회
            return pickComment.id.loe(pickCommentId);
        }

        // sort 조건에 맞는 getCursorCondition 반환
        return Arrays.stream(PickCommentSort.values())
                .filter(sort -> sort.equals(pickCommentSort))
                .findFirst()
                .map(sort -> sort.getCursorCondition(findPickComment))
                .orElse(PickCommentSort.LATEST.getCursorCondition(findPickComment));
    }

    private BooleanExpression getPickOptionTypeCondition(EnumSet<PickOptionType> pickOptionTypes) {

        if (ObjectUtils.isEmpty(pickOptionTypes)) {
            return null;
        }

        return pickOption.pickOptionType.in(pickOptionTypes)
                .and(pickComment.isPublic.isTrue());
    }

    private OrderSpecifier<?> pickCommentSort(PickCommentSort pickCommentSort) {
        return Optional.ofNullable(pickCommentSort)
                .orElse(PickCommentSort.LATEST).getOrderSpecifierByPickCommentSort();
    }

    private boolean hasNextPage(List<PickComment> contents, int pageSize) {
        return contents.size() >= pageSize;
    }
}
