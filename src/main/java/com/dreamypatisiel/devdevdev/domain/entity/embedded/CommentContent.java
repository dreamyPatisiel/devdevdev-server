package com.dreamypatisiel.devdevdev.domain.entity.embedded;

import com.dreamypatisiel.devdevdev.exception.CommentContentException;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public class CommentContent {
    public static final int MIN_COMMENT_CONTENT_LENGTH = 1;
    public static final int MAX_COMMENT_CONTENT_LENGTH = 1_000;
    public static final String VALID_COMMENT_CONTENT_MESSAGE = "댓글은 %d글자 이상 %d글자 이하여야 합니다.";

    private String commentContent;

    public CommentContent(String commentContent) {
        validationCommentContent(commentContent);
        this.commentContent = commentContent;
    }

    private void validationCommentContent(String commentContent) {
        if(!isCommentContentLength(commentContent)) {
            throw new CommentContentException(getValidCommentContentMessage());
        }
    }

    private static boolean isCommentContentLength(String commentContent) {
        return StringUtils.hasText(commentContent) && commentContent.length() <= MAX_COMMENT_CONTENT_LENGTH;
    }

    public static String getValidCommentContentMessage() {
        return String.format(VALID_COMMENT_CONTENT_MESSAGE,
                MIN_COMMENT_CONTENT_LENGTH, MAX_COMMENT_CONTENT_LENGTH);
    }
}
