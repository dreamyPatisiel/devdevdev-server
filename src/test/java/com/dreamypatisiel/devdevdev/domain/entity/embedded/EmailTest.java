package com.dreamypatisiel.devdevdev.domain.entity.embedded;

import com.dreamypatisiel.devdevdev.exception.EmailException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmailTest {
    @DisplayName("유효한 형식의 이메일을 생성할 수 있다.")
    @ParameterizedTest
    @ValueSource(strings = {"devdevdev@devdevdev.com", "12345@dev.com", "Dev123@dev.com"})
    void email(String value) {
        // given // when // then
        assertThatCode(()->new Email(value))
                .doesNotThrowAnyException();
    }

    @DisplayName("유효하지 않은 이메일을 생성할 경우 예외가 발생한다.")
    @ParameterizedTest
    @ValueSource(strings = {"댑댑댑@example.com",
            "dev!@dev.com", "dev@devcom", "dev@devcom",
            "devdev.com", "devdevdev"})
    void emailException(String value) {
        // given // when // then
        assertThatThrownBy(()->new Email(value))
                .isInstanceOf(EmailException.class)
                .hasMessage(Email.INVALID_EMAIL_FORMAT_MESSAGE);
    }
}