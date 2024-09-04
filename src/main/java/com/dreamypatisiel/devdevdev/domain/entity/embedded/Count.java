package com.dreamypatisiel.devdevdev.domain.entity.embedded;

import com.dreamypatisiel.devdevdev.exception.CountException;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public class Count {

    private static final String INVALID_COUNT_MESSAGE = "카운트 값은 %d 이상이어야 합니다.";
    public static final long MIN_COUNT = 0L;

    @Getter
    private long count;

    public Count(long count) {
        validationCount(count);
        this.count = count;
    }

    private void validationCount(long count) {
        if (!isValidCount(count)) {
            throw new CountException(getInvalidCountExceptionMessage());
        }
    }

    public void incrementCount() {
        this.count++;
    }

    public static String getInvalidCountExceptionMessage() {
        return String.format(INVALID_COUNT_MESSAGE, MIN_COUNT);
    }

    private static boolean isValidCount(long count) {
        return count >= MIN_COUNT;
    }

    public static Count plusOne(Count count) {
        if (count == null) {
            count = Count.defaultCount();
        }
        return new Count(count.getCount() + 1L);
    }

    public static Count minusOne(Count count) {
        if (count == null) {
            count = Count.defaultCount();
        }
        
        long result = count.getCount() - 1L;
        // 음수이면
        if (result < MIN_COUNT) {
            return new Count(MIN_COUNT);
        }
        return new Count(count.getCount() - 1L);
    }

    public static Count defaultCount() {
        return new Count(MIN_COUNT);
    }
}
