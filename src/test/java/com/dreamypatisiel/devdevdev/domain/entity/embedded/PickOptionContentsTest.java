package com.dreamypatisiel.devdevdev.domain.entity.embedded;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.dreamypatisiel.devdevdev.exception.TopicContentsException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;

class PickOptionContentsTest {

    @ParameterizedTest
    @NullSource
    @MethodSource("generateValidData")
    @DisplayName(PickOptionContents.MAX_PICK_CONTENTS_LENGTH + " 길이의 토픽 내용을 생성할 수 있다.")
    void pickOptionContents(String pickOptionContents) {
        // given // when // then
        assertThatCode(() -> new PickOptionContents(pickOptionContents))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @MethodSource("generateInvalidData")
    @DisplayName(PickOptionContents.MAX_PICK_CONTENTS_LENGTH + " 길이가 아닌 토픽 내용을 생성할 경우 예외가 발생한다.")
    void tpickOptionContentsException(String pickOptionContents) {
        // given // when // then
        assertThatThrownBy(() -> new PickOptionContents(pickOptionContents))
                .isInstanceOf(TopicContentsException.class)
                .hasMessage(PickOptionContents.getInvalidLengthExceptionMessage());
    }

    @ParameterizedTest
    @CsvSource(value = {":", "내용입니다.:내용입니다."}, delimiter = ':')
    @DisplayName("픽옵션 content를 String으로 반환한다. null일 경우 빈문자열을 반환한다.")
    void getContentsAsString(String contents, String result) {
        // given
        PickOptionContents pickOptionContents = new PickOptionContents(contents);

        // when
        String pickOptionContentsString = pickOptionContents.getPickOptionContents();

        // then
        assertEquals(result, pickOptionContentsString);
    }

    static Stream<Arguments> generateValidData() {
        int[] dataLengths = {PickOptionContents.MAX_PICK_CONTENTS_LENGTH};
        List<String> data = new ArrayList<>();
        for (int i = 0; i < dataLengths.length; i++) {
            data.add(createStringBy(dataLengths[i]));
        }
        return data.stream().map(Arguments::of);
    }

    static Stream<Arguments> generateInvalidData() {
        int[] dataLengths = {PickOptionContents.MAX_PICK_CONTENTS_LENGTH + 1};
        List<String> data = new ArrayList<>();
        for (int i = 0; i < dataLengths.length; i++) {
            data.add(createStringBy(dataLengths[i]));
        }
        return data.stream().map(Arguments::of);
    }

    public static String createStringBy(int length) {
        return "a".repeat(Math.max(0, length));
    }
}