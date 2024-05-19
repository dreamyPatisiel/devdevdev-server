package com.dreamypatisiel.devdevdev.domain.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WordType {
    ADVERB("부사", 1),
    ADJECTIVE("형용사", 2),
    NOUN("명사", 3);

    private final String type;
    private final int priority;
}
