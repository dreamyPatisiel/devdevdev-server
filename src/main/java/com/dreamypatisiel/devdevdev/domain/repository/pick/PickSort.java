package com.dreamypatisiel.devdevdev.domain.repository.pick;

import static com.dreamypatisiel.devdevdev.domain.entity.QPick.pick;

import com.dreamypatisiel.devdevdev.domain.entity.Pick;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import io.swagger.v3.oas.models.security.SecurityScheme.In;

public enum PickSort {

    LATEST {
        @Override
        public OrderSpecifier getOrderSpecifierByPickSort() {
            return new OrderSpecifier<>(Order.DESC, pick.createdAt);
        }

        @Override
        public BooleanExpression getCursorCondition(Pick findPick) {
            return pick.id.lt(findPick.getId());
        }
    },
    POPULAR {
        @Override
        public OrderSpecifier getOrderSpecifierByPickSort() {
            return new OrderSpecifier<>(Order.DESC, pick.popularScore.count);
        }

        @Override
        public BooleanExpression getCursorCondition(Pick findPick) {
            return pick.popularScore.count.lt(findPick.getPopularScore().getCount())
                    .or(pick.popularScore.count.eq(findPick.getPopularScore().getCount())
                            .and(pick.id.lt(findPick.getId())));
        }
    },
    MOST_VIEWED {
        @Override
        public OrderSpecifier getOrderSpecifierByPickSort() {
            return new OrderSpecifier<>(Order.DESC, pick.viewTotalCount.count);
        }

        @Override
        public BooleanExpression getCursorCondition(Pick findPick) {
            return pick.viewTotalCount.count.lt(findPick.getViewTotalCount().getCount())
                    .or(pick.viewTotalCount.count.eq(findPick.getViewTotalCount().getCount())
                            .and(pick.id.lt(findPick.getId())));
        }
    },
    MOST_COMMENTED {
        @Override
        public OrderSpecifier getOrderSpecifierByPickSort() {
            return new OrderSpecifier<>(Order.DESC, pick.commentTotalCount.count);
        }

        @Override
        public BooleanExpression getCursorCondition(Pick findPick) {
            return pick.commentTotalCount.count.lt(findPick.getCommentTotalCount().getCount())
                    .or(pick.commentTotalCount.count.eq(findPick.getCommentTotalCount().getCount())
                            .and(pick.id.lt(findPick.getId())));
        }
    };


    abstract public OrderSpecifier getOrderSpecifierByPickSort();
    abstract public BooleanExpression getCursorCondition(Pick pick);
}
