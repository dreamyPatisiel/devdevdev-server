package com.dreamypatisiel.devdevdev.web.dto.request.common;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
public class BlameRequest {
    private Long pickId;
    private Long pickCommentId;
    private Long techArticleId;
    private Long techArticleCommentId;

    @NotNull(message = "신고 사유 종류 아이디는 필수 입니다.")
    private final Long blameTypeId;

    private String customReason;

    @Builder
    public BlameRequest(Long pickId, Long pickCommentId, Long techArticleId,
                        Long techArticleCommentId, Long blameTypeId, String customReason) {
        this.pickId = pickId;
        this.pickCommentId = pickCommentId;
        this.techArticleId = techArticleId;
        this.techArticleCommentId = techArticleCommentId;
        this.blameTypeId = blameTypeId;
        this.customReason = customReason;
    }
}
