package com.dreamypatisiel.devdevdev.domain.entity.embedded;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.dreamypatisiel.devdevdev.exception.CommentContentException;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class CommentContentsTest {

    @ParameterizedTest
    @MethodSource("createValidString")
    @DisplayName(CommentContents.MIN_COMMENT_CONTENT_LENGTH + "글자 이상 "
            + CommentContents.MAX_COMMENT_CONTENT_LENGTH + "글자 이하의 "
            + "댓글을 입력할 수 있다.")
    void createCommentContent(String commentContent) {
        // given // when // then
        assertThatCode(() -> new CommentContents(commentContent))
                .doesNotThrowAnyException();
    }

    static Stream<Arguments> createValidString() {
        String commentContentLength1000 = createStringBy(1000);
        String commentContentLength999 = createStringBy(999);
        String commentContentLength1 = createStringBy(1);

        return Stream.of(
                Arguments.of(commentContentLength1000),
                Arguments.of(commentContentLength999),
                Arguments.of(commentContentLength1)
        );
    }

    public static String createStringBy(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append("a");
        }

        return sb.toString();
    }

    @ParameterizedTest
    @MethodSource("createInValidString")
    @DisplayName(CommentContents.MIN_COMMENT_CONTENT_LENGTH + "글자 이하 "
            + CommentContents.MAX_COMMENT_CONTENT_LENGTH + "글자 이상의 "
            + "댓글을 입력할 수 없다.")
    void createCommentContentException(String commentContent) {
        // given // when // then
        assertThatThrownBy(() -> new CommentContents(commentContent))
                .isInstanceOf(CommentContentException.class)
                .hasMessage(CommentContents.getValidCommentContentMessage());
    }

    static Stream<Arguments> createInValidString() {
        String commentContentLength1001 = createStringBy(1001);
        String commentContentLength0 = createStringBy(0);

        return Stream.of(
                Arguments.of(commentContentLength1001),
                Arguments.of(commentContentLength0)
        );
    }
}