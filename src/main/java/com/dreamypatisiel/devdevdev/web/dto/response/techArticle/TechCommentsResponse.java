package com.dreamypatisiel.devdevdev.web.dto.response.techArticle;

import com.dreamypatisiel.devdevdev.domain.entity.AnonymousMember;
import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.TechComment;
import com.dreamypatisiel.devdevdev.global.common.TimeProvider;
import com.dreamypatisiel.devdevdev.web.dto.util.CommentResponseUtil;
import com.dreamypatisiel.devdevdev.web.dto.util.CommonResponseUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.List;
import javax.annotation.Nullable;
import lombok.Builder;
import lombok.Data;

@Data
public class TechCommentsResponse {
    private Long techCommentId;
    private Long memberId;
    private Long anonymousMemberId;
    private String author;
    private String maskedEmail;
    private String contents;
    private Long replyTotalCount;
    private Long recommendTotalCount;
    private Boolean isCommentAuthor;
    private Boolean isRecommended;
    private Boolean isModified;
    private Boolean isDeleted;
    private List<TechRepliedCommentsResponse> replies;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = TimeProvider.DEFAULT_ZONE_ID)
    private LocalDateTime createdAt;

    @Builder
    public TechCommentsResponse(Long techCommentId, Long memberId, Long anonymousMemberId, String author, String maskedEmail,
                                String contents, Long replyTotalCount, Long recommendTotalCount, Boolean isDeleted,
                                Boolean isCommentAuthor, Boolean isModified, Boolean isRecommended,
                                List<TechRepliedCommentsResponse> replies, LocalDateTime createdAt) {
        this.techCommentId = techCommentId;
        this.memberId = memberId;
        this.anonymousMemberId = anonymousMemberId;
        this.author = author;
        this.maskedEmail = maskedEmail;
        this.contents = contents;
        this.replyTotalCount = replyTotalCount;
        this.recommendTotalCount = recommendTotalCount;
        this.isDeleted = isDeleted;
        this.isCommentAuthor = isCommentAuthor;
        this.isModified = isModified;
        this.isRecommended = isRecommended;
        this.replies = replies;
        this.createdAt = createdAt;
    }

    public static TechCommentsResponse of(@Nullable Member member, @Nullable AnonymousMember anonymousMember,
                                          TechComment originParentTechComment, List<TechRepliedCommentsResponse> replies) {

        Member createdBy = originParentTechComment.getCreatedBy();
        AnonymousMember createdAnonymousBy = originParentTechComment.getCreatedAnonymousBy();

        // 회원이 작성한 댓글 응답
        if (originParentTechComment.isCreatedMember()) {
            return createTechCommentsResponseByCreatedMember(member, anonymousMember, originParentTechComment, replies,
                    createdBy);
        }

        // 익명회원이 작성한 댓글 응답
        return createTechCommentsResponseByCreatedAnonymousMember(member, anonymousMember, originParentTechComment, replies,
                createdAnonymousBy);
    }

    private static TechCommentsResponse createTechCommentsResponseByCreatedAnonymousMember(Member member,
                                                                                           AnonymousMember anonymousMember,
                                                                                           TechComment originParentTechComment,
                                                                                           List<TechRepliedCommentsResponse> replies,
                                                                                           AnonymousMember createdAnonymousBy) {
        return TechCommentsResponse.builder()
                .techCommentId(originParentTechComment.getId())
                .anonymousMemberId(createdAnonymousBy.getId())
                .author(createdAnonymousBy.getNickname())
                .contents(CommentResponseUtil.getCommentByTechCommentStatus(originParentTechComment))
                .replyTotalCount(originParentTechComment.getReplyTotalCount().getCount())
                .recommendTotalCount(originParentTechComment.getRecommendTotalCount().getCount())
                .isDeleted(originParentTechComment.isDeleted())
                .isModified(originParentTechComment.isModified())
                .isRecommended(CommentResponseUtil.isTechCommentRecommendedByMember(member, originParentTechComment))
                .createdAt(originParentTechComment.getCreatedAt())
                .isCommentAuthor(CommentResponseUtil.isTechCommentAuthor(member, anonymousMember, originParentTechComment))
                .replies(replies)
                .build();
    }

    private static TechCommentsResponse createTechCommentsResponseByCreatedMember(Member member, AnonymousMember anonymousMember,
                                                                                  TechComment originParentTechComment,
                                                                                  List<TechRepliedCommentsResponse> replies,
                                                                                  Member createdBy) {
        return TechCommentsResponse.builder()
                .techCommentId(originParentTechComment.getId())
                .memberId(createdBy.getId())
                .author(createdBy.getNickname().getNickname())
                .maskedEmail(CommonResponseUtil.sliceAndMaskEmail(createdBy.getEmail().getEmail()))
                .contents(CommentResponseUtil.getCommentByTechCommentStatus(originParentTechComment))
                .replyTotalCount(originParentTechComment.getReplyTotalCount().getCount())
                .recommendTotalCount(originParentTechComment.getRecommendTotalCount().getCount())
                .isDeleted(originParentTechComment.isDeleted())
                .isModified(originParentTechComment.isModified())
                .isRecommended(CommentResponseUtil.isTechCommentRecommendedByMember(member, originParentTechComment))
                .createdAt(originParentTechComment.getCreatedAt())
                .isCommentAuthor(CommentResponseUtil.isTechCommentAuthor(member, anonymousMember, originParentTechComment))
                .replies(replies)
                .build();
    }
}
