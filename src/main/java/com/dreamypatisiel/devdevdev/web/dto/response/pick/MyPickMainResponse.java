package com.dreamypatisiel.devdevdev.web.dto.response.pick;

import com.dreamypatisiel.devdevdev.domain.entity.Pick;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Count;
import com.dreamypatisiel.devdevdev.domain.entity.embedded.Title;
import com.dreamypatisiel.devdevdev.domain.entity.enums.ContentStatus;
import com.dreamypatisiel.devdevdev.web.dto.util.PickResponseUtils;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MyPickMainResponse {
    private Long id;
    private String title;
    private long voteTotalCount;
    private long commentTotalCount;
    private long viewTotalCount;
    private Boolean isVoted;
    private String contentStatus;
    private List<MyPickMainOptionResponse> pickOptions;

    @Builder
    public MyPickMainResponse(Long id, Title title, Count voteTotalCount, Count commentTotalCount,
                              Count viewTotalCount, Boolean isVoted, ContentStatus contentStatus,
                              List<MyPickMainOptionResponse> pickOptions) {
        this.id = id;
        this.title = title.getTitle();
        this.voteTotalCount = voteTotalCount.getCount();
        this.commentTotalCount = commentTotalCount.getCount();
        this.viewTotalCount = viewTotalCount.getCount();
        this.isVoted = isVoted;
        this.contentStatus = contentStatus.name();
        this.pickOptions = pickOptions;
    }

    public static MyPickMainResponse from(Pick pick) {
        return MyPickMainResponse.builder()
                .id(pick.getId())
                .title(pick.getTitle())
                .voteTotalCount(pick.getVoteTotalCount())
                .commentTotalCount(pick.getCommentTotalCount())
                .viewTotalCount(pick.getViewTotalCount())
                .isVoted(PickResponseUtils.isVotedMember(pick, pick.getMember()))
                .contentStatus(pick.getContentStatus())
                .pickOptions(mapToMyPickMainOption(pick))
                .build();
    }

    private static List<MyPickMainOptionResponse> mapToMyPickMainOption(Pick pick) {
        return pick.getPickOptions().stream()
                .map(pickOption -> MyPickMainOptionResponse.of(pick, pickOption))
                .toList();
    }
}
