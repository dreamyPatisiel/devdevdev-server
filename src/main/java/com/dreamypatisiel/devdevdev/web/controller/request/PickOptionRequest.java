package com.dreamypatisiel.devdevdev.web.controller.request;

import com.dreamypatisiel.devdevdev.domain.entity.PickOptionImage;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class PickOptionRequest {
    private final String pickOptionTitle;
    private final String pickOptionContent;
    private final List<PickOptionImageRequest> pickOptionImages;

    @Builder
    public PickOptionRequest(String pickOptionTitle, String pickOptionContent, List<PickOptionImageRequest> pickOptionImages) {
        this.pickOptionTitle = pickOptionTitle;
        this.pickOptionContent = pickOptionContent;
        this.pickOptionImages = pickOptionImages;
    }
}
