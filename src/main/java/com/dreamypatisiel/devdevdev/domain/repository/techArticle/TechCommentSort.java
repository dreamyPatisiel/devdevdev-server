package com.dreamypatisiel.devdevdev.domain.repository.techArticle;

import static com.dreamypatisiel.devdevdev.domain.entity.QTechComment.techComment;

import com.dreamypatisiel.devdevdev.domain.entity.TechComment;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TechCommentSort {

    OLDEST("등록순") {
        @Override
        public OrderSpecifier<?> getOrderSpecifierByTechCommentSort() {
            return new OrderSpecifier<>(Order.ASC, techComment.createdAt);
        }

        @Override
        public BooleanExpression getCursorCondition(TechComment findTechComment) {
            return techComment.id.gt(findTechComment.getId());
        }
    },
    LATEST("최신순") {
        @Override
        public OrderSpecifier<?> getOrderSpecifierByTechCommentSort() {
            return new OrderSpecifier<>(Order.DESC, techComment.createdAt);
        }

        @Override
        public BooleanExpression getCursorCondition(TechComment findTechComment) {
            return techComment.id.lt(findTechComment.getId());
        }
    },
    LIKED("좋아요순") {
        @Override
        public OrderSpecifier<?> getOrderSpecifierByTechCommentSort() {
            return new OrderSpecifier<>(Order.DESC, techComment.recommendTotalCount.count);
        }

        @Override
        public BooleanExpression getCursorCondition(TechComment findTechComment) {
            return techComment.recommendTotalCount.count.lt(findTechComment.getRecommendTotalCount().getCount())
                    .or(techComment.recommendTotalCount.count.eq(findTechComment.getRecommendTotalCount().getCount())
                            .and(techComment.id.lt(findTechComment.getId())));
        }
    },
    MOST_COMMENTED("답글 많은순") {
        @Override
        public OrderSpecifier<?> getOrderSpecifierByTechCommentSort() {
            return new OrderSpecifier<>(Order.DESC, techComment.replyTotalCount.count);
        }

        @Override
        public BooleanExpression getCursorCondition(TechComment findTechComment) {
            return techComment.replyTotalCount.count.lt(findTechComment.getReplyTotalCount().getCount())
                    .or(techComment.replyTotalCount.count.eq(findTechComment.getReplyTotalCount().getCount())
                            .and(techComment.id.lt(findTechComment.getId())));
        }
    };

    private final String description;

    abstract public OrderSpecifier<?> getOrderSpecifierByTechCommentSort();

    abstract public BooleanExpression getCursorCondition(TechComment findTechComment);
}
