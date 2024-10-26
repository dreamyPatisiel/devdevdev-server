package com.dreamypatisiel.devdevdev.web.dto.response.pick;

import lombok.Data;

@Data
public class PickCommentRecommendResponse {
    private final Boolean isRecommended;
    private final Long recommendTotalCount;

    public PickCommentRecommendResponse(Boolean isRecommended, Long recommendTotalCount) {
        this.isRecommended = isRecommended;
        this.recommendTotalCount = recommendTotalCount;
    }
}
