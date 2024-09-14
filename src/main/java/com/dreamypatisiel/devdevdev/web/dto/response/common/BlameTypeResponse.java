package com.dreamypatisiel.devdevdev.web.dto.response.common;

import com.dreamypatisiel.devdevdev.domain.entity.BlameType;
import lombok.Builder;
import lombok.Data;

@Data
public class BlameTypeResponse {
    private final Long id;
    private final String reason;
    private final int sortOrder;

    @Builder
    public BlameTypeResponse(Long id, String reason, int sortOrder) {
        this.id = id;
        this.reason = reason;
        this.sortOrder = sortOrder;
    }

    public static BlameTypeResponse from(BlameType blameType) {
        return BlameTypeResponse.builder()
                .id(blameType.getId())
                .reason(blameType.getReason())
                .sortOrder(blameType.getSortOrder())
                .build();
    }
}
