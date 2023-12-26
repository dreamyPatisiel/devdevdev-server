package com.dreamypatisiel.devdevdev.domain.entity;

import com.dreamypatisiel.devdevdev.exception.EmailException;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Email {
    private static final String EMAIL_REGEX = "^[a-zA-Z0-9+-\\_.]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$";
    public static final String INVALID_EMAIL_FORMAT_MESSAGE = "유효한 형식의 이메일 주소를 입력해주세요.";
    private String email;

    public Email(String email) {
        validationEmail(email);
        this.email = email;
    }

    private void validationEmail(String email) {
        if(!isMatchWithRegex(email)){
            throw new EmailException(INVALID_EMAIL_FORMAT_MESSAGE);
        }
    }

    private static boolean isMatchWithRegex(String email) {
        return email.matches(EMAIL_REGEX);
    }
}
