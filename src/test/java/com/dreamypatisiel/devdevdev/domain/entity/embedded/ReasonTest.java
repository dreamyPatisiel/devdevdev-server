package com.dreamypatisiel.devdevdev.domain.entity.embedded;

import com.dreamypatisiel.devdevdev.exception.NicknameException;
import com.dreamypatisiel.devdevdev.exception.ReasonException;
import net.bytebuddy.implementation.bind.annotation.Argument;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

class ReasonTest {
    @ParameterizedTest
    @MethodSource("generateValidData")
    @DisplayName(Reason.MIN_REASON_LENGTH+" ~ "+Reason.MAX_REASON_LENGTH
            +" 길이의 신고 사유를 생성할 수 있다.")
    void reason(String reason) {
        // given // when // then
        assertThatCode(() -> new Reason(reason))
                .doesNotThrowAnyException();

    }

    @ParameterizedTest
    @MethodSource("generateInvalidData")
    @DisplayName(Reason.MIN_REASON_LENGTH+" ~ "+Reason.MAX_REASON_LENGTH
            +" 길이가 아닌 신고 사유를 생성할 경우 예외가 발생한다.")
    void nicknameException(String reason) {
        // given // when // then
        assertThatThrownBy(() -> new Reason(reason))
                .isInstanceOf(ReasonException.class)
                .hasMessage(Reason.getInvalidLengthExceptionMessage());
    }

    static Stream<Arguments> generateValidData() {
        int[] dataLengths = {Reason.MIN_REASON_LENGTH, Reason.MAX_REASON_LENGTH};
        List<String> data = new ArrayList<>();
        for (int i = 0; i < dataLengths.length; i++) {
            data.add(createStringBy(dataLengths[i]));
        }
        return data.stream().map(Arguments::of);
    }

    static Stream<Arguments> generateInvalidData() {
        int[] dataLengths = {0, Reason.MAX_REASON_LENGTH+1};
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