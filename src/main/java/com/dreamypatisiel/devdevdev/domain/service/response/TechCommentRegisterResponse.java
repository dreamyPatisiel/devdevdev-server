package com.dreamypatisiel.devdevdev.domain.service.response;

import com.dreamypatisiel.devdevdev.domain.entity.TechComment;
import lombok.Builder;
import lombok.Data;

@Data
public class TechCommentRegisterResponse {

    private final Long techCommentId;

    @Builder
    public TechCommentRegisterResponse(Long techCommentId) {
        this.techCommentId = techCommentId;
    }

    public static TechCommentRegisterResponse from(TechComment techComment) {
        return TechCommentRegisterResponse.builder()
                .techCommentId(techComment.getId())
                .build();
    }
}
