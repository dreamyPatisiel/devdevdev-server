package com.dreamypatisiel.devdevdev.web.dto.response.pick;

import com.dreamypatisiel.devdevdev.domain.entity.PickOptionImage;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
public class PickOptionImagesResponse {
    private final String name;
    private final Long pickOptionImageId;
    private final String imageUrl;
    private final String imageKey;

    @Builder
    public PickOptionImagesResponse(String name, Long pickOptionImageId, String imageUrl, String imageKey) {
        this.name = name;
        this.pickOptionImageId = pickOptionImageId;
        this.imageUrl = imageUrl;
        this.imageKey = imageKey;
    }

    public static PickOptionImagesResponse of(String name, Long pickOptionImageId, String imageUrl, String imageKey) {
        return PickOptionImagesResponse.builder()
                .name(name)
                .pickOptionImageId(pickOptionImageId)
                .imageUrl(imageUrl)
                .imageKey(imageKey)
                .build();
    }

    public static PickOptionImagesResponse from(PickOptionImage pickOptionImage) {
        return PickOptionImagesResponse.builder()
                .name(pickOptionImage.getName())
                .pickOptionImageId(pickOptionImage.getId())
                .imageUrl(pickOptionImage.getImageUrl())
                .imageKey(pickOptionImage.getImageKey())
                .build();
    }

    public static List<PickOptionImagesResponse> mapToPickOptionsResponse(List<PickOptionImage> pickOptionImages) {
        return pickOptionImages.stream()
                .map(PickOptionImagesResponse::from)
                .toList();
    }
}
