package com.dreamypatisiel.devdevdev.web.dto.response.pick;

import com.dreamypatisiel.devdevdev.domain.entity.AnonymousMember;
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
    private Long anonymousMemberId;
    private Long pickParentCommentMemberId; // 부모 댓글의 작성자 회원 아이디
    private Long pickParentCommentAnonymousMemberId; // 부모 댓글의 작성자 익명회원 아이디
    private Long pickParentCommentId;
    private Long pickOriginParentCommentId;

    @JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = TimeProvider.DEFAULT_ZONE_ID)
    private LocalDateTime createdAt;

    private Boolean isCommentOfPickAuthor; // 댓글 작성자가 픽픽픽 작성자인지 여부
    private Boolean isCommentAuthor; // 로그인한 회원이 댓글 작성자인지 여부
    private Boolean isRecommended; // 로그인한 회원이 댓글 추천을 했는지 여부
    private String pickParentCommentAuthor; // 부모 댓글의 작성자 닉네임
    private String author;
    private String maskedEmail;
    private String contents;
    private Long recommendTotalCount;
    private Boolean isModified;
    private Boolean isDeleted;

    @Builder
    public PickRepliedCommentsResponse(Long pickCommentId, Long memberId, Long anonymousMemberId, Long pickParentCommentMemberId,
                                       Long pickParentCommentId, Long pickOriginParentCommentId,
                                       Long pickParentCommentAnonymousMemberId, LocalDateTime createdAt,
                                       Boolean isCommentOfPickAuthor, Boolean isCommentAuthor,
                                       Boolean isRecommended, String pickParentCommentAuthor, String author,
                                       String maskedEmail, String contents, Long recommendTotalCount,
                                       Boolean isModified, Boolean isDeleted) {
        this.pickCommentId = pickCommentId;
        this.memberId = memberId;
        this.anonymousMemberId = anonymousMemberId;
        this.pickParentCommentMemberId = pickParentCommentMemberId;
        this.pickParentCommentId = pickParentCommentId;
        this.pickOriginParentCommentId = pickOriginParentCommentId;
        this.pickParentCommentAnonymousMemberId = pickParentCommentAnonymousMemberId;
        this.createdAt = createdAt;
        this.isCommentOfPickAuthor = isCommentOfPickAuthor;
        this.isCommentAuthor = isCommentAuthor;
        this.isRecommended = isRecommended;
        this.pickParentCommentAuthor = pickParentCommentAuthor;
        this.author = author;
        this.maskedEmail = maskedEmail;
        this.contents = contents;
        this.recommendTotalCount = recommendTotalCount;
        this.isModified = isModified;
        this.isDeleted = isDeleted;
    }

    public static PickRepliedCommentsResponse of(@Nullable Member member, @Nullable AnonymousMember anonymousMember,
                                                 PickComment repliedPickComment) {

        // 부모 댓글
        PickComment parentPickComment = repliedPickComment.getParent();

        // 부모 댓글/답글 익명회원이 작성한 경우
        if (parentPickComment.isCreatedAnonymousMember() && repliedPickComment.isCreatedAnonymousMember()) {
            return createResponseForAnonymousReplyToAnonymous(member, anonymousMember, repliedPickComment, parentPickComment);
        }

        // 부모 댓글은 익명회원이 작성하고 답글은 회원이 작성한 경우
        if (parentPickComment.isCreatedAnonymousMember() && repliedPickComment.isCreatedMember()) {
            return createResponseForMemberReplyToAnonymous(member, anonymousMember, repliedPickComment, parentPickComment);
        }

        // 부모 댓글은 회원이 작성하고 답글은 익명회원이 작성한 경우
        if (parentPickComment.isCreatedMember() && repliedPickComment.isCreatedAnonymousMember()) {
            return createResponseForAnonymousReplyToMember(member, anonymousMember, repliedPickComment, parentPickComment);
        }

        // 부모 댓글/답글 회원이 작성한 경우
        return createResponseForMemberReplyToMember(member, anonymousMember, repliedPickComment, parentPickComment);
    }

    private static PickRepliedCommentsResponse createResponseForMemberReplyToMember(Member member,
                                                                                    AnonymousMember anonymousMember,
                                                                                    PickComment repliedPickComment,
                                                                                    PickComment parentPickComment) {
        Member parentCreatedBy = parentPickComment.getCreatedBy();
        Member repliedCreatedBy = repliedPickComment.getCreatedBy();

        return PickRepliedCommentsResponse.builder()
                .pickCommentId(repliedPickComment.getId())
                .memberId(repliedCreatedBy.getId())
                .pickParentCommentMemberId(parentCreatedBy.getId())
                .author(repliedCreatedBy.getNickname().getNickname())
                .pickParentCommentAuthor(parentCreatedBy.getNicknameAsString())
                .pickParentCommentId(parentPickComment.getId())
                .pickOriginParentCommentId(repliedPickComment.getOriginParent().getId())
                .createdAt(repliedPickComment.getCreatedAt())
                .isCommentOfPickAuthor(CommentResponseUtil.isPickAuthor(repliedCreatedBy, repliedPickComment.getPick()))
                .isCommentAuthor(CommentResponseUtil.isPickCommentAuthor(member, anonymousMember, repliedPickComment))
                .isRecommended(CommentResponseUtil.isPickCommentRecommended(member, repliedPickComment))
                .maskedEmail(CommonResponseUtil.sliceAndMaskEmail(repliedCreatedBy.getEmail().getEmail()))
                .contents(CommentResponseUtil.getCommentByPickCommentStatus(repliedPickComment))
                .recommendTotalCount(repliedPickComment.getRecommendTotalCount().getCount())
                .isModified(repliedPickComment.isModified())
                .isDeleted(repliedPickComment.isDeleted())
                .build();
    }

    private static PickRepliedCommentsResponse createResponseForMemberReplyToAnonymous(Member member,
                                                                                       AnonymousMember anonymousMember,
                                                                                       PickComment repliedPickComment,
                                                                                       PickComment parentPickComment) {

        AnonymousMember parentCreatedAnonymousBy = parentPickComment.getCreatedAnonymousBy();
        Member repliedCreatedBy = repliedPickComment.getCreatedBy();

        return PickRepliedCommentsResponse.builder()
                .pickCommentId(repliedPickComment.getId())
                .memberId(repliedCreatedBy.getId())
                .pickParentCommentAnonymousMemberId(parentCreatedAnonymousBy.getId())
                .author(repliedCreatedBy.getNickname().getNickname())
                .pickParentCommentAuthor(parentCreatedAnonymousBy.getNickname())
                .pickParentCommentId(parentPickComment.getId())
                .pickOriginParentCommentId(repliedPickComment.getOriginParent().getId())
                .createdAt(repliedPickComment.getCreatedAt())
                .isCommentOfPickAuthor(CommentResponseUtil.isPickAuthor(repliedCreatedBy, repliedPickComment.getPick()))
                .isCommentAuthor(CommentResponseUtil.isPickCommentAuthor(member, anonymousMember, repliedPickComment))
                .isRecommended(CommentResponseUtil.isPickCommentRecommended(member, repliedPickComment))
                .maskedEmail(CommonResponseUtil.sliceAndMaskEmail(repliedCreatedBy.getEmail().getEmail()))
                .contents(CommentResponseUtil.getCommentByPickCommentStatus(repliedPickComment))
                .recommendTotalCount(repliedPickComment.getRecommendTotalCount().getCount())
                .isModified(repliedPickComment.isModified())
                .isDeleted(repliedPickComment.isDeleted())
                .build();
    }

    private static PickRepliedCommentsResponse createResponseForAnonymousReplyToMember(Member member,
                                                                                       AnonymousMember anonymousMember,
                                                                                       PickComment repliedPickComment,
                                                                                       PickComment parentPickComment) {

        Member parentCreatedBy = parentPickComment.getCreatedBy();
        AnonymousMember repliedCreatedAnonymousBy = repliedPickComment.getCreatedAnonymousBy();

        return PickRepliedCommentsResponse.builder()
                .pickCommentId(repliedPickComment.getId())
                .anonymousMemberId(repliedCreatedAnonymousBy.getId())
                .pickParentCommentAnonymousMemberId(parentCreatedBy.getId())
                .author(repliedCreatedAnonymousBy.getNickname())
                .pickParentCommentAuthor(parentCreatedBy.getNicknameAsString())
                .pickParentCommentId(parentPickComment.getId())
                .pickOriginParentCommentId(repliedPickComment.getOriginParent().getId())
                .createdAt(repliedPickComment.getCreatedAt())
                .isCommentOfPickAuthor(CommentResponseUtil.isPickAuthor(null, repliedPickComment.getPick()))
                .isCommentAuthor(CommentResponseUtil.isPickCommentAuthor(member, anonymousMember, repliedPickComment))
                .isRecommended(CommentResponseUtil.isPickCommentRecommended(member, repliedPickComment))
                .contents(CommentResponseUtil.getCommentByPickCommentStatus(repliedPickComment))
                .recommendTotalCount(repliedPickComment.getRecommendTotalCount().getCount())
                .isModified(repliedPickComment.isModified())
                .isDeleted(repliedPickComment.isDeleted())
                .build();
    }

    private static PickRepliedCommentsResponse createResponseForAnonymousReplyToAnonymous(Member member,
                                                                                          AnonymousMember anonymousMember,
                                                                                          PickComment repliedPickComment,
                                                                                          PickComment parentPickComment) {

        AnonymousMember parentCreatedAnonymousBy = parentPickComment.getCreatedAnonymousBy();
        AnonymousMember repliedCreatedAnonymousBy = repliedPickComment.getCreatedAnonymousBy();

        return PickRepliedCommentsResponse.builder()
                .pickCommentId(repliedPickComment.getId())
                .anonymousMemberId(repliedCreatedAnonymousBy.getId())
                .pickParentCommentAnonymousMemberId(parentCreatedAnonymousBy.getId())
                .author(repliedCreatedAnonymousBy.getNickname())
                .pickParentCommentAuthor(parentCreatedAnonymousBy.getNickname())
                .pickParentCommentId(parentPickComment.getId())
                .pickOriginParentCommentId(repliedPickComment.getOriginParent().getId())
                .createdAt(repliedPickComment.getCreatedAt())
                .isCommentOfPickAuthor(CommentResponseUtil.isPickAuthor(null, repliedPickComment.getPick()))
                .isCommentAuthor(CommentResponseUtil.isPickCommentAuthor(member, anonymousMember, repliedPickComment))
                .isRecommended(CommentResponseUtil.isPickCommentRecommended(member, repliedPickComment))
                .contents(CommentResponseUtil.getCommentByPickCommentStatus(repliedPickComment))
                .recommendTotalCount(repliedPickComment.getRecommendTotalCount().getCount())
                .isModified(repliedPickComment.isModified())
                .isDeleted(repliedPickComment.isDeleted())
                .build();
    }
}
