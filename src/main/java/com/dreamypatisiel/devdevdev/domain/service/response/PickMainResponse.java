package com.dreamypatisiel.devdevdev.domain.service.response;

import com.dreamypatisiel.devdevdev.domain.entity.embedded.Count;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Title;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
public class PickMainResponse {
    private final Long id;
    private final String title;
    private final long voteTotalCount;
    private final long commentTotalCount;
    private final long viewTotalCount;
    private final long popularScore;
    private final Boolean isVoted;
    private final List<PickMainOptionResponse> pickOptions;

    @Builder
    public PickMainResponse(Long id, Title title, Count voteTotalCount, Count commentTotalCount,
                            Count viewTotalCount, Count popularScore, Boolean isVoted,
                            List<PickMainOptionResponse> pickOptions) {
        this.id = id;
        this.title = title.getTitle();
        this.voteTotalCount = voteTotalCount.getCount();
        this.commentTotalCount = commentTotalCount.getCount();
        this.viewTotalCount = viewTotalCount.getCount();
        this.popularScore = popularScore.getCount();
        this.isVoted = isVoted;
        this.pickOptions = pickOptions;
    }
}
