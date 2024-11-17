package com.dreamypatisiel.devdevdev.web.dto.request.pick;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ModifyPickCommentRequest {

    @NotBlank(message = "내용을 작성해주세요.")
    private String contents;

    public ModifyPickCommentRequest(String contents) {
        this.contents = contents;
    }
}
