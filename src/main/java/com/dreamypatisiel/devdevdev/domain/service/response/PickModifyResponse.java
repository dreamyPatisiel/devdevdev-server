package com.dreamypatisiel.devdevdev.domain.service.response;

import lombok.Data;

@Data
public class PickModifyResponse {
    private final Long pickId;

    public PickModifyResponse(Long pickId) {
        this.pickId = pickId;
    }
}
