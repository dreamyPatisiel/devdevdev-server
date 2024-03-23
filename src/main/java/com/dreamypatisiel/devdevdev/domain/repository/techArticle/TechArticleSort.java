package com.dreamypatisiel.devdevdev.domain.repository.techArticle;

import com.dreamypatisiel.devdevdev.elastic.domain.document.ElasticTechArticle;
import org.elasticsearch.search.sort.*;

public enum TechArticleSort {

    LATEST {
        @Override
        public SortBuilder<?> getSortCondition() {
            return getFieldSortBuilder(LATEST_SORT_FIELD_NAME);
        }

        @Override
        public Object getSearchAfterCondition(ElasticTechArticle elasticTechArticle) {
            return elasticTechArticle.getRegDate().toString();
        }
    },
    POPULAR {
        @Override
        public SortBuilder<?> getSortCondition() {
            return getFieldSortBuilder(POPULAR_SORT_FIELD_NAME);
        }

        @Override
        public Object getSearchAfterCondition(ElasticTechArticle elasticTechArticle) {
            return elasticTechArticle.getPopularScore();
        }
    },
    MOST_VIEWED {
        @Override
        public SortBuilder<?> getSortCondition() {
            return getFieldSortBuilder(MOST_VIEWED_SORT_FIELD_NAME);
        }

        @Override
        public Object getSearchAfterCondition(ElasticTechArticle elasticTechArticle) {
            return elasticTechArticle.getViewTotalCount();
        }
    },
    MOST_COMMENTED {
        @Override
        public SortBuilder<?> getSortCondition() {
            return getFieldSortBuilder(MOST_COMMENTED_SORT_FIELD_NAME);
        }

        @Override
        public Object getSearchAfterCondition(ElasticTechArticle elasticTechArticle) {
            return elasticTechArticle.getCommentTotalCount();
        }
    },

    HIGHEST_SCORE {
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
    };

    private final static String LATEST_SORT_FIELD_NAME = "regDate";
    private final static String POPULAR_SORT_FIELD_NAME = "popularScore";
    private final static String MOST_VIEWED_SORT_FIELD_NAME = "viewTotalCount";
    private final static String MOST_COMMENTED_SORT_FIELD_NAME = "commentTotalCount";
}
