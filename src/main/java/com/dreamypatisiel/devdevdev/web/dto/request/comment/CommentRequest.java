package com.dreamypatisiel.devdevdev.web.dto.request.comment;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CommentRequest {
    @NotNull(message = "픽픽픽 댓글 아이디는 필수 입니다.")
    private Long pickCommentId;

    @NotNull(message = "기술블로그 댓글 아이디는 필수 입니다.")
    private Long techCommentId;
}
