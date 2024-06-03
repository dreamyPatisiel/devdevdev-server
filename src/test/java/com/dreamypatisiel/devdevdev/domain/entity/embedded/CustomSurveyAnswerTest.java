package com.dreamypatisiel.devdevdev.domain.entity.embedded;

import static com.dreamypatisiel.devdevdev.domain.entity.embedded.CustomSurveyAnswer.MAX_MESSAGE_LENGTH;
import static com.dreamypatisiel.devdevdev.domain.entity.embedded.CustomSurveyAnswer.MIN_MESSAGE_LENGTH;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.dreamypatisiel.devdevdev.exception.CustomSurveyAnswerException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;

class CustomSurveyAnswerTest {

    @Test
    @DisplayName("회원 탈퇴 서베이 의견은 " + MIN_MESSAGE_LENGTH + "글자 이상 " + MAX_MESSAGE_LENGTH
            + "글자 이하를 입력할 수 있다.")
    void createCustomSurveyAnswer() {
        // given
        String maxBoundaryMessage = "1".repeat(MAX_MESSAGE_LENGTH);
        String minBoundaryMessage = "1".repeat(MIN_MESSAGE_LENGTH);
        String validMessage = "다 좋은데, 서비스 기능이 조금 부족한 것 같아요...!";
        String nullMessage = null;

        // when // then
        assertThatCode(() -> new CustomSurveyAnswer(maxBoundaryMessage))
                .doesNotThrowAnyException();
        assertThatCode(() -> new CustomSurveyAnswer(minBoundaryMessage))
                .doesNotThrowAnyException();
        assertThatCode(() -> new CustomSurveyAnswer(validMessage))
                .doesNotThrowAnyException();
        assertThatCode(() -> new CustomSurveyAnswer(nullMessage))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @EmptySource
    @DisplayName("회원 탈퇴 서베이 의견이 빈 문자열이면 예외가 발생한다.")
    void createShortCustomSurveyAnswer(String shortMessage) {
        // given // when // then
        assertThatThrownBy(() -> new CustomSurveyAnswer(shortMessage))
                .isInstanceOf(CustomSurveyAnswerException.class)
                .hasMessage(CustomSurveyAnswer.getInvalidCustomSurveyAnswerExceptionMessage());
    }
}