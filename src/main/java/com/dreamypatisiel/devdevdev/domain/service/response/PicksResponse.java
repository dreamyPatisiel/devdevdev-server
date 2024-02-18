package com.dreamypatisiel.devdevdev.domain.service.response;

import com.dreamypatisiel.devdevdev.domain.entity.embedded.Count;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Title;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL) // null 인 필드는 제외
public class PicksResponse {
    private final Long id;
    private final String title;
    private final int voteTotalCount;
    private final int commentTotalCount;
    private final Boolean isVoted;
    private final List<PickOptionResponse> pickOptionsResponse;
    private final boolean nextCursor;

    @Builder
    public PicksResponse(Long id, Title title, Count voteTotalCount, Count commentTotalCount,
                         Boolean isVoted, List<PickOptionResponse> pickOptionsResponse, boolean nextCursor) {
        this.id = id;
        this.title = title.getTitle();
        this.voteTotalCount = voteTotalCount.getCount();
        this.commentTotalCount = commentTotalCount.getCount();
        this.isVoted = isVoted;
        this.pickOptionsResponse = pickOptionsResponse;
        this.nextCursor = nextCursor;
    }
}
