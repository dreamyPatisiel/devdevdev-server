package com.dreamypatisiel.devdevdev.domain.service.common.dto;

import lombok.Builder;
import lombok.Data;

@Data
public class BlamePickDto {
    private Long pickId;
    private Long pickCommentId;
    private final Long blameTypeId;
    private String customReason;

    @Builder
    public BlamePickDto(Long pickId, Long pickCommentId, Long blameTypeId, String customReason) {
        this.pickId = pickId;
        this.pickCommentId = pickCommentId;
        this.blameTypeId = blameTypeId;
        this.customReason = customReason;
    }

    public static BlamePickDto create(BlameDto blameDto) {
        return BlamePickDto.builder()
                .pickId(blameDto.getPickId())
                .pickCommentId(blameDto.getPickCommentId())
                .blameTypeId(blameDto.getBlameTypeId())
                .customReason(blameDto.getCustomReason())
                .build();
    }
}
