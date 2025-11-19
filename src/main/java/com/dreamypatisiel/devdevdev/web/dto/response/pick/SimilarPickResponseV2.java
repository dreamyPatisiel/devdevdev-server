package com.dreamypatisiel.devdevdev.web.dto.response.pick;

import com.dreamypatisiel.devdevdev.domain.entity.Pick;
import com.dreamypatisiel.devdevdev.openai.data.response.PickWithSimilarityDto;
import com.dreamypatisiel.devdevdev.web.dto.util.PickResponseUtils;
import lombok.Builder;
import lombok.Data;

@Data
public class SimilarPickResponseV2 {
    private final Long id;
    private final String title;
    private final long voteTotalCount;
    private final long commentTotalCount;
    private final double similarity;
    private final Boolean isNew;

    @Builder
    public SimilarPickResponseV2(Long id, String title, long voteTotalCount, long commentTotalCount, 
                               double similarity, Boolean isNew) {
        this.id = id;
        this.title = title;
        this.voteTotalCount = voteTotalCount;
        this.commentTotalCount = commentTotalCount;
        this.similarity = similarity;
        this.isNew = isNew;
    }

    public static SimilarPickResponseV2 from(PickWithSimilarityDto pickWithSimilarityDto) {
        Pick pick = pickWithSimilarityDto.getPick();

        return SimilarPickResponseV2.builder()
                .id(pick.getId())
                .title(pick.getTitle().getTitle())
                .voteTotalCount(pick.getVoteTotalCount().getCount())
                .commentTotalCount(pick.getCommentTotalCount().getCount())
                .similarity(pickWithSimilarityDto.getSimilarity())
                .isNew(PickResponseUtils.isNewPick(pick))
                .build();
    }

}
