package com.dreamypatisiel.devdevdev.domain.service.response;

import lombok.Data;

@Data
public class BookmarkResponse {

    public Long techArticleId;
    public boolean value;

    public BookmarkResponse(Long techArticleId, boolean value) {
        this.techArticleId = techArticleId;
        this.value = value;
    }
}
