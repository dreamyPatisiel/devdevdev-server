package com.dreamypatisiel.devdevdev.domain.repository.pick;

import lombok.Getter;

@Getter
public class PickSearchDto {
    private final Long pickId;
    private final Double maxTotalScore;

    public PickSearchDto(Long pickId, Double maxTotalScore) {
        this.pickId = pickId;
        this.maxTotalScore = maxTotalScore;
    }
}
