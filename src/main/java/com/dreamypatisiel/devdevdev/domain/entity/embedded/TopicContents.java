package com.dreamypatisiel.devdevdev.domain.entity.embedded;

import com.dreamypatisiel.devdevdev.exception.TopicContentsException;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public class TopicContents {
    public static final int MAX_TOPIC_CONTENTS_LENGTH = 1000;
    public static final int MIN_TOPIC_CONTENTS_LENGTH = 1;
    private static final String INVALID_TOPIC_LENGTH_MESSAGE = "토픽 내용의 길이는 %d ~ %d 사이의 값이어야 합니다.";
    private String topicContents;

    public TopicContents(String topicContents) {
        validationTopicContents(topicContents);
        this.topicContents = topicContents;
    }

    private void validationTopicContents(String topicContents) {
        if(!isValidLength(topicContents)) {
            throw new TopicContentsException(getInvalidLengthExceptionMessage());
        }
    }

    private static boolean isValidLength(String topicContents) {
        return StringUtils.hasText(topicContents) && topicContents.length() <= MAX_TOPIC_CONTENTS_LENGTH;
    }

    public static String getInvalidLengthExceptionMessage() {
        return String.format(INVALID_TOPIC_LENGTH_MESSAGE, MIN_TOPIC_CONTENTS_LENGTH, MAX_TOPIC_CONTENTS_LENGTH);
    }
}
