package com.dreamypatisiel.devdevdev.domain.repository.comment;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class MyWrittenComment {
    private Long postId;
    private String postTitle;
    private Long commentId;
    private String commentType;
    private String commentContents;
    private LocalDateTime commentCreatedAt;
    private String pickOptionTitle;
    private String pickOptionType;
}
