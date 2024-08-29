package com.dreamypatisiel.devdevdev.domain.repository.pick;

import static com.dreamypatisiel.devdevdev.domain.entity.QPickComment.pickComment;

import com.dreamypatisiel.devdevdev.domain.entity.PickComment;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PickCommentSort {

    LATEST("최신순") {
        @Override
        public OrderSpecifier<LocalDateTime> getOrderSpecifierByPickCommentSort() {
            return new OrderSpecifier<>(Order.DESC, pickComment.createdAt);
        }

        @Override
        public BooleanExpression getCursorCondition(PickComment findPickComment) {
            return pickComment.id.lt(findPickComment.getId());
        }
    },
    MOST_LIKED("좋아요순") {
        @Override
        public OrderSpecifier<Long> getOrderSpecifierByPickCommentSort() {
            return new OrderSpecifier<>(Order.DESC, pickComment.recommendTotalCount.count);
        }

        @Override
        public BooleanExpression getCursorCondition(PickComment findPickComment) {
            return pickComment.recommendTotalCount.count.lt(findPickComment.getRecommendTotalCount().getCount())
                    .or(pickComment.recommendTotalCount.count.eq(findPickComment.getRecommendTotalCount().getCount())
                            .and(pickComment.id.lt(findPickComment.getId())));
        }
    },
    MOST_COMMENTED("답글 많은순") {
        @Override
        public OrderSpecifier<Long> getOrderSpecifierByPickCommentSort() {
            return new OrderSpecifier<>(Order.DESC, pickComment.recommendTotalCount.count);
        }

        @Override
        public BooleanExpression getCursorCondition(PickComment findPickComment) {
            return pickComment.replyTotalCount.count.lt(findPickComment.getReplyTotalCount().getCount())
                    .or(pickComment.replyTotalCount.count.eq(findPickComment.getReplyTotalCount().getCount())
                            .and(pickComment.id.lt(findPickComment.getId())));
        }
    };


    abstract public OrderSpecifier<?> getOrderSpecifierByPickCommentSort();

    abstract public BooleanExpression getCursorCondition(PickComment findPickComment);

    private final String description;
}
