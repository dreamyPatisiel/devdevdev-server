package com.dreamypatisiel.devdevdev.domain.entity.embedded;

import com.dreamypatisiel.devdevdev.exception.TitleException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class TitleTest {

    @ParameterizedTest
    @MethodSource("createValidString")
    @DisplayName("제목은 빈 값이거나 "+Title.MAX_TITLE_LENGTH+"자 이상이다.")
    void createTitle(String title) {
        // given // when // then
        assertThatCode(() -> new Title(title))
                .doesNotThrowAnyException();
    }

    static Stream<Arguments> createValidString() {
        String commentContentLength50 = createStringBy(50);
        String commentContentLength49 = createStringBy(49);
        String commentContentLength1 = createStringBy(1);

        return Stream.of(
                Arguments.of(commentContentLength50),
                Arguments.of(commentContentLength49),
                Arguments.of(commentContentLength1)
        );
    }

    public static String createStringBy(int length) {
        return "a".repeat(Math.max(0, length));
    }

    @ParameterizedTest
    @MethodSource("createInValidString")
    @DisplayName("제목은 빈 값이거나 "+Title.MAX_TITLE_LENGTH+"자 이상일 수 없다.")
    void createTitleException(String title) {
        // given // when // then
        assertThatThrownBy(() -> new Title(title))
                .isInstanceOf(TitleException.class)
                .hasMessage(Title.getInvalidTitleExceptionMessage());
    }

    static Stream<Arguments> createInValidString() {
        String commentContentLength51 = createStringBy(51);
        String commentContentLength0 = createStringBy(0);

        return Stream.of(
                Arguments.of(commentContentLength51),
                Arguments.of(commentContentLength0)
        );
    }

}
