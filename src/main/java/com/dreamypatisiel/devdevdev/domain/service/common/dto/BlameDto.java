package com.dreamypatisiel.devdevdev.domain.service.common.dto;

import com.dreamypatisiel.devdevdev.web.dto.request.common.BlameRequest;
import lombok.Builder;
import lombok.Data;

@Data
public class BlameDto {
    private final Long memberId;
    private Long pickId;
    private Long pickCommentId;
    private Long techArticleId;
    private Long techCommentId;

    public BlameDto(Long memberId) {
        this.memberId = memberId;
    }

    @Builder
    public BlameDto(Long memberId, Long pickId, Long pickCommentId, Long techArticleId, Long techCommentId) {
        this.memberId = memberId;
        this.pickId = pickId;
        this.pickCommentId = pickCommentId;
        this.techArticleId = techArticleId;
        this.techCommentId = techCommentId;
    }

    public static BlameDto of(Long memberId, BlameRequest blameRequest) {
        BlameDto blameDto = new BlameDto(memberId);
        blameDto.pickId = blameRequest.getPickId();
        blameDto.pickCommentId = blameRequest.getPickCommentId();
        blameDto.techArticleId = blameRequest.getTechArticleId();
        blameDto.techCommentId = blameRequest.getTechArticleCommentId();

        return blameDto;
    }

    public boolean isBlamePick() {
        return this.pickId != null && this.pickCommentId == null;
    }

    public boolean isBlamePickComment() {
        return this.pickId != null && this.pickCommentId != null;
    }

    public boolean isBlameTechArticleComment() {
        return this.techArticleId != null && this.techCommentId != null;
    }
}
