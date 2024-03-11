package com.dreamypatisiel.devdevdev.domain.repository.techArticle;

import com.dreamypatisiel.devdevdev.elastic.domain.document.ElasticTechArticle;

public enum TechArticleSort {

    LATEST {
        @Override
        public String getSortFieldName() {
            return LATEST_SORT_FIELD_NAME;
        }

        @Override
        public Object getCursorCondition(ElasticTechArticle elasticTechArticle) {
            return elasticTechArticle.getRegDate().toString();
        }
    },
    POPULAR {
        @Override
        public String getSortFieldName() {
            return POPULAR_SORT_FIELD_NAME;
        }

        @Override
        public Object getCursorCondition(ElasticTechArticle elasticTechArticle) {
            return elasticTechArticle.getPopularScore();
        }
    },
    MOST_VIEWED {
        @Override
        public String getSortFieldName() {
            return MOST_VIEWED_SORT_FIELD_NAME;
        }

        @Override
        public Object getCursorCondition(ElasticTechArticle elasticTechArticle) {
            return elasticTechArticle.getViewTotalCount();
        }
    },
    MOST_COMMENTED {
        @Override
        public String getSortFieldName() {
            return MOST_COMMENTED_SORT_FIELD_NAME;
        }

        @Override
        public Object getCursorCondition(ElasticTechArticle elasticTechArticle) {
            return elasticTechArticle.getCommentTotalCount();
        }
    };

    abstract public String getSortFieldName();
    abstract public Object getCursorCondition(ElasticTechArticle elasticTechArticle);
    private final static String LATEST_SORT_FIELD_NAME = "regDate.keyword";
    private final static String POPULAR_SORT_FIELD_NAME = "popularScore";
    private final static String MOST_VIEWED_SORT_FIELD_NAME = "viewTotalCount";
    private final static String MOST_COMMENTED_SORT_FIELD_NAME = "commentTotalCount";
}
