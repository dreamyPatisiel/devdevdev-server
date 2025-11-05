package com.dreamypatisiel.devdevdev.web.dto.response.pick;

import com.dreamypatisiel.devdevdev.domain.entity.AnonymousMember;
import com.dreamypatisiel.devdevdev.domain.entity.Member;
import com.dreamypatisiel.devdevdev.domain.entity.Pick;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Count;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Title;
import com.dreamypatisiel.devdevdev.web.dto.util.PickResponseUtils;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PickMainResponseV2 {
    private Long id;
    private String title;
    private long voteTotalCount;
    private long commentTotalCount;
    private long viewTotalCount;
    private long popularScore;
    private Boolean isVoted;
    private Boolean isNew;
    private List<PickMainOptionResponseV2> pickOptions;

    @Builder
    public PickMainResponseV2(Long id, Title title, Count voteTotalCount, Count commentTotalCount,
                              Count viewTotalCount, Count popularScore, Boolean isVoted, Boolean isNew,
                              List<PickMainOptionResponseV2> pickOptions) {
        this.id = id;
        this.title = title.getTitle();
        this.voteTotalCount = voteTotalCount.getCount();
        this.commentTotalCount = commentTotalCount.getCount();
        this.viewTotalCount = viewTotalCount.getCount();
        this.popularScore = popularScore.getCount();
        this.isVoted = isVoted;
        this.isNew = isNew;
        this.pickOptions = pickOptions;
    }

    // 회원 전용
    public static PickMainResponseV2 of(Pick pick, Member member) {
        return PickMainResponseV2.builder()
                .id(pick.getId())
                .title(pick.getTitle())
                .voteTotalCount(pick.getVoteTotalCount())
                .commentTotalCount(pick.getCommentTotalCount())
                .viewTotalCount(pick.getViewTotalCount())
                .popularScore(pick.getPopularScore())
                .pickOptions(mapToPickOptionsResponse(pick, member))
                .isVoted(PickResponseUtils.isVotedMember(pick, member))
                .isNew(PickResponseUtils.isNewPick(pick))
                .build();
    }

    // 익명 회원 전용
    public static PickMainResponseV2 of(Pick pick, AnonymousMember anonymousMember) {
        return PickMainResponseV2.builder()
                .id(pick.getId())
                .title(pick.getTitle())
                .voteTotalCount(pick.getVoteTotalCount())
                .commentTotalCount(pick.getCommentTotalCount())
                .viewTotalCount(pick.getViewTotalCount())
                .popularScore(pick.getPopularScore())
                .pickOptions(mapToPickOptionsResponse(pick, anonymousMember))
                .isVoted(PickResponseUtils.isVotedAnonymousMember(pick, anonymousMember))
                .isNew(PickResponseUtils.isNewPick(pick))
                .build();
    }

    protected static List<PickMainOptionResponseV2> mapToPickOptionsResponse(Pick pick, Member member) {
        return pick.getPickOptions().stream()
                .map(pickOption -> PickMainOptionResponseV2.of(pick, pickOption, member))
                .toList();
    }

    protected static List<PickMainOptionResponseV2> mapToPickOptionsResponse(Pick pick, AnonymousMember anonymousMember) {
        return pick.getPickOptions().stream()
                .map(pickOption -> PickMainOptionResponseV2.of(pick, pickOption, anonymousMember))
                .toList();
    }
}
