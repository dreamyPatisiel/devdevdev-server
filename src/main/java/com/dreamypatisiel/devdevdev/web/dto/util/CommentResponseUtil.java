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
    public static final String CONTACT_ADMIN_MESSAGE = "오류가 발생 했습니다. 관리자에게 문의 하세요.";

    public static String getCommentByPickCommentStatus(PickComment pickComment) {

        if (!pickComment.isDeleted()) {
            return pickComment.getContents().getCommentContents();
        }

        // 익명회원이 작성한 댓글인 경우
        if (pickComment.isCreatedAnonymousMember()) {
            // 자기자신이 삭제한 경우
            if (pickComment.isDeletedByAnonymousMember()) {
                return DELETE_COMMENT_MESSAGE;
            }

            // 어드민이 삭제한 경우
            if (pickComment.getDeletedBy().isAdmin()) {
                return DELETE_INVALID_COMMUNITY_POLICY_COMMENT_MESSAGE;
            }

            return CONTACT_ADMIN_MESSAGE;
        }

        // 회원이 작성한 댓글인 경우
        if (pickComment.isCreatedMember()) {
            // 자기 자신인 경우
            if (pickComment.isDeletedMemberByMySelf()) {
                return DELETE_COMMENT_MESSAGE;
            }

            // 어드민이 삭제한 경우
            if (pickComment.getDeletedBy().isAdmin()) {
                return DELETE_INVALID_COMMUNITY_POLICY_COMMENT_MESSAGE;
            }

            return CONTACT_ADMIN_MESSAGE;
        }

        return CONTACT_ADMIN_MESSAGE;
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

        // 회원이 조회하고 픽픽픽 댓글을 회원이 작성한 경우
        if (member != null && pickComment.isCreatedMember()) {
            // 픽픽픽 댓글을 회원이 작성한 경우
            return pickComment.getCreatedBy().isEqualsId(member.getId());
        }

        // 익명회원이 조회하고 픽픽픽 댓글을 익명회원이 작성한 경우
        if (anonymousMember != null && pickComment.isCreatedAnonymousMember()) {
            return pickComment.getCreatedAnonymousBy().isEqualAnonymousMemberId(anonymousMember.getId());
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

    public static boolean isTechCommentAuthor(@Nullable Member member, @Nullable AnonymousMember anonymousMember,
                                              TechComment techComment) {
        // 회원이 조회하고 기술블로그 댓글을 회원이 작성한 경우
        if (member != null && techComment.isCreatedMember()) {
            return techComment.getCreatedBy().isEqualsId(member.getId());
        }

        // 익명회원이 조회하고 기술블로그 댓글을 익명회원이 작성한 경우
        if (anonymousMember != null && techComment.isCreatedAnonymousMember()) {
            return techComment.getCreatedAnonymousBy().isEqualAnonymousMemberId(anonymousMember.getId());
        }

        return false;
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
