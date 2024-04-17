package com.dreamypatisiel.devdevdev.web.controller.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

/**
 * nested dto를 검증하려면 @Valid 어노테이션
 */
@Data
public class ModifyPickRequest {

    @NotBlank(message = "픽픽픽 제목을 작성해주세요.")
    private final String pickTitle;

    @Valid
    private final Map<String, ModifyPickOptionRequest> pickOptions;

    @Builder
    public ModifyPickRequest(String pickTitle, Map<String, ModifyPickOptionRequest> pickOptions) {
        this.pickTitle = pickTitle;
        this.pickOptions = pickOptions;
    }
}
