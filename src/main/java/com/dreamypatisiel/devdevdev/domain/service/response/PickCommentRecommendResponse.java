package com.dreamypatisiel.devdevdev.domain.service.response;

import lombok.Data;

@Data
public class PickCommentRecommendResponse {
    private final Boolean recommendStatus;

    public PickCommentRecommendResponse(Boolean recommendStatus) {
        this.recommendStatus = recommendStatus;
    }
}
