package com.dreamypatisiel.devdevdev.domain.entity.embedded;

import com.dreamypatisiel.devdevdev.exception.TopicContentsException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TopicContentsTest {
    @ParameterizedTest
    @MethodSource("generateValidData")
    @DisplayName(TopicContents.MIN_TOPIC_CONTENTS_LENGTH+" ~ " +TopicContents.MAX_TOPIC_CONTENTS_LENGTH
            +" 길이의 토픽 내용을 생성할 수 있다.")
    void topicContents(String topicContents) {
        // given // when // then
        assertThatCode(() -> new TopicContents(topicContents))
                .doesNotThrowAnyException();
    }
    @ParameterizedTest
    @MethodSource("generateInvalidData")
    @DisplayName(TopicContents.MIN_TOPIC_CONTENTS_LENGTH + " ~ "+TopicContents.MAX_TOPIC_CONTENTS_LENGTH
            +" 길이가 아닌 토픽 내용을 생성할 경우 예외가 발생한다.")
    void topicContentsException(String topicContents) {
        // given // when // then
        assertThatThrownBy(() -> new TopicContents(topicContents))
                .isInstanceOf(TopicContentsException.class)
                .hasMessage(TopicContents.getInvalidLengthExceptionMessage());
    }

    static Stream<Arguments> generateValidData() {
        int[] dataLengths = {TopicContents.MIN_TOPIC_CONTENTS_LENGTH, TopicContents.MAX_TOPIC_CONTENTS_LENGTH};
        List<String> data = new ArrayList<>();
        for (int i = 0; i < dataLengths.length; i++) {
            data.add(createStringBy(dataLengths[i]));
        }
        return data.stream().map(Arguments::of);
    }

    static Stream<Arguments> generateInvalidData() {
        int[] dataLengths = {0, TopicContents.MAX_TOPIC_CONTENTS_LENGTH+1};
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