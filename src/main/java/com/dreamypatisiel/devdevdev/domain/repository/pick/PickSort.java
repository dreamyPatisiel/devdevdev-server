package com.dreamypatisiel.devdevdev.domain.repository.pick;

import static com.dreamypatisiel.devdevdev.domain.entity.QPick.pick;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.NumberExpression;

public enum PickSort {

    LATEST {
        @Override
        public OrderSpecifier getOrderSpecifierByPickSort() {
            return new OrderSpecifier<>(Order.DESC, pick.createdAt);
        }
    },
    POPULAR {
        @Override
        public OrderSpecifier getOrderSpecifierByPickSort() {
            return new OrderSpecifier<>(Order.DESC, pickPopular());
        }
    },
    MOST_VIEWED {
        @Override
        public OrderSpecifier getOrderSpecifierByPickSort() {
            return new OrderSpecifier<>(Order.DESC, pick.viewTotalCount.count);
        }
    },
    MOST_COMMENTED {
        @Override
        public OrderSpecifier getOrderSpecifierByPickSort() {
            return new OrderSpecifier<>(Order.DESC, pick.commentTotalCount.count);
        }
    };

    public static final int COMMENT_WEIGHT = 4;
    public static final int VOTE_WEIGHT = 4;
    public static final int VIEW_WEIGHT = 2;

    abstract public OrderSpecifier getOrderSpecifierByPickSort();
    private static NumberExpression<Integer> pickPopular() {
        NumberExpression<Integer> add = pick.commentTotalCount.count.multiply(COMMENT_WEIGHT)
                .add(pick.voteTotalCount.count.multiply(VOTE_WEIGHT))
                .add(pick.viewTotalCount.count.multiply(VIEW_WEIGHT));

        return add;
    }
}
