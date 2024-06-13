package com.dreamypatisiel.devdevdev.domain.service.response;

import com.dreamypatisiel.devdevdev.domain.entity.Pick;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import lombok.Builder;
import lombok.Data;

@Data
public class PickModifyResponse {

    private final Long pickId;

    @JsonProperty(access = Access.WRITE_ONLY)
    private final String pickTitle;

    @Builder
    public PickModifyResponse(Long pickId, String pickTitle) {
        this.pickId = pickId;
        this.pickTitle = pickTitle;
    }

    public static PickModifyResponse from(Pick pick) {
        return PickModifyResponse.builder()
                .pickId(pick.getId())
                .pickTitle(pick.getTitle().getTitle())
                .build();
    }
}
