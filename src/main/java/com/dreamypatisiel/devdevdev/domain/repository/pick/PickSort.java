package com.dreamypatisiel.devdevdev.domain.repository.pick;


import static com.dreamypatisiel.devdevdev.domain.entity.QPick.pick;

import com.dreamypatisiel.devdevdev.domain.entity.Pick;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PickSort {

    LATEST("최신순") {
        @Override
        public OrderSpecifier<?> getOrderSpecifierByPickSort() {
            return new OrderSpecifier<>(Order.DESC, pick.createdAt);
        }

        @Override
        public BooleanExpression getCursorCondition(Pick findPick) {
            return pick.id.lt(findPick.getId());
        }
    },
    POPULAR("인기순") {
        @Override
        public OrderSpecifier<?> getOrderSpecifierByPickSort() {
            return new OrderSpecifier<>(Order.DESC, pick.popularScore.count);
        }

        @Override
        public BooleanExpression getCursorCondition(Pick findPick) {
            return pick.popularScore.count.lt(findPick.getPopularScore().getCount())
                    .or(pick.popularScore.count.eq(findPick.getPopularScore().getCount())
                            .and(pick.id.lt(findPick.getId())));
        }
    },
    MOST_VIEWED("조회수") {
        @Override
        public OrderSpecifier<?> getOrderSpecifierByPickSort() {
            return new OrderSpecifier<>(Order.DESC, pick.viewTotalCount.count);
        }

        @Override
        public BooleanExpression getCursorCondition(Pick findPick) {
            return pick.viewTotalCount.count.lt(findPick.getViewTotalCount().getCount())
                    .or(pick.viewTotalCount.count.eq(findPick.getViewTotalCount().getCount())
                            .and(pick.id.lt(findPick.getId())));
        }
    },
    MOST_COMMENTED("댓글순") {
        @Override
        public OrderSpecifier<?> getOrderSpecifierByPickSort() {
            return new OrderSpecifier<>(Order.DESC, pick.commentTotalCount.count);
        }

        @Override
        public BooleanExpression getCursorCondition(Pick findPick) {
            return pick.commentTotalCount.count.lt(findPick.getCommentTotalCount().getCount())
                    .or(pick.commentTotalCount.count.eq(findPick.getCommentTotalCount().getCount())
                            .and(pick.id.lt(findPick.getId())));
        }
    };


    abstract public OrderSpecifier<?> getOrderSpecifierByPickSort();

    abstract public BooleanExpression getCursorCondition(Pick pick);

    private final String description;
}
