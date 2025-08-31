package com.dreamypatisiel.devdevdev.domain.service.techArticle.dto;

import com.dreamypatisiel.devdevdev.web.dto.request.techArticle.ModifyTechCommentRequest;
import com.dreamypatisiel.devdevdev.web.dto.request.techArticle.RegisterTechCommentRequest;
import lombok.Data;

@Data
public class TechCommentDto {
    private String anonymousMemberId;
    private String contents;

    public static TechCommentDto createRegisterCommentDto(RegisterTechCommentRequest registerTechCommentRequest,
                                                          String anonymousMemberId) {
        TechCommentDto techCommentDto = new TechCommentDto();
        techCommentDto.setContents(registerTechCommentRequest.getContents());
        techCommentDto.setAnonymousMemberId(anonymousMemberId);
        return techCommentDto;
    }

    public static TechCommentDto createModifyCommentDto(ModifyTechCommentRequest modifyTechCommentRequest,
                                                        String anonymousMemberId) {
        TechCommentDto techCommentDto = new TechCommentDto();
        techCommentDto.setContents(modifyTechCommentRequest.getContents());
        techCommentDto.setAnonymousMemberId(anonymousMemberId);
        return techCommentDto;
    }
}
