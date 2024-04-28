package com.dreamypatisiel.devdevdev.domain.entity.enums;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PickOptionType {
    firstPickOption("FIRST_PICK_OPTION"),
    secondPickOption("SECOND_PICK_OPTION");

    private final String label;
}
