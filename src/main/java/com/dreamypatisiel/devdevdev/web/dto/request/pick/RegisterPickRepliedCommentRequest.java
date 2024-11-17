package com.dreamypatisiel.devdevdev.web.dto.request.pick;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RegisterPickRepliedCommentRequest {

    @NotBlank(message = "내용을 작성해주세요.")
    private String contents;

    public RegisterPickRepliedCommentRequest(String contents) {
        this.contents = contents;
    }
}
