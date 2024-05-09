package com.dreamypatisiel.devdevdev.domain.entity.embedded;

import com.dreamypatisiel.devdevdev.exception.NicknameException;
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
public class Nickname {
    public static final int MIN_NICKNAME_LENGTH = 1;
    public static final int MAX_NICKNAME_LENGTH = 20;
    public static final String INVALID_NICKNAME_LENGTH_MESSAGE = "닉네임 길이는 %d부터 %d 사이여야 합니다.";

    private String nickname;

    public Nickname(String nickname) {
        validationNickname(nickname);
        this.nickname = nickname;
    }

    private void validationNickname(String nickname) {
        if (!isValidLength(nickname)) {
            throw new NicknameException(getInvalidLengthExceptionMessage());
        }
    }

    private boolean isValidLength(String nickname) {
        return StringUtils.hasText(nickname) && nickname.length() <= MAX_NICKNAME_LENGTH;
    }

    public static String getInvalidLengthExceptionMessage() {
        return String.format(INVALID_NICKNAME_LENGTH_MESSAGE, MIN_NICKNAME_LENGTH, MAX_NICKNAME_LENGTH);
    }
}
