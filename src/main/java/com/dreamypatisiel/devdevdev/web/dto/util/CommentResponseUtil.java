package com.dreamypatisiel.devdevdev.web.dto.util;

import com.dreamypatisiel.devdevdev.domain.entity.AnonymousMember;
import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.Pick;
import com.dreamypatisiel.devdevdev.domain.entity.PickComment;
import com.dreamypatisiel.devdevdev.domain.entity.PickCommentRecommend;
import com.dreamypatisiel.devdevdev.domain.entity.TechComment;
import com.dreamypatisiel.devdevdev.domain.entity.TechCommentRecommend;
import java.util.Optional;
import javax.annotation.Nullable;

public class CommentResponseUtil {

    public static final String DELETE_COMMENT_MESSAGE = "댓글 작성자에 의해 삭제된 댓글입니다.";
    public static final String DELETE_INVALID_COMMUNITY_POLICY_COMMENT_MESSAGE = "커뮤니티 정책을 위반하여 삭제된 댓글입니다.";

    public static String getCommentByPickCommentStatus(PickComment pickComment) {
        if (pickComment.isDeleted()) {
            // 익명회원 작성자에 의해 삭제된 경우
            if (pickComment.isDeletedByAnonymousMember()) {
                AnonymousMember createdAnonymousBy = pickComment.getCreatedAnonymousBy();
                AnonymousMember deletedAnonymousBy = pickComment.getDeletedAnonymousBy();

                if (deletedAnonymousBy.isEqualAnonymousMemberId(createdAnonymousBy.getId())) {
                    return DELETE_COMMENT_MESSAGE;
                }
            }

            // 회원 작성자에 의해 삭제된 경우
            Member createdBy = pickComment.getCreatedBy();
            Member deletedBy = pickComment.getDeletedBy();

            // 익명회원이 작성한 댓글인 경우
            if (createdBy == null) {
                // 어드민이 삭제함
                return DELETE_INVALID_COMMUNITY_POLICY_COMMENT_MESSAGE;
            }

            if (deletedBy.isEqualsId(createdBy.getId())) {
                return DELETE_COMMENT_MESSAGE;
            }

            return DELETE_INVALID_COMMUNITY_POLICY_COMMENT_MESSAGE;
        }

        return pickComment.getContents().getCommentContents();
    }

    public static String getCommentByTechCommentStatus(TechComment techComment) {
        if (techComment.isDeleted()) {
            // 댓글 작성자에 의해 삭제된 경우
            if (techComment.getDeletedBy().isEqualsId(techComment.getCreatedBy().getId())) {
                return DELETE_COMMENT_MESSAGE;
            }
            return DELETE_INVALID_COMMUNITY_POLICY_COMMENT_MESSAGE;
        }

        return techComment.getContents().getCommentContents();
    }

    public static boolean isDeletedByAdmin(PickComment pickComment) {
        if (pickComment.isDeleted()) {
            return pickComment.getDeletedBy().isAdmin();
        }
        return false;
    }

    public static boolean isPickAuthor(@Nullable Member member, Pick pick) {
        if (member == null) {
            return false;
        }
        return pick.getMember().isEqualsId(member.getId());
    }

    public static boolean isPickCommentAuthor(@Nullable Member member, @Nullable AnonymousMember anonymousMember,
                                              PickComment pickComment) {

        // 회원이 조회한 경우
        if (member != null) {
            Member createdBy = pickComment.getCreatedBy();
            // createdBy가 null인 경우는 익명회원이 작성한 댓글
            if (createdBy == null) {
                return false;
            }

            return createdBy.isEqualsId(member.getId());
        }

        // 익명회원이 조회한 경우
        if (anonymousMember != null) {
            AnonymousMember createdAnonymousBy = pickComment.getCreatedAnonymousBy();
            // createdAnonymousBy 가 null인 경우는 회원이 작성한 댓글
            if (createdAnonymousBy == null) {
                return false;
            }

            return createdAnonymousBy.isEqualAnonymousMemberId(anonymousMember.getId());
        }

        return false;
    }

    public static boolean isPickCommentRecommended(@Nullable Member member, PickComment pickComment) {
        // member 가 null 인 경우 익명회원이 조회한 것
        if (member == null) {
            return false;
        }

        return pickComment.getPickCommentRecommends().stream()
                .filter(PickCommentRecommend::isRecommended)
                .anyMatch(pickCommentRecommend -> pickCommentRecommend.getMember().isEqualsId(member.getId()));
    }

    public static boolean isTechCommentAuthor(Member member, TechComment techComment) {
        // member 가 null 인 경우 익명회원이 조회한 것
        if (member == null) {
            return false;
        }
        return techComment.getCreatedBy().isEqualsId(member.getId());
    }

    public static boolean isTechCommentRecommendedByMember(@Nullable Member member, TechComment techComment) {
        // member가 null 인 경우 익명회원이 조회한 것
        if (member == null) {
            return false;
        }

        Optional<TechCommentRecommend> recommends = techComment.getRecommends().stream()
                .filter(recommend -> recommend.getMember().isEqualsId(member.getId()))
                .findAny();

        return recommends.map(TechCommentRecommend::isRecommended).orElse(false);
    }

    public static String createUniqueCommentId(String commentType, Long postId, Long commentId) {
        return commentType + "_" + postId + "_" + commentId;
    }
}
