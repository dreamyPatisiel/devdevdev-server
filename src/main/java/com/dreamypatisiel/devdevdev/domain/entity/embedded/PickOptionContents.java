package com.dreamypatisiel.devdevdev.domain.entity.embedded;

import com.dreamypatisiel.devdevdev.exception.TopicContentsException;
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
public class PickOptionContents {
    public static final int MAX_PICK_CONTENTS_LENGTH = 30_000;
    private static final String INVALID_PICK_LENGTH_MESSAGE = "픽픽픽 내용은 최대 %d자 입니다.";

    private String pickOptionContents;

    public PickOptionContents(String pickOptionContents) {
        // 빈문자열 이거나 null 이면 null 로 저장해야한다.
        if (!StringUtils.hasText(pickOptionContents)) {
            this.pickOptionContents = null;
        } else {
            validationTopicContents(pickOptionContents);
            this.pickOptionContents = pickOptionContents;
        }
    }

    private void validationTopicContents(String pickOptionContents) {
        if (!isValidLength(pickOptionContents)) {
            throw new TopicContentsException(getInvalidLengthExceptionMessage());
        }
    }

    private static boolean isValidLength(String pickOptionContents) {
        return pickOptionContents.length() <= MAX_PICK_CONTENTS_LENGTH;
    }

    public static String getInvalidLengthExceptionMessage() {
        return String.format(INVALID_PICK_LENGTH_MESSAGE, MAX_PICK_CONTENTS_LENGTH);
    }
}
