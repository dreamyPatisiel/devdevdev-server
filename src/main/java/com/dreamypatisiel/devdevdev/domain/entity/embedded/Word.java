package com.dreamypatisiel.devdevdev.domain.entity.embedded;

import com.dreamypatisiel.devdevdev.exception.TitleException;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
@Getter
public class Word {
    public static final String INVALID_WORD_MESSAGE = "단어는 빈 값이거나 %d자를 초과 할 수 없습니다.";
    public static final int MAX_WORD_LENGTH = 10;
    public static final int MIN_WORD_LENGTH = 1;

    private String word;

    public Word(String word) {
        validationWord(word);
        this.word = word;
    }

    private void validationWord(String word) {
        if (!isValidWordLength(word)) {
            throw new TitleException(getInvalidWordExceptionMessage());
        }
    }

    private boolean isValidWordLength(String title) {
        return StringUtils.hasText(title) && title.length() <= MAX_WORD_LENGTH;
    }

    public static String getInvalidWordExceptionMessage() {
        return String.format(INVALID_WORD_MESSAGE, MAX_WORD_LENGTH);
    }
}
