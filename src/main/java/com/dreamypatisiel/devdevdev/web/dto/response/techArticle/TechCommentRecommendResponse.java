package com.dreamypatisiel.devdevdev.web.dto.response.techArticle;

import lombok.Data;

@Data
public class TechCommentRecommendResponse {
    private final Boolean isRecommended;
    private final Long recommendTotalCount;

    public TechCommentRecommendResponse(Boolean isRecommended, Long recommendTotalCount) {
        this.isRecommended = isRecommended;
        this.recommendTotalCount = recommendTotalCount;
    }
}
