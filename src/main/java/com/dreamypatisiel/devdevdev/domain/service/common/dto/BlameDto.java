package com.dreamypatisiel.devdevdev.domain.service.common.dto;

import com.dreamypatisiel.devdevdev.web.dto.request.common.BlamePathType;
import lombok.Data;

@Data
public class BlameDto {
    private BlamePathType blamePathType;
    private Long pickId;
    private Long pickCommentId;
    private Long techArticleId;
    private Long techArticleCommentId;
    private final Long blameTypeId;
    private String customReason;

    public boolean isEqualBlamePathType(BlamePathType blamePathType) {
        return this.blamePathType.equals(blamePathType);
    }
}
