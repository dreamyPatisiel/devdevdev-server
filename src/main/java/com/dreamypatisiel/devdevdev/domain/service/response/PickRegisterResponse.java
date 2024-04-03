package com.dreamypatisiel.devdevdev.domain.service.response;

import lombok.Data;

@Data
public class PickRegisterResponse {
    private final Long pickId;

    public PickRegisterResponse(Long pickId) {
        this.pickId = pickId;
    }
}
