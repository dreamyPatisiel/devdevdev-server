package com.dreamypatisiel.devdevdev.web.controller.pick.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ModifyPickCommentRequest {

    @NotBlank(message = "내용을 작성해주세요.")
    private final String contents;
}
