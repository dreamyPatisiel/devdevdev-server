package com.dreamypatisiel.devdevdev.openai.data.response;

import com.dreamypatisiel.devdevdev.domain.entity.Pick;
import lombok.Data;

@Data
public class PickWithSimilarityDto {
    private final Pick pick;
    private final Double similarity;

    public PickWithSimilarityDto(Pick pick, Double similarity) {
        this.pick = pick;
        this.similarity = similarity;
    }
}
