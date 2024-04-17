package com.dreamypatisiel.devdevdev.domain.entity.embedded;

import com.dreamypatisiel.devdevdev.exception.TopicContentsException;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.ObjectUtils;
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
        validationTopicContents(pickOptionContents);
        this.pickOptionContents = pickOptionContents;
    }

    private void validationTopicContents(String pickOptionContents) {
        if(!isValidLength(pickOptionContents)) {
            throw new TopicContentsException(getInvalidLengthExceptionMessage());
        }
    }

    private static boolean isValidLength(String pickOptionContents) {
        return ObjectUtils.isEmpty(pickOptionContents) || pickOptionContents.length() <= MAX_PICK_CONTENTS_LENGTH;
    }

    public static String getInvalidLengthExceptionMessage() {
        return String.format(INVALID_PICK_LENGTH_MESSAGE, MAX_PICK_CONTENTS_LENGTH);
    }
}
