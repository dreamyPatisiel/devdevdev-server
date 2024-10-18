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
    private Long parentCommentMemberId; // 부모 댓글의 작성자 회원 아이디
    private Long pickParentCommentId;
    private Long pickOriginParentCommentId;

    @JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = TimeProvider.DEFAULT_ZONE_ID)
    private LocalDateTime createdAt;

    private Boolean isCommentOfPickAuthor; // 댓글 작성자가 픽픽픽 작성자인지 여부
    private Boolean isCommentAuthor; // 로그인한 회원이 댓글 작성자인지 여부
    private Boolean isRecommended; // 로그인한 회원이 댓글 추천을 했는지 여부
    private String parentCommentAuthor; // 부모 댓글의 작성자 닉네임
    private String author;
    private String maskedEmail;
    private String contents;
    private Long likeTotalCount;
    private Boolean isModified;
    private Boolean isDeleted;

    @Builder
    public PickRepliedCommentsResponse(Long pickCommentId, Long memberId, Long parentCommentMemberId,
                                       Long pickParentCommentId, Long pickOriginParentCommentId,
                                       LocalDateTime createdAt, Boolean isCommentOfPickAuthor, Boolean isCommentAuthor,
                                       Boolean isRecommended, String parentCommentAuthor, String author,
                                       String maskedEmail, String contents, Long likeTotalCount,
                                       Boolean isModified, Boolean isDeleted) {
        this.pickCommentId = pickCommentId;
        this.memberId = memberId;
        this.parentCommentMemberId = parentCommentMemberId;
        this.pickParentCommentId = pickParentCommentId;
        this.pickOriginParentCommentId = pickOriginParentCommentId;
        this.createdAt = createdAt;
        this.isCommentOfPickAuthor = isCommentOfPickAuthor;
        this.isCommentAuthor = isCommentAuthor;
        this.isRecommended = isRecommended;
        this.parentCommentAuthor = parentCommentAuthor;
        this.author = author;
        this.maskedEmail = maskedEmail;
        this.contents = contents;
        this.likeTotalCount = likeTotalCount;
        this.isModified = isModified;
        this.isDeleted = isDeleted;
    }

    // member 가 null 인 경우 익명회원 응답
    public static PickRepliedCommentsResponse of(@Nullable Member member, PickComment repliedPickComment) {

        Member createdBy = repliedPickComment.getCreatedBy();
        PickComment parentPickComment = repliedPickComment.getParent();

        return PickRepliedCommentsResponse.builder()
                .pickCommentId(repliedPickComment.getId())
                .memberId(createdBy.getId())
                .parentCommentMemberId(parentPickComment.getCreatedBy().getId())
                .author(createdBy.getNickname().getNickname())
                .parentCommentAuthor(parentPickComment.getCreatedBy().getNicknameAsString())
                .pickParentCommentId(parentPickComment.getId())
                .pickOriginParentCommentId(repliedPickComment.getOriginParent().getId())
                .createdAt(repliedPickComment.getCreatedAt())
                .isCommentOfPickAuthor(CommentResponseUtil.isPickAuthor(createdBy, repliedPickComment.getPick()))
                .isCommentAuthor(CommentResponseUtil.isPickCommentAuthor(member, repliedPickComment))
                .isRecommended(CommentResponseUtil.isPickCommentRecommended(member,
                        repliedPickComment.getPickCommentRecommends()))
                .maskedEmail(CommonResponseUtil.sliceAndMaskEmail(createdBy.getEmail().getEmail()))
                .contents(CommentResponseUtil.getCommentByPickCommentStatus(repliedPickComment))
                .likeTotalCount(repliedPickComment.getRecommendTotalCount().getCount())
                .isModified(repliedPickComment.isModified())
                .isDeleted(repliedPickComment.isDeleted())
                .build();
    }
}
