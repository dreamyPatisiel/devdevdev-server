package com.dreamypatisiel.devdevdev.domain.entity.enums;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PickOptionType {
    FIRST_PICK_OPTION("firstPickOption"),
    SECOND_PICK_OPTION("secondPickOption");

    private final String label;
}
