package com.dreamypatisiel.devdevdev.domain.entity.embedded;

import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.UUID;

@Getter
@Embeddable
@EqualsAndHashCode
public class Password {
    private String password;

    public Password() {
        this.password = createPassword();
    }

    private String createPassword() {
        return UUID.randomUUID().toString();
    }
}
