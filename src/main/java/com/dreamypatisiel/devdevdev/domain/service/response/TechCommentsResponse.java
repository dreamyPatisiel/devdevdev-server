package com.dreamypatisiel.devdevdev.domain.service.response;

import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.TechComment;
import com.dreamypatisiel.devdevdev.domain.service.response.util.CommentResponseUtil;
import com.dreamypatisiel.devdevdev.domain.service.response.util.CommonResponseUtil;
import com.dreamypatisiel.devdevdev.global.common.TimeProvider;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class TechCommentsResponse {
    private Long techCommentId;
    private Long memberId;
    private String author;
    private String maskedEmail;
    private String contents;
    private Long replyTotalCount;
    private Long likeTotalCount;
    private Boolean isDeleted;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = TimeProvider.DEFAULT_ZONE_ID)
    private LocalDateTime createdAt;

    private List<TechRepliedCommentsResponse> replies;

    @Builder
    public TechCommentsResponse(Long techCommentId, Long memberId, String author, String maskedEmail, String contents,
                                Long replyTotalCount, Long likeTotalCount, Boolean isDeleted, LocalDateTime createdAt,
                                List<TechRepliedCommentsResponse> replies) {
        this.techCommentId = techCommentId;
        this.memberId = memberId;
        this.author = author;
        this.maskedEmail = maskedEmail;
        this.contents = contents;
        this.replyTotalCount = replyTotalCount;
        this.likeTotalCount = likeTotalCount;
        this.isDeleted = isDeleted;
        this.createdAt = createdAt;
        this.replies = replies;
    }

    public static TechCommentsResponse from(TechComment originParentTechComment,
                                            List<TechRepliedCommentsResponse> replies) {
        Member createdBy = originParentTechComment.getCreatedBy();

        return TechCommentsResponse.builder()
                .techCommentId(originParentTechComment.getId())
                .memberId(createdBy.getId())
                .author(createdBy.getNickname().getNickname())
                .maskedEmail(CommonResponseUtil.sliceAndMaskEmail(createdBy.getEmail().getEmail()))
                .contents(CommentResponseUtil.getCommentByTechCommentStatus(originParentTechComment))
                .replyTotalCount(originParentTechComment.getReplyTotalCount().getCount())
                .likeTotalCount(originParentTechComment.getRecommendTotalCount().getCount())
                .isDeleted(originParentTechComment.isDeleted())
                .createdAt(originParentTechComment.getCreatedAt())
                .replies(replies)
                .build();
    }
}
