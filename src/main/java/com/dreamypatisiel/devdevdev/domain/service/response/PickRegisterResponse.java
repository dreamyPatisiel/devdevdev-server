package com.dreamypatisiel.devdevdev.domain.service.response;

import com.dreamypatisiel.devdevdev.domain.entity.Pick;
import lombok.Builder;
import lombok.Data;

@Data
public class PickRegisterResponse {

    private final Long pickId;

    @Builder
    public PickRegisterResponse(Long pickId) {
        this.pickId = pickId;
    }

    public static PickRegisterResponse from(Pick pick) {
        return PickRegisterResponse.builder()
                .pickId(pick.getId())
                .build();
    }
}
