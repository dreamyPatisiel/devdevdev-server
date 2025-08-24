package com.dreamypatisiel.devdevdev.domain.repository.techArticle;

import static com.dreamypatisiel.devdevdev.domain.entity.QTechArticle.techArticle;

import com.dreamypatisiel.devdevdev.domain.entity.TechArticle;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TechArticleSort {

    LATEST("최신순") {
        @Override
        public OrderSpecifier<?> getOrderSpecifierByTechArticleSort() {
            return new OrderSpecifier<>(Order.DESC, techArticle.regDate);
        }

        @Override
        public BooleanExpression getCursorCondition(TechArticle findTechArticle) {
            return techArticle.regDate.lt(findTechArticle.getRegDate())
                    .or(techArticle.regDate.eq(findTechArticle.getRegDate())
                            .and(techArticle.id.lt(findTechArticle.getId())));
        }
    },
    POPULAR("인기순") {
        public OrderSpecifier<?> getOrderSpecifierByTechArticleSort() {
            return new OrderSpecifier<>(Order.DESC, techArticle.popularScore.count);
        }

        @Override
        public BooleanExpression getCursorCondition(TechArticle findTechArticle) {
            return techArticle.popularScore.count.lt(findTechArticle.getPopularScore().getCount())
                    .or(techArticle.popularScore.count.eq(findTechArticle.getPopularScore().getCount())
                            .and(techArticle.regDate.eq(findTechArticle.getRegDate())));
        }
    },
    MOST_VIEWED("조회순") {
        public OrderSpecifier<?> getOrderSpecifierByTechArticleSort() {
            return new OrderSpecifier<>(Order.DESC, techArticle.viewTotalCount.count);
        }

        @Override
        public BooleanExpression getCursorCondition(TechArticle findTechArticle) {
            return techArticle.viewTotalCount.count.lt(findTechArticle.getViewTotalCount().getCount())
                    .or(techArticle.viewTotalCount.count.eq(findTechArticle.getViewTotalCount().getCount())
                            .and(techArticle.regDate.eq(findTechArticle.getRegDate())));
        }
    },
    MOST_COMMENTED("댓글순") {
        public OrderSpecifier<?> getOrderSpecifierByTechArticleSort() {
            return new OrderSpecifier<>(Order.DESC, techArticle.commentTotalCount.count);
        }

        @Override
        public BooleanExpression getCursorCondition(TechArticle findTechArticle) {
            return techArticle.commentTotalCount.count.lt(findTechArticle.getCommentTotalCount().getCount())
                    .or(techArticle.commentTotalCount.count.eq(findTechArticle.getCommentTotalCount().getCount())
                            .and(techArticle.regDate.eq(findTechArticle.getRegDate())));
        }
    },

    HIGHEST_SCORE("정확도순") {
        public OrderSpecifier<?> getOrderSpecifierByTechArticleSort() {
            return new OrderSpecifier<>(Order.DESC, techArticle.id);
        }

        @Override
        public BooleanExpression getCursorCondition(TechArticle findTechArticle) {
            return techArticle.regDate.lt(findTechArticle.getRegDate())
                    .or(techArticle.regDate.eq(findTechArticle.getRegDate())
                            .and(techArticle.regDate.eq(findTechArticle.getRegDate())));
        }
    };

    abstract public OrderSpecifier<?> getOrderSpecifierByTechArticleSort();

    abstract public BooleanExpression getCursorCondition(TechArticle techArticle);

    private final String description;
}
