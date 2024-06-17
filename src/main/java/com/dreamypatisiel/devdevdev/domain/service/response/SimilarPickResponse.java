package com.dreamypatisiel.devdevdev.domain.service.response;

import com.dreamypatisiel.devdevdev.domain.entity.Pick;
import com.dreamypatisiel.devdevdev.openai.response.PickWithSimilarityDto;
import lombok.Builder;
import lombok.Data;

@Data
public class SimilarPickResponse {
    private final Long id;
    private final String title;
    private final long voteTotalCount;
    private final long commentTotalCount;
    private final double similarity;

    @Builder
    public SimilarPickResponse(Long id, String title, long voteTotalCount, long commentTotalCount, double similarity) {
        this.id = id;
        this.title = title;
        this.voteTotalCount = voteTotalCount;
        this.commentTotalCount = commentTotalCount;
        this.similarity = similarity;
    }

    public static SimilarPickResponse from(PickWithSimilarityDto pickWithSimilarityDto) {
        Pick pick = pickWithSimilarityDto.getPick();

        return SimilarPickResponse.builder()
                .id(pick.getId())
                .title(pick.getTitle().getTitle())
                .voteTotalCount(pick.getVoteTotalCount().getCount())
                .commentTotalCount(pick.getCommentTotalCount().getCount())
                .similarity(pickWithSimilarityDto.getSimilarity())
                .build();
    }
}
