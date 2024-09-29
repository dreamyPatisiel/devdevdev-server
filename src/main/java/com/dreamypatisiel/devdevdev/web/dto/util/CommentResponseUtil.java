package com.dreamypatisiel.devdevdev.web.dto.util;

import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.Pick;
import com.dreamypatisiel.devdevdev.domain.entity.PickComment;
import com.dreamypatisiel.devdevdev.domain.entity.TechComment;

public class CommentResponseUtil {
    public static String getCommentByPickCommentStatus(PickComment pickComment) {
        if (pickComment.isDeleted()) {
            // 댓글 작성자에 의해 삭제된 경우
            if (pickComment.getDeletedBy().isEqualId(pickComment.getCreatedBy().getId())) {
                return "댓글 작성자에 의해 삭제된 댓글입니다.";
            }
            return "커뮤니티 정책을 위반하여 삭제된 댓글입니다.";
        }

        return pickComment.getContents().getCommentContents();
    }

    public static String getCommentByTechCommentStatus(TechComment techComment) {
        if (techComment.isDeleted()) {
            // 댓글 작성자에 의해 삭제된 경우
            if (techComment.getDeletedBy().isEqualId(techComment.getCreatedBy().getId())) {
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

    public static boolean isPickAuthor(Member member, Pick pick) {
        if (member == null) {
            return false;
        }
        return pick.getMember().isEqualId(member.getId());
    }

    public static boolean isPickCommentAuthor(Member member, PickComment pickComment) {
        // member 가 null 인 경우 익명회원이 조회한 것
        if (member == null) {
            return false;
        }
        return pickComment.getCreatedBy().isEqualId(member.getId());
    }
}
