package com.dreamypatisiel.devdevdev.domain.service.pick.request;

import lombok.Builder;
import lombok.Data;

@Data
public class DeletePickOptionImageRequest {
    private String name;
    private Long pickOptionImageId;

    @Builder
    public DeletePickOptionImageRequest(String name, Long pickOptionImageId) {
        this.name = name;
        this.pickOptionImageId = pickOptionImageId;
    }
}
