package com.dreamypatisiel.devdevdev.web.dto.response.techArticle;

import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.TechComment;
import com.dreamypatisiel.devdevdev.global.common.TimeProvider;
import com.dreamypatisiel.devdevdev.web.dto.util.CommentResponseUtil;
import com.dreamypatisiel.devdevdev.web.dto.util.CommonResponseUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import javax.annotation.Nullable;
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
    private Boolean isCommentAuthor;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = TimeProvider.DEFAULT_ZONE_ID)
    private LocalDateTime createdAt;

    private List<TechRepliedCommentsResponse> replies;

    @Builder
    public TechCommentsResponse(Long techCommentId, Long memberId, String author, String maskedEmail, String contents,
                                Long replyTotalCount, Long likeTotalCount, Boolean isDeleted, LocalDateTime createdAt,
                                Boolean isCommentAuthor, List<TechRepliedCommentsResponse> replies) {
        this.techCommentId = techCommentId;
        this.memberId = memberId;
        this.author = author;
        this.maskedEmail = maskedEmail;
        this.contents = contents;
        this.replyTotalCount = replyTotalCount;
        this.isCommentAuthor = isCommentAuthor;
        this.likeTotalCount = likeTotalCount;
        this.isDeleted = isDeleted;
        this.createdAt = createdAt;
        this.replies = replies;
    }

    public static TechCommentsResponse of(@Nullable Member member,
                                          TechComment originParentTechComment,
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
                .isCommentAuthor(CommentResponseUtil.isTechCommentAuthor(member, originParentTechComment))
                .replies(replies)
                .build();
    }
}
