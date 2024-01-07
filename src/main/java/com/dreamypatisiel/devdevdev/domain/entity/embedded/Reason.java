package com.dreamypatisiel.devdevdev.domain.entity.embedded;

import com.dreamypatisiel.devdevdev.exception.ReasonException;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public class Reason {

    public static final int MIN_REASON_LENGTH = 1;
    public static final int MAX_REASON_LENGTH = 200;
    public static final String INVALID_REASON_LENGTH_MESSAGE = "신고 사유의 길이는 %d부터 %d 사이여야 합니다.";

    private String reason;

    public Reason(String reason) {
        validationReason(reason);
        this.reason = reason;
    }

    private void validationReason(String reason) {
        if(!isValidLength(reason)) {
            throw new ReasonException(getInvalidLengthExceptionMessage());
        }
    }

    private boolean isValidLength(String reason) {
        return StringUtils.hasText(reason) && reason.length() <= MAX_REASON_LENGTH;
    }

    public static String getInvalidLengthExceptionMessage() {
        return String.format(INVALID_REASON_LENGTH_MESSAGE, MIN_REASON_LENGTH, MAX_REASON_LENGTH);
    }
}
