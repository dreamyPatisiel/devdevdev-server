package com.dreamypatisiel.devdevdev.domain.entity.embedded;

import com.dreamypatisiel.devdevdev.exception.NicknameException;
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

class
NicknameTest {
    @ParameterizedTest
    @MethodSource("generateValidData")
    @DisplayName(Nickname.MIN_NICKNAME_LENGTH+" ~ " +Nickname.MAX_NICKNAME_LENGTH
            +" 길이의 닉네임을 생성할 수 있다.")
    void nickname(String nickname) {
        // given // when // then
        assertThatCode(() -> new Nickname(nickname))
                .doesNotThrowAnyException();
    }
    @ParameterizedTest
    @MethodSource("generateInvalidData")
    @DisplayName(Nickname.MIN_NICKNAME_LENGTH + " ~ "+Nickname.MAX_NICKNAME_LENGTH
            +" 길이가 아닌 닉네임을 생성할 경우 예외가 발생한다.")
    void nicknameException(String nickname) {
        // given // when // then
        assertThatThrownBy(() -> new Nickname(nickname))
                .isInstanceOf(NicknameException.class)
                .hasMessage(Nickname.getInvalidLengthExceptionMessage());
    }

    static Stream<Arguments> generateValidData() {
        int[] dataLengths = {Nickname.MIN_NICKNAME_LENGTH, Nickname.MAX_NICKNAME_LENGTH};
        List<String> data = new ArrayList<>();
        for (int i = 0; i < dataLengths.length; i++) {
            data.add(createStringBy(dataLengths[i]));
        }
        return data.stream().map(Arguments::of);
    }

    static Stream<Arguments> generateInvalidData() {
        int[] dataLengths = {0, Nickname.MAX_NICKNAME_LENGTH+1};
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