package com.dreamypatisiel.devdevdev.domain.repository.comment;

import com.querydsl.core.annotations.QueryProjection;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class MyWrittenCommentDto {
    private Long postId;
    private String postTitle;
    private Long commentId;
    private String commentType;
    private String commentContents;
    private Long commentRecommendTotalCount;
    private LocalDateTime commentCreatedAt;
    private String pickOptionTitle;
    private String pickOptionType;

    @QueryProjection
    public MyWrittenCommentDto(Long postId, String postTitle, Long commentId, String commentType,
                               String commentContents, Long commentRecommendTotalCount, LocalDateTime commentCreatedAt,
                               String pickOptionTitle, String pickOptionType) {
        this.postId = postId;
        this.postTitle = postTitle;
        this.commentId = commentId;
        this.commentType = commentType;
        this.commentContents = commentContents;
        this.commentRecommendTotalCount = commentRecommendTotalCount;
        this.commentCreatedAt = commentCreatedAt;
        this.pickOptionTitle = pickOptionTitle;
        this.pickOptionType = pickOptionType;
    }
}
