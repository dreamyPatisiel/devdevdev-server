package com.dreamypatisiel.devdevdev.domain.entity.embedded;

import com.dreamypatisiel.devdevdev.exception.TitleException;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public class Title {
    public static final String INVALID_TITLE_MESSAGE = "제목은 빈 값이거나 %d자 이상일 수 없습니다.";
    public static final int MAX_TITLE_LENGTH = 50;
    public static final int MIN_TITLE_LENGTH = 1;

    private String title;

    public Title(String title) {
        validationTitle(title);
        this.title = title;
    }

    private void validationTitle(String title) {
        if(!isValidTitleLength(title)) {
            throw new TitleException(getInvalidTitleExceptionMessage());
        }
    }

    private boolean isValidTitleLength(String title) {
        return StringUtils.hasText(title) && title.length() <= MAX_TITLE_LENGTH;
    }

    public static String getInvalidTitleExceptionMessage() {
        return String.format(INVALID_TITLE_MESSAGE, MAX_TITLE_LENGTH);
    }
}
