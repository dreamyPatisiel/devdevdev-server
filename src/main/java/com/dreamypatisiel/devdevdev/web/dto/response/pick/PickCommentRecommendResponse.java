package com.dreamypatisiel.devdevdev.web.dto.response.pick;

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
