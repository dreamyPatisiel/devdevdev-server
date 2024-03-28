package com.dreamypatisiel.devdevdev.web.controller.request;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
public class PickRegisterRequest {

    @NotBlank(message = "제목을 작성해주세요.")
    private final String pickTitle;
    private final Map<String, PickOptionRequest> pickOptions;

    @Builder
    public PickRegisterRequest(String pickTitle, Map<String, PickOptionRequest> pickOptions) {
        this.pickTitle = pickTitle;
        this.pickOptions = pickOptions;
    }
}
