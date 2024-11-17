package com.dreamypatisiel.devdevdev.domain.entity.enums;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PickOptionType {
    firstPickOption("FIRST_PICK_OPTION", "첫 번째 픽 옵션"),
    secondPickOption("SECOND_PICK_OPTION", "두 번째 픽 옵션");

    private final String label;
    private final String description;
}
