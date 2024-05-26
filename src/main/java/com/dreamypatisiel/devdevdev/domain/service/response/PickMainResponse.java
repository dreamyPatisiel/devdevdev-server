package com.dreamypatisiel.devdevdev.domain.service.response;

import com.dreamypatisiel.devdevdev.domain.entity.AnonymousMember;
import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.Pick;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Count;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Title;
import com.dreamypatisiel.devdevdev.domain.service.response.util.PickResponseUtils;
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

    public static PickMainResponse of(Pick pick, Member member) {
        return PickMainResponse.builder()
                .id(pick.getId())
                .title(pick.getTitle())
                .voteTotalCount(pick.getVoteTotalCount())
                .commentTotalCount(pick.getCommentTotalCount())
                .viewTotalCount(pick.getViewTotalCount())
                .popularScore(pick.getPopularScore())
                .pickOptions(mapToPickOptionsResponse(pick, member))
                .isVoted(PickResponseUtils.isVotedMember(pick, member))
                .build();
    }

    public static PickMainResponse of(Pick pick, AnonymousMember anonymousMember) {
        return PickMainResponse.builder()
                .id(pick.getId())
                .title(pick.getTitle())
                .voteTotalCount(pick.getVoteTotalCount())
                .commentTotalCount(pick.getCommentTotalCount())
                .viewTotalCount(pick.getViewTotalCount())
                .popularScore(pick.getPopularScore())
                .pickOptions(mapToPickOptionsResponse(pick, anonymousMember))
                .isVoted(PickResponseUtils.isVotedAnonymousMember(pick, anonymousMember))
                .build();
    }

    private static List<PickMainOptionResponse> mapToPickOptionsResponse(Pick pick, Member member) {
        return pick.getPickOptions().stream()
                .map(pickOption -> PickMainOptionResponse.of(pick, pickOption, member))
                .toList();
    }

    private static List<PickMainOptionResponse> mapToPickOptionsResponse(Pick pick, AnonymousMember anonymousMember) {
        return pick.getPickOptions().stream()
                .map(pickOption -> PickMainOptionResponse.of(pick, pickOption, anonymousMember))
                .toList();
    }
}
