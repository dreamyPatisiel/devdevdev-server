package com.dreamypatisiel.devdevdev.web.dto.response.pick;

import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.Pick;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Count;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Title;
import com.dreamypatisiel.devdevdev.web.dto.util.PickResponseUtils;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
public class PickMainSearchResponseV2 extends PickMainResponseV2 {
    private final Double searchScore;

    @Builder(builderMethodName = "searchBuilder")
    public PickMainSearchResponseV2(Long id, Title title, Count voteTotalCount, Count commentTotalCount, Count viewTotalCount,
                                    Count popularScore, Boolean isVoted, Boolean isNew,
                                    List<PickMainOptionResponseV2> pickOptions, Double searchScore) {
        super(id, title, voteTotalCount, commentTotalCount, viewTotalCount, popularScore, isVoted, isNew, pickOptions);
        this.searchScore = searchScore;
    }

    public static PickMainSearchResponseV2 of(Pick pick, Member member, Double searchScore) {
        return PickMainSearchResponseV2.searchBuilder()
                .id(pick.getId())
                .title(pick.getTitle())
                .voteTotalCount(pick.getVoteTotalCount())
                .commentTotalCount(pick.getCommentTotalCount())
                .viewTotalCount(pick.getViewTotalCount())
                .popularScore(pick.getPopularScore())
                .pickOptions(mapToPickOptionsResponse(pick, member))
                .isVoted(PickResponseUtils.isVotedMember(pick, member))
                .isNew(PickResponseUtils.isNewPick(pick))
                .searchScore(searchScore)
                .build();
    }
}
