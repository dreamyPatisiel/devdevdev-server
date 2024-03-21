package com.dreamypatisiel.devdevdev.domain.service.response;

import com.dreamypatisiel.devdevdev.domain.entity.PickOptionImage;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class PickUploadImageResponse {
    private List<PickOptionImagesResponse> pickOptionImages;

    @Builder
    public PickUploadImageResponse(List<PickOptionImagesResponse> pickOptionImages) {
        this.pickOptionImages = pickOptionImages;
    }

    public static PickUploadImageResponse from(List<PickOptionImage> pickOptionImages) {
        return PickUploadImageResponse.builder()
                .pickOptionImages(PickOptionImagesResponse.mapToPickOptionsResponse(pickOptionImages))
                .build();
    }
}
