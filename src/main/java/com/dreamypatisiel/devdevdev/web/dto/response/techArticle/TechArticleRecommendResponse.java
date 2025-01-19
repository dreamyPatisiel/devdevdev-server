package com.dreamypatisiel.devdevdev.web.dto.response.techArticle;

import lombok.Data;

@Data
public class TechArticleRecommendResponse {

    public final Long techArticleId;
    public final Boolean status;
    public final Long recommendTotalCount;

    public TechArticleRecommendResponse(Long techArticleId, Boolean status, Long recommendTotalCount) {
        this.techArticleId = techArticleId;
        this.status = status;
        this.recommendTotalCount = recommendTotalCount;
    }
}
