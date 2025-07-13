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

        // 댓글
        Member repliedCreatedBy = repliedPickComment.getCreatedBy();
        AnonymousMember repliedCreatedAnonymousBy = repliedPickComment.getCreatedAnonymousBy();

        // 부모 댓글
        PickComment parentPickComment = repliedPickComment.getParent();
        Member parentCreatedBy = parentPickComment.getCreatedBy();
        AnonymousMember parentCreatedAnonymousBy = parentPickComment.getCreatedAnonymousBy();

        // 댓글을 익명회원이 작성한 경우
        if (repliedCreatedBy == null) {
            // 부모 댓글을 익명회원이 작성한 경우
            if (parentCreatedBy == null) {
                return createResponseForAnonymousReplyToAnonymous(member, anonymousMember, repliedPickComment,
                        repliedCreatedAnonymousBy, parentCreatedAnonymousBy, parentPickComment);
            }
            // 부모 댓글을 회원이 작성한 경우
            return createResponseForAnonymousReplyToMember(member, anonymousMember, repliedPickComment, repliedCreatedAnonymousBy,
                    parentCreatedBy, parentPickComment);
        }

        // 댓글을 회원이 작성한 경우
        // 부모 댓글을 익명회원이 작성한 경우
        if (parentCreatedBy == null) {
            return createResponseForMemberReplyToAnonymous(member, anonymousMember, repliedPickComment, repliedCreatedBy,
                    parentCreatedAnonymousBy, parentPickComment);
        }

        // 부모 댓글을 회원이 작성한 경우
        return createResponseForMemberReplyToMember(member, anonymousMember, repliedPickComment, repliedCreatedBy,
                parentPickComment);
    }

    private static PickRepliedCommentsResponse createResponseForMemberReplyToMember(Member member,
                                                                                    AnonymousMember anonymousMember,
                                                                                    PickComment repliedPickComment,
                                                                                    Member repliedCreatedBy,
                                                                                    PickComment parentPickComment) {
        return PickRepliedCommentsResponse.builder()
                .pickCommentId(repliedPickComment.getId())
                .memberId(repliedCreatedBy.getId())
                .pickParentCommentMemberId(parentPickComment.getCreatedBy().getId())
                .author(repliedCreatedBy.getNickname().getNickname())
                .pickParentCommentAuthor(parentPickComment.getCreatedBy().getNicknameAsString())
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
                                                                                       Member repliedCreatedBy,
                                                                                       AnonymousMember parentCreatedAnonymousBy,
                                                                                       PickComment parentPickComment) {
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
                                                                                       AnonymousMember repliedCreatedAnonymousBy,
                                                                                       Member parentCreatedBy,
                                                                                       PickComment parentPickComment) {
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
                                                                                          AnonymousMember repliedCreatedAnonymousBy,
                                                                                          AnonymousMember parentCreatedAnonymousBy,
                                                                                          PickComment parentPickComment) {
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
