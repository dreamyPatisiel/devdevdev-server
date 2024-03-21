package com.dreamypatisiel.devdevdev.aws.s3;

import com.dreamypatisiel.devdevdev.domain.entity.PickOptionImage;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
public class S3ImageObject {

    private Long pickOptionImageId;
    private final String imageUrl;
    private final String key;

    @Builder
    private S3ImageObject(Long pickOptionImageId, String imageUrl, String key) {
        this.pickOptionImageId = pickOptionImageId;
        this.imageUrl = imageUrl;
        this.key = key;
    }

    public static S3ImageObject of(String imageUrl, String key) {
        return S3ImageObject.builder()
                .imageUrl(imageUrl)
                .key(key)
                .build();
    }


    public static S3ImageObject from(PickOptionImage pickOptionImage) {
        return S3ImageObject.builder()
                .pickOptionImageId(pickOptionImage.getId())
                .imageUrl(pickOptionImage.getImageUrl())
                .key(pickOptionImage.getImageKey())
                .build();
    }
}
