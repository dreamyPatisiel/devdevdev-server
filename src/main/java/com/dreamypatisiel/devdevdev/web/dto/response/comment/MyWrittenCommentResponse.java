package com.dreamypatisiel.devdevdev.web.dto.response.comment;

import com.dreamypatisiel.devdevdev.domain.repository.comment.MyWrittenCommentDto;
import com.dreamypatisiel.devdevdev.global.common.TimeProvider;
import com.dreamypatisiel.devdevdev.web.dto.util.CommentResponseUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import java.time.LocalDateTime;
import java.util.List;
import javax.annotation.Nullable;
import lombok.Builder;
import lombok.Data;

@Data
public class MyWrittenCommentResponse {
    private String uniqueCommentId;
    private Long postId;
    private String postTitle;
    private Long commentId;
    private String commentType;
    private String commentContents;
    private Long commentRecommendTotalCount;

    @JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = TimeProvider.DEFAULT_ZONE_ID)
    private LocalDateTime commentCreatedAt;

    private String pickOptionTitle;
    private String pickOptionType;

    @Builder
    public MyWrittenCommentResponse(String uniqueCommentId, Long postId, String postTitle,
                                    Long commentId, String commentType,
                                    String commentContents, Long commentRecommendTotalCount,
                                    LocalDateTime commentCreatedAt, @Nullable String pickOptionTitle,
                                    @Nullable String pickOptionType) {
        this.uniqueCommentId = uniqueCommentId;
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

    public static MyWrittenCommentResponse from(MyWrittenCommentDto myWrittenCommentDto) {

        Long postId = myWrittenCommentDto.getPostId();
        Long commentId = myWrittenCommentDto.getCommentId();
        String commentType = myWrittenCommentDto.getCommentType();

        return MyWrittenCommentResponse.builder()
                .uniqueCommentId(CommentResponseUtil.createUniqueCommentId(commentType, postId, commentId))
                .postId(postId)
                .postTitle(myWrittenCommentDto.getPostTitle())
                .commentId(commentId)
                .commentType(commentType)
                .commentContents(myWrittenCommentDto.getCommentContents())
                .commentRecommendTotalCount(myWrittenCommentDto.getCommentRecommendTotalCount())
                .commentCreatedAt(myWrittenCommentDto.getCommentCreatedAt())
                .pickOptionTitle(myWrittenCommentDto.getPickOptionTitle())
                .pickOptionType(myWrittenCommentDto.getPickOptionType())
                .build();
    }

    public static List<MyWrittenCommentResponse> from(List<MyWrittenCommentDto> myWrittenCommentsDto) {
        return myWrittenCommentsDto.stream()
                .map(MyWrittenCommentResponse::from)
                .toList();
    }
}
