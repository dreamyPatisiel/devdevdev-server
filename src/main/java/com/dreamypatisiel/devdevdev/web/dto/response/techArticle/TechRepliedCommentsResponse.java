package com.dreamypatisiel.devdevdev.web.dto.response.techArticle;

import com.dreamypatisiel.devdevdev.domain.entity.AnonymousMember;
import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.TechComment;
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
public class TechRepliedCommentsResponse {
    private Long techCommentId;
    private Long memberId;
    private Long anonymousMemberId;
    private Long techParentCommentMemberId;
    private Long techParentCommentAnonymousMemberId; // 부모 댓글의 작성자 익명회원 아이디
    private Long techParentCommentId;
    private Long techOriginParentCommentId;

    private Boolean isCommentAuthor;
    private Boolean isRecommended;

    private String techParentCommentAuthor;
    private String author;
    private String maskedEmail;
    private String contents;
    private Long recommendTotalCount;
    private Boolean isDeleted;
    private Boolean isModified;

    @JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = TimeProvider.DEFAULT_ZONE_ID)
    private LocalDateTime createdAt;

    @Builder
    public TechRepliedCommentsResponse(Long techCommentId, Long memberId, Long anonymousMemberId, Long techParentCommentMemberId,
                                       Long techParentCommentAnonymousMemberId, Long techParentCommentId,
                                       Long techOriginParentCommentId, Boolean isCommentAuthor, Boolean isRecommended,
                                       String techParentCommentAuthor, String author, String maskedEmail, String contents,
                                       Long recommendTotalCount, Boolean isDeleted, Boolean isModified, LocalDateTime createdAt) {
        this.techCommentId = techCommentId;
        this.memberId = memberId;
        this.anonymousMemberId = anonymousMemberId;
        this.techParentCommentMemberId = techParentCommentMemberId;
        this.techParentCommentAnonymousMemberId = techParentCommentAnonymousMemberId;
        this.techParentCommentId = techParentCommentId;
        this.techOriginParentCommentId = techOriginParentCommentId;
        this.isCommentAuthor = isCommentAuthor;
        this.isRecommended = isRecommended;
        this.techParentCommentAuthor = techParentCommentAuthor;
        this.author = author;
        this.maskedEmail = maskedEmail;
        this.contents = contents;
        this.recommendTotalCount = recommendTotalCount;
        this.isDeleted = isDeleted;
        this.isModified = isModified;
        this.createdAt = createdAt;
    }

    public static TechRepliedCommentsResponse of(@Nullable Member member, @Nullable AnonymousMember anonymousMember,
                                                 TechComment repliedTechComment) {

        // 부모 댓글
        TechComment parentTechComment = repliedTechComment.getParent();

        // 부모 댓글/답글 익명회원이 작성한 경우
        if (parentTechComment.isCreatedAnonymousMember() && repliedTechComment.isCreatedAnonymousMember()) {
            return createResponseForAnonymousReplyToAnonymous(member, anonymousMember, repliedTechComment, parentTechComment);
        }

        // 부모 댓글은 익명회원이 작성하고 답글은 회원이 작성한 경우
        if (parentTechComment.isCreatedAnonymousMember() && repliedTechComment.isCreatedMember()) {
            return createResponseForMemberReplyToAnonymous(member, anonymousMember, repliedTechComment, parentTechComment);
        }

        // 부모 댓글은 회원이 작성하고 답글은 익명회원이 작성한 경우
        if (parentTechComment.isCreatedMember() && repliedTechComment.isCreatedAnonymousMember()) {
            return createResponseForAnonymousReplyToMember(member, anonymousMember, repliedTechComment, parentTechComment);
        }

        // 부모 댓글/답글 회원이 작성한 경우
        return createResponseForMemberReplyToMember(member, anonymousMember, repliedTechComment, parentTechComment);
    }

    private static TechRepliedCommentsResponse createResponseForAnonymousReplyToAnonymous(Member member,
                                                                                          AnonymousMember anonymousMember,
                                                                                          TechComment repliedTechComment,
                                                                                          TechComment parentTechComment) {

        AnonymousMember parentCreatedAnonymousBy = parentTechComment.getCreatedAnonymousBy();
        AnonymousMember repliedCreatedAnonymousBy = repliedTechComment.getCreatedAnonymousBy();

        return TechRepliedCommentsResponse.builder()
                .techCommentId(repliedTechComment.getId())
                .anonymousMemberId(repliedCreatedAnonymousBy.getId())
                .author(repliedCreatedAnonymousBy.getNickname())
                .techParentCommentAnonymousMemberId(parentCreatedAnonymousBy.getId())
                .techParentCommentAuthor(parentCreatedAnonymousBy.getNickname())
                .techParentCommentId(repliedTechComment.getParent().getId())
                .techOriginParentCommentId(repliedTechComment.getOriginParent().getId())
                .createdAt(repliedTechComment.getCreatedAt())
                .isCommentAuthor(CommentResponseUtil.isTechCommentAuthor(member, anonymousMember, repliedTechComment))
                .contents(CommentResponseUtil.getCommentByTechCommentStatus(repliedTechComment))
                .recommendTotalCount(repliedTechComment.getRecommendTotalCount().getCount())
                .isRecommended(CommentResponseUtil.isTechCommentRecommendedByMember(member, repliedTechComment))
                .isDeleted(repliedTechComment.isDeleted())
                .isModified(repliedTechComment.isModified())
                .build();
    }

    private static TechRepliedCommentsResponse createResponseForMemberReplyToAnonymous(Member member,
                                                                                       AnonymousMember anonymousMember,
                                                                                       TechComment repliedTechComment,
                                                                                       TechComment parentTechComment) {

        AnonymousMember parentCreatedAnonymousBy = parentTechComment.getCreatedAnonymousBy();
        Member repliedCreatedBy = repliedTechComment.getCreatedBy();

        return TechRepliedCommentsResponse.builder()
                .techCommentId(repliedTechComment.getId())
                .memberId(repliedCreatedBy.getId())
                .author(repliedCreatedBy.getNickname().getNickname())
                .techParentCommentAnonymousMemberId(parentCreatedAnonymousBy.getId())
                .techParentCommentAuthor(parentCreatedAnonymousBy.getNickname())
                .techParentCommentId(repliedTechComment.getParent().getId())
                .techOriginParentCommentId(repliedTechComment.getOriginParent().getId())
                .createdAt(repliedTechComment.getCreatedAt())
                .isCommentAuthor(CommentResponseUtil.isTechCommentAuthor(member, anonymousMember, repliedTechComment))
                .maskedEmail(CommonResponseUtil.sliceAndMaskEmail(repliedCreatedBy.getEmail().getEmail()))
                .contents(CommentResponseUtil.getCommentByTechCommentStatus(repliedTechComment))
                .recommendTotalCount(repliedTechComment.getRecommendTotalCount().getCount())
                .isRecommended(CommentResponseUtil.isTechCommentRecommendedByMember(member, repliedTechComment))
                .isDeleted(repliedTechComment.isDeleted())
                .isModified(repliedTechComment.isModified())
                .build();
    }

    private static TechRepliedCommentsResponse createResponseForMemberReplyToMember(Member member,
                                                                                    AnonymousMember anonymousMember,
                                                                                    TechComment repliedTechComment,
                                                                                    TechComment parentTechComment) {

        Member parentCreatedBy = parentTechComment.getCreatedBy();
        Member repliedCreatedBy = repliedTechComment.getCreatedBy();

        return TechRepliedCommentsResponse.builder()
                .techCommentId(repliedTechComment.getId())
                .memberId(repliedCreatedBy.getId())
                .author(repliedCreatedBy.getNickname().getNickname())
                .techParentCommentMemberId(parentCreatedBy.getId())
                .techParentCommentAuthor(parentCreatedBy.getNicknameAsString())
                .techParentCommentId(repliedTechComment.getParent().getId())
                .techOriginParentCommentId(repliedTechComment.getOriginParent().getId())
                .createdAt(repliedTechComment.getCreatedAt())
                .isCommentAuthor(CommentResponseUtil.isTechCommentAuthor(member, anonymousMember, repliedTechComment))
                .maskedEmail(CommonResponseUtil.sliceAndMaskEmail(repliedCreatedBy.getEmail().getEmail()))
                .contents(CommentResponseUtil.getCommentByTechCommentStatus(repliedTechComment))
                .recommendTotalCount(repliedTechComment.getRecommendTotalCount().getCount())
                .isRecommended(CommentResponseUtil.isTechCommentRecommendedByMember(member, repliedTechComment))
                .isDeleted(repliedTechComment.isDeleted())
                .isModified(repliedTechComment.isModified())
                .build();
    }

    private static TechRepliedCommentsResponse createResponseForAnonymousReplyToMember(Member member,
                                                                                       AnonymousMember anonymousMember,
                                                                                       TechComment repliedTechComment,
                                                                                       TechComment parentTechComment) {

        Member parentCreatedBy = parentTechComment.getCreatedBy();
        AnonymousMember repliedCreatedAnonymousBy = repliedTechComment.getCreatedAnonymousBy();

        return TechRepliedCommentsResponse.builder()
                .techCommentId(repliedTechComment.getId())
                .anonymousMemberId(repliedCreatedAnonymousBy.getId())
                .author(repliedCreatedAnonymousBy.getNickname())
                .techParentCommentMemberId(parentCreatedBy.getId())
                .techParentCommentAuthor(parentCreatedBy.getNicknameAsString())
                .techParentCommentId(repliedTechComment.getParent().getId())
                .techOriginParentCommentId(repliedTechComment.getOriginParent().getId())
                .createdAt(repliedTechComment.getCreatedAt())
                .isCommentAuthor(CommentResponseUtil.isTechCommentAuthor(member, anonymousMember, repliedTechComment))
                .contents(CommentResponseUtil.getCommentByTechCommentStatus(repliedTechComment))
                .recommendTotalCount(repliedTechComment.getRecommendTotalCount().getCount())
                .isRecommended(CommentResponseUtil.isTechCommentRecommendedByMember(member, repliedTechComment))
                .isDeleted(repliedTechComment.isDeleted())
                .isModified(repliedTechComment.isModified())
                .build();
    }
}
