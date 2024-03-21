package com.dreamypatisiel.devdevdev.domain.policy;

import com.dreamypatisiel.devdevdev.domain.entity.TechArticle;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Count;
import org.springframework.stereotype.Component;

@Component
public class TechArticlePopularScorePolicy implements PopularScorePolicy {

    public static final long COMMENT_WEIGHT = 4L;
    public static final long RECOMMEND_WEIGHT = 4L;
    public static final long VIEW_WEIGHT = 2L;
    public static final String INVALID_TECH_ARTICLE_TYPE_MESSAGE = "올바른 기술블로그 타입이 아닙니다.";

    @Override
    public Count calculatePopularScore(Object object) {
        boolean isTechArticleClassType = object instanceof TechArticle;
        if(!isTechArticleClassType) {
            throw new IllegalArgumentException(INVALID_TECH_ARTICLE_TYPE_MESSAGE);
        }

        TechArticle techArticle = (TechArticle) object;
        long commentScore = techArticle.getCommentTotalCount().getCount() * COMMENT_WEIGHT;
        long recommendScore = techArticle.getRecommendTotalCount().getCount() * RECOMMEND_WEIGHT;
        long viewScore = techArticle.getViewTotalCount().getCount() * VIEW_WEIGHT;

        return new Count(commentScore + recommendScore + viewScore);
    }
}
