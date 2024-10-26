package com.dreamypatisiel.devdevdev.web.dto.response.techArticle;

import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.TechComment;
import com.dreamypatisiel.devdevdev.global.common.TimeProvider;
import com.dreamypatisiel.devdevdev.web.dto.util.CommentResponseUtil;
import com.dreamypatisiel.devdevdev.web.dto.util.CommonResponseUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import lombok.Builder;
import lombok.Data;

import javax.annotation.Nullable;
import java.time.LocalDateTime;

@Data
public class TechRepliedCommentsResponse {
    private Long techCommentId;
    private Long memberId;
    private Long techCommentParentMemberId;
    private Long techCommentParentId;
    private Long techCommentOriginParentId;

    private Boolean isCommentAuthor;
    private Boolean isRecommended;

    private String techCommentParentAuthor;
    private String author;
    private String maskedEmail;
    private String contents;
    private Long recommendTotalCount;
    private Boolean isDeleted;
    private Boolean isModified;


    @JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = TimeProvider.DEFAULT_ZONE_ID)
    private LocalDateTime createdAt;

    @Builder
    public TechRepliedCommentsResponse(Long techCommentId, Long memberId, Long techCommentParentMemberId,
                                       Long techCommentParentId, Long techCommentOriginParentId, Boolean isCommentAuthor,
                                       Boolean isRecommended, String techCommentParentAuthor, String author,
                                       String maskedEmail, String contents, Long recommendTotalCount, Boolean isDeleted,
                                       Boolean isModified, LocalDateTime createdAt) {
        this.techCommentId = techCommentId;
        this.memberId = memberId;
        this.techCommentParentMemberId = techCommentParentMemberId;
        this.techCommentParentId = techCommentParentId;
        this.techCommentOriginParentId = techCommentOriginParentId;
        this.isCommentAuthor = isCommentAuthor;
        this.isRecommended = isRecommended;
        this.techCommentParentAuthor = techCommentParentAuthor;
        this.author = author;
        this.maskedEmail = maskedEmail;
        this.contents = contents;
        this.recommendTotalCount = recommendTotalCount;
        this.isDeleted = isDeleted;
        this.isModified = isModified;
        this.createdAt = createdAt;
    }

    public static TechRepliedCommentsResponse of(@Nullable Member member, TechComment repliedTechComment) {

        Member createdBy = repliedTechComment.getCreatedBy();
        TechComment techCommentParent = repliedTechComment.getParent();

        return TechRepliedCommentsResponse.builder()
                .techCommentId(repliedTechComment.getId())
                .memberId(createdBy.getId())
                .author(createdBy.getNickname().getNickname())
                .techCommentParentMemberId(techCommentParent.getCreatedBy().getId())
                .techCommentParentAuthor(techCommentParent.getCreatedBy().getNicknameAsString())
                .techCommentParentId(repliedTechComment.getParent().getId())
                .techCommentOriginParentId(repliedTechComment.getOriginParent().getId())
                .createdAt(repliedTechComment.getCreatedAt())
                .isCommentAuthor(CommentResponseUtil.isTechCommentAuthor(member, repliedTechComment))
                .maskedEmail(CommonResponseUtil.sliceAndMaskEmail(createdBy.getEmail().getEmail()))
                .contents(CommentResponseUtil.getCommentByTechCommentStatus(repliedTechComment))
                .recommendTotalCount(repliedTechComment.getRecommendTotalCount().getCount())
                .isRecommended(CommentResponseUtil.isTechCommentRecommendedByMember(member, repliedTechComment))
                .isDeleted(repliedTechComment.isDeleted())
                .isModified(repliedTechComment.isModified())
                .build();
    }
}
