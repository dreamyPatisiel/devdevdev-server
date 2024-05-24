package com.dreamypatisiel.devdevdev.domain.service.response;

import com.dreamypatisiel.devdevdev.domain.entity.embedded.Count;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Title;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PickMainResponse {
    private Long id;
    private String title;
    private long voteTotalCount;
    private long commentTotalCount;
    private long viewTotalCount;
    private long popularScore;
    private Boolean isVoted;
    private List<PickMainOptionResponse> pickOptions;

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
