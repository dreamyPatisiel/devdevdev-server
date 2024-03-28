package com.dreamypatisiel.devdevdev.web.controller.request;


import static com.dreamypatisiel.devdevdev.domain.service.pick.MemberPickService.FIRST_PICK_OPTION_IMAGE;
import static com.dreamypatisiel.devdevdev.domain.service.pick.MemberPickService.SECOND_PICK_OPTION_IMAGE;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PickOptionName {
    FIRST_PICK_OPTION("firstPickOption"),
    SECOND_PICK_OPTION("secondPickOption");

    private final String description;
}
