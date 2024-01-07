package com.dreamypatisiel.devdevdev.domain.entity.embedded;

import com.dreamypatisiel.devdevdev.exception.CountException;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public class Count {

    private static final String INVALID_COUNT_MESSAGE = "카운트 값은 %d 이상이어야 합니다.";
    public static final int MIN_COUNT = 0;

    private int count;

    public Count(int count) {
        validationCount(count);
        this.count = count;
    }

    private void validationCount(int count) {
        if(!isValidCount(count)) {
            throw new CountException(getInvalidCountExceptionMessage());
        }
    }

    public static String getInvalidCountExceptionMessage() {
        return String.format(INVALID_COUNT_MESSAGE, MIN_COUNT);
    }

    private static boolean isValidCount(int count) {
        return count >= MIN_COUNT;
    }
}
