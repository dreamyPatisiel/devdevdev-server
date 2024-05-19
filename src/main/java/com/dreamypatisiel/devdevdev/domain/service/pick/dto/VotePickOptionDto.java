package com.dreamypatisiel.devdevdev.domain.service.pick.dto;

import com.dreamypatisiel.devdevdev.web.controller.request.VotePickOptionRequest;
import lombok.Builder;
import lombok.Data;

@Data
public class VotePickOptionDto {

    private final Long pickId;
    private final Long pickOptionId;
    private final String anonymousMemberId;

    @Builder
    public VotePickOptionDto(Long pickId, Long pickOptionId, String anonymousMemberId) {
        this.pickId = pickId;
        this.pickOptionId = pickOptionId;
        this.anonymousMemberId = anonymousMemberId;
    }

    public static VotePickOptionDto of(VotePickOptionRequest request, String anonymousMemberId) {
        return VotePickOptionDto.builder()
                .pickId(request.getPickId())
                .pickOptionId(request.getPickOptionId())
                .anonymousMemberId(anonymousMemberId)
                .build();
    }
}
