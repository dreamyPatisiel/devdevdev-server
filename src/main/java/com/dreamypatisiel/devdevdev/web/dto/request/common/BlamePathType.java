package com.dreamypatisiel.devdevdev.web.dto.request.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BlamePathType {
    PICK("픽픽픽"),
    TECH_ARTICLE("기술 블로그");

    private final String description;
}
