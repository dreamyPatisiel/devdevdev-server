package com.dreamypatisiel.devdevdev.domain.service.pick.dto;

import com.dreamypatisiel.devdevdev.web.dto.request.pick.RegisterPickCommentRequest;
import com.dreamypatisiel.devdevdev.web.dto.request.pick.RegisterPickRepliedCommentRequest;
import lombok.Builder;
import lombok.Data;

@Data
public class PickCommentDto {
    private final String contents;
    private Boolean isPickVotePublic;
    private final String anonymousMemberId;

    @Builder
    public PickCommentDto(String contents, Boolean isPickVotePublic, String anonymousMemberId) {
        this.contents = contents;
        this.isPickVotePublic = isPickVotePublic;
        this.anonymousMemberId = anonymousMemberId;
    }

    public static PickCommentDto createRegisterCommentDto(RegisterPickCommentRequest registerPickCommentRequest,
                                                          String anonymousMemberId) {
        return PickCommentDto.builder()
                .contents(registerPickCommentRequest.getContents())
                .isPickVotePublic(registerPickCommentRequest.getIsPickVotePublic())
                .anonymousMemberId(anonymousMemberId)
                .build();
    }

    public static PickCommentDto createRepliedCommentDto(RegisterPickRepliedCommentRequest registerPickRepliedCommentRequest,
                                                         String anonymousMemberId) {
        return PickCommentDto.builder()
                .contents(registerPickRepliedCommentRequest.getContents())
                .anonymousMemberId(anonymousMemberId)
                .build();
    }
}
