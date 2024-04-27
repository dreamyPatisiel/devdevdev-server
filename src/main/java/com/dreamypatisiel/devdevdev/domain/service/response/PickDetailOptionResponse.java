package com.dreamypatisiel.devdevdev.domain.service.response;

import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
public class PickDetailOptionResponse {
    private final Long id;
    private final String title;
    private final boolean isPicked;
    private final int percent;
    private final String content;
    private final long voteTotalCount;
    private final List<PickDetailOptionImage> pickDetailOptionImages;

    @Builder
    public PickDetailOptionResponse(Long id, String title, boolean isPicked, BigDecimal percent, String content,
                                    long voteTotalCount, List<PickDetailOptionImage> pickDetailOptionImages) {
        this.id = id;
        this.title = title;
        this.isPicked = isPicked;
        this.percent = percent.intValueExact();
        this.content = content;
        this.voteTotalCount = voteTotalCount;
        this.pickDetailOptionImages = pickDetailOptionImages;
    }
}
