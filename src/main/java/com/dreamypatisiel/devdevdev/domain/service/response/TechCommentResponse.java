package com.dreamypatisiel.devdevdev.domain.service.response;

import com.dreamypatisiel.devdevdev.domain.entity.TechComment;
import lombok.Builder;
import lombok.Data;

@Data
public class TechCommentResponse {

    private final Long techCommentId;

    @Builder
    public TechCommentResponse(Long techCommentId) {
        this.techCommentId = techCommentId;
    }

    public static TechCommentResponse from(TechComment techComment) {
        return TechCommentResponse.builder()
                .techCommentId(techComment.getId())
                .build();
    }
}
