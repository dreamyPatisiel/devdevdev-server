package com.dreamypatisiel.devdevdev.domain.service.common.dto;

import com.dreamypatisiel.devdevdev.web.dto.request.common.BlameRequest;
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

    public static BlamePickDto create(BlameRequest blameRequest) {
        return BlamePickDto.builder()
                .pickId(blameRequest.getPickId())
                .pickCommentId(blameRequest.getPickCommentId())
                .blameTypeId(blameRequest.getBlameTypeId())
                .customReason(blameRequest.getCustomReason())
                .build();
    }
}
