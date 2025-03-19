package com.dreamypatisiel.devdevdev.domain.repository.techArticle;

import static com.dreamypatisiel.devdevdev.domain.entity.QBookmark.bookmark;
import static com.dreamypatisiel.devdevdev.domain.entity.QTechArticle.techArticle;

import com.dreamypatisiel.devdevdev.domain.entity.Bookmark;
import com.dreamypatisiel.devdevdev.domain.entity.TechArticle;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BookmarkSort {

    BOOKMARKED("등록순") {
        @Override
        public OrderSpecifier<?> getOrderSpecifierByBookmarkSort() {
            return new OrderSpecifier<>(Order.DESC, bookmark.lastModifiedAt);
        }

        @Override
        public BooleanExpression getCursorCondition(Bookmark findBookmark, TechArticle findTechArticle) {
            return bookmark.lastModifiedAt.before(findBookmark.getLastModifiedAt())
                    .or(bookmark.lastModifiedAt.eq(findBookmark.getLastModifiedAt())
                            .and(techArticle.id.lt(findTechArticle.getId())));
        }
    },

    LATEST("최신순") {
        @Override
        public OrderSpecifier<?> getOrderSpecifierByBookmarkSort() {
            return new OrderSpecifier<>(Order.DESC, techArticle.createdAt);
        }

        @Override
        public BooleanExpression getCursorCondition(Bookmark findBookmark, TechArticle findTechArticle) {
            return techArticle.createdAt.before(findTechArticle.getCreatedAt())
                    .or(techArticle.createdAt.eq(findTechArticle.getCreatedAt())
                            .and(techArticle.id.lt(findTechArticle.getId())));
        }
    },

    MOST_COMMENTED("댓글순") {
        @Override
        public OrderSpecifier<?> getOrderSpecifierByBookmarkSort() {
            return new OrderSpecifier<>(Order.DESC, techArticle.commentTotalCount.count);
        }

        @Override
        public BooleanExpression getCursorCondition(Bookmark findBookmark, TechArticle findTechArticle) {
            return techArticle.commentTotalCount.count.lt(findTechArticle.getCommentTotalCount().getCount())
                    .or(techArticle.commentTotalCount.count.eq(findTechArticle.getCommentTotalCount().getCount())
                            .and(techArticle.id.lt(findTechArticle.getId())));
        }
    };

    private final String description;

    abstract public OrderSpecifier<?> getOrderSpecifierByBookmarkSort();

    abstract public BooleanExpression getCursorCondition(Bookmark findBookmark, TechArticle findTechArticle);
}
