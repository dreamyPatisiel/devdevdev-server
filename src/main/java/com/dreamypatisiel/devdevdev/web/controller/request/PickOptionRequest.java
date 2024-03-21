package com.dreamypatisiel.devdevdev.web.controller.request;

import lombok.Builder;
import lombok.Data;

@Data
public class PickOptionRequest {
    private final Long pickOptionId;
    private final String pickOptionTitle;
    private final String pickOptionContent;

    @Builder
    public PickOptionRequest(Long pickOptionId, String pickOptionTitle, String pickOptionContent) {
        this.pickOptionId = pickOptionId;
        this.pickOptionTitle = pickOptionTitle;
        this.pickOptionContent = pickOptionContent;
    }
}
