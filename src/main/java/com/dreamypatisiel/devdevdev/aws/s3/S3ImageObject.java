package com.dreamypatisiel.devdevdev.aws.s3;

import com.dreamypatisiel.devdevdev.domain.entity.PickOptionImage;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;

@Data
public class S3ImageObject {

    @JsonIgnore
    public static final String UPLOAD_FAIL = "UPLOAD_FAIL";

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

    public static S3ImageObject fail() {
        return S3ImageObject.builder()
                .imageUrl(UPLOAD_FAIL)
                .key(UPLOAD_FAIL)
                .build();
    }
}
