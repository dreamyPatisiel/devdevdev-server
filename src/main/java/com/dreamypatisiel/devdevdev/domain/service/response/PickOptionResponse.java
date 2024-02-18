package com.dreamypatisiel.devdevdev.domain.service.response;

import com.dreamypatisiel.devdevdev.domain.entity.embedded.Title;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL) // null 인 필드는 제외
public class PickOptionResponse {
    private final Long id;
    private final String title;
    private final int percent;
    private final Boolean isPicked;

    @Builder
    public PickOptionResponse(Long id, Title title, BigDecimal percent, Boolean isPicked) {
        this.id = id;
        this.title = title.getTitle();
        this.percent = percent.intValue();
        this.isPicked = isPicked;
    }
}
