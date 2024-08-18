package com.dreamypatisiel.devdevdev.web.controller.techArticle.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RegisterTechReplyRequest {

    @NotBlank(message = "댓글 내용을 작성해주세요.")
    private String contents;

    public RegisterTechReplyRequest(String contents) {
        this.contents = contents;
    }
}
