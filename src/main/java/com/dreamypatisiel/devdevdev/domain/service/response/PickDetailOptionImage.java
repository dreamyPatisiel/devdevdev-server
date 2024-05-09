package com.dreamypatisiel.devdevdev.domain.service.response;

import lombok.Builder;
import lombok.Data;

@Data
public class PickDetailOptionImage {
    private final Long id;
    private final String imageUrl;

    @Builder
    public PickDetailOptionImage(Long id, String imageUrl) {
        this.id = id;
        this.imageUrl = imageUrl;
    }
}
