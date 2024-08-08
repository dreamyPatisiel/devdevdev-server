package com.dreamypatisiel.devdevdev.web.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RegisterPickCommentRequest {

    @NotBlank(message = "내용을 작성해주세요.")
    private final String contents;
    private final Long pickOptionId;

    @NotNull(message = "픽픽픽 공개 여부는 필수 값 입니다.")
    private final Boolean isPickVotePublic;
}
