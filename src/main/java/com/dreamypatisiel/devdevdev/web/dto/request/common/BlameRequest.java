package com.dreamypatisiel.devdevdev.web.dto.request.common;

import lombok.Builder;
import lombok.Data;

@Data
public class BlameRequest {
    private BlamePathType blamePathType;
    private Long pickId;
    private Long pickCommentId;
    private Long techArticleId;
    private Long techArticleCommentId;
    private final Long blameTypeId;
    private String customReason;

    @Builder
    public BlameRequest(BlamePathType blamePathType, Long pickId, Long pickCommentId, Long techArticleId,
                        Long techArticleCommentId, Long blameTypeId, String customReason) {
        this.blamePathType = blamePathType;
        this.pickId = pickId;
        this.pickCommentId = pickCommentId;
        this.techArticleId = techArticleId;
        this.techArticleCommentId = techArticleCommentId;
        this.blameTypeId = blameTypeId;
        this.customReason = customReason;
    }

    public boolean isEqualBlamePathType(BlamePathType blamePathType) {
        return this.blamePathType.equals(blamePathType);
    }
}
