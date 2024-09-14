package com.dreamypatisiel.devdevdev.web.dto.response.pick;

import com.dreamypatisiel.devdevdev.domain.entity.PickOptionImage;
import lombok.Builder;
import lombok.Data;

@Data
public class PickDetailOptionImageResponse {
    private final Long id;
    private final String imageUrl;

    @Builder
    public PickDetailOptionImageResponse(Long id, String imageUrl) {
        this.id = id;
        this.imageUrl = imageUrl;
    }

    public static PickDetailOptionImageResponse from(PickOptionImage pickOptionImage) {
        return PickDetailOptionImageResponse.builder()
                .id(pickOptionImage.getId())
                .imageUrl(pickOptionImage.getImageUrl())
                .build();
    }
}
