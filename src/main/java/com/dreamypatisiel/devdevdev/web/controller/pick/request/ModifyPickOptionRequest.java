package com.dreamypatisiel.devdevdev.web.controller.pick.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;

@Data
public class ModifyPickOptionRequest {

    @NotNull(message = "픽픽픽 선택지가 없습니다.")
    private final Long pickOptionId;

    @NotBlank(message = "픽픽픽 선택지 제목을 작성해주세요.")
    private final String pickOptionTitle;

    private final String pickOptionContent;
    private final List<Long> pickOptionImageIds;

    public ModifyPickOptionRequest(Long pickOptionId, String pickOptionTitle, String pickOptionContent,
                                   List<Long> pickOptionImageIds) {
        this.pickOptionId = pickOptionId;
        this.pickOptionTitle = pickOptionTitle;
        this.pickOptionContent = pickOptionContent;
        this.pickOptionImageIds = pickOptionImageIds;
    }
}
