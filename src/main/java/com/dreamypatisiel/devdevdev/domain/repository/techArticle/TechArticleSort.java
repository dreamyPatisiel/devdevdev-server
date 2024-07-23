package com.dreamypatisiel.devdevdev.domain.repository.techArticle;

import static com.dreamypatisiel.devdevdev.elastic.constant.ElasticsearchConstant.LATEST_SORT_FIELD_NAME;
import static com.dreamypatisiel.devdevdev.elastic.constant.ElasticsearchConstant.MOST_COMMENTED_SORT_FIELD_NAME;
import static com.dreamypatisiel.devdevdev.elastic.constant.ElasticsearchConstant.MOST_VIEWED_SORT_FIELD_NAME;
import static com.dreamypatisiel.devdevdev.elastic.constant.ElasticsearchConstant.POPULAR_SORT_FIELD_NAME;

import com.dreamypatisiel.devdevdev.elastic.domain.document.ElasticTechArticle;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.opensearch.search.sort.FieldSortBuilder;
import org.opensearch.search.sort.SortBuilder;
import org.opensearch.search.sort.SortBuilders;
import org.opensearch.search.sort.SortOrder;

@Getter
@RequiredArgsConstructor
public enum TechArticleSort {

    LATEST("최신순") {
        @Override
        public SortBuilder<?> getSortCondition() {
            return getFieldSortBuilder(LATEST_SORT_FIELD_NAME);
        }

        @Override
        public Object getSearchAfterCondition(ElasticTechArticle elasticTechArticle) {
            return elasticTechArticle.getRegDate().toString();
        }
    },
    POPULAR("인기순") {
        @Override
        public SortBuilder<?> getSortCondition() {
            return getFieldSortBuilder(POPULAR_SORT_FIELD_NAME);
        }

        @Override
        public Object getSearchAfterCondition(ElasticTechArticle elasticTechArticle) {
            return elasticTechArticle.getPopularScore();
        }
    },
    MOST_VIEWED("조회순") {
        @Override
        public SortBuilder<?> getSortCondition() {
            return getFieldSortBuilder(MOST_VIEWED_SORT_FIELD_NAME);
        }

        @Override
        public Object getSearchAfterCondition(ElasticTechArticle elasticTechArticle) {
            return elasticTechArticle.getViewTotalCount();
        }
    },
    MOST_COMMENTED("댓글순") {
        @Override
        public SortBuilder<?> getSortCondition() {
            return getFieldSortBuilder(MOST_COMMENTED_SORT_FIELD_NAME);
        }

        @Override
        public Object getSearchAfterCondition(ElasticTechArticle elasticTechArticle) {
            return elasticTechArticle.getCommentTotalCount();
        }
    },

    HIGHEST_SCORE("정확도순") {
        @Override
        public SortBuilder<?> getSortCondition() {
            return SortBuilders.scoreSort().order(SortOrder.DESC);
        }

        @Override
        public Object getSearchAfterCondition(ElasticTechArticle elasticTechArticle) {
            return null;
        }
    };

    abstract public SortBuilder<?> getSortCondition();

    abstract public Object getSearchAfterCondition(ElasticTechArticle elasticTechArticle);

    private static FieldSortBuilder getFieldSortBuilder(String sortFieldName) {
        return SortBuilders.fieldSort(sortFieldName).order(SortOrder.DESC);
    }

    private final String description;
}
