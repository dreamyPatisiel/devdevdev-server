package com.dreamypatisiel.devdevdev.web.dto.response.pick;

import com.dreamypatisiel.devdevdev.domain.entity.PickOption;
import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

@Data
public class VotePickOptionResponse {
    private final Long pickOptionId;
    private final Long pickVoteId;
    private final long voteTotalCount;
    private final int percent;
    private final Boolean isPicked;

    @Builder
    public VotePickOptionResponse(Long pickOptionId, Long pickVoteId, long voteTotalCount,
                                  int percent, Boolean isPicked) {
        this.pickOptionId = pickOptionId;
        this.pickVoteId = pickVoteId;
        this.voteTotalCount = voteTotalCount;
        this.percent = percent;
        this.isPicked = isPicked;
    }

    public static VotePickOptionResponse of(PickOption pickOption, Long pickVoteId, BigDecimal percent,
                                            Boolean isPicked) {
        return VotePickOptionResponse.builder()
                .pickOptionId(pickOption.getId())
                .pickVoteId(pickVoteId)
                .voteTotalCount(pickOption.getVoteTotalCount().getCount())
                .percent(percent.intValueExact())
                .isPicked(isPicked)
                .build();
    }
}
