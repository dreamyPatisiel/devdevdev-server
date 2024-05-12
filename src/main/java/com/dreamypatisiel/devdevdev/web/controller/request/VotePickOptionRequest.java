package com.dreamypatisiel.devdevdev.web.controller.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
public class VotePickOptionRequest {

    @NotNull(message = "픽픽픽 아이디는 필수 입니다.")
    private final Long pickId;
    
    @NotNull(message = "픽픽픽 선택지 아이디는 필수 입니다.")
    private final Long pickOptionId;

    @Builder
    public VotePickOptionRequest(Long pickId, Long pickOptionId) {
        this.pickId = pickId;
        this.pickOptionId = pickOptionId;
    }
}
