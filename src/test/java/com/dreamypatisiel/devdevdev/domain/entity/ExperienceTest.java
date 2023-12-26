package com.dreamypatisiel.devdevdev.domain.entity;

import com.dreamypatisiel.devdevdev.exception.ExperienceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
class ExperienceTest {

    @DisplayName("0년~50년 범위의 연차를 생성할 수 있다.")
    @ParameterizedTest
    @ValueSource(ints = {0, 50, 25})
    void experience(int value) {
        // given // when // then
        assertThatCode(() -> new Experience(value))
                .doesNotThrowAnyException();
    }
    @DisplayName("0년~50년 범위가 아닌 연차를 생성할 경우 예외가 발생한다.")
    @ParameterizedTest
    @ValueSource(ints = {-1, 51})
    void experienceException(int value) {
        // given // when // then
        assertThatThrownBy(() -> new Experience(value))
                .isInstanceOf(ExperienceException.class)
                .hasMessage(Experience.INVALID_EXPERIENCE_MESSAGE);
    }


}