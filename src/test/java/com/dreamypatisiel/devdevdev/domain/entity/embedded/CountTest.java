package com.dreamypatisiel.devdevdev.domain.entity.embedded;

import com.dreamypatisiel.devdevdev.exception.CountException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class CountTest {
    @ParameterizedTest
    @MethodSource("generateValidData")
    @DisplayName(Count.MIN_COUNT + " 이상의 카운트를 생성할 수 있다.")
    void count(int count) {
        // given // when // then
        assertThatCode(() -> new Count(count))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @MethodSource("generateInvalidData")
    @DisplayName(Count.MIN_COUNT + " 미만의 카운트를 생성할 경우 예외가 발생한다.")
    void countException(int count) {
        // given // when // then
        assertThatThrownBy(() -> new Count(count))
                .isInstanceOf(CountException.class)
                .hasMessage(Count.getInvalidCountExceptionMessage());
    }

    static Stream<Arguments> generateValidData() {
        int[] dataLengths = {Count.MIN_COUNT, Count.MIN_COUNT+1};
        List<Integer> data = new ArrayList<>();
        for (int i = 0; i < dataLengths.length; i++) {
            data.add(dataLengths[i]);
        }
        return data.stream().map(Arguments::of);
    }

    static Stream<Arguments> generateInvalidData() {
        int[] dataLengths = {Count.MIN_COUNT-1};
        List<Integer> data = new ArrayList<>();
        for (int i = 0; i < dataLengths.length; i++) {
            data.add(dataLengths[i]);
        }
        return data.stream().map(Arguments::of);
    }

}