package com.dreamypatisiel.devdevdev.web.dto.response.pick;

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
import lombok.Builder;
import lombok.Data;
import org.springframework.util.ObjectUtils;

@Data
public class PickCommentsResponse {
    private Long pickCommentId;

    @JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = TimeProvider.DEFAULT_ZONE_ID)
    private LocalDateTime createdAt;

    private Long memberId;
    private String author;
    private Boolean isPickAuthor;
    private Boolean isPickCommentAuthor;
    private String maskedEmail;
    private PickOptionType votedPickOption;
    private String votedPickOptionTitle;
    private String contents;
    private Long replyTotalCount;
    private Long likeTotalCount;
    private Boolean isDeleted;
    private List<PickRepliedCommentsResponse> replies;

    @Builder
    public PickCommentsResponse(Long pickCommentId, LocalDateTime createdAt, Long memberId, String author,
                                Boolean isPickAuthor, Boolean isPickCommentAuthor, String maskedEmail,
                                PickOptionType votedPickOption, String votedPickOptionTitle, String contents,
                                Long replyTotalCount, Long likeTotalCount, Boolean isDeleted,
                                List<PickRepliedCommentsResponse> replies) {
        this.pickCommentId = pickCommentId;
        this.createdAt = createdAt;
        this.memberId = memberId;
        this.author = author;
        this.isPickAuthor = isPickAuthor;
        this.isPickCommentAuthor = isPickCommentAuthor;
        this.maskedEmail = maskedEmail;
        this.votedPickOption = votedPickOption;
        this.votedPickOptionTitle = votedPickOptionTitle;
        this.contents = contents;
        this.replyTotalCount = replyTotalCount;
        this.likeTotalCount = likeTotalCount;
        this.isDeleted = isDeleted;
        this.replies = replies;
    }

    public static PickCommentsResponse of(Member member, PickComment originParentPickComment,
                                          List<PickRepliedCommentsResponse> replies) {

        Member createdBy = originParentPickComment.getCreatedBy();
        PickVote pickVote = originParentPickComment.getPickVote();

        PickCommentsResponseBuilder responseBuilder = PickCommentsResponse.builder()
                .pickCommentId(originParentPickComment.getId())
                .createdAt(originParentPickComment.getCreatedAt())
                .memberId(createdBy.getId())
                .author(createdBy.getNickname().getNickname())
                .isPickAuthor(CommentResponseUtil.isPickAuthor(createdBy, originParentPickComment.getPick()))
                .isPickCommentAuthor(CommentResponseUtil.isPickCommentAuthor(member, originParentPickComment))
                .maskedEmail(CommonResponseUtil.sliceAndMaskEmail(createdBy.getEmail().getEmail()))
                .contents(CommentResponseUtil.getCommentByPickCommentStatus(originParentPickComment))
                .replyTotalCount((long) replies.size())
                .likeTotalCount(originParentPickComment.getRecommendTotalCount().getCount())
                .isDeleted(originParentPickComment.isDeleted())
                .replies(replies);

        // 회원이 픽픽픽 투표를 안했거나, 투표 비공개일 경우
        if (ObjectUtils.isEmpty(pickVote) || originParentPickComment.isVotePrivate()) {
            return responseBuilder.build();
        }

        return responseBuilder
                .votedPickOption(pickVote.getPickOption().getPickOptionType())
                .votedPickOptionTitle(pickVote.getPickOption().getTitle().getTitle())
                .build();
    }
}
