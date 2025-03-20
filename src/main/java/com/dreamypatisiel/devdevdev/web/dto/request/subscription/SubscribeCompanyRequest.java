package com.dreamypatisiel.devdevdev.web.dto.request.subscription;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SubscribeCompanyRequest {

    @NotNull(message = "기업 아이디는 필수 입니다.")
    private Long companyId;

    public SubscribeCompanyRequest(Long companyId) {
        this.companyId = companyId;
    }
}
