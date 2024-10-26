package com.dreamypatisiel.devdevdev.web.dto.util;

import com.dreamypatisiel.devdevdev.domain.entity.*;

import javax.annotation.Nullable;
import java.util.Optional;

public class CommentResponseUtil {
    public static String getCommentByPickCommentStatus(PickComment pickComment) {
        if (pickComment.isDeleted()) {
            // 댓글 작성자에 의해 삭제된 경우
            if (pickComment.getDeletedBy().isEqualsId(pickComment.getCreatedBy().getId())) {
                return "댓글 작성자에 의해 삭제된 댓글입니다.";
            }
            return "커뮤니티 정책을 위반하여 삭제된 댓글입니다.";
        }

        return pickComment.getContents().getCommentContents();
    }

    public static String getCommentByTechCommentStatus(TechComment techComment) {
        if (techComment.isDeleted()) {
            // 댓글 작성자에 의해 삭제된 경우
            if (techComment.getDeletedBy().isEqualsId(techComment.getCreatedBy().getId())) {
                return "댓글 작성자에 의해 삭제된 댓글입니다.";
            }
            return "커뮤니티 정책을 위반하여 삭제된 댓글입니다.";
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

    public static boolean isPickCommentAuthor(@Nullable Member member, PickComment pickComment) {
        // member 가 null 인 경우 익명회원이 조회한 것
        if (member == null) {
            return false;
        }
        return pickComment.getCreatedBy().isEqualId(member.getId());
    }

    public static boolean isTechCommentAuthor(Member member, TechComment techComment) {
        // member 가 null 인 경우 익명회원이 조회한 것
        if (member == null) {
            return false;
        }
        return techComment.getCreatedBy().isEqualId(member.getId());
    }

    public static boolean isTechCommentRecommendedByMember(@Nullable Member member, TechComment techComment) {
        // member가 null 인 경우 익명회원이 조회한 것
        if (member == null) {
            return false;
        }

        Optional<TechCommentRecommend> recommends = techComment.getRecommends().stream()
                .filter(recommend -> recommend.getMember().isEqualId(member.getId()))
                .findAny();

        return recommends.map(TechCommentRecommend::isRecommended).orElse(false);
    }
}
