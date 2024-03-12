package com.dreamypatisiel.devdevdev.web.controller.request;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
public class RegisterPickRequest {
    private final String pickTitle;
    private final List<PickOptionRequest> pickOptions;

    @Builder
    public RegisterPickRequest(String pickTitle, List<PickOptionRequest> pickOptions) {
        this.pickTitle = pickTitle;
        this.pickOptions = pickOptions;
    }
}
