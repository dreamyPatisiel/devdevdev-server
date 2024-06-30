package com.dreamypatisiel.devdevdev.domain.service.response;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
public class VotePickResponse {
    private final Long pickId;
    private final List<VotePickOptionResponse> votePickOptions;

    @Builder
    public VotePickResponse(Long pickId, List<VotePickOptionResponse> votePickOptions) {
        this.pickId = pickId;
        this.votePickOptions = votePickOptions;
    }

    public static VotePickResponse of(Long pickId, List<VotePickOptionResponse> votePickOptions) {
        return VotePickResponse.builder()
                .pickId(pickId)
                .votePickOptions(votePickOptions)
                .build();
    }
}
