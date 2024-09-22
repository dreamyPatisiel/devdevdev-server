package com.dreamypatisiel.devdevdev.web.dto.response.techArticle;

import lombok.Data;

@Data
public class TechCommentRecommendResponse {
    private final Boolean recommendStatus;
    private final Long recommendTotalCount;

    public TechCommentRecommendResponse(Boolean recommendStatus, Long recommendTotalCount) {
        this.recommendStatus = recommendStatus;
        this.recommendTotalCount = recommendTotalCount;
    }
}
