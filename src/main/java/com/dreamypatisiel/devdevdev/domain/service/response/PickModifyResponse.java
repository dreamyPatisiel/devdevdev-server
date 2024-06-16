package com.dreamypatisiel.devdevdev.domain.service.response;

import com.dreamypatisiel.devdevdev.domain.entity.Pick;
import lombok.Builder;
import lombok.Data;

@Data
public class PickModifyResponse {

    private final Long pickId;


    @Builder
    public PickModifyResponse(Long pickId) {
        this.pickId = pickId;
    }

    public static PickModifyResponse from(Pick pick) {
        return PickModifyResponse.builder()
                .pickId(pick.getId())
                .build();
    }
}
