package com.dreamypatisiel.devdevdev.domain.service.response;

import lombok.Data;

@Data
public class PickCommentRecommendResponse {
    private final Boolean recommendStatus;
    private final Long recommendTotalCount;

    public PickCommentRecommendResponse(Boolean recommendStatus, Long recommendTotalCount) {
        this.recommendStatus = recommendStatus;
        this.recommendTotalCount = recommendTotalCount;
    }
}
