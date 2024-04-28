package com.dreamypatisiel.devdevdev.web.controller.request;

import com.dreamypatisiel.devdevdev.domain.entity.enums.PickOptionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
public class RegisterPickRequest {

    @NotBlank(message = "제목을 작성해주세요.")
    private final String pickTitle;
    @Valid
    private final Map<PickOptionType, RegisterPickOptionRequest> pickOptions;

    @Builder
    public RegisterPickRequest(String pickTitle, Map<PickOptionType, RegisterPickOptionRequest> pickOptions) {
        this.pickTitle = pickTitle;
        this.pickOptions = pickOptions;
    }
}
