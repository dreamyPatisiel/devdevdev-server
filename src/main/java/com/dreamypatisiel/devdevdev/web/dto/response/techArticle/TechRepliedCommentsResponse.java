package com.dreamypatisiel.devdevdev.web.dto.response.techArticle;

import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.TechComment;
import com.dreamypatisiel.devdevdev.global.common.TimeProvider;
import com.dreamypatisiel.devdevdev.web.dto.util.CommentResponseUtil;
import com.dreamypatisiel.devdevdev.web.dto.util.CommonResponseUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
public class TechRepliedCommentsResponse {
    private Long techCommentId;
    private Long memberId;
    private String author;
    private String maskedEmail;
    private String contents;
    private Long recommendTotalCount;
    private Boolean isDeleted;

    private Long techCommentParentId;
    private Long techCommentOriginParentId;

    @JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = TimeProvider.DEFAULT_ZONE_ID)
    private LocalDateTime createdAt;

    @Builder
    public TechRepliedCommentsResponse(Long techCommentId, Long memberId, String author, String maskedEmail,
                                       String contents, Long recommendTotalCount, Boolean isDeleted,
                                       Long techCommentParentId,
                                       Long techCommentOriginParentId, LocalDateTime createdAt) {
        this.techCommentId = techCommentId;
        this.memberId = memberId;
        this.author = author;
        this.maskedEmail = maskedEmail;
        this.contents = contents;
        this.recommendTotalCount = recommendTotalCount;
        this.isDeleted = isDeleted;
        this.techCommentParentId = techCommentParentId;
        this.techCommentOriginParentId = techCommentOriginParentId;
        this.createdAt = createdAt;
    }

    public static TechRepliedCommentsResponse from(TechComment repliedTechComment) {

        Member createdBy = repliedTechComment.getCreatedBy();

        return TechRepliedCommentsResponse.builder()
                .techCommentId(repliedTechComment.getId())
                .memberId(createdBy.getId())
                .author(createdBy.getNickname().getNickname())
                .techCommentParentId(repliedTechComment.getParent().getId())
                .techCommentOriginParentId(repliedTechComment.getOriginParent().getId())
                .createdAt(repliedTechComment.getCreatedAt())
                .maskedEmail(CommonResponseUtil.sliceAndMaskEmail(createdBy.getEmail().getEmail()))
                .contents(CommentResponseUtil.getCommentByTechCommentStatus(repliedTechComment))
                .recommendTotalCount(repliedTechComment.getRecommendTotalCount().getCount())
                .isDeleted(repliedTechComment.isDeleted())
                .build();
    }
}
