package com.dreamypatisiel.devdevdev.domain.service.pick.dto;

import com.dreamypatisiel.devdevdev.web.controller.pick.request.RegisterPickCommentRequest;
import lombok.Builder;
import lombok.Data;

@Data
public class RegisterPickCommentDto {
    private final Long pickId;
    private final String contents;
    private final Long pickOptionId;
    private final Boolean isPickVotePublic;

    @Builder
    public RegisterPickCommentDto(Long pickId, String contents, Long pickOptionId, Boolean isPickVotePublic) {
        this.pickId = pickId;
        this.contents = contents;
        this.pickOptionId = pickOptionId;
        this.isPickVotePublic = isPickVotePublic;
    }

    public static RegisterPickCommentDto of(Long pickId, RegisterPickCommentRequest request) {
        return RegisterPickCommentDto.builder()
                .pickId(pickId)
                .contents(request.getContents())
                .pickOptionId(request.getPickOptionId())
                .isPickVotePublic(request.getIsPickVotePublic())
                .build();
    }
}
