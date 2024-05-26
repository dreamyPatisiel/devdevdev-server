package com.dreamypatisiel.devdevdev.domain.service.response;

import com.dreamypatisiel.devdevdev.domain.entity.PickOptionImage;
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

    public static PickDetailOptionImage from(PickOptionImage pickOptionImage) {
        return PickDetailOptionImage.builder()
                .id(pickOptionImage.getId())
                .imageUrl(pickOptionImage.getImageUrl())
                .build();
    }
}
