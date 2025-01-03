package com.dreamypatisiel.devdevdev.web.dto.response.techArticle;

import lombok.Data;

@Data
public class TechArticleRecommendResponse {

    public final Long techArticleId;
    public final Boolean status;

    public TechArticleRecommendResponse(Long techArticleId, Boolean status) {
        this.techArticleId = techArticleId;
        this.status = status;
    }
}
