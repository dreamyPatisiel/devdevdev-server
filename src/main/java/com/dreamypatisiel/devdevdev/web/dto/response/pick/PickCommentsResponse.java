package com.dreamypatisiel.devdevdev.web.dto.response.pick;

import com.dreamypatisiel.devdevdev.domain.entity.AnonymousMember;
import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.PickComment;
import com.dreamypatisiel.devdevdev.domain.entity.PickVote;
import com.dreamypatisiel.devdevdev.domain.entity.enums.PickOptionType;
import com.dreamypatisiel.devdevdev.global.common.TimeProvider;
import com.dreamypatisiel.devdevdev.web.dto.util.CommentResponseUtil;
import com.dreamypatisiel.devdevdev.web.dto.util.CommonResponseUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import java.time.LocalDateTime;
import java.util.List;
import javax.annotation.Nullable;
import lombok.Builder;
import lombok.Data;
import org.springframework.util.ObjectUtils;

@Data
public class PickCommentsResponse {
    private Long pickCommentId;

    @JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = TimeProvider.DEFAULT_ZONE_ID)
    private LocalDateTime createdAt;

    private Long memberId;
    private Long anonymousMemberId;
    private String author;
    private Boolean isCommentOfPickAuthor;
    private Boolean isCommentAuthor;
    private Boolean isRecommended;
    private String maskedEmail;
    private PickOptionType votedPickOption;
    private String votedPickOptionTitle;
    private String contents;
    private Long replyTotalCount;
    private Long recommendTotalCount;
    private Boolean isModified;
    private Boolean isDeleted;
    private List<PickRepliedCommentsResponse> replies;

    @Builder
    public PickCommentsResponse(Long pickCommentId, LocalDateTime createdAt, Long memberId, Long anonymousMemberId, String author,
                                Boolean isCommentOfPickAuthor, Boolean isCommentAuthor, String maskedEmail,
                                PickOptionType votedPickOption, String votedPickOptionTitle, String contents,
                                Long replyTotalCount, Long recommendTotalCount, Boolean isModified, Boolean isDeleted,
                                Boolean isRecommended, List<PickRepliedCommentsResponse> replies) {
        this.pickCommentId = pickCommentId;
        this.createdAt = createdAt;
        this.memberId = memberId;
        this.anonymousMemberId = anonymousMemberId;
        this.author = author;
        this.isCommentOfPickAuthor = isCommentOfPickAuthor;
        this.isCommentAuthor = isCommentAuthor;
        this.isRecommended = isRecommended;
        this.maskedEmail = maskedEmail;
        this.votedPickOption = votedPickOption;
        this.votedPickOptionTitle = votedPickOptionTitle;
        this.contents = contents;
        this.replyTotalCount = replyTotalCount;
        this.recommendTotalCount = recommendTotalCount;
        this.isModified = isModified;
        this.isDeleted = isDeleted;
        this.replies = replies;
    }

    public static PickCommentsResponse of(@Nullable Member member, @Nullable AnonymousMember anonymousMember,
                                          PickComment originParentPickComment, List<PickRepliedCommentsResponse> replies) {

        Member createdBy = originParentPickComment.getCreatedBy();
        AnonymousMember createdAnonymousBy = originParentPickComment.getCreatedAnonymousBy();
        PickVote pickVote = originParentPickComment.getPickVote();

        PickCommentsResponseBuilder responseBuilder = createPickCommentsResponseBuilder(
                member, anonymousMember, originParentPickComment, replies, createdBy, createdAnonymousBy);

        // 회원이 픽픽픽 투표를 안했거나, 투표 비공개일 경우
        if (ObjectUtils.isEmpty(pickVote) || originParentPickComment.isVotePrivate()) {
            return responseBuilder.build();
        }

        return responseBuilder
                .votedPickOption(pickVote.getPickOption().getPickOptionType())
                .votedPickOptionTitle(pickVote.getPickOption().getTitle().getTitle())
                .build();
    }

    private static PickCommentsResponseBuilder createPickCommentsResponseBuilder(Member member, AnonymousMember anonymousMember,
                                                                                 PickComment originParentPickComment,
                                                                                 List<PickRepliedCommentsResponse> replies,
                                                                                 Member createdBy,
                                                                                 AnonymousMember createdAnonymousBy) {
        // 익명회원이 작성한 댓글인 경우
        if (createdBy == null) {
            return PickCommentsResponse.builder()
                    .pickCommentId(originParentPickComment.getId())
                    .createdAt(originParentPickComment.getCreatedAt())
                    .anonymousMemberId(createdAnonymousBy.getId())
                    .author(createdAnonymousBy.getNickname())
                    .isCommentOfPickAuthor(CommentResponseUtil.isPickAuthor(null, originParentPickComment.getPick()))
                    .isCommentAuthor(CommentResponseUtil.isPickCommentAuthor(member, anonymousMember, originParentPickComment))
                    .isRecommended(CommentResponseUtil.isPickCommentRecommended(member, originParentPickComment))
                    .contents(CommentResponseUtil.getCommentByPickCommentStatus(originParentPickComment))
                    .replyTotalCount((long) replies.size())
                    .recommendTotalCount(originParentPickComment.getRecommendTotalCount().getCount())
                    .isModified(originParentPickComment.isModified())
                    .isDeleted(originParentPickComment.isDeleted())
                    .replies(replies);
        }

        return PickCommentsResponse.builder()
                .pickCommentId(originParentPickComment.getId())
                .createdAt(originParentPickComment.getCreatedAt())
                .memberId(createdBy.getId())
                .author(createdBy.getNickname().getNickname())
                .isCommentOfPickAuthor(CommentResponseUtil.isPickAuthor(createdBy, originParentPickComment.getPick()))
                .isCommentAuthor(CommentResponseUtil.isPickCommentAuthor(member, anonymousMember, originParentPickComment))
                .isRecommended(CommentResponseUtil.isPickCommentRecommended(member, originParentPickComment))
                .maskedEmail(CommonResponseUtil.sliceAndMaskEmail(createdBy.getEmail().getEmail()))
                .contents(CommentResponseUtil.getCommentByPickCommentStatus(originParentPickComment))
                .replyTotalCount((long) replies.size())
                .recommendTotalCount(originParentPickComment.getRecommendTotalCount().getCount())
                .isModified(originParentPickComment.isModified())
                .isDeleted(originParentPickComment.isDeleted())
                .replies(replies);
    }
}
