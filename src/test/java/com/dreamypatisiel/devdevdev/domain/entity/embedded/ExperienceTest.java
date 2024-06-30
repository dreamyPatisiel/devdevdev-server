package com.dreamypatisiel.devdevdev.domain.entity.embedded;

import com.dreamypatisiel.devdevdev.exception.ExperienceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
class ExperienceTest {


    @ParameterizedTest
    @MethodSource("generateValidData")
    @DisplayName(Experience.MIN_EXPERIENCE + "년 ~ "+Experience.MAX_EXPERIENCE
            +"년 범위의 연차를 생성할 수 있다.")
    void experience(int value) {
        // given // when // then
        assertThatCode(() -> new Experience(value))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @MethodSource("generateInvalidData")
    @DisplayName(Experience.MIN_EXPERIENCE + "년 ~ "+Experience.MAX_EXPERIENCE
            +"년 범위가 아닌 연차를 생성할 경우 예외가 발생한다.")
    void experienceException(int value) {
        // given // when // then
        assertThatThrownBy(() -> new Experience(value))
                .isInstanceOf(ExperienceException.class)
                .hasMessage(Experience.getInvalidRangeExceptionMessage());
    }

    static Stream<Arguments> generateValidData() {
        int[] dataLengths = {Experience.MIN_EXPERIENCE, Experience.MAX_EXPERIENCE};
        List<Integer> data = new ArrayList<>();
        for (int i = 0; i < dataLengths.length; i++) {
            data.add(dataLengths[i]);
        }
        return data.stream().map(Arguments::of);
    }

    static Stream<Arguments> generateInvalidData() {
        int[] dataLengths = {Experience.MIN_EXPERIENCE-1, Experience.MAX_EXPERIENCE+1};
        List<Integer> data = new ArrayList<>();
        for (int i = 0; i < dataLengths.length; i++) {
            data.add(dataLengths[i]);
        }
        return data.stream().map(Arguments::of);
    }
}