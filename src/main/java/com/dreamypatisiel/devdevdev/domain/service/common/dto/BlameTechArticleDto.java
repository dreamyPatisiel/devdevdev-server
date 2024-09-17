package com.dreamypatisiel.devdevdev.domain.service.common.dto;

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

    public static BlameTechArticleDto create(BlameDto blameDto) {
        return BlameTechArticleDto.builder()
                .techArticleId(blameDto.getTechArticleId())
                .techArticleCommentId(blameDto.getTechArticleCommentId())
                .blameTypeId(blameDto.getBlameTypeId())
                .customReason(blameDto.getCustomReason())
                .build();
    }
}
