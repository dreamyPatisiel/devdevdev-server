package com.dreamypatisiel.devdevdev.web.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
public class RegisterPickOptionRequest {

    @NotBlank(message = "선택지 제목을 작성해주세요.")
    private final String pickOptionTitle;

    @NotBlank(message = "선택지에 대한 설명을 작성해주세요.")
    private final String pickOptionContent;
    private final List<Long> pickOptionImageIds;

    @Builder
    public RegisterPickOptionRequest(String pickOptionTitle, String pickOptionContent, List<Long> pickOptionImageIds) {
        this.pickOptionTitle = pickOptionTitle;
        this.pickOptionContent = pickOptionContent;
        this.pickOptionImageIds = pickOptionImageIds;
    }
}
