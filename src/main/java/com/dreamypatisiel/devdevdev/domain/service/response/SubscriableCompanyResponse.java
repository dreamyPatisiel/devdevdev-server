package com.dreamypatisiel.devdevdev.domain.service.response;

import com.dreamypatisiel.devdevdev.domain.entity.Company;
import lombok.Builder;
import lombok.Data;

@Data
public class SubscriableCompanyResponse {
    private final Long companyId;
    private final String companyImageUrl;
    private final Boolean isSubscribed;

    @Builder
    public SubscriableCompanyResponse(Long companyId, String companyImageUrl, Boolean isSubscribed) {
        this.companyId = companyId;
        this.companyImageUrl = companyImageUrl;
        this.isSubscribed = isSubscribed;
    }

    public static SubscriableCompanyResponse create(Company company) {
        return SubscriableCompanyResponse.builder()
                .companyId(company.getId())
                .companyImageUrl(company.getOfficialImageUrl().getUrl())
                .isSubscribed(false)
                .build();
    }

    public static SubscriableCompanyResponse createWithIsSubscribed(Company company, Boolean isSubscribed) {
        return SubscriableCompanyResponse.builder()
                .companyId(company.getId())
                .companyImageUrl(company.getOfficialImageUrl().getUrl())
                .isSubscribed(isSubscribed)
                .build();
    }
}
