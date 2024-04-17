package com.dreamypatisiel.devdevdev.domain.service.response;

import lombok.Data;

@Data
public class BookmarkResponse {

    public Long techArticleId;
    public Boolean status;

    public BookmarkResponse(Long techArticleId, Boolean status) {
        this.techArticleId = techArticleId;
        this.status = status;
    }
}
