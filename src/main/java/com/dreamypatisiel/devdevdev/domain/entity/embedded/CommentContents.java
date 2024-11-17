package com.dreamypatisiel.devdevdev.domain.entity.embedded;

import com.dreamypatisiel.devdevdev.exception.CommentContentsException;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
@Getter
public class CommentContents {
    public static final int MIN_COMMENT_CONTENTS_LENGTH = 1;
    public static final int MAX_COMMENT_CONTENTS_LENGTH = 1_000;
    public static final String VALID_COMMENT_CONTENTS_MESSAGE = "댓글은 %d글자 이상 %d글자 이하여야 합니다.";

    private String commentContents;

    public CommentContents(String commentContents) {
        validationCommentContents(commentContents);
        this.commentContents = commentContents;
    }

    private void validationCommentContents(String commentContents) {
        if (!isCommentContentsLength(commentContents)) {
            throw new CommentContentsException(getValidCommentContentsMessage());
        }
    }

    private static boolean isCommentContentsLength(String commentContents) {
        return StringUtils.hasText(commentContents) && commentContents.length() <= MAX_COMMENT_CONTENTS_LENGTH;
    }

    public static String getValidCommentContentsMessage() {
        return String.format(VALID_COMMENT_CONTENTS_MESSAGE,
                MIN_COMMENT_CONTENTS_LENGTH, MAX_COMMENT_CONTENTS_LENGTH);
    }
}
