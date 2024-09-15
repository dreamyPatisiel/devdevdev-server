package com.dreamypatisiel.devdevdev.web.dto.request.pick;

import com.dreamypatisiel.devdevdev.domain.entity.enums.PickOptionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

/**
 * nested dto를 검증하려면 @Valid 어노테이션
 */
@Data
public class ModifyPickRequest {

    @NotBlank(message = "픽픽픽 제목을 작성해주세요.")
    private final String pickTitle;

    @Valid
    private final Map<PickOptionType, ModifyPickOptionRequest> pickOptions;

    @Builder
    public ModifyPickRequest(String pickTitle, Map<PickOptionType, ModifyPickOptionRequest> pickOptions) {
        this.pickTitle = pickTitle;
        this.pickOptions = pickOptions;
    }
}
