package com.dreamypatisiel.devdevdev.domain.service.response;

import com.dreamypatisiel.devdevdev.domain.entity.embedded.Title;
import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

@Data
public class PickOptionResponse {
    private final Long id;
    private final String title;
    private final int percent;
    private final Boolean isPicked;

    @Builder
    public PickOptionResponse(Long id, Title title, BigDecimal percent, Boolean isPicked) {
        this.id = id;
        this.title = title.getTitle();
        this.percent = percent.intValueExact();
        this.isPicked = isPicked;
    }
}
