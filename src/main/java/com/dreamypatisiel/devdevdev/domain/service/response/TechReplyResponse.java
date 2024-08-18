package com.dreamypatisiel.devdevdev.domain.service.response;

import com.dreamypatisiel.devdevdev.domain.entity.TechReply;
import lombok.Builder;
import lombok.Data;

@Data
public class TechReplyResponse {

    private final Long techReplyId;

    @Builder
    public TechReplyResponse(Long techReplyId) {
        this.techReplyId = techReplyId;
    }

    public static TechReplyResponse from(TechReply techReply) {
        return TechReplyResponse.builder()
                .techReplyId(techReply.getId())
                .build();
    }
}
