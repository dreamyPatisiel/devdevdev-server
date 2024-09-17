package com.dreamypatisiel.devdevdev.domain.service.common.dto;

import com.dreamypatisiel.devdevdev.web.dto.request.common.BlameRequest;
import lombok.Builder;
import lombok.Data;

@Data
public class BlameTechArticleDto {
    private final Long techArticleId;
    private final Long techArticleCommentId;
    private final Long blameTypeId;
    private String customReason;

    @Builder
    public BlameTechArticleDto(Long techArticleId, Long techArticleCommentId, Long blameTypeId, String customReason) {
        this.techArticleId = techArticleId;
        this.techArticleCommentId = techArticleCommentId;
        this.blameTypeId = blameTypeId;
        this.customReason = customReason;
    }

    public static BlameTechArticleDto create(BlameRequest blameRequest) {
        return BlameTechArticleDto.builder()
                .techArticleId(blameRequest.getTechArticleId())
                .techArticleCommentId(blameRequest.getTechArticleCommentId())
                .blameTypeId(blameRequest.getBlameTypeId())
                .customReason(blameRequest.getCustomReason())
                .build();
    }
}
