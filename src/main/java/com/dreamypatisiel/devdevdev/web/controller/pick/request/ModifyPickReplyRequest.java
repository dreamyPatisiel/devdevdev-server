package com.dreamypatisiel.devdevdev.web.controller.pick.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ModifyPickReplyRequest {

    @NotBlank(message = "내용을 작성해주세요.")
    private String contents;

    public ModifyPickReplyRequest(String contents) {
        this.contents = contents;
    }
}
