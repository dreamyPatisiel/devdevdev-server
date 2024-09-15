package com.dreamypatisiel.devdevdev.domain.service.response.util;

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
}
