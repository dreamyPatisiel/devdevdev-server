package com.dreamypatisiel.devdevdev.web.dto.request.techArticle;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ModifyTechCommentRequest {

    @NotBlank(message = "댓글 내용을 작성해주세요.")
    private String contents;

    public ModifyTechCommentRequest(String contents) {
        this.contents = contents;
    }
}
