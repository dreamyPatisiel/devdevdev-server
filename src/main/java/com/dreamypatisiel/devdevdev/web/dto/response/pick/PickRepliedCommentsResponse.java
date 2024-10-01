package com.dreamypatisiel.devdevdev.web.dto.response.pick;

import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.PickComment;
import com.dreamypatisiel.devdevdev.global.common.TimeProvider;
import com.dreamypatisiel.devdevdev.web.dto.util.CommentResponseUtil;
import com.dreamypatisiel.devdevdev.web.dto.util.CommonResponseUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import java.time.LocalDateTime;
import javax.annotation.Nullable;
import lombok.Builder;
import lombok.Data;

@Data
public class PickRepliedCommentsResponse {
    private Long pickCommentId;
    private Long memberId;
    private Long pickCommentParentId;
    private Long pickCommentOriginParentId;

    @JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = TimeProvider.DEFAULT_ZONE_ID)
    private LocalDateTime createdAt;

    private Boolean isCommentOfPickAuthor; // 댓글 작성자가 픽픽픽 작성자인지 여부
    private Boolean isCommentAuthor; // 로그인한 회원이 댓글 작성자인지 여부
    private String author;
    private String maskedEmail;
    private String contents;
    private Long likeTotalCount;
    private Boolean isDeleted;

    @Builder
    public PickRepliedCommentsResponse(Long pickCommentId, Long memberId, Long pickCommentParentId,
                                       Long pickCommentOriginParentId, LocalDateTime createdAt,
                                       Boolean isCommentOfPickAuthor,
                                       Boolean isCommentAuthor, String author, String maskedEmail, String contents,
                                       Long likeTotalCount, Boolean isDeleted) {
        this.pickCommentId = pickCommentId;
        this.memberId = memberId;
        this.pickCommentParentId = pickCommentParentId;
        this.pickCommentOriginParentId = pickCommentOriginParentId;
        this.createdAt = createdAt;
        this.isCommentOfPickAuthor = isCommentOfPickAuthor;
        this.isCommentAuthor = isCommentAuthor;
        this.author = author;
        this.maskedEmail = maskedEmail;
        this.contents = contents;
        this.likeTotalCount = likeTotalCount;
        this.isDeleted = isDeleted;
    }

    // member 가 null 인 경우 익명회원 응답
    public static PickRepliedCommentsResponse of(@Nullable Member member, PickComment repliedPickComment) {

        Member createdBy = repliedPickComment.getCreatedBy();

        return PickRepliedCommentsResponse.builder()
                .pickCommentId(repliedPickComment.getId())
                .memberId(createdBy.getId())
                .author(createdBy.getNickname().getNickname())
                .pickCommentParentId(repliedPickComment.getParent().getId())
                .pickCommentOriginParentId(repliedPickComment.getOriginParent().getId())
                .createdAt(repliedPickComment.getCreatedAt())
                .isCommentOfPickAuthor(CommentResponseUtil.isPickAuthor(createdBy, repliedPickComment.getPick()))
                .isCommentAuthor(CommentResponseUtil.isPickCommentAuthor(member, repliedPickComment))
                .maskedEmail(CommonResponseUtil.sliceAndMaskEmail(createdBy.getEmail().getEmail()))
                .contents(CommentResponseUtil.getCommentByPickCommentStatus(repliedPickComment))
                .likeTotalCount(repliedPickComment.getRecommendTotalCount().getCount())
                .isDeleted(repliedPickComment.isDeleted())
                .build();
    }
}
