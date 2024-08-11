package com.dreamypatisiel.devdevdev.domain.entity.embedded;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.dreamypatisiel.devdevdev.exception.CommentContentsException;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class CommentContentsTest {

    @ParameterizedTest
    @MethodSource("createValidString")
    @DisplayName(CommentContents.MIN_COMMENT_CONTENTS_LENGTH + "글자 이상 "
            + CommentContents.MAX_COMMENT_CONTENTS_LENGTH + "글자 이하의 "
            + "댓글을 입력할 수 있다.")
    void createCommentContents(String commentContents) {
        // given // when // then
        assertThatCode(() -> new CommentContents(commentContents))
                .doesNotThrowAnyException();
    }

    static Stream<Arguments> createValidString() {
        String commentContentsLength1000 = createStringBy(1000);
        String commentContentsLength999 = createStringBy(999);
        String commentContentsLength1 = createStringBy(1);

        return Stream.of(
                Arguments.of(commentContentsLength1000),
                Arguments.of(commentContentsLength999),
                Arguments.of(commentContentsLength1)
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
    @DisplayName(CommentContents.MIN_COMMENT_CONTENTS_LENGTH + "글자 이하 "
            + CommentContents.MAX_COMMENT_CONTENTS_LENGTH + "글자 이상의 "
            + "댓글을 입력할 수 없다.")
    void createCommentContentsException(String commentContents) {
        // given // when // then
        assertThatThrownBy(() -> new CommentContents(commentContents))
                .isInstanceOf(CommentContentsException.class)
                .hasMessage(CommentContents.getValidCommentContentsMessage());
    }

    static Stream<Arguments> createInValidString() {
        String commentContentsLength1001 = createStringBy(1001);
        String commentContentsLength0 = createStringBy(0);

        return Stream.of(
                Arguments.of(commentContentsLength1001),
                Arguments.of(commentContentsLength0)
        );
    }
}