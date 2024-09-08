package com.dreamypatisiel.devdevdev.web.dto.util;

import com.dreamypatisiel.devdevdev.domain.entity.PickComment;

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

    public static boolean isDeletedByAdmin(PickComment pickComment) {
        if (pickComment.isDeleted()) {
            return pickComment.getDeletedBy().isAdmin();
        }
        return false;
    }
}
