package com.dreamypatisiel.devdevdev.web.controller.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
public class RegisterTechCommentRequest {

    @NotBlank(message = "댓글 내용을 작성해주세요.")
    private final String contents;

    @Builder
    public RegisterTechCommentRequest(String contents) {
        this.contents = contents;
    }
}
