package com.dreamypatisiel.devdevdev.domain.entity.embedded;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class PasswordTest {
    @Test
    @DisplayName("랜덤한 비밀번호를 생성할 수 있다.")
    void Password() {
        // given
        Password password1 = new Password();
        Password password2 = new Password();

        // when
        String pw1 = password1.getPassword();
        String pw2 = password2.getPassword();

        // then
        assertAll(
                () -> assertThat(pw1).isNotNull(),
                () -> assertThat(pw2).isNotNull(),
                () -> assertThat(pw1).isNotEqualTo(pw2)
        );
    }
}