package com.dreamypatisiel.devdevdev.domain.entity.embedded;

import com.dreamypatisiel.devdevdev.exception.CustomSurveyAnswerException;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public class CustomSurveyAnswer {

    public static final int MIN_MESSAGE_LENGTH = 10;
    public static final int MAX_MESSAGE_LENGTH = 500;
    public static final String INVALID_CUSTOM_SURVEY_ANSWER_MESSAGE = "%d 이상 %d 이하의 글자를 입력해주세요.";

    @Getter
    private String message;

    public CustomSurveyAnswer(String message) {
        validationMessage(message);
        this.message = message;
    }

    private void validationMessage(String message) {
        if (!isValidMessage(message)) {
            throw new CustomSurveyAnswerException(getInvalidCustomSurveyAnswerExceptionMessage());
        }
    }

    /**
     * @Note: DB에 null은 허용하기 때문에 message가 null 이면 true를 반환한다.
     * @Author: 장세웅
     * @Since: 2024.06.01
     */
    private static boolean isValidMessage(String message) {
        if (message == null) {
            return true;
        }
        return !message.isBlank() && message.length() >= MIN_MESSAGE_LENGTH
                && message.length() <= MAX_MESSAGE_LENGTH;
    }

    public static String getInvalidCustomSurveyAnswerExceptionMessage() {
        return String.format(INVALID_CUSTOM_SURVEY_ANSWER_MESSAGE, MIN_MESSAGE_LENGTH, MAX_MESSAGE_LENGTH);
    }
}
