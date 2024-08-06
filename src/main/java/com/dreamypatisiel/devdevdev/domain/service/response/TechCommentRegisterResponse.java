package com.dreamypatisiel.devdevdev.domain.service.response;

import com.dreamypatisiel.devdevdev.domain.entity.TechComment;
import lombok.Builder;
import lombok.Data;

@Data
public class TechCommentRegisterResponse {

    private final Long id;

    @Builder
    public TechCommentRegisterResponse(Long id) {
        this.id = id;
    }

    public static TechCommentRegisterResponse from(TechComment techComment) {
        return TechCommentRegisterResponse.builder()
                .id(techComment.getId())
                .build();
    }
}
