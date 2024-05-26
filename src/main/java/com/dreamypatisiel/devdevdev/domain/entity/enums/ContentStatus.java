package com.dreamypatisiel.devdevdev.domain.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ContentStatus {
    REJECT("거절"),
    READY("대기"),
    APPROVAL("승인");

    private final String description;
}
